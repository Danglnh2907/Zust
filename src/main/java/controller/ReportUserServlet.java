package controller;

import dao.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Account;
import model.ResReportAccountDTO;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(
        name = "ReportUserServlet",
        value = "/reportUser"
)
public class ReportUserServlet extends HttpServlet {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        AccountDAO accountDAO = new AccountDAO();
        List<ResReportAccountDTO> reportPostList = accountDAO.getAllReports();
        LOGGER.info("Successfully retrieved " + reportPostList.size() + " report posts");

        request.setAttribute("reportPostList", reportPostList);
        request.getRequestDispatcher("/WEB-INF/views/report_user.jsp").forward(request, response);
        LOGGER.info("Forwarded request to /WEB-INF/views/report_post.jsp");

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}