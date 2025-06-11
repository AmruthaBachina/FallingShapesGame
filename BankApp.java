import java.sql.*;
import java.util.*;

public class BankApp {
    static final String URL = "jdbc:mysql://localhost:3306/Bank?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "amrutha2545"; // Replace with your actual MySQL password

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to Database!");

            while (true) {
                System.out.println("\n--- Bank Menu ---");
                System.out.println("1. Create Account");
                System.out.println("2. View Balance");
                System.out.println("3. Deposit");
                System.out.println("4. Withdraw");
                System.out.println("5. Transfer");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1: createAccount(); break;
                    case 2: viewBalance(); break;
                    case 3: deposit(); break;
                    case 4: withdraw(); break;
                    case 5: transfer(); break;
                    case 6: 
                        System.out.println("Thank you for using our bank!");
                        con.close();
                        return;
                    default: System.out.println("Invalid choice");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void createAccount() throws SQLException {
        System.out.print("Enter Account Number: ");
        int acc = sc.nextInt();
        System.out.print("Enter Account Holder Name: ");
        sc.nextLine(); // consume newline
        String name = sc.nextLine();
        System.out.print("Enter Initial Balance: ");
        double balance = sc.nextDouble();

        PreparedStatement pst = con.prepareStatement("INSERT INTO bank_system (account_no, name, balance) VALUES (?, ?, ?)");
        pst.setInt(1, acc);
        pst.setString(2, name);
        pst.setDouble(3, balance);

        int rows = pst.executeUpdate();
        System.out.println(rows > 0 ? "Account created successfully!" : "Account creation failed.");
    }

    static void viewBalance() throws SQLException {
        System.out.print("Enter Account Number: ");
        int acc = sc.nextInt();
        PreparedStatement pst = con.prepareStatement("SELECT balance FROM bank_system WHERE account_no = ?");
        pst.setInt(1, acc);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            System.out.println("Balance: ₹" + rs.getDouble("balance"));
        } else {
            System.out.println("Account not found.");
        }
    }

    static void deposit() throws SQLException {
        System.out.print("Enter Account Number: ");
        int acc = sc.nextInt();
        System.out.print("Enter Deposit Amount: ");
        double amount = sc.nextDouble();
        PreparedStatement pst = con.prepareStatement("UPDATE bank_system SET balance = balance + ? WHERE account_no = ?");
        pst.setDouble(1, amount);
        pst.setInt(2, acc);
        int rows = pst.executeUpdate();
        System.out.println(rows > 0 ? "Amount deposited." : "Account not found.");
    }

    static void withdraw() throws SQLException {
        System.out.print("Enter Account Number: ");
        int acc = sc.nextInt();
        System.out.print("Enter Withdraw Amount: ");
        double amount = sc.nextDouble();

        PreparedStatement check = con.prepareStatement("SELECT balance FROM bank_system WHERE account_no = ?");
        check.setInt(1, acc);
        ResultSet rs = check.executeQuery();
        if (rs.next()) {
            double bal = rs.getDouble("balance");
            if (bal >= amount) {
                PreparedStatement pst = con.prepareStatement("UPDATE bank_system SET balance = balance - ? WHERE account_no = ?");
                pst.setDouble(1, amount);
                pst.setInt(2, acc);
                pst.executeUpdate();
                System.out.println("Amount withdrawn.");
            } else {
                System.out.println("Insufficient balance.");
            }
        } else {
            System.out.println("Account not found.");
        }
    }

    static void transfer() throws SQLException {
        System.out.print("Enter From Account Number: ");
        int from = sc.nextInt();
        System.out.print("Enter To Account Number: ");
        int to = sc.nextInt();
        System.out.print("Enter Amount to Transfer: ");
        double amount = sc.nextDouble();

        con.setAutoCommit(false); // Start transaction

        try {
            PreparedStatement check = con.prepareStatement("SELECT balance FROM bank_system WHERE account_no = ?");
            check.setInt(1, from);
            ResultSet rs = check.executeQuery();

            if (rs.next() && rs.getDouble("balance") >= amount) {
                PreparedStatement withdraw = con.prepareStatement("UPDATE bank_system SET balance = balance - ? WHERE account_no = ?");
                withdraw.setDouble(1, amount);
                withdraw.setInt(2, from);
                withdraw.executeUpdate();

                PreparedStatement deposit = con.prepareStatement("UPDATE bank_system SET balance = balance + ? WHERE account_no = ?");
                deposit.setDouble(1, amount);
                deposit.setInt(2, to);
                deposit.executeUpdate();

                con.commit();
                System.out.println("Transfer successful.");
            } else {
                System.out.println("Insufficient balance or account not found.");
                con.rollback();
            }

        } catch (SQLException e) {
            con.rollback();
            System.out.println("Transfer failed. Rolling back.");
        } finally {
            con.setAutoCommit(true);
        }
    }
}