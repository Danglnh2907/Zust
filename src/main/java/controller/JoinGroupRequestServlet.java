package controller;

import dao.GroupDAO;
import dao.JoinGroupRequestDAO;
import dto.JoinGroupRequestDTO;
import dto.ResGroupDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/joinRequest")
public class JoinGroupRequestServlet extends HttpServlet {
    private final JoinGroupRequestDAO requestDAO = new JoinGroupRequestDAO();
    private final GroupDAO groupDAO = new GroupDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String groupIdParam = req.getParameter("groupId");
        if (groupIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "groupId is required");
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid groupId");
            return;
        }

        // Lấy thông tin nhóm từ GroupDAO
        ResGroupDTO groupInfo = groupDAO.getGroup(groupId);
        if (groupInfo == null) {
            req.setAttribute("groupInfo", null);
            req.setAttribute("error", "Group information not found for groupId: " + groupId);
        } else {
            req.setAttribute("groupInfo", groupInfo);
        }

        // Lấy danh sách yêu cầu tham gia
        List<JoinGroupRequestDTO> requests = requestDAO.getRequestsByGroupId(groupId);
        req.setAttribute("requests", requests);
        req.setAttribute("groupId", groupId);
        req.setAttribute("csrfToken", UUID.randomUUID().toString());
        req.getSession().setAttribute("csrfToken", req.getAttribute("csrfToken"));

        req.getRequestDispatcher("/WEB-INF/views/joinRequest.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        int managerId = 1; // Hardcode để test

        String sessionCsrfToken = (String) req.getSession().getAttribute("csrfToken");
        String requestCsrfToken = req.getParameter("csrfToken");
        if (sessionCsrfToken == null || !sessionCsrfToken.equals(requestCsrfToken)) {
            req.setAttribute("error", "Invalid CSRF token");
            doGet(req, resp);
            return;
        }

        String requestIdParam = req.getParameter("requestId");
        String action = req.getParameter("action");
        String groupIdParam = req.getParameter("groupId");

        if (requestIdParam == null || action == null || groupIdParam == null) {
            req.setAttribute("error", "Request ID, action, and groupId are required");
            doGet(req, resp);
            return;
        }

        int requestId;
        try {
            requestId = Integer.parseInt(requestIdParam);
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Invalid request ID");
            doGet(req, resp);
            return;
        }

        if (!"approve".equalsIgnoreCase(action) && !"disapprove".equalsIgnoreCase(action)) {
            req.setAttribute("error", "Invalid action");
            doGet(req, resp);
            return;
        }

        boolean success = requestDAO.processRequest(requestId, action);
        if (success) {
            req.setAttribute("message", "Request " + (action.equalsIgnoreCase("approve") ? "accepted" : "rejected") + " successfully");
        } else {
            req.setAttribute("error", "Failed to process request");
        }

        doGet(req, resp);
    }
}