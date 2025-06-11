import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BankAWTApp extends Frame implements ActionListener {

    TextField tfAccNo, tfName, tfBalance;
    Button btnCreate;
    Connection con;

    public BankAWTApp() {
        setLayout(new GridLayout(4, 2));

        // Create UI components
        add(new Label("Account Number:"));
        tfAccNo = new TextField();
        add(tfAccNo);

        add(new Label("Name:"));
        tfName = new TextField();
        add(tfName);

        add(new Label("Initial Balance:"));
        tfBalance = new TextField();
        add(tfBalance);

        btnCreate = new Button("Create Account");
        add(btnCreate);

        btnCreate.addActionListener(this);

        // Frame settings
        setTitle("Bank Account Creation");
        setSize(300, 200);
        setVisible(true);

        // DB connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/Bank?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                "root",
                "amrutha2545"
            );
            System.out.println("Connected to MySQL");
        } catch (Exception e) {
            e.printStackTrace();
        }

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    if (con != null) con.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    public void actionPerformed(ActionEvent ae) {
        try {
            int accNo = Integer.parseInt(tfAccNo.getText());
            String name = tfName.getText();
            double balance = Double.parseDouble(tfBalance.getText());

            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO bank_system (account_no, name, balance) VALUES (?, ?, ?)"
            );
            pst.setInt(1, accNo);
            pst.setString(2, name);
            pst.setDouble(3, balance);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("Account created successfully!");
            } else {
                System.out.println("Failed to create account.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new BankAWTApp();
    }
}