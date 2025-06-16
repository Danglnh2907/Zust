package controller;

import java.io.*;
import java.nio.file.Files;
import java.util.logging.Logger;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import util.service.FileService;

@WebServlet(name = "CommentImageServlet", value = "/comment-image/*")
public class CommentImageController extends HttpServlet {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final FileService fileService = new FileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String imageName = request.getPathInfo();
        logger.info("Received imageName: " + imageName); // Debug log
        if (imageName == null || imageName.contains("..") || !imageName.startsWith("/images/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Extract the filename by removing the leading / and the /images/ prefix
        String relativePath = imageName.substring(1); // e.g., "images/d87de1be-32d6-4844-9d92-4f2119892610_Cute-Cat.jpg"
        String fileName = relativePath.substring(relativePath.indexOf('/') + 1); // e.g., "d87de1be-32d6-4844-9d92-4f2119892610_Cute-Cat.jpg"
        logger.info("Processed filename: " + fileName); // Debug log
        String fullPath = fileService.getImagePath(fileName);
        logger.info("Full path: " + fullPath); // Debug log
        File file = new File(fullPath);

        if (!file.exists() || !file.isFile()) {
            logger.info("Image not found: " + fullPath);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null || !mimeType.startsWith("image/")) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }
        response.setContentType(mimeType);
        response.setContentLengthLong(file.length());

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