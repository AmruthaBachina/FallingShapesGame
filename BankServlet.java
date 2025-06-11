import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class BankServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("<h2>Welcome to Simple Bank Servlet App</h2>");
        out.println("<p>This is a placeholder page for your bank operations.</p>");
    }
}