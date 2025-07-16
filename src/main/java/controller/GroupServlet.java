package controller;

import dao.GroupDAO;
import dao.JoinGroupRequestDAO;
import dao.PostDAO;
import dto.InteractGroupDTO;
import dto.MemberDTO;
import dto.RespPostDTO;
import dao.MemberViewDAO; // Thêm import
import dao.FeedbackGroupDAO;
import dto.FeedbackGroupDTO;
import dto.PostApprovalDTO;
import dao.PostApprovalDAO;
import dto.JoinGroupRequestDTO;
import dao.ReportGroupPostDAO;
import dto.ResGroupReportPostDTO;
import dto.AcceptGroupReportDTO;
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
import java.sql.SQLException; // Thêm import

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
        if (tag == null || (!tag.equals("members") && !tag.equals("requests") && !tag.equals("pending") && !tag.equals("feedback") && !tag.equals("assign") && !tag.equals("report"))) {
            PostDAO postDAO = new PostDAO();
            logger.info("Fetching posts and pending posts for group ID: " + groupId);
            List<RespPostDTO> pendingPosts = postDAO.getPendingPosts(userID, groupId);
            List<RespPostDTO> posts = postDAO.getPostsInGroup(userID, groupId);
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
        }
        else if (tag.equals("requests")) {
            logger.info("Fetching join requests for group ID: " + groupId);
            if (groupDAO.isManager(userID, groupId) || groupDAO.isLeader(userID, groupId)) {
                JoinGroupRequestDAO joinGroupRequestDAO = new JoinGroupRequestDAO();
                List<JoinGroupRequestDTO> joinRequests = joinGroupRequestDAO.getRequestsByGroupId(groupId);
                request.setAttribute("joinRequests", joinRequests);
                request.getRequestDispatcher("/WEB-INF/views/joinRequest.jsp").forward(request, response);
            } else {
                logger.warning("User ID: " + userID + " does not have permission to view requests for group ID: " + groupId);
                response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&error=" + URLEncoder.encode("Permission denied", StandardCharsets.UTF_8));
            }
        }

        else if (tag.equals("pending")) {
            logger.info("Fetching pending posts for group ID: " + groupId);
            if (group == null) {
                logger.warning("Group ID: " + groupId + " not found");
                response.sendRedirect(request.getContextPath() + "/group?error=" + URLEncoder.encode("Group not found", StandardCharsets.UTF_8));
                return;
            }
            if (groupDAO.isManager(userID, groupId) || groupDAO.isLeader(userID, groupId)) {
                PostApprovalDAO postApprovalDAO = new PostApprovalDAO();
                List<PostApprovalDTO> pendingPosts = postApprovalDAO.getPendingPosts(userID);
                request.setAttribute("pendingPosts", pendingPosts);
                request.getRequestDispatcher("/WEB-INF/views/postApprove.jsp").forward(request, response);
            } else {
                logger.warning("User ID: " + userID + " does not have permission to view pending posts for group ID: " + groupId);
                response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&error=" + URLEncoder.encode("Permission denied", StandardCharsets.UTF_8));
            }
        }
        else if (tag.equals("feedback")) {
            logger.info("Fetching feedbacks for group ID: " + groupId);
            if (group == null) {
                logger.warning("Group ID: " + groupId + " not found");
                response.sendRedirect(request.getContextPath() + "/group?error=" + URLEncoder.encode("Group not found", StandardCharsets.UTF_8));
                return;
            }
            if (groupDAO.isManager(userID, groupId) || groupDAO.isLeader(userID, groupId)) {
                FeedbackGroupDAO feedbackDAO = new FeedbackGroupDAO();
                List<FeedbackGroupDTO> feedbacks = feedbackDAO.getFeedbacksByGroupId(groupId, "sent");
                request.setAttribute("feedbacks", feedbacks);
                request.getRequestDispatcher("/WEB-INF/views/viewFeedback.jsp").forward(request, response);
            } else {
                logger.warning("User ID: " + userID + " does not have permission to view feedbacks for group ID: " + groupId);
                response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&error=" + URLEncoder.encode("Permission denied", StandardCharsets.UTF_8));
            }
        }
        else if (tag.equals("report")) {
            logger.info("Fetching reported posts for group ID: " + groupId);
            if (groupDAO.isManager(userID, groupId) || groupDAO.isLeader(userID, groupId)) {
                ReportGroupPostDAO reportGroupPostDAO = new ReportGroupPostDAO();
                try {
                    List<ResGroupReportPostDTO> reportPostList = reportGroupPostDAO.getAllReportsForGroupManager(groupId, userID);
                    request.setAttribute("reportPostList", reportPostList);
                    request.getRequestDispatcher("/WEB-INF/views/reported_group.jsp").forward(request, response);
                } catch (SQLException e) {
                    logger.severe("Database error retrieving report posts: " + e.getMessage());
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&error=" + URLEncoder.encode("Failed to retrieve report posts", StandardCharsets.UTF_8));
                }
            }
            }
        else {
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
        MemberViewDAO memberDAO = new MemberViewDAO(); // Thêm instance
        PostDAO postDAO = new PostDAO();
        ReportGroupPostDAO reportGroupPostDAO = new ReportGroupPostDAO(); // Thêm DAO
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
            case "approve":
            case "reject":
                if (!groupDAO.isManager(userID, groupId) && !groupDAO.isLeader(userID, groupId)) {
                    logger.warning("User ID: " + userID + " does not have permission to process requests for group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&error=" + URLEncoder.encode("Permission denied", StandardCharsets.UTF_8));
                    return;
                }
                String requestIdParam = request.getParameter("requestId");
                int requestId;
                try {
                    requestId = Integer.parseInt(requestIdParam);
                } catch (NumberFormatException e) {
                    logger.warning("Invalid request ID format: " + requestIdParam);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=requests&error=" + URLEncoder.encode("Invalid request ID", StandardCharsets.UTF_8));
                    return;
                }
                boolean processSuccess = joinGroupRequestDAO.processRequest(requestId, action);
                if (processSuccess) {
                    logger.info("User ID: " + userID + " successfully " + action + "d request ID: " + requestId + " for group ID: " + groupId);
                } else {
                    logger.warning("Failed to " + action + " request ID: " + requestId + " for group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=requests&error=" + URLEncoder.encode("Failed to process request", StandardCharsets.UTF_8));
                    return;
                }
                break;

            case "approve_post":
            case "reject_post":
                if (!groupDAO.isManager(userID, groupId) && !groupDAO.isLeader(userID, groupId)) {
                    logger.warning("User ID: " + userID + " does not have permission to process posts for group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&error=" + URLEncoder.encode("Permission denied", StandardCharsets.UTF_8));
                    return;
                }
                String postIdParam = request.getParameter("postId");
                int postId;
                try {
                    postId = Integer.parseInt(postIdParam);
                } catch (NumberFormatException e) {
                    logger.warning("Invalid post ID format: " + postIdParam);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=pending&error=" + URLEncoder.encode("Invalid post ID", StandardCharsets.UTF_8));
                    return;
                }
                PostApprovalDAO postApprovalDAO = new PostApprovalDAO();
                boolean processSuccesspost = postApprovalDAO.processPost(postId, action.equals("approve_post") ? "approve" : "reject");
                if (processSuccesspost) {
                    logger.info("User ID: " + userID + " successfully " + action + "d post ID: " + postId + " for group ID: " + groupId);
                } else {
                    logger.warning("Failed to " + action + " post ID: " + postId + " for group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=pending&error=" + URLEncoder.encode("Failed to process post", StandardCharsets.UTF_8));
                    return;
                }
                break;
            case "accept":
                if (!groupDAO.isManager(userID, groupId) && !groupDAO.isLeader(userID, groupId)) {
                    logger.warning("User ID: " + userID + " does not have permission to process reports for group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&error=" + URLEncoder.encode("Không có quyền xử lý báo cáo", StandardCharsets.UTF_8));
                    return;
                }
                int reportId = Integer.parseInt(request.getParameter("reportId"));
                int reporterId = Integer.parseInt(request.getParameter("reporterId"));
                int reportedPostId = Integer.parseInt(request.getParameter("reportedPostId"));
                String suspensionMessage = request.getParameter("suspensionMessage");

                int reportedAccountId;
                try {
                    reportedAccountId = postDAO.getAccountIdByPostId(reportedPostId);
                    if (reportedAccountId == 0) {
                        logger.warning("No account found for reported post ID: " + reportedPostId);
                        response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=report&error=" + URLEncoder.encode("ID bài viết không hợp lệ", StandardCharsets.UTF_8));
                        return;
                    }
                } catch (SQLException e) {
                    logger.severe("Database error retrieving account ID for post ID: " + reportedPostId + ": " + e.getMessage());
                    e.printStackTrace(); // Thêm để debug
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=report&error=" + URLEncoder.encode("Lỗi cơ sở dữ liệu khi lấy thông tin bài viết", StandardCharsets.UTF_8));
                    return;
                }

                AcceptGroupReportDTO acceptReportDTO = new AcceptGroupReportDTO();
                acceptReportDTO.setReportId(reportId);
                acceptReportDTO.setReportAccountId(reporterId);
                acceptReportDTO.setReportedAccountId(reportedAccountId);
                acceptReportDTO.setReportedPostId(reportedPostId);
                acceptReportDTO.setNotificationContent(suspensionMessage);

                try {
                    reportGroupPostDAO.acceptReportForGroup(acceptReportDTO, userID);
                    logger.info("User ID: " + userID + " successfully accepted report ID: " + reportId + " for post ID: " + reportedPostId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=report&success=" + URLEncoder.encode("Báo cáo đã được chấp nhận", StandardCharsets.UTF_8));
                    return; // Tránh gọi doGet
                } catch (SQLException e) {
                    logger.severe("Database error processing report: " + e.getMessage());
                    e.printStackTrace(); // Thêm để debug lỗi DAO
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=report&error=" + URLEncoder.encode("Lỗi khi xử lý báo cáo: " + e.getMessage(), StandardCharsets.UTF_8));
                    return;
                }

            case "dismiss":
                if (!groupDAO.isManager(userID, groupId) && !groupDAO.isLeader(userID, groupId)) {
                    logger.warning("User ID: " + userID + " does not have permission to process reports for group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&error=" + URLEncoder.encode("Permission denied", StandardCharsets.UTF_8));
                    return;
                }
                int dismissReportId = Integer.parseInt(request.getParameter("reportId"));
                try {
                    reportGroupPostDAO.dismissReportForGroup(dismissReportId, userID);
                    logger.info("User ID: " + userID + " successfully dismissed report ID: " + dismissReportId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=report&success=" + URLEncoder.encode("Báo cáo đã bị từ chối", StandardCharsets.UTF_8));
                    return; // Tránh gọi doGet
                } catch (SQLException e) {
                    logger.severe("Database error dismissing report: " + e.getMessage());
                    e.printStackTrace(); // Thêm để debug lỗi DAO
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=report&error=" + URLEncoder.encode("Failed to dismiss report: " + e.getMessage(), StandardCharsets.UTF_8));
                    return;
                }
            case "kick_member":
                if (!groupDAO.isManager(userID, groupId) && !groupDAO.isLeader(userID, groupId)) {
                    logger.warning("User ID: " + userID + " does not have permission to kick members in group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=members&error=" + URLEncoder.encode("Permission denied", StandardCharsets.UTF_8));
                    return;
                }
                String memberIdParam = request.getParameter("memberId");
                int memberId;
                try {
                    memberId = Integer.parseInt(memberIdParam);
                } catch (NumberFormatException e) {
                    logger.warning("Invalid member ID format: " + memberIdParam);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=members&error=" + URLEncoder.encode("Invalid member ID", StandardCharsets.UTF_8));
                    return;
                }
                boolean kickSuccess = memberDAO.removeMember(memberId, groupId);
                if (kickSuccess) {
                    logger.info("User ID: " + userID + " successfully kicked member ID: " + memberId + " from group ID: " + groupId);
                } else {
                    logger.warning("Failed to kick member ID: " + memberId + " from group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=members&error=" + URLEncoder.encode("Failed to kick member", StandardCharsets.UTF_8));
                    return;
                }
                break;

            case "promote_manager":
                if (!groupDAO.isManager(userID, groupId) && !groupDAO.isLeader(userID, groupId)) {
                    logger.warning("User ID: " + userID + " does not have permission to promote managers in group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=members&error=" + URLEncoder.encode("Permission denied", StandardCharsets.UTF_8));
                    return;
                }
                String promoteMemberIdParam = request.getParameter("memberId");
                int promoteMemberId;
                try {
                    promoteMemberId = Integer.parseInt(promoteMemberIdParam);
                } catch (NumberFormatException e) {
                    logger.warning("Invalid member ID format: " + promoteMemberIdParam);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=members&error=" + URLEncoder.encode("Invalid member ID", StandardCharsets.UTF_8));
                    return;
                }
                boolean promoteSuccess = memberDAO.promoteToManager(promoteMemberId, groupId);
                if (promoteSuccess) {
                    logger.info("User ID: " + userID + " successfully promoted member ID: " + promoteMemberId + " to manager in group ID: " + groupId);
                } else {
                    logger.warning("Failed to promote member ID: " + promoteMemberId + " to manager in group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=members&error=" + URLEncoder.encode("Failed to promote manager", StandardCharsets.UTF_8));
                    return;
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