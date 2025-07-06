package controller;

import dao.GroupDAO;
import dao.JoinGroupRequestDAO;
import dao.PostDAO;
import dto.InteractGroupDTO;
import dto.MemberDTO;
import dto.RespPostDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(
        name = "GroupServlet",
        value = "/group"
)
public class GroupServlet extends HttpServlet {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        int userID;
        try {
            Account account = (Account) session.getAttribute("users");
            if (account == null) {
                logger.warning("No user found in session, redirecting to login");
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }
            userID = account.getId();
            logger.info("Retrieved user ID: " + userID + " from session");
        } catch (Exception e) {
            logger.warning("Error getting user from session: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        GroupDAO groupDAO = new GroupDAO();
        List<InteractGroupDTO> joinedGroups = groupDAO.getJoinedGroups(userID);
        request.setAttribute("joinedGroups", joinedGroups);
        String idParam = request.getParameter("id");
        if (idParam == null) {
            // No id provided, get all groups and joined groups
            logger.info("No group ID provided, fetching all groups and joined groups for user ID: " + userID);
            List<InteractGroupDTO> allGroups = groupDAO.getAllGroups(userID);
            request.setAttribute("allGroups", allGroups);
            request.getRequestDispatcher("/WEB-INF/views/groups.jsp").forward(request, response);
            return;
        }

        // Parse group ID
        int groupId;
        try {
            groupId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            logger.warning("Invalid group ID format: " + idParam);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid group ID");
            return;
        }

        // Get group details
        InteractGroupDTO group = groupDAO.getGroup(userID, groupId);
        // Get tag parameter
        String tag = request.getParameter("tag");
        logger.info("Processing tag: " + tag + " for group ID: " + groupId);
        request.setAttribute("group", group);

        // Handle actions
        if (tag == null || (!tag.equals("members") && !tag.equals("requests") && !tag.equals("pending") && !tag.equals("feedback") && !tag.equals("assign"))) {
            PostDAO postDAO = new PostDAO();
            logger.info("Fetching posts and pending posts for group ID: " + groupId);
            List<RespPostDTO> posts = postDAO.getPosts(groupId);
            List<RespPostDTO> pendingPosts = postDAO.getPendingPosts(userID, groupId);
            request.setAttribute("posts", posts);
            request.setAttribute("pendingPosts", pendingPosts);

            request.getRequestDispatcher("/WEB-INF/views/group.jsp").forward(request, response);
        } else if (tag.equals("members")) {
            logger.info("Fetching managers and members for group ID: " + groupId);
            List<MemberDTO> managers = groupDAO.getManagers(userID, groupId);
            List<MemberDTO> members = groupDAO.getMembers(userID, groupId);
            request.setAttribute("managers", managers);
            request.setAttribute("members", members);

            request.getRequestDispatcher("/WEB-INF/views/member.jsp").forward(request, response);
        } else {
            logger.info("Action " + tag + " for group ID: " + groupId + " is not implemented, doing nothing");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        int userID;
        try {
            Account account = (Account) session.getAttribute("users");
            if (account == null) {
                logger.warning("No user found in session, redirecting to login");
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }
            userID = account.getId();
            logger.info("Retrieved user ID: " + userID + " from session for POST request");
        } catch (Exception e) {
            logger.warning("Error getting user from session: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        GroupDAO groupDAO = new GroupDAO();
        JoinGroupRequestDAO joinGroupRequestDAO = new JoinGroupRequestDAO();
        // Get form parameters
        String action = request.getParameter("action");
        String groupIdParam = request.getParameter("groupId");
        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
        } catch (NumberFormatException e) {
            logger.warning("Invalid group ID format: " + groupIdParam);
            response.sendRedirect(request.getContextPath() + "/group?error=" + URLEncoder.encode("Invalid group ID", StandardCharsets.UTF_8));
            return;
        }

        // Validate action
        if (action == null) {
            logger.warning("No action provided for group ID: " + groupId);
            response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&error=" + URLEncoder.encode("Invalid action", StandardCharsets.UTF_8));
            return;
        }

        logger.info("Processing POST action: " + action + " for group ID: " + groupId + " by user ID: " + userID);
//        String redirectUrl = request.getContextPath() + "/group?id=" + groupId;

        switch (action) {
            case "leave":
                boolean leaveSuccess = groupDAO.leaveGroup(userID, groupId);
                if (leaveSuccess) {
                    logger.info("User ID: " + userID + " successfully left group ID: " + groupId);
                } else {
                    logger.warning("Failed to leave group ID: " + groupId + " for user ID: " + userID);
//                    response.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("Failed to leave group", StandardCharsets.UTF_8));
                }
                break;

            case "cancel_request":
                boolean cancelSuccess = joinGroupRequestDAO.cancelJoinGroup(userID, groupId);
                if (cancelSuccess) {
                    logger.info("User ID: " + userID + " successfully canceled join request for group ID: " + groupId);
//                    response.sendRedirect(redirectUrl);
                } else {
                    logger.warning("Failed to cancel join request for group ID: " + groupId + " for user ID: " + userID);
//                    response.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("Failed to cancel join request", StandardCharsets.UTF_8));
                }
                break;

            case "send_feedback":
                String feedbackContent = request.getParameter("feedbackContent");
                if (feedbackContent == null || feedbackContent.trim().isEmpty()) {
                    logger.warning("Feedback content is empty for group ID: " + groupId + " by user ID: " + userID);
//                    response.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("Feedback content is required", StandardCharsets.UTF_8));
                    return;
                }
                boolean feedbackSuccess = groupDAO.feedback(userID, groupId, feedbackContent.trim());
                if (feedbackSuccess) {
                    logger.info("User ID: " + userID + " successfully sent feedback for group ID: " + groupId);
//                    response.sendRedirect(redirectUrl);
                } else {
                    logger.warning("Failed to send feedback for group ID: " + groupId + " for user ID: " + userID);
//                    response.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("Failed to send feedback", StandardCharsets.UTF_8));
                }
                break;

            case "join":
                String joinMessage = request.getParameter("joinMessage");
                if (joinMessage == null) {
                    joinMessage = "";
                }
                boolean joinSuccess = joinGroupRequestDAO.joinGroup(userID, joinMessage.trim(), groupId);
                if (joinSuccess) {
                    logger.info("User ID: " + userID + " successfully sent join request for group ID: " + groupId);
//                    response.sendRedirect(redirectUrl);

                } else {
                    logger.warning("Failed to send join request for group ID: " + groupId + " for user ID: " + userID);
//                    response.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("Failed to send join request", StandardCharsets.UTF_8));
                }
                break;

            default:
                logger.warning("Invalid action: " + action + " for group ID: " + groupId);
//                response.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("Invalid action", StandardCharsets.UTF_8));
                break;
        }
        doGet(request, response);
    }
}