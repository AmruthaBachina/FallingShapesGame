import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class BankAppSwing extends JFrame implements ActionListener {
    JTextField tfAccNo, tfName, tfAmount, tfToAcc;
    JTextArea ta;
    JButton btnCreate, btnView, btnDeposit, btnWithdraw, btnTransfer, btnUpdate, btnClose, btnViewAll;
    Connection conn;

    public BankAppSwing() {
        setTitle("Bank Management System - Swing");
        setLayout(null);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel l1 = new JLabel("Account No:");
        l1.setBounds(50, 30, 100, 30); add(l1);
        tfAccNo = new JTextField(); tfAccNo.setBounds(160, 30, 200, 30); add(tfAccNo);

        JLabel l2 = new JLabel("Name:");
        l2.setBounds(50, 70, 100, 30); add(l2);
        tfName = new JTextField(); tfName.setBounds(160, 70, 200, 30); add(tfName);

        JLabel l3 = new JLabel("Amount:");
        l3.setBounds(50, 110, 100, 30); add(l3);
        tfAmount = new JTextField(); tfAmount.setBounds(160, 110, 200, 30); add(tfAmount);

        JLabel l4 = new JLabel("To Account:");
        l4.setBounds(50, 150, 100, 30); add(l4);
        tfToAcc = new JTextField(); tfToAcc.setBounds(160, 150, 200, 30); add(tfToAcc);

        btnCreate = new JButton("Create"); btnCreate.setBounds(50, 200, 100, 30); btnCreate.addActionListener(this); add(btnCreate);
        btnView = new JButton("View"); btnView.setBounds(160, 200, 100, 30); btnView.addActionListener(this); add(btnView);
        btnDeposit = new JButton("Deposit"); btnDeposit.setBounds(270, 200, 100, 30); btnDeposit.addActionListener(this); add(btnDeposit);
        btnWithdraw = new JButton("Withdraw"); btnWithdraw.setBounds(380, 200, 100, 30); btnWithdraw.addActionListener(this); add(btnWithdraw);
        btnTransfer = new JButton("Transfer"); btnTransfer.setBounds(490, 200, 100, 30); btnTransfer.addActionListener(this); add(btnTransfer);
        btnUpdate = new JButton("Update"); btnUpdate.setBounds(600, 200, 100, 30); btnUpdate.addActionListener(this); add(btnUpdate);
        btnClose = new JButton("Close"); btnClose.setBounds(710, 200, 100, 30); btnClose.addActionListener(this); add(btnClose);
        btnViewAll = new JButton("View All"); btnViewAll.setBounds(50, 240, 100, 30); btnViewAll.addActionListener(this); add(btnViewAll);

        ta = new JTextArea(); ta.setBounds(50, 290, 800, 240);
        JScrollPane scrollPane = new JScrollPane(ta);
        scrollPane.setBounds(50, 290, 800, 240);
        add(scrollPane);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "amrutha2545");
            ta.setText("Successfully connected to the database.\n");
        } catch (Exception e) {
            ta.setText("Error connecting to database: " + e.getMessage());
        }

        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        try {
            int accNo = Integer.parseInt(tfAccNo.getText().trim());

            switch (cmd) {
                case "Create":
                    PreparedStatement createStmt = conn.prepareStatement("INSERT INTO accounts (acc_no, name, balance) VALUES (?, ?, ?)");
                    createStmt.setInt(1, accNo);
                    createStmt.setString(2, tfName.getText().trim());
                    createStmt.setDouble(3, 0.0);
                    createStmt.executeUpdate();
                    ta.setText("Account created successfully.");
                    break;

                case "View":
                    PreparedStatement viewStmt = conn.prepareStatement("SELECT * FROM accounts WHERE acc_no=?");
                    viewStmt.setInt(1, accNo);
                    ResultSet rs = viewStmt.executeQuery();
                    if (rs.next()) {
                        ta.setText("Account No: " + rs.getInt("acc_no") +
                                   "\nName: " + rs.getString("name") +
                                   "\nBalance: " + rs.getDouble("balance"));
                    } else {
                        ta.setText("Account not found.");
                    }
                    break;

                case "Deposit":
                    double depositAmount = Double.parseDouble(tfAmount.getText().trim());
                    PreparedStatement depStmt = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE acc_no = ?");
                    depStmt.setDouble(1, depositAmount);
                    depStmt.setInt(2, accNo);
                    depStmt.executeUpdate();
                    ta.setText("Amount deposited successfully.");
                    break;

                case "Withdraw":
                    double withdrawAmount = Double.parseDouble(tfAmount.getText().trim());
                    PreparedStatement balCheck = conn.prepareStatement("SELECT balance FROM accounts WHERE acc_no=?");
                    balCheck.setInt(1, accNo);
                    ResultSet brs = balCheck.executeQuery();
                    if (brs.next() && brs.getDouble("balance") >= withdrawAmount) {
                        PreparedStatement withStmt = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE acc_no = ?");
                        withStmt.setDouble(1, withdrawAmount);
                        withStmt.setInt(2, accNo);
                        withStmt.executeUpdate();
                        ta.setText("Amount withdrawn successfully.");
                    } else {
                        ta.setText("Insufficient balance.");
                    }
                    break;

                case "Transfer":
                    int toAcc = Integer.parseInt(tfToAcc.getText().trim());
                    double amt = Double.parseDouble(tfAmount.getText().trim());
                    conn.setAutoCommit(false);
                    PreparedStatement checkBal = conn.prepareStatement("SELECT balance FROM accounts WHERE acc_no=?");
                    checkBal.setInt(1, accNo);
                    ResultSet rs1 = checkBal.executeQuery();
                    if (rs1.next() && rs1.getDouble("balance") >= amt) {
                        PreparedStatement withdraw = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE acc_no = ?");
                        withdraw.setDouble(1, amt);
                        withdraw.setInt(2, accNo);
                        withdraw.executeUpdate();

                        PreparedStatement deposit = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE acc_no = ?");
                        deposit.setDouble(1, amt);
                        deposit.setInt(2, toAcc);
                        deposit.executeUpdate();

                        conn.commit();
                        ta.setText("Transfer successful.");
                    } else {
                        conn.rollback();
                        ta.setText("Transfer failed. Insufficient balance.");
                    }
                    conn.setAutoCommit(true);
                    break;

                case "Update":
                    PreparedStatement updateStmt = conn.prepareStatement("UPDATE accounts SET name=? WHERE acc_no=?");
                    updateStmt.setString(1, tfName.getText().trim());
                    updateStmt.setInt(2, accNo);
                    updateStmt.executeUpdate();
                    ta.setText("Account updated successfully.");
                    break;

                case "Close":
                    PreparedStatement closeStmt = conn.prepareStatement("DELETE FROM accounts WHERE acc_no=?");
                    closeStmt.setInt(1, accNo);
                    closeStmt.executeUpdate();
                    ta.setText("Account closed successfully.");
                    break;

                case "View All":
                    Statement stmt = conn.createStatement();
                    ResultSet allRs = stmt.executeQuery("SELECT * FROM accounts");
                    StringBuilder sb = new StringBuilder("All Accounts:\n");
                    while (allRs.next()) {
                        sb.append("AccNo: ").append(allRs.getInt("acc_no"))
                          .append(", Name: ").append(allRs.getString("name"))
                          .append(", Balance: ").append(allRs.getDouble("balance")).append("\n");
                    }
                    ta.setText(sb.toString());
                    break;
            }
        } catch (Exception e) {
            ta.setText("Error: " + e.getMessage());
            try { conn.rollback(); } catch (Exception ex) {}
        }
    }

    public static void main(String[] args) {
        new BankAppSwing();
    }
}
