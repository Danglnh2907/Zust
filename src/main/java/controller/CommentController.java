package controller;

import dao.CommentDAO;
import model.ReportCommentDTO;
import model.ReqCommentDTO;
import model.RespCommentDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import util.service.FileService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/comment"})
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) //5MB
public class CommentController extends HttpServlet {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private PrintWriter out;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * /comment?postID=post_id: Get all comments by postID
         * /comment?action=report: show report comment form
         */

        out = response.getWriter();

        //Fetch userID in sessions
        HttpSession session = request.getSession();
        Account account;
        try {
            account = (Account) session.getAttribute("users");
            if (account == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"User not authenticated\"}");
                return;
            }
        } catch (Exception e) {
            logger.warning("Error getting user from session: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Authentication error\"}");
            return;
        }

        //If no action is found, assume this as view comments
        //Get request parameter
        String postIDRaw = request.getParameter("postID");
        int postID;
        try {
            postID = Integer.parseInt(postIDRaw);
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse postID from request parameter: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.println("{ \"error\": \"" + e.getMessage() + "\" }");
            return;
        }

        //Prepare the comment form and template
        String template = """
                <div class="comment-section" id="comment-section">
                    <h2>Comments (<span id="comment-count">%d</span>)</h2>
                    %s
                    <div class="comment-list" id="comment-list">
                        %s
                    </div>
                </div>""";
        String form = new String(this.getClass().getClassLoader()
                .getResourceAsStream("templates/comment_form.html").readAllBytes());

        //Fetch comments from database
        CommentDAO commentDAO = new CommentDAO();
        ArrayList<RespCommentDTO> comments = commentDAO.getAllComments(postID, account.getId());
        if (comments.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            out.println(String.format(template, comments.size(), String.format(form, account.getAvatar()), ""));
        } else {
            //We return the HTML directly
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");

            //Get the comment items
            StringBuilder commentItems = new StringBuilder();
            LinkedHashMap<RespCommentDTO, ArrayList<RespCommentDTO>> hierarchyComments = commentDAO.filterComment(comments);
            for (Map.Entry<RespCommentDTO, ArrayList<RespCommentDTO>> topLevelComment : hierarchyComments.entrySet()) {
                commentItems.append(topLevelComment.getKey()).append("\n");
                for (RespCommentDTO reply : topLevelComment.getValue()) {
                    commentItems.append(reply).append("\n");
                }
            }

            //Print the HTML to the output stream
            String output = String.format(template,
                    comments.size(), //Get the total comment
                    String.format(form, account.getAvatar()), //Get the comment-posting form (with custom user avatar)
                    commentItems);
            //logger.info(output); 

            out.print(output);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * /comment?action=create
         * /comment?action=edit&commentID=comment_id
         * /comment?action=delete&commentID=comment_id
         * /comment?action=like&commentID=comment_id
         * /comment?action=unlike&commentID=comment_id
         * /comment?action=report&commentID=comment_id
         */

        //Get session
        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("users");
        if (session.getAttribute("users") == null) {
            response.sendRedirect("/auth");
            return;
        }

        //Get request parameter
        String action = request.getParameter("action");
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.println("{ \"error\": \"" + "Bad request" + "\" }");
            return;
        }
        action = action.toLowerCase().trim();

        CommentDAO commentDAO = new CommentDAO();
        switch (action) {
            case "create" -> {
                try {
                    //Extract data
                    ReqCommentDTO commentDTO = extractData(account.getId(), request.getParts());
                    boolean success = commentDAO.createComment(commentDTO);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } catch (NumberFormatException e) {
                    logger.severe("Failed to parse postID from request parameter: " + e.getMessage());
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            case "edit" -> {
                try {
                    //Extract commentID
                    int commentID = Integer.parseInt(request.getParameter("commentID"));

                    //Extract data
                    ReqCommentDTO commentDTO = extractData(account.getId(), request.getParts());
                    boolean success = commentDAO.updateComment(commentID, commentDTO);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } catch (NumberFormatException e) {
                    logger.severe("Failed to parse postID from request parameter: " + e.getMessage());
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            case "delete" -> {
                //Extract commentID
                try {
                    int commentID = Integer.parseInt(request.getParameter("commentID"));
                    boolean success = commentDAO.deleteComment(commentID, account.getId());
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } catch (NumberFormatException e) {
                    logger.severe("Failed to parse postID from request parameter: " + e.getMessage());
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            case "like" -> {
                try {
                    //Extract commentID
                    int commentID = Integer.parseInt(request.getParameter("commentID"));
                    boolean success = commentDAO.likeComment(account.getId(), commentID);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } catch (NumberFormatException e) {
                    logger.severe("Failed to parse postID from request parameter: " + e.getMessage());
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            case "unlike" -> {
                try {
                    int commentID = Integer.parseInt(request.getParameter("commentID"));
                    boolean success = commentDAO.unlikeComment(account.getId(), commentID);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } catch (NumberFormatException e) {
                    logger.severe("Failed to parse postID from request parameter: " + e.getMessage());
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            case "report" -> {
                try {
                    int commentID = Integer.parseInt(request.getParameter("commentID"));
                    String content = request.getParameter("content");

                    ReportCommentDTO dto = new ReportCommentDTO();
                    dto.setCommentID(commentID);
                    dto.setContent(content);
                    dto.setCreatedAt(LocalDateTime.now());
                    dto.setStatus("sent");
                    dto.setAccountID(account.getId());
                    boolean success = commentDAO.report(dto);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } catch (NumberFormatException e) {
                    logger.severe("Failed to parse postID from request parameter: " + e.getMessage());
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
    }

    private ReqCommentDTO extractData(int accountID, Collection<Part> parts) {
        //Variable declarations
        String content = "";
        String image = "";
        int postID = -1;
        int replyID = -1;

        try {
            for (Part part : parts) {
                String fieldName = part.getName();
                if (fieldName == null) continue;

                //Get comment content
                switch (fieldName) {
                    case "content" -> content = readPartAsString(part);
                    case "image" -> image = handleImagePart(part);
                    case "replyID" -> replyID = Integer.parseInt(readPartAsString(part));
                    case "postID" -> postID = Integer.parseInt(readPartAsString(part));
                }
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse content: " + e.getMessage());
        }

        ReqCommentDTO commentDTO = new ReqCommentDTO();
        commentDTO.setAccountID(accountID);
        commentDTO.setPostID(postID);
        commentDTO.setCreatedAt(LocalDateTime.now());
        commentDTO.setUpdatedAt(LocalDateTime.now());
        commentDTO.setContent(content);
        commentDTO.setImage(image);
        commentDTO.setReplyID(replyID);
        return commentDTO;
    }

    private String readPartAsString(Part part) {
        try (Scanner scanner = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            logger.severe("Failed to read part as string: " + e.getMessage());
            return null;
        }
    }

    private String handleImagePart(Part part) {
        try {
            String fileName = "";
            String contentDisposition = part.getHeader("content-disposition");
            String uploadDir = (new FileService()).getLocationPath();

            // Extract filename from content-disposition header
            if (contentDisposition != null) {
                for (String cd : contentDisposition.split(";")) {
                    if (cd.trim().startsWith("filename")) {
                        fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                        // Extract just the filename without path
                        fileName = fileName.substring(Math.max(fileName.lastIndexOf('/'),
                                fileName.lastIndexOf('\\')) + 1);
                        break;
                    }
                }
            }

            // Handle existing image reference (for edit when no new image is uploaded)
            if (fileName.isEmpty()) {
                String existingImage = readPartAsString(part);
                if (existingImage != null && !existingImage.trim().isEmpty()) {
                    return existingImage.trim();
                }
                return null; // No image provided
            }

            // Handle new image upload
            if (part.getSize() > 0) {
                String fileExtension = "";
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                    fileExtension = fileName.substring(dotIndex);
                }

                String newFileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + fileExtension;
                String filePath = uploadDir + File.separator + "images" + File.separator + newFileName;

                part.write(filePath);
                logger.info("Successfully saved image: " + newFileName);
                logger.info("Get image at: " + filePath);
                return newFileName;
            }

            return null; // No valid image content
        } catch (Exception e) {
            logger.severe("Failed to handle image part: " + e.getMessage());
            return null;
        }
    }
}
