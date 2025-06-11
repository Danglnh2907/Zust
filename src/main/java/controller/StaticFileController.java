package controller;

import java.io.*;
import java.nio.file.Files;
import java.util.logging.Logger;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.ServletException;

@WebServlet(name = "StaticFileServlet", value = "/static/*")
public class StaticFileController extends HttpServlet {
    //Logger for debug
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    //dotenv for loading configurations in .env files
    Dotenv dotenv = Dotenv.configure()
            .filename("save.env")
            .load();
    //File storage directory (load from save.env)
    private final String FILE_STORAGE_DIR = dotenv.get("FILE_STORAGE_PATH") == null ? "C:\\" : dotenv.get("FILE_STORAGE_PATH");

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //Get the path after the /static
        String fileName = request.getPathInfo();
        if (fileName == null || fileName.contains("..")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        File file = new File(FILE_STORAGE_DIR + fileName);
        if (!file.exists()) {
            logger.info("File not found: " + FILE_STORAGE_DIR + fileName);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //Set response metadata
        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        response.setContentType(mimeType);
        response.setContentLengthLong(file.length());

        //Read from file and write to response
        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}