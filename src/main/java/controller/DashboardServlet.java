package controller;

import dao.StatisticsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(
        name = "DashboardServlet",
        value = "/dashboard"
)
public class DashboardServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardServlet.class);

    private void getStatistics(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (session.getAttribute("isAdminLoggedIn") == null || !((boolean) session.getAttribute("isAdminLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

        StatisticsDAO dao = new StatisticsDAO();
        req.setAttribute("total_users", dao.getTotalUsers());
        req.setAttribute("total_groups", dao.getTotalGroups());
        req.setAttribute("total_pending_reports", dao.getTotalPendingReports());
        req.setAttribute("total_post_in_last_24_hours", dao.getTotalPostsIn24Hours());
        req.setAttribute("user_ranking", dao.getTop10Accounts());
        req.setAttribute("group_ranking", dao.getTop10Groups());
        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getStatistics(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getStatistics(request, response);
    }
}