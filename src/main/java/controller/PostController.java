package controller;

import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

import dao.AccountDAO;
import dao.CommentDAO;
import dao.PostDAO;
import dto.ReqPostDTO;
import dto.RespCommentDTO;
import dto.RespPostDTO;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import model.Account;
import util.service.FileService;

@WebServlet(name = "PostControllerServlet", value = "/post")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) //5MB
public class PostController extends HttpServlet {
    //Logger for debug
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    //FileService
    private final FileService fileService = new FileService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        /*
         * Handle all post related GET action:
         * /post: Get all post created by a users (UserID fetch in sessions)
         * /post?id=post_id: Get view a certain post by ID (include comments)
         * /post?action=create: create post form
         */

        //Fetch userID in sessions
        HttpSession session = request.getSession();
        int userID;
        try {
            Account account = (Account) session.getAttribute("users");
            if (account == null) {
                // Redirect to login page if user is not authenticated
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }
            userID = account.getId();
        } catch (Exception e) {
            logger.warning("Error getting user from session: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        //Get account information from userID
        AccountDAO accountDAO = new AccountDAO();
        Account account = accountDAO.getAccountById(userID);
        if (account == null) {
            logger.warning("Account not found");
            //Redirect to /error
            request.getRequestDispatcher("/error").forward(request, response);
            return;
        }
        request.setAttribute("account", account);

        //Get action and id in request parameter
        String action = request.getParameter("action");
        String idRaw = request.getParameter("id");
        PostDAO postDAO = new PostDAO();

        //No provided id or action -> View all posts belong to a user
        if (action == null && idRaw == null) {
            try {
                ArrayList<RespPostDTO> posts = postDAO.getPosts(userID, userID);
                if (posts.isEmpty()) {
                    request.setAttribute("message", "No posts found");
                } else {
                    request.setAttribute("posts", posts);
                }
            } catch (Exception e) {
                logger.severe("Error fetching posts for user " + userID + ": " + e.getMessage());
                request.setAttribute("message", "Error loading posts");
            }
            request.getRequestDispatcher("/WEB-INF/views/post.jsp").forward(request, response);
            return;
        }

        //If the action is create, serve the create form (post_upload.jsp)
        if (action != null && action.toLowerCase().trim().equals("create")) {
            request.getRequestDispatcher("/WEB-INF/views/post_upload.jsp").forward(request, response);
            return;
        }

        //If id exist, then fetch the only post with that post_id
        if (idRaw != null) {
            try {
                int id = Integer.parseInt(idRaw);
                RespPostDTO post = postDAO.getPost(id, userID);
                CommentDAO commentDAO = new CommentDAO();
                ArrayList<RespCommentDTO> comments = commentDAO.getAllComments(id, account.getId());
                if (post == null) {
                    request.setAttribute("message", "No post/comments found");
                } else {
                    request.setAttribute("post", post);
                    request.setAttribute("total_comments", comments.size());
                    request.setAttribute("comments", commentDAO.filterComment(comments));
                }
            } catch (NumberFormatException e) {
                logger.warning("Invalid postID: " + idRaw);
                request.setAttribute("message", "Invalid post ID");
            } catch (Exception e) {
                logger.severe("Error fetching post " + idRaw + ": " + e.getMessage());
                request.setAttribute("message", "Error loading post/comments");
            }
            request.getRequestDispatcher("/WEB-INF/views/full_post.jsp").forward(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        /*
         * Handle all post related POST action:
         * /post?action=create: Create new post (UserID fetch in sessions)
         * /post?action=edit&id=post_id: Edit a post by ID (UserID fetch in sessions)
         * /post?action=delete&id=post_id: Delete a post by ID (UserID fetch in session)
         * /post?action=like&id=post_id: like post
         * /post?action=unlike&id=post_id: unlike a post
         * /post?action=repost&id=post_id: repost a post
         */

        //Set response content type for JSON responses
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        //Fetch userID in sessions
        HttpSession session = request.getSession();
        int userID;
        try {
            Account account = (Account) session.getAttribute("users");
            if (account == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"User not authenticated\"}");
                return;
            }
            userID = account.getId();
        } catch (Exception e) {
            logger.warning("Error getting user from session: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Authentication error\"}");
            return;
        }

        //Get request parameter: action
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Action parameter is required\"}");
            return;
        }

        action = action.toLowerCase().trim();
        PostDAO postDAO = new PostDAO();

        //Work based on action
        try {
            switch (action) {
                case "create" -> {
                    ReqPostDTO dto = extractData(userID, request.getParts());
                    if (dto == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Failed to process post data\"}");
                    } else {
                        boolean success = postDAO.createPost(dto);
                        if (success) {
                            response.setStatus(HttpServletResponse.SC_CREATED);
                            response.getWriter().write("{\"message\":\"Post created successfully\"}");
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            response.getWriter().write("{\"error\":\"Failed to create post\"}");
                        }
                    }
                }
                case "edit" -> {
                    String idRaw = request.getParameter("id");
                    if (idRaw == null || idRaw.trim().isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Post ID is required for edit\"}");
                        return;
                    }

                    int id = Integer.parseInt(idRaw);
                    ReqPostDTO dto = extractData(userID, request.getParts());
                    if (dto == null) {
                        //response.sendRedirect("/error");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Failed to process post data\"}");
                    } else {
                        boolean success = postDAO.editPost(id, dto);
                        if (success) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{\"message\":\"Post updated successfully\"}");
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            response.getWriter().write("{\"error\":\"Post not found or permission denied\"}");
                        }
                    }
                }
                case "delete" -> {
                    String idRaw = request.getParameter("id");
                    if (idRaw == null || idRaw.trim().isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Post ID is required for delete\"}");
                        return;
                    }

                    int id = Integer.parseInt(idRaw);
                    boolean success = postDAO.deletePost(id, userID);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write("{\"message\":\"Post deleted successfully\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"error\":\"Post not found or permission denied\"}");
                    }
                }
                case "like" -> {
                    String idRaw = request.getParameter("id");
                    if (idRaw == null || idRaw.trim().isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Post ID is required for like\"}");
                        return;
                    }

                    int id = Integer.parseInt(idRaw);
                    boolean success = postDAO.likePost(id, userID);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write("{\"message\":\"Post like successfully\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"error\":\"Post not found or permission denied\"}");
                    }
                }
                case "unlike" -> {
                    String idRaw = request.getParameter("id");
                    if (idRaw == null || idRaw.trim().isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Post ID is required for unlike\"}");
                        return;
                    }
                    int id = Integer.parseInt(idRaw);
                    boolean success = postDAO.unlikePost(id, userID);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write("{\"message\":\"Post unlike successfully\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"error\":\"Post not found or permission denied\"}");
                    }
                }
                case "repost" -> {
                    String idRaw = request.getParameter("id");
                    if (idRaw == null || idRaw.trim().isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Post ID is required for repost\"}");
                        return;
                    }

                    int id = Integer.parseInt(idRaw);
                    boolean success = postDAO.repost(id, userID);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write("{\"message\":\"Post repost successfully\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"error\":\"Post not found or permission denied\"}");
                    }
                }
                default -> {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid action: " + action + "\"}");
                }
            }
        } catch (NumberFormatException e) {
            logger.warning("Invalid post ID format: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid post ID format\"}");
        } catch (Exception e) {
            logger.severe("Unexpected error in POST request: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    private ReqPostDTO extractData(int userID, Collection<Part> parts) {
        //Variables declaration
        String htmlContent = null;
        String postPrivacy = "public"; // Default privacy
        String hashtags = null;
        int groupID = -1;
        String postStatus = "publish";
        ArrayList<String> imagePaths = new ArrayList<>();
        ReqPostDTO dto = new ReqPostDTO();

        //Create storage directory if not exist
        File uploadDir = new File(fileService.getLocationPath() + File.separator + "images");
        if (!uploadDir.exists()) {
            //Create directory and check if it success
            if (!uploadDir.mkdirs()) { // Use mkdirs() to create parent directories too
                logger.severe("Failed to create upload directory: " + uploadDir.getAbsolutePath());
                return null;
            }
        }

        //Extract data
        try {
            for (Part part : parts) {
                String fieldName = part.getName();
                if (fieldName == null) continue;

                //Get HTML content
                if (fieldName.equals("htmlContent")) {
                    htmlContent = readPartAsString(part);
                    if (htmlContent == null) {
                        logger.warning("Failed to read HTML content");
                        return null;
                    }
                }
                //Get list of hashtags
                else if (fieldName.equals("hashtags")) {
                    hashtags = readPartAsString(part);
                }
                //Get post_privacy
                else if (fieldName.equals("post_privacy")) {
                    String privacy = readPartAsString(part);
                    if (privacy != null && !privacy.trim().isEmpty()) {
                        postPrivacy = privacy.trim();
                    }
                }
                //Handle images - both new uploads and existing ones
                else if (fieldName.startsWith("image")) {
                    handleImagePart(part, imagePaths, uploadDir);
                }
                //Get group_id (if this post is uploaded in a group)
                else if (fieldName.equals("group_id")) {
                    String groupIDRaw = readPartAsString(part);
                    if (groupIDRaw != null && !groupIDRaw.trim().isEmpty()) {
                        try {
                            groupID = Integer.parseInt(groupIDRaw.trim());
                        } catch (NumberFormatException e) {
                            logger.warning("Invalid group ID format: " + groupIDRaw);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Error extracting data from parts: " + e.getMessage());
            return null;
        }

        // Validate required fields
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            if (imagePaths.isEmpty()) {
                logger.warning("Post must have either content or images");
                return null;
            }
        }

        //Format the HTML (remove all img tags, the frontend will gather them in a different section)
        htmlContent = formatContent(htmlContent);

        // Create DTO objects
        dto.setAccountID(userID);
        dto.setPostContent(htmlContent);
        dto.setPrivacy(postPrivacy);
        //When create post, status can only be published (personal) or sent (group)
        //Edit can not change this status (status = deleted -> delete operation, not related to ReqPostDTO)
        dto.setStatus(groupID != -1 ? "sent" : postStatus);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setLastModified(LocalDateTime.now());
        dto.setGroupID(groupID);

        //Add images path
        dto.getImages().addAll(imagePaths);

        //Add hashtags
        if (hashtags != null && !hashtags.trim().isEmpty()) {
            //Split the format String into array (format: #123#hello#first_time -> [123, hello, first_time])
            String[] hashtagArray = hashtags.split("#");
            for (String hashtag : hashtagArray) {
                String cleanHashtag = hashtag.trim();
                if (!cleanHashtag.isEmpty()) {
                    dto.getHashtags().add(cleanHashtag);
                }
            }
        }
        return dto;
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

    private void handleImagePart(Part part, ArrayList<String> imagePaths, File uploadDir) {
        try {
            String fileName = "";
            String contentDisposition = part.getHeader("content-disposition");

            //Read images' filenames
            if (contentDisposition != null) {
                for (String cd : contentDisposition.split(";")) {
                    if (cd.trim().startsWith("filename")) {
                        fileName = cd.substring(cd.indexOf('=') + 1)
                                .trim().replace("\"", "");
                        // Extract just the filename without path
                        fileName = fileName.substring(Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\')) + 1);
                        break;
                    }
                }
            }

            // If no filename, check if it's an existing image reference
            if (fileName.isEmpty()) {
                String existingImage = readPartAsString(part);
                if (existingImage != null && !existingImage.trim().isEmpty()) {
                    imagePaths.add(existingImage.trim());
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
                imagePaths.add(newFileName);
                logger.info("Successfully saved image: " + newFileName);
            }
        } catch (Exception e) {
            logger.severe("Failed to handle image part: " + e.getMessage());
        }
    }

    private String formatContent(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return htmlContent;
        }
        // Matches <img> tags with any attributes, e.g., <img src="..." alt="...">
        String imgTagRegex = "<img\\s*[^>]*>";
        return htmlContent.replaceAll(imgTagRegex, "").trim();
    }
}
