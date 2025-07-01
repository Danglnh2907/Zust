package controller;    
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(
        name = "AccountDashboardServlet", 
        value = "/accountDashboard"
    )
    public class AccountDashboardServlet extends HttpServlet {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            request.getRequestDispatcher("/WEB-INF/views/account_dashboard.jsp").forward(request, response);
        }
    
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
        }
    }