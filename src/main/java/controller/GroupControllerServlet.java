package controller;

import dto.ResGroupDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.MultipartConfig;

import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;
import jakarta.servlet.http.Part;
import dao.GroupDAO;
import model.Group;
import dto.ReqGroupDTO;
import java.io.InputStream;
import util.service.FileService;

@WebServlet(
        name = "GroupControllerServlet",
        value = "/group"
)
@MultipartConfig(maxFileSize = 1024 * 1024 * 10)
public class GroupControllerServlet extends HttpServlet {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "view":
                int id = Integer.parseInt(request.getParameter("id"));
                ResGroupDTO group = dao.getGroup(id);
                request.setAttribute("group", group);
                request.getRequestDispatcher("/WEB-INF/viewGroup.jsp").forward(request, response);
                break;
            case "create":
                request.getRequestDispatcher("/WEB-INF/createGroup.jsp").forward(request, response);
                break;
            case "disband":
                break;
            default:
                List<ResGroupDTO> groups = dao.getGroups();
                request.setAttribute("groups", groups);
                request.getRequestDispatcher("/WEB-INF/viewGroups.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "list":
                break;
            case "create":
                try {
                    FileService fileService = new FileService(getServletContext());

                    String groupName = request.getParameter("name");
                    String groupDescription = request.getParameter("description");
                    // Get the file part from the form
                    Part filePart = request.getPart("image"); // "image" matches the name attribute in the form
                    String fileName = filePart.getSubmittedFileName();

                    // Validate file type (optional, as FileService already checks extensions)
                    if (!isImageFile(fileName)) {
                        request.setAttribute("msg", "Only image files are allowed.");
                        doGet(request, response);
                        return;
                    }

                    // Get InputStream of the uploaded file
                    try (InputStream fileContent = filePart.getInputStream()) {
                        // Save the file using FileService
                        String savedPath = fileService.saveFile(fileName, fileContent);

                        // Save the file path to the database
                        ReqGroupDTO group = new ReqGroupDTO(groupName, groupDescription, savedPath);
                        if(dao.createGroup(group)){
                            request.setAttribute("msg", "Created group successfully.");
                        } else {
                            request.setAttribute("msg", "Failed to create group.");
                        }
                    }
                } catch (Exception e) {
                    // Log error and send error response
                    e.printStackTrace();
                    request.setAttribute("msg", "Error uploading: " + e.getMessage());
                    doGet(request, response);
                    return;
                }
                break;
            case "disband":
                break;
            case "delete":
                break;
        }
        doGet(request, response);
    }

    private boolean isImageFile(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(".jpg") ||
                lowerCaseFileName.endsWith(".jpeg") ||
                lowerCaseFileName.endsWith(".png") ||
                lowerCaseFileName.endsWith(".gif");
    }
}