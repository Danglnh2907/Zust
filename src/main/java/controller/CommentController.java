package controller;

import dao.CommentDAO;
import dto.ReqCommentDTO;
import dto.RespCommentDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import util.service.FileService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

/*
 * Handle all comment-related HTTP requests:
 * - GET /comment: Retrieve all comments for a specific post
 * - POST /comment?action=create: Create a new comment
 * - POST /comment?action=edit&id=comment_id: Edit an existing comment
 * - POST /comment?action=delete&id=comment_id: Delete a comment
 */
@WebServlet(name = "CommentControllerServlet", value = "/comment")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5MB
public class CommentController extends HttpServlet {
    // Logger for debugging
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    // FileService for handling file uploads
    private final FileService fileService = new FileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Fetch userID from session
        HttpSession session = request.getSession();
        String userIDRaw = (String) session.getAttribute("userID");
        int userID;
        try {
            userID = Integer.parseInt(userIDRaw);
        } catch (NumberFormatException e) {
            userID = 1; // Temporary, remove after authentication
        }

        // Get postId from request parameter
        String postIdRaw = request.getParameter("post_id");
        postIdRaw = postIdRaw == null ? "1" : postIdRaw.trim(); // Temporary, remove after authentication
//        if (postIdRaw == null || postIdRaw.trim().isEmpty()) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            return;
//        }

