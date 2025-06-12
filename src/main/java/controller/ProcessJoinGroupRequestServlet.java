package controller;

import dao.JoinGroupRequestDAO;
import dto.JoinGroupRequestDTO;
import model.Account;
import model.Group;
import model.JoinGroupRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/processJoinGroupRequest")
public class ProcessJoinGroupRequestServlet extends HttpServlet {
    private final JoinGroupRequestDAO joinGroupRequestDAO = new JoinGroupRequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
        String accountRole = (String) req.getSession().getAttribute("accountRole");
        if (accountId == null || !"admin".equals(accountRole)) {
            accountId = 1004; // Set tạm accountId để test
            req.getSession().setAttribute("accountId", accountId);
            req.getSession().setAttribute("accountRole", "admin"); // Set tạm role để test
            System.out.println("Set temporary accountId: " + accountId + " and role: admin for testing");
        }

        String groupIdParam = req.getParameter("groupId");
        if (groupIdParam == null || groupIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Group ID is required");
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid group ID");
            return;
        }

        List<JoinGroupRequest> requests = joinGroupRequestDAO.getPendingJoinRequests(groupId, accountId);
        List<JoinGroupRequestDTO> requestDtos = new ArrayList<>();
        for (JoinGroupRequest request : requests) {
            Account account = new Account();
            account.setId(request.getAccount().getId());
            Group group = new Group();
            group.setId(request.getGroup().getId());
            requestDtos.add(new JoinGroupRequestDTO(request, account, group));
        }
        req.setAttribute("requests", requestDtos);
        req.getRequestDispatcher("/WEB-INF/views/ProcessJoinGroupRequest.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
        String accountRole = (String) req.getSession().getAttribute("accountRole");
        if (accountId == null || !"admin".equals(accountRole)) {
            resp.sendRedirect("login.jsp");
            return;
        }

        String requestIdParam = req.getParameter("requestId");
        String action = req.getParameter("action");
        if (requestIdParam == null || requestIdParam.trim().isEmpty() || action == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request ID and action are required");
            return;
        }

        int requestId;
        try {
            requestId = Integer.parseInt(requestIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request ID");
            return;
        }

        if (!"approve".equalsIgnoreCase(action) && !"disapprove".equalsIgnoreCase(action)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            return;
        }

        boolean success = false;
        if ("approve".equalsIgnoreCase(action)) {
            success = joinGroupRequestDAO.approveJoinRequest(requestId, accountId);
        } else if ("disapprove".equalsIgnoreCase(action)) {
            success = joinGroupRequestDAO.disapproveJoinRequest(requestId, accountId);
        }

        if (success) {
            req.setAttribute("message", "Request " + action + "d successfully");
        } else {
            req.setAttribute("error", "Failed to process request");
        }
        String groupIdParam = req.getParameter("groupId");
        if (groupIdParam != null && !groupIdParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/processJoinGroupRequest?groupId=" + groupIdParam);
        } else {
            resp.sendRedirect(req.getContextPath() + "/processJoinGroupRequest");
        }
    }
}