import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

public class ContinuousShapeGame extends JFrame implements KeyListener {
    private ArrayList<Shape> shapes;
    private JLabel scoreLabel, missedLabel;
    private final int BLADE_WIDTH = 80, BLADE_HEIGHT = 10;
    private int bladeX = 150;
    private final int bladeY = 420;
    private int score = 0;
    private int missed = 0;
    private final int MAX_MISSES = 5;

    private AtomicBoolean running;
    private GamePanel panel;

    public ContinuousShapeGame() {
        setupUI();
        startGame();
    }

    private void setupUI() {
        setTitle("Continuous Falling Shape Game");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        missedLabel = new JLabel("Missed: 0");
        missedLabel.setFont(new Font("Arial", Font.BOLD, 16));

        topPanel.add(scoreLabel);
        topPanel.add(missedLabel);

        add(topPanel, BorderLayout.NORTH);

        panel = new GamePanel();
        add(panel, BorderLayout.CENTER);

        addKeyListener(this);
        setFocusable(true);
        setVisible(true);
    }

    private void startGame() {
        shapes = new ArrayList<>();
        score = 0;
        missed = 0;
        bladeX = 150;
        running = new AtomicBoolean(true);
        scoreLabel.setText("Score: 0");
        missedLabel.setText("Missed: 0");

        // Thread 1: falling shapes
        Thread fallThread = new Thread(() -> {
            Random rand = new Random();
            while (running.get()) {
                synchronized (shapes) {
                    shapes.add(new Shape(rand.nextInt(panel.getWidth() - 30), 0));
                }
                for (int i = 0; i < 25 && running.get(); i++) {
                    synchronized (shapes) {
                        for (Shape s : shapes) {
                            s.y += 5;
                        }
                    }
                    panel.repaint();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Thread 2: collision detection
        Thread detectThread = new Thread(() -> {
            while (running.get()) {
                synchronized (shapes) {
                    Iterator<Shape> iter = shapes.iterator();
                    while (iter.hasNext()) {
                        Shape s = iter.next();
                        Rectangle bladeRect = new Rectangle(bladeX, bladeY, BLADE_WIDTH, BLADE_HEIGHT);
                        Rectangle shapeRect = new Rectangle(s.x, s.y, 30, 30);

                        if (bladeRect.intersects(shapeRect)) {
                            iter.remove();
                            score++;
                            scoreLabel.setText("Score: " + score);
                        } else if (s.y > panel.getHeight()) {
                            iter.remove();
                            missed++;
                            missedLabel.setText("Missed: " + missed);
                            if (missed >= MAX_MISSES) {
                                running.set(false);
                                SwingUtilities.invokeLater(this::showGameOverDialog);
                                break;
                            }
                        }
                    }
                }
                panel.repaint();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        fallThread.start();
        detectThread.start();
    }

    private void showGameOverDialog() {
        int option = JOptionPane.showOptionDialog(
                this,
                "Game Over!\nScore: " + score + "\nMissed: " + missed + "\nDo you want to restart?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"Restart", "Exit"},
                "Restart"
        );

        if (option == JOptionPane.YES_OPTION) {
            startGame(); // Restart the game
        } else {
            System.exit(0);
        }
    }

    class Shape {
        int x, y;

        Shape(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    class GamePanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);

            // Draw shapes
            g.setColor(Color.RED);
            synchronized (shapes) {
                for (Shape shape : shapes) {
                    g.fillOval(shape.x, shape.y, 30, 30);
                }
            }

            // Draw blade
            g.setColor(Color.BLACK);
            g.fillRect(bladeX, bladeY, BLADE_WIDTH, BLADE_HEIGHT);
        }
    }

    // Blade control
    @Override
    public void keyPressed(KeyEvent e) {
        if (!running.get()) return;

        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT && bladeX > 0) {
            bladeX -= 20;
        } else if (code == KeyEvent.VK_RIGHT && bladeX < getWidth() - BLADE_WIDTH) {
            bladeX += 20;
        }
        panel.repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContinuousShapeGame::new);
    }
}