        try {
            int postId = Integer.parseInt(postIdRaw);
            CommentDAO commentDAO = new CommentDAO();
            ArrayList<RespCommentDTO> comments = commentDAO.viewAllComments(postId, userID);
            request.setAttribute("postId", postId);
            if (comments.isEmpty()) {
                request.setAttribute("message", "No comments found");
            } else {
                request.setAttribute("comments", comments);
            }
            request.getRequestDispatcher("/WEB-INF/views/comments.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            logger.warning("Invalid postID: " + postIdRaw);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Fetch userID from session
        HttpSession session = request.getSession();
        String userIDRaw = (String) session.getAttribute("userID");
        int userID;
        try {
            userID = Integer.parseInt(userIDRaw);
        } catch (NumberFormatException e) {
            userID = 1; // Temporary, remove after authentication
        }

        // Set response content type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Get action parameter
        String action = request.getParameter("action");
        action = action == null ? "" : action.toLowerCase().trim();
        CommentDAO commentDAO = new CommentDAO();

        switch (action) {
            case "create":
                ReqCommentDTO createDTO = extractData(userID, request.getParts());
//                if (createDTO == null) {
//                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                    out.write("{\"success\": false, \"message\": \"Invalid input\"}");
//                    return;
//                }
                // Checking if either content or image is present
                if (createDTO.getCommentContent() == null && createDTO.getCommentImage() == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"success\": false, \"message\": \"Comment must contain text or an image\"}");
                    return;
                }
                boolean createSuccess = commentDAO.createComment(createDTO);
                if (createSuccess) {
                    RespCommentDTO createdComment = commentDAO.getCommentById(commentDAO.getLatestCommentId(userID)); // Assuming this method exists
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.write("{\"success\": true, \"comment\": " + toJson(createdComment) + "}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write("{\"success\": false, \"message\": \"Failed to create comment\"}");
                }
                break;

            case "edit":
                // Get commentId
                String commentIdRaw = request.getParameter("id");
                if (commentIdRaw == null || commentIdRaw.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"success\": false, \"message\": \"Comment ID required\"}");
                    return;
                }
                try {
                    int commentId = Integer.parseInt(commentIdRaw);
                    ReqCommentDTO editDTO = extractData(userID, request.getParts());
                    if (editDTO == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write("{\"success\": false, \"message\": \"Invalid input\"}");
                        return;
                    }
                    boolean editSuccess = commentDAO.editComment(commentId, editDTO);
                    response.setStatus(editSuccess ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_ACCEPTABLE);
                    out.println("{\"success\": " + editSuccess + "}");
                } catch (NumberFormatException e) {
                    logger.warning("Invalid commentID: " + commentIdRaw);
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"success\": false, \"message\": \"Invalid comment ID\"}");
                }
                break;

            case "delete":
                // Get commentId
                commentIdRaw = request.getParameter("id");
                if (commentIdRaw == null || commentIdRaw.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"success\": false, \"message\": \"Comment ID required\"}");
                    return;
                }
                try {
                    int commentId = Integer.parseInt(commentIdRaw);
                    boolean deleteSuccess = commentDAO.deleteComment(commentId, userID);
                    response.setStatus(deleteSuccess ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_ACCEPTABLE);
                    out.println("{\"success\": " + deleteSuccess + "}");
                } catch (NumberFormatException e) {
                    logger.warning("Invalid commentID: " + commentIdRaw);
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"success\": false, \"message\": \"Invalid comment ID\"}");
                }
                break;

            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"success\": false, \"message\": \"Invalid action\"}");
                break;
        }
    }

    /*
     * Extract comment data from multipart form parts
     * @param userID - The ID of the user creating or editing the comment
     * @param parts - Collection of multipart form parts
     * @return ReqCommentDTO - Data transfer object containing comment details
     */
    private ReqCommentDTO extractData(int userID, Collection<Part> parts) {
        String commentContent = null;
        String commentImage = null;
        Integer replyCommentId = null;
        int postId = -1;
        LocalDateTime now = LocalDateTime.now();

        // Create storage directory if not exists
        File uploadDir = new File(fileService.getLocationPath() + File.separator + "images");
        if (!uploadDir.exists()) {
            if (!uploadDir.mkdir()) {
                logger.severe("Failed to create upload directory: " + uploadDir.getPath());
                return null;
            }
        }

        for (Part part : parts) {
            String fieldName = part.getName();
            logger.info("Processing part: " + fieldName);
            try {
                if (fieldName.equals("commentContent")) {
                    try (Scanner scanner = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)) {
                        if (scanner.hasNext()) {
                            commentContent = scanner.useDelimiter("\\A").next().trim();
                        } else {
                            logger.warning("No content found in commentContent part");
                        }
                    }
                } else if (fieldName.equals("commentImage")) {
                    String fileName = "";
                    String contentDisposition = part.getHeader("content-disposition");
                    if (contentDisposition != null) {
                        for (String cd : contentDisposition.split(";")) {
                            if (cd.trim().startsWith("filename")) {
                                fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                                fileName = fileName.substring(fileName.lastIndexOf('/') + 1)
                                        .substring(fileName.lastIndexOf('\\') + 1);
                                fileName = fileName.replaceAll("[\\p{Cntrl}\\\\/:*?\"<>]", "_");
                            }
                        }
                    }
                    if (!fileName.isEmpty()) {
                        String newFileName = UUID.randomUUID() + "_" + fileName;
                        try {
                            String fullPath = fileService.saveFile(newFileName, part.getInputStream());
                            logger.info("Image saved at: " + fullPath);
                            commentImage = newFileName;
                        } catch (IOException e) {
                            logger.severe("Failed to save comment image: " + e.getMessage());
                        }
                    }
                } else if (fieldName.equals("replyCommentId")) {
                    try (Scanner scanner = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)) {
                        if (scanner.hasNext()) {
                            String replyIdRaw = scanner.useDelimiter("\\A").next().trim();
                            if (!replyIdRaw.isEmpty()) {
                                replyCommentId = Integer.parseInt(replyIdRaw);
                            }
                        }
                    } catch (NumberFormatException e) {
                        logger.warning("Invalid replyCommentId: " + e.getMessage());
                    }
                } else if (fieldName.equals("postId")) {
                    try (Scanner scanner = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)) {
                        if (scanner.hasNext()) {
                            String postIdRaw = scanner.useDelimiter("\\A").next().trim();
                            postId = Integer.parseInt(postIdRaw);
                        } else {
                            logger.warning("No postId found");
                        }
                    } catch (NumberFormatException e) {
                        logger.severe("Invalid postId: " + e.getMessage());
                        return null;
                    }
                }
            } catch (IOException e) {
                logger.severe("Error reading part " + fieldName + ": " + e.getMessage());
                return null;
            }
        }

        // Validate required fields
        if ((commentContent == null || commentContent.trim().isEmpty()) && commentImage == null) {
            logger.severe("Comment content is required");
            return null;
        }
        if (postId == -1) {
            logger.severe("Post ID is required");
            return null;
        }

        return new ReqCommentDTO(userID, postId, commentContent, commentImage, replyCommentId, now, now);
    }

    /*
     * Convert a comment DTO to JSON string
     * @param comment - The comment data transfer object
     * @return String - JSON representation of the comment
     */
    private String toJson(RespCommentDTO comment) {
        if (comment == null) return "{}";
        logger.info("Serializing commentImage: " + comment.getCommentImage());
        return "{"
                + "\"commentId\":" + comment.getCommentId() + ","
                + "\"username\":\"" + comment.getUsername() + "\","
                + "\"commentContent\":\"" + comment.getCommentContent().replace("\"", "\\\"") + "\","
                + "\"commentImage\":\"" + (comment.getCommentImage() != null ? comment.getCommentImage() : "") + "\","
                + "\"likeCount\":" + comment.getLikeCount() + ","
                + "\"postId\":" + comment.getPostId() + ","
                + "\"replyCommentId\":" + (comment.getReplyCommentId() != null ? comment.getReplyCommentId() : "null")
                + "}";
    }
}