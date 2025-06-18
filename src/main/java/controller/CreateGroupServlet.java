package controller;

import dao.AccountDAO;
import dao.GroupDAO;
import dto.ReqGroupDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import model.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.service.FileService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.UUID;

@WebServlet(
        name = "CreateGroupServlet",
        value = "/createGroup"
)
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class CreateGroupServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateGroupServlet.class);
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession();
            if(session.getAttribute("users") == null) {
                response.sendRedirect("auth");
                return;
            }
            Account account = (Account) session.getAttribute("users");
            request.setAttribute("accountId", account.getId());
            request.getRequestDispatcher("/WEB-INF/views/createGroup.jsp").forward(request, response);
        }catch (Exception e) {
            LOGGER.error("Create group: Error in get user Id from session: {}", e.getMessage());
            response.sendRedirect("error.jsp");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        GroupDAO groupDAO = new GroupDAO();
        ReqGroupDTO groupDTO = extractData(request.getParts());
        if(groupDTO == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendRedirect("error.jsp");
        } else {
            boolean success = groupDAO.createGroup(groupDTO);
            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                request.setAttribute("msg", "Created group successfully.");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                request.setAttribute("msg", "Failed to create group.");
            }
        }
        doGet(request, response);
    }

    private ReqGroupDTO extractData(Collection<Part> parts) {
        FileService fileService = new FileService(getServletContext());
        File uploadDir = new File(fileService.getLocationPath() + File.separator + "images");

        String groupName = null;
        String groupDescription = null;
        String imagePath = null;
        int managerId = 0;

        if (!uploadDir.exists()) {
            //Create directory and check if it success
            if (!uploadDir.mkdirs()) { // Use mkdirs() to create parent directories too
                LOGGER.error("Failed to create upload directory: " + uploadDir.getAbsolutePath());
                return null;
            }
        }
        try {
            for (Part part : parts) {
                String fieldName = part.getName();
                if (fieldName == null) continue;

                if (fieldName.equals("groupName")) {
                    groupName = readPartAsString(part);
                    if (groupName == null) {
                        LOGGER.warn("Failed to read HTML content");
                        return null;
                    }
                }
                else if (fieldName.equals("groupDescription")) {
                    groupDescription = readPartAsString(part);
                }
                else if (fieldName.startsWith("coverImage")) {
                    imagePath = handleImagePart(part, uploadDir);
                }
                else if (fieldName.equals("managerId")) {
                    String managerIDRaw = readPartAsString(part);
                    if (managerIDRaw != null && !managerIDRaw.trim().isEmpty()) {
                        try {
                            managerId = Integer.parseInt(managerIDRaw.trim());
                        } catch (NumberFormatException e) {
                            LOGGER.warn("Invalid group ID format: " + managerIDRaw);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error extracting data from parts: " + e.getMessage());
            return null;
        }

        ReqGroupDTO groupDTO = new ReqGroupDTO(groupName, groupDescription, imagePath, managerId);
        return groupDTO;
    }

    private String readPartAsString(Part part) {
        try (Scanner scanner = new Scanner(part.getInputStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            LOGGER.warn("Failed to read part as string: " + e.getMessage());
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
                LOGGER.info("Successfully saved image: " + newFileName);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to handle image part: " + e.getMessage());
        }
        return imagePath;
    }

    private boolean isImageFile(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(".jpg") ||
                lowerCaseFileName.endsWith(".jpeg") ||
                lowerCaseFileName.endsWith(".png") ||
                lowerCaseFileName.endsWith(".gif");
    }
}