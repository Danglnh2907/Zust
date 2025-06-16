package controller;

import java.io.*;
import java.util.logging.Logger;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import util.service.FileService;

/*
 * Handle HTTP GET requests for retrieving comment images:
 * - GET /comment-image/*: Serve an image file based on the path
 */
@WebServlet(name = "CommentImageServlet", value = "/comment-image/*")
public class CommentImageController extends HttpServlet {
    // Logger for debugging
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    // FileService for accessing image files
    private final FileService fileService = new FileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Extract image name from request path
        String imageName = request.getPathInfo();
        logger.info("Received imageName: " + imageName);
        if (imageName == null || imageName.contains("..") || !imageName.startsWith("/images/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Extract filename from path
        String relativePath = imageName.substring(1);
        String fileName = relativePath.substring(relativePath.indexOf('/') + 1);
        logger.info("Processed filename: " + fileName);
        String fullPath = fileService.getImagePath(fileName);
        logger.info("Full path: " + fullPath);
        File file = new File(fullPath);

        // Check if file exists and is valid
        if (!file.exists() || !file.isFile()) {
            logger.info("Image not found: " + fullPath);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Validate MIME type
        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null || !mimeType.startsWith("image/")) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }
        response.setContentType(mimeType);
        response.setContentLengthLong(file.length());

        // Stream the image to the response
        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}