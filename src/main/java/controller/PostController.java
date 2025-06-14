package controller;

import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

import dao.PostDAO;
import dto.ReqPostDTO;
import dto.RespPostDTO;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
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
        String userIDRaw = (String) session.getAttribute("userID"); //May change the attribute name?
        int userID;
        try {
            userID = Integer.parseInt(userIDRaw);
        } catch (NumberFormatException e) {
            //logger.warning("Invalid userID: " + userIDRaw);
            //Redirect to /error
            //request.getRequestDispatcher("/error").forward(request, response);
            //return;
            userID = 1; //Remove later when incorporate with authentication
        }

        //Get action and id in request parameter
        String action = request.getParameter("action");
        String idRaw = request.getParameter("id");
        PostDAO postDAO = new PostDAO();

        //No provided id or action -> View all posts belong to a user
        if (action == null && idRaw == null) {
            ArrayList<RespPostDTO> posts = postDAO.getPosts(userID);
            if (posts.isEmpty()) {
                request.setAttribute("message", "No posts found");
            } else {
                //logger.info("Found " + posts.size() + " posts");
                request.setAttribute("posts", posts);
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
                RespPostDTO post = postDAO.getPost(id);
                if (post == null) {
                    request.setAttribute("message", "No post found");
                } else {
                    request.setAttribute("post", post);
                }
            } catch (NumberFormatException e) {
                logger.warning("Invalid postID: " + idRaw);
            }
            request.getRequestDispatcher("/WEB-INF/views/post.jsp").forward(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        /*
         * Handle all post related POST action:
         * /post?action=create: Create new post (UserID fetch in sessions)
         * /post?action=edit&id=post_id: Edit a post by ID (UserID fetch in sessions)
         * /post?action=delete&id=post_id: Delete a post by ID (UserID fetch in session)
         */

        //Fetch userID in sessions
        HttpSession session = request.getSession();
        String userIDRaw = (String) session.getAttribute("userID"); //May change the attribute name?
        int userID;
        try {
            userID = Integer.parseInt(userIDRaw);
        } catch (NumberFormatException e) {
            //logger.warning("Invalid userID: " + userIDRaw);
            //Redirect to /error
            //request.getRequestDispatcher("/error").forward(request, response);
            //return;
            userID = 1; //Remove later when incorporate with authentication
        }

        //Get request parameter: action
        String action = request.getParameter("action");
        action = action == null ? "" : action.toLowerCase().trim();
        PostDAO postDAO = new PostDAO();

        //Work base on action
        //All action here work in a single page (use Javascript to call instead of form submit),
        //we return a status code instead of forward to a new page
        switch (action) {
            case "create" -> {
                ReqPostDTO dto = extractData(userID, request.getParts());
                if (dto == null) {
                    //response.sendRedirect("/error");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    boolean success = postDAO.createPost(dto);
                    if (success) {
                        //logger.info("Post created successfully");
                        response.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        //logger.warning("Error creating post");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    //request.getRequestDispatcher("/WEB-INF/views/post/posts.jsp").forward(request, response);
                }
            }
            case "edit" -> {
                //Extract postID
                String idRaw = request.getParameter("id");
                if (idRaw == null || idRaw.toLowerCase().trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    //logger.warning("Invalid postID: " + idRaw);
                    //response.sendRedirect("/error");
                    return;
                }
                try {
                    int id = Integer.parseInt(idRaw);
                    ReqPostDTO dto = extractData(userID, request.getParts());
                    if (dto == null) {
                        //response.sendRedirect("/error");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    } else {
                        boolean success = postDAO.editPost(id, dto);
                        if (success) {
                            //logger.info("Successfully edited post");
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            //logger.info("Failed to edit post");
                            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                        }
                        //request.getRequestDispatcher("/WEB-INF/views/post/posts.jsp").forward(request, response);
                    }
                } catch (NumberFormatException e) {
                    //logger.warning("Invalid postID: " + idRaw);
                    //response.sendRedirect("/error");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            case "delete" -> {
                //Extract postID
                String idRaw = request.getParameter("id");
                if (idRaw == null || idRaw.toLowerCase().trim().isEmpty()) {
                    //response.sendRedirect("/error");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                try {
                    int id = Integer.parseInt(idRaw);
                    boolean success = postDAO.deletePost(id, userID);
                    if (success) {
                        //logger.info("Deleted post with ID: " + id);
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        //logger.warning("Failed to delete post: " + id);
                        response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                    }
                    //request.getRequestDispatcher("/WEB-INF/views/post/posts.jsp").forward(request, response);
                } catch (NumberFormatException e) {
                    //logger.warning("Invalid postID: " + idRaw);
                    //response.sendRedirect("/error");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
    }

    private ReqPostDTO extractData(int userID, Collection<Part> parts) {
        //Variables declaration
        String htmlContent = null;
        String postPrivacy = null;
        String hashtags = null;
        int groupID = -1;
        String postStatus = "public";
        ArrayList<String> imagePaths = new ArrayList<>();
        ReqPostDTO dto = new ReqPostDTO();

        //Create storage directory if not exist
        File uploadDir = new File(fileService.getLocationPath() + File.separator + "images");
        if (!uploadDir.exists()) {
            //Create directory and check if it success
            if (!uploadDir.mkdir()) {
                logger.severe("Failed to create upload directory: " + fileService.getLocationPath() + File.separator + "images\n");
                return null;
            }
        }

        //Extract data
        for (Part part : parts) {
            String fieldName = part.getName();

            //Get HTML content
            if (fieldName.equals("htmlContent")) {
                try {
                    htmlContent = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)
                            .useDelimiter("\\A").next();
                } catch (Exception e) {
                    logger.severe(String.format("Failed to read HTML from part\nError: %s", e.getMessage()));
                    return null; //All post should have content, whether text or images, so we stop if failed to read content
                }
            }
            //Get list of hashtags
            else if (fieldName.equals("hashtags")) {
                try {
                    hashtags = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)
                            .useDelimiter("\\A").next();
                } catch (Exception e) {
                    logger.severe(String.format("Failed to read hashtags from part\nError: %s", e.getMessage()));
                }
            }
            //Get post_privacy
            else if (fieldName.equals("post_privacy")) {
                try {
                    postPrivacy = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)
                            .useDelimiter("\\A").next();
                } catch (Exception e) {
                    logger.severe(String.format("Failed to read post privacy from part\nError: %s", e.getMessage()));
                }
            }
            //Handle images - both new uploads and existing ones
            else if (fieldName.startsWith("image")) {
                String fileName = "";
                String contentDisposition = part.getHeader("content-disposition");

                //Read images' filenames
                if (contentDisposition != null) {
                    for (String cd : contentDisposition.split(";")) {
                        if (cd.trim().startsWith("filename")) {
                            fileName = cd.substring(cd.indexOf('=') + 1)
                                    .trim().replace("\"", "");
                            fileName = fileName
                                    .substring(fileName.lastIndexOf('/') + 1)
                                    .substring(fileName.lastIndexOf('\\') + 1);
                        }
                    }
                }

                // If no filename, check if it's an existing image reference
                if (fileName.isEmpty()) {
                    try {
                        String existingImage = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)
                                .useDelimiter("\\A").next();
                        if (!existingImage.isEmpty()) {
                            imagePaths.add(existingImage); // Add existing image filename
                        }
                    } catch (IOException e) {
                        logger.warning("Failed to read existing image reference: " + e.getMessage());
                    }
                } else {
                    // Handle new image upload
                    String newFileName = UUID.randomUUID() + "_" + fileName;
                    String filePath = fileService.getLocationPath() + File.separator + "images" + File.separator + newFileName;

                    try {
                        part.write(filePath);
                        imagePaths.add(newFileName);
                    } catch (Exception e) {
                        logger.severe("Failed to write image: " + e.getMessage());
                    }
                }
            }
            //Get group_id (if this post is uploaded in a group)
            else if (fieldName.equals("group_id")) {
                try {
                    String groupIDRaw = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)
                            .useDelimiter("\\A").next();
                    groupID = Integer.parseInt(groupIDRaw);
                } catch (Exception e) {
                    logger.severe(String.format("Failed to read group_id from part\nError: %s", e.getMessage()));
                    //return null;
                }
            }
            //Get posts_status
            else if (fieldName.equals("is_drafted")) {
                String isDraftedRaw;
                try {
                    isDraftedRaw = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)
                            .useDelimiter("\\A").next();
                    boolean isDrafted = Boolean.parseBoolean(isDraftedRaw);
                    if (isDrafted) {
                        postStatus = "drafted";
                    }
                } catch (Exception e) {
                    logger.warning(e.getMessage());
                }
            }
        }

        //Format the HTML (remove all img tags, the frontend will gather them in a different section)
        htmlContent = formatContent(htmlContent);

        // Create DTO objects
        dto.setAccountID(userID);
        dto.setPostContent(htmlContent);
        dto.setPrivacy(postPrivacy);
        dto.setStatus(postStatus);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setLastModified(LocalDateTime.now());
        dto.setGroupID(groupID);
        //Add images path
        for (String imagePath : imagePaths) {
            dto.getImages().add(imagePath);
        }
        //Add hashtags
        if (hashtags != null && !hashtags.isEmpty()) {
            //Split the format String into array (format: #123#hello#first_time -> [123, hello, first_time])
            String[] hashtagArray = hashtags.split("#");
            for (String hashtag : hashtagArray) {
                if (!hashtag.trim().isEmpty()) {
                    dto.getHashtags().add(hashtag);
                }
            }
        }
        return dto;
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