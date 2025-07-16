//package controller;
//
//import dao.GroupDAO;
//import dao.MemberViewDAO;
//import dto.MemberViewDTO;
//import dto.ResGroupDTO;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.UUID;
//
///**
// * Servlet handling requests related to viewing and managing group members.
// */
//@WebServlet("/viewMembers")
//public class MemberViewServlet extends HttpServlet {
//    private final MemberViewDAO memberDAO = new MemberViewDAO();
//    private final GroupDAO groupDAO = new GroupDAO();
//
//    /**
//     * Handles GET requests to display the list of members in a group.
//     */
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        // Hardcoded groupId for testing
//        int groupId = 1;
//
//        // Retrieve group information from GroupDAO
//        ResGroupDTO groupInfo = groupDAO.getGroup(groupId);
//        if (groupInfo == null) {
//            req.setAttribute("groupInfo", null);
//            req.setAttribute("error", "Group information not found for groupId: " + groupId);
//        } else {
//            req.setAttribute("groupInfo", groupInfo);
//        }
//
////        // Retrieve list of members
//////        List<MemberViewDTO> members = memberDAO.getMembersByGroupId(groupId);
////        req.setAttribute("members", members);
////        req.setAttribute("groupId", groupId);
////        req.setAttribute("csrfToken", UUID.randomUUID().toString());
////        req.getSession().setAttribute("csrfToken", req.getAttribute("csrfToken"));
////
////        req.getRequestDispatcher("/WEB-INF/views/memberView.jsp").forward(req, resp);
////    }
//
//    /**
//     * Handles POST requests to remove a member or promote a member to manager.
//     */
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        // Hardcoded managerId and groupId for testing
//        int managerId = 4;
//        int groupId = 1;
//
//        // Verify CSRF token
//        String sessionCsrfToken = (String) req.getSession().getAttribute("csrfToken");
//        String requestCsrfToken = req.getParameter("csrfToken");
//        if (sessionCsrfToken == null || !sessionCsrfToken.equals(requestCsrfToken)) {
//            req.setAttribute("error", "Invalid CSRF token");
//            doGet(req, resp);
//            return;
//        }
//
//        String action = req.getParameter("action");
//        String accountIdParam = req.getParameter("accountId");
//        if (accountIdParam == null) {
//            req.setAttribute("error", "accountId is required");
//            doGet(req, resp);
//            return;
//        }
//
//        int accountId;
//        try {
//            accountId = Integer.parseInt(accountIdParam);
//        } catch (NumberFormatException e) {
//            req.setAttribute("error", "Invalid accountId");
//            doGet(req, resp);
//            return;
//        }
//
//        // Check if the user is a manager
//        if (!memberDAO.isGroupManager(managerId, groupId)) {
//            req.setAttribute("error", "You do not have permission to perform this action");
//            doGet(req, resp);
//            return;
//        }
//
//        boolean success = false;
//        if ("remove".equals(action)) {
//            // Remove member
//            success = memberDAO.removeMember(accountId, groupId);
//            if (success) {
//                req.setAttribute("message", "Member removed successfully");
//            } else {
//                req.setAttribute("error", "Failed to remove member");
//            }
//        } else if ("promote".equals(action)) {
//            // Promote member to manager
//            success = memberDAO.promoteToManager(accountId, groupId);
//            if (success) {
//                req.setAttribute("message", "Member promoted to manager successfully");
//            } else {
//                req.setAttribute("error", "Failed to promote member to manager");
//            }
//        } else {
//            req.setAttribute("error", "Invalid action");
//        }
//
//        // Generate new CSRF token
//        String newCsrfToken = UUID.randomUUID().toString();
//        req.setAttribute("csrfToken", newCsrfToken);
//        req.getSession().setAttribute("csrfToken", newCsrfToken);
//
//        doGet(req, resp);
//    }
//}