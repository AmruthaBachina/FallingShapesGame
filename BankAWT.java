import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BankAWT extends Frame implements ActionListener {

    Label accLabel, nameLabel, balLabel, msgLabel;
    TextField accField, nameField, balField;
    Button createBtn, viewBtn;

    Connection con;

    public BankAWT() {
        setTitle("Bank App");
        setLayout(null);
        setSize(400, 400);
        setVisible(true);

        accLabel = new Label("Account No:");
        nameLabel = new Label("Name:");
        balLabel = new Label("Balance:");
        msgLabel = new Label();

        accField = new TextField();
        nameField = new TextField();
        balField = new TextField();

        createBtn = new Button("Create");
        viewBtn = new Button("View Balance");

        accLabel.setBounds(50, 50, 100, 30);
        accField.setBounds(160, 50, 150, 30);

        nameLabel.setBounds(50, 100, 100, 30);
        nameField.setBounds(160, 100, 150, 30);

        balLabel.setBounds(50, 150, 100, 30);
        balField.setBounds(160, 150, 150, 30);

        createBtn.setBounds(100, 200, 80, 30);
        viewBtn.setBounds(200, 200, 120, 30);

        msgLabel.setBounds(50, 250, 300, 30);

        add(accLabel); add(accField);
        add(nameLabel); add(nameField);
        add(balLabel); add(balField);
        add(createBtn); add(viewBtn);
        add(msgLabel);

        createBtn.addActionListener(this);
        viewBtn.addActionListener(this);

        connectDB();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    if (con != null) con.close();
                } catch (Exception e) {}
                System.exit(0);
            }
        });
    }

    public void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/Bank?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                "root",
                "amrutha2545" // use your password
            );
            msgLabel.setText("Connected to DB!");
        } catch (Exception e) {
            msgLabel.setText("DB Error: " + e.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createBtn) {
            try {
                int acc = Integer.parseInt(accField.getText());
                String name = nameField.getText();
                double bal = Double.parseDouble(balField.getText());

                PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO bank_system (account_no, name, balance) VALUES (?, ?, ?)");
                pst.setInt(1, acc);
                pst.setString(2, name);
                pst.setDouble(3, bal);

                int rows = pst.executeUpdate();
                msgLabel.setText(rows > 0 ? "Account created!" : "Failed to create account");

            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
            }
        } else if (e.getSource() == viewBtn) {
            try {
                int acc = Integer.parseInt(accField.getText());

                PreparedStatement pst = con.prepareStatement(
                    "SELECT balance FROM bank_system WHERE account_no = ?");
                pst.setInt(1, acc);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    double bal = rs.getDouble("balance");
                    msgLabel.setText("Balance: ₹" + bal);
                } else {
                    msgLabel.setText("Account not found.");
                }

            } catch (Exception ex) {
                msgLabel.setText("Error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new BankAWT();
    }
}