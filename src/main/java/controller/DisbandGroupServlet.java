package controller;

import dao.DisbandGroupDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/disbandGroup")
public class DisbandGroupServlet extends HttpServlet {
    private final DisbandGroupDAO disbandGroupDAO = new DisbandGroupDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionCsrfToken = (String) req.getSession().getAttribute("csrfToken");
        String requestCsrfToken = req.getParameter("csrfToken");
        if (sessionCsrfToken == null || !sessionCsrfToken.equals(requestCsrfToken)) {
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\": false, \"error\": \"Invalid CSRF token\"}");
            return;
        }

        String groupIdParam = req.getParameter("groupId");
        if (groupIdParam == null) {
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\": false, \"error\": \"groupId is required\"}");
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
        } catch (NumberFormatException e) {
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\": false, \"error\": \"Invalid groupId\"}");
            return;
        }

        boolean success = disbandGroupDAO.disbandGroup(groupId);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"success\": " + success + "}");
    }
}