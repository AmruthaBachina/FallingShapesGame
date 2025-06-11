import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;

public class FallingShapesGame extends JPanel {

    private static final int SHAPE_WIDTH = 20;
    private static final int SHAPE_HEIGHT = 30;
    private static final int CATCHER_WIDTH = 100;
    private static final int CATCHER_HEIGHT = 15;

    private int catcherX;
    private int misses = 0;
    private int score = 0;
    private boolean gameOver = false;
    private volatile boolean paused = false;

    private final Random rand = new Random();
    private final CopyOnWriteArrayList<Shape> shapes = new CopyOnWriteArrayList<>();

    public FallingShapesGame() {
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                int key = evt.getKeyCode();
                if (key == KeyEvent.VK_LEFT && catcherX > 0) {
                    catcherX -= 20;
                } else if (key == KeyEvent.VK_RIGHT && catcherX < getWidth() - CATCHER_WIDTH) {
                    catcherX += 20;
                } else if (key == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                } else if (key == KeyEvent.VK_P) {
                    paused = !paused;
                }
            }
        });
    }

    public void startGame() {
        resetGame();

        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(rand.nextInt(3000));
                } catch (InterruptedException ignored) {}

                Shape shape = new Shape();
                shapes.add(shape);

                new Thread(() -> fallShape(shape)).start();
            }).start();
        }

        new Thread(this::collisionCheckerThread).start();
    }

    private void resetGame() {
        shapes.clear();
        catcherX = 200;
        score = 0;
        misses = 0;
        gameOver = false;
    }

    private void fallShape(Shape shape) {
        int fallDelay = 30;  // Increased base speed (smaller = faster)
        long lastSpeedUp = System.currentTimeMillis();

        while (!gameOver) {
            if (!paused) {
                shape.y += 6;  // Increased fall step

                if (shape.y > getHeight()) {
                    misses++;
                    if (misses >= 5) {
                        gameOver = true;
                        repaint();
                        showGameOverDialog();
                        return;
                    }
                    shape.reset(getWidth());
                }

                repaint();

                if (System.currentTimeMillis() - lastSpeedUp > 6000 && fallDelay > 10) {
                    fallDelay -= 2;
                    lastSpeedUp = System.currentTimeMillis();
                }
            }

            try {
                Thread.sleep(fallDelay + rand.nextInt(20));
            } catch (InterruptedException ignored) {}
        }
    }

    private void collisionCheckerThread() {
        while (!gameOver) {
            if (!paused) {
                for (Shape shape : shapes) {
                    if (shape.y + SHAPE_HEIGHT >= getHeight() - 50) {
                        if (shape.x + SHAPE_WIDTH > catcherX && shape.x < catcherX + CATCHER_WIDTH) {
                            score++;
                            shape.reset(getWidth());
                        }
                    }
                }
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {}
        }
    }

    private void showGameOverDialog() {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Game Over! Score: " + score,
                    "Game Over",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"Restart", "Exit", "Cancel"},
                    "Restart"
            );

            if (choice == JOptionPane.YES_OPTION) {
                startGame();
            } else if (choice == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.ORANGE);
        for (Shape shape : shapes) {
            g.fillRect(shape.x, shape.y, SHAPE_WIDTH, SHAPE_HEIGHT);
        }

        g.setColor(Color.DARK_GRAY);
        g.fillRect(catcherX, getHeight() - 50, CATCHER_WIDTH, CATCHER_HEIGHT);

        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Misses: " + misses + "/5", 10, 40);
        g.drawString("Press P to Pause/Resume | ESC to Exit", 10, 60);

        if (paused) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("PAUSED", getWidth() / 2 - 50, getHeight() / 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Falling Rectangles Game");
            FallingShapesGame game = new FallingShapesGame();
            frame.add(game);
            frame.setSize(500, 500);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            game.requestFocusInWindow();
            game.startGame();
        });
    }

    private class Shape {
        int x, y;

        Shape() {
            reset(getWidth());
        }

        void reset(int panelWidth) {
            int minX = 10;
            int maxX = Math.max(panelWidth - SHAPE_WIDTH - 10, minX + 1);
            x = minX + rand.nextInt(maxX - minX + 1);
            y = 0;
        }
    }
}
