package controller;

import dao.GroupDAO;
import dao.PostDAO;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import model.*;
import dao.ReportGroupPostDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import util.service.FileService;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;
import java.sql.SQLException; // Thêm import

@WebServlet(
        name = "GroupServlet",
        value = "/group"
)
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
public class GroupServlet extends HttpServlet {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * /group?action=create
         * /group?id=GROUP_ID&tag=TAG
         */

        //Get session
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

        //If request has action parameter, serve the UI for the create group page
        String action = request.getParameter("action");
        if (action != null && action.equals("create")) {
            request.setAttribute("accountId", userID);
            request.getRequestDispatcher("/WEB-INF/views/createGroup.jsp").forward(request, response);
            return;
        }

        //Else, process the group information fetching
        GroupDAO groupDAO = new GroupDAO();
        PostDAO postDAO = new PostDAO();
        request.setAttribute("joinedGroups", groupDAO.getJoinedGroups(userID));

        String idParam = request.getParameter("id");
        if (idParam == null) {
            // No id provided, get all groups and joined groups
            logger.info("No group ID provided, fetching all groups and joined groups for user ID: " + userID);
            request.setAttribute("allGroups", groupDAO.getAllGroups(userID));
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
        if (tag == null || (!tag.equals("members") && !tag.equals("requests") && !tag.equals("pending") &&
                            !tag.equals("feedback") && !tag.equals("assign") && !tag.equals("report") && !tag.equals("edit"))) {
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
                request.setAttribute("joinRequests", groupDAO.getAllPendingUser(groupId));
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
                request.setAttribute("pendingPosts", (new PostDAO()).getPendingPosts(userID, groupId));
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
                List<FeedbackGroupDTO> feedbacks = groupDAO.getAllFeedback(groupId);
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
        else if (tag.equals("edit")) {
            Group grp = groupDAO.getGroupProfile(groupId);
            request.setAttribute("group", grp);
            request.getRequestDispatcher("/WEB-INF/views/groupProfile.jsp").forward(request, response);
        }
        else {
            logger.info("Action " + tag + " for group ID: " + groupId + " is not implemented, doing nothing");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * /group?id=GROUP_ID&action=ACTION
         */

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

        //Handle create group action first (since no group ID is provided unlike other action)
        GroupDAO groupDAO = new GroupDAO();
        String action = request.getParameter("action");
        if (action != null && action.equals("create")) {
            ReqGroupDTO groupDTO = extractData(request.getParts());
            if(groupDTO == null) {
                logger.info("Create group: Error in parsing data");
                request.setAttribute("msg", "Failed to send data");
            } else {
                boolean success = groupDAO.createGroup(groupDTO);
                if (success) {
                    logger.info("Create group: Success");
                    request.setAttribute("msg", "Created group successfully. Please wait for admin approval.");
                } else {
                    logger.info("Create group: Error");
                    request.setAttribute("msg", "Failed to create group. Try again.");
                }
            }
            doGet(request, response);
        }

        // Get form parameters
        PostDAO postDAO = new PostDAO();
        ReportGroupPostDAO reportGroupPostDAO = new ReportGroupPostDAO(); // Thêm DAO
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

        switch (action) {
            case "leave": { //Leave group
                boolean leaveSuccess = groupDAO.leaveGroup(userID, groupId);
                if (leaveSuccess) {
                    logger.info("User ID: " + userID + " successfully left group ID: " + groupId);
                } else {
                    logger.warning("Failed to leave group ID: " + groupId + " for user ID: " + userID);
                }
                break;
            }
            case "cancel_request": { //Cancel join group request
                boolean cancelSuccess = groupDAO.cancelJoinGroup(userID, groupId);
                if (cancelSuccess) {
                    logger.info("User ID: " + userID + " successfully canceled join request for group ID: " + groupId);
                } else {
                    logger.warning("Failed to cancel join request for group ID: " + groupId + " for user ID: " + userID);
                }
                break;
            }
            case "send_feedback": { //Send feedback to group manager
                String feedbackContent = request.getParameter("feedbackContent");
                if (feedbackContent == null || feedbackContent.trim().isEmpty()) {
                    logger.warning("Feedback content is empty for group ID: " + groupId + " by user ID: " + userID);
                    return;
                }
                boolean feedbackSuccess = groupDAO.feedback(userID, groupId, feedbackContent.trim());
                if (feedbackSuccess) {
                    logger.info("User ID: " + userID + " successfully sent feedback for group ID: " + groupId);
                } else {
                    logger.warning("Failed to send feedback for group ID: " + groupId + " for user ID: " + userID);
                }
                break;
            }
            case "join": { //Send join group request to group manager
                String joinMessage = request.getParameter("joinMessage");
                if (joinMessage == null) {
                    joinMessage = "";
                }
                boolean joinSuccess = groupDAO.joinGroup(userID, joinMessage.trim(), groupId);
                if (joinSuccess) {
                    logger.info("User ID: " + userID + " successfully sent join request for group ID: " + groupId);
                } else {
                    logger.warning("Failed to send join request for group ID: " + groupId + " for user ID: " + userID);
                }
                break;
            }
            case "edit": { //Edit group profile (group manager use case)
                try {
                    String groupName = request.getParameter("groupName");
                    String description = request.getParameter("description");
                    String imagePath = "";
                    File uploadDir = new File((new FileService()).getLocationPath() + File.separator + "images");
                    for (Part part : request.getParts()) {
                        imagePath = handleImagePart(part, uploadDir);
                    }
                    Group group = new Group();
                    group.setId(Integer.parseInt(request.getParameter("groupId")));
                    group.setGroupName(groupName);
                    group.setGroupCoverImage(imagePath);
                    group.setGroupDescription(description);
                    boolean success = groupDAO.setGroupProfile(group);
                    if (success) {
                        request.setAttribute("message", "Group successfully updated.");
                        request.setAttribute("group", group);
                    } else {
                        request.setAttribute("error", "Failed to update group.");
                        request.setAttribute("group", group);
                    }
                    response.sendRedirect(request.getContextPath() + "/group?action=edit&id=" + groupId);
                    return;
                } catch (NumberFormatException e) {
                    logger.warning("Invalid group ID: " + request.getParameter("groupId"));
                }
                break;
            }
            case "approve": //Approve join group request (group manager use case)
            case "reject": { //Reject join group request (group manager use case)
                //Both approve join group request and reject is the same core logic, just different value
                //So we group them into one block for processing
                try {
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    boolean success = groupDAO.setPendingUsers(requestId, groupId, action.equals("approve"));
                    if (success) {
                        logger.info("Approve join request success: " + groupId);
                    } else {
                        logger.warning("Failed to approve join request: " + groupId + " for user ID: " + userID);
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Invalid request ID: " + request.getParameter("requestId"));
                }
                break;
            }
            case "accept-post": //Approve pending post (group manager use case)
            case "dismiss-post": { //Dismiss pending post (group manager use case)
                //Core logic is the same, just different value, so we group both in one process
                if (!groupDAO.isManager(userID, groupId) && !groupDAO.isLeader(userID, groupId)) {
                    logger.warning("User ID: " + userID + " does not have permission to process posts for group ID: " + groupId);
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&error=" + URLEncoder.encode("Permission denied", StandardCharsets.UTF_8));
                    return;
                }

                try {
                    int postID = Integer.parseInt(request.getParameter("postId"));
                    boolean success = groupDAO.processPost(postID, action.equals("accept-post"));
                    if (success) {
                        logger.info("Post processing request success for post ID: " + postID);
                    } else {
                        logger.warning("Failed to process post for post ID: " + postID + " by user ID: " + userID);
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Invalid post ID format: " + request.getParameter("postId"));
                }
                response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=pending");
                return;
            }
            case "accept-report":
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
                    response.sendRedirect(request.getContextPath() + "/group?id=" + groupId + "&tag=report&error=" + URLEncoder.encode("Lỗi khi xử lý báo cáo: " + e.getMessage(), StandardCharsets.UTF_8));
                    return;
                }
            case "dismiss-report":
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
                    return;
                } catch (SQLException e) {
                    logger.severe("Database error dismissing report: " + e.getMessage());
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
                boolean kickSuccess = groupDAO.kickMember(memberId, groupId);
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
                boolean promoteSuccess = groupDAO.assignManager(promoteMemberId, groupId);
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
                break;
        }
        response.sendRedirect(request.getContextPath() + "/group?id=" + groupId);
    }

    private ReqGroupDTO extractData(Collection<Part> parts) {
        FileService fileService = new FileService(getServletContext());
        File uploadDir = new File(fileService.getLocationPath() + File.separator + "images");

        String groupName = null;
        String groupDescription = null;
        String imagePath = null;
        int creatorId = 0;

        if (!uploadDir.exists()) {
            //Create directory and check if it success
            if (!uploadDir.mkdirs()) { // Use mkdirs() to create parent directories too
                logger.warning("Failed to create upload directory: " + uploadDir.getAbsolutePath());
                return null;
            }
        }
        try {
            for (Part part : parts) {
                String fieldName = part.getName();
                if (fieldName == null) continue;

                if (fieldName.equals("groupName")) {
                    groupName = readPartAsString(part).trim();
                }
                else if (fieldName.equals("groupDescription")) {
                    groupDescription = readPartAsString(part);
                }
                else if (fieldName.startsWith("coverImage")) {
                    imagePath = handleImagePart(part, uploadDir);
                }
                else if (fieldName.equals("creatorId")) {
                    String managerIDRaw = readPartAsString(part);
                    if (managerIDRaw != null && !managerIDRaw.trim().isEmpty()) {
                        try {
                            creatorId = Integer.parseInt(managerIDRaw.trim());
                        } catch (NumberFormatException e) {
                            logger.warning("Invalid group ID format: " + managerIDRaw);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Error extracting data from parts: " + e.getMessage());
            return null;
        }

        return new ReqGroupDTO(groupName, groupDescription, imagePath, creatorId);
    }

    private String readPartAsString(Part part) {
        try (Scanner scanner = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            logger.warning("Failed to read part as string: " + e.getMessage());
            return null;
        }
    }

    private String handleImagePart(Part part, File uploadDir) {
        String imagePath = null;
        try {
            String fileName = "";
            String contentDisposition = part.getHeader("content-disposition");

            if (contentDisposition != null) {
                for (String cd : contentDisposition.split(";")) {
                    if (cd.trim().startsWith("filename")) {
                        fileName = cd.substring(cd.indexOf('=') + 1)
                                .trim().replace("\"", "");
                        fileName = fileName.substring(Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\')) + 1);
                        break;
                    }
                }
            }

            if (fileName.isEmpty()) {
                String existingImage = readPartAsString(part);
                if (existingImage != null && !existingImage.trim().isEmpty()) {
                    imagePath = existingImage.trim();
                }
            } else if (part.getSize() > 0) {
                // Handle new image upload - only if there's actual content
                String fileExtension = "";
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                    fileExtension = fileName.substring(dotIndex);
                }

                String newFileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + fileExtension;
                String filePath = uploadDir.getAbsolutePath() + File.separator + newFileName;

                part.write(filePath);
                imagePath = newFileName;
                logger.info("Successfully saved image: " + newFileName);
            }
        } catch (Exception e) {
            logger.warning("Failed to handle image part: " + e.getMessage());
        }
        return imagePath;
    }
}