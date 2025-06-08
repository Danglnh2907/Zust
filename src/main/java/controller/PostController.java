package controller;

import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

import dao.PostDAO;
import dto.CreatePostDTO;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet(name = "PostControllerServlet", value = "/post")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) //5MB
public class PostController extends HttpServlet {
    //Logger for debug
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    //dotenv for loading configurations in .env files
    Dotenv dotenv = Dotenv.configure()
            .filename("save.env")
            .load();
    //File storage directory (load from save.env)
    private final String FILE_STORAGE_DIR = dotenv.get("FILE_STORAGE_PATH") == null ? "C:\\" : dotenv.get("FILE_STORAGE_PATH");

    /**
     * Util method for return error status for operations
     * @param resp - HTTPServletResponse object for setting status code
     * @param statusCode - Status code
     */
    private void handleError(HttpServletResponse resp, int statusCode) throws IOException {
        resp.setStatus(statusCode);
        //Print writer for output return value
        resp.getWriter().println("Error occurred while executing! Please try again.");
    }

    /**
     * Util method for extracting file name from multipart form
     * @param part - Part object of multipart form that need extraction
     * @return - The String file name of the extracted file
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String cd : contentDisposition.split(";")) {
                if (cd.trim().startsWith("filename")) {
                    String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                    return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1);
                }
            }
        }
        return null;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.getRequestDispatcher("/WEB-INF/post.jsp").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //Get request parameter: action
        String action = request.getParameter("action");
        action = action == null ? "" : action.toLowerCase().trim();

        //Work base on action
        if (action.equals("create")) {
            //Variables declaration
            String htmlContent = null;
            String postPrivacy = null;
            String hashtagsJson = null;
            ArrayList<String> imagePaths = new ArrayList<>();

            //Create storage directory if not exist
            File uploadDir = new File(FILE_STORAGE_DIR);
            if (!uploadDir.exists()) {
                //Create directory and check if it success
                if (!uploadDir.mkdir()) {
                    logger.severe("Failed to create upload directory: " + FILE_STORAGE_DIR + "\n");
                    handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
            }

            //Extract data
            for (Part part : request.getParts()) {
                String fieldName = part.getName();
                String fileName = getFileName(part);

                //Get HTML content
                if (fieldName.equals("htmlContent")) {
                    htmlContent = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)
                            .useDelimiter("\\A").next();
                }
                //Get list of hashtags
                else if (fieldName.equals("hashtags")) {
                    //The format should be a JSON array: [#123, #hello]
                    hashtagsJson = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)
                            .useDelimiter("\\A").next();
                }
                //Get post_privacy
                else if (fieldName.equals("post_privacy")) {
                    postPrivacy = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)
                            .useDelimiter("\\A").next();
                }
                //Get list of images
                else if (fieldName.startsWith("image")) {
                    if (fileName != null && !fileName.isEmpty()) {
                        //Include random UUID to prevent duplicate
                        String filePath = FILE_STORAGE_DIR + File.separator + UUID.randomUUID() + "_" + fileName;
                        part.write(filePath);
                        imagePaths.add(filePath);
                    }
                }
            }

            // Create DTO objects
            CreatePostDTO dto = new CreatePostDTO();
            dto.setAccountID(1); //Change later if login is implemented
            dto.setPostContent(htmlContent);
            dto.setPrivacy(postPrivacy);
            dto.setStatus("published"); //Change later
            dto.setCreatedAt(LocalDateTime.now());
            dto.setLastModified(LocalDateTime.now());
            dto.setGroupID(-1); //Personal post -> change later
            //Add images path
            for (String imagePath : imagePaths) {
                dto.getImages().add(imagePath);
            }
            //Add hashtags
            if (hashtagsJson != null && !hashtagsJson.isEmpty()) {
                //Split the JSON into array
                String[] hashtagArray = hashtagsJson.replaceAll("[\\[\\]\"]", "").split(",");
                for (String hashtag : hashtagArray) {
                    if (!hashtag.trim().isEmpty()) {
                        dto.getHashtags().add(hashtag);
                    }
                }
            }

            // Call PostDAO
            PostDAO dao = new PostDAO();
            boolean success;
            try {
                success = dao.createPost(dto);
            } catch (SQLException e) {
                logger.warning("Error executing SQL to update database: " + e.getMessage());
                handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            // Set response status
            response.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}