package controller;

import dao.AccountDAO;
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
import dto.ReqGroupDTO;
import java.io.InputStream;

import model.Account;
import util.service.FileService;

@WebServlet(
        name = "GroupControllerServlet",
        value = "/group"
)
@MultipartConfig(maxFileSize = 1024 * 1024 * 5)
public class GroupControllerServlet extends HttpServlet {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        String action = request.getParameter("action");
        action = action == null ? "" : action.toLowerCase().trim();
        switch (action) {
            case "view":
                try{
                    int id = Integer.parseInt(request.getParameter("id"));
                    ResGroupDTO group = dao.getActiveGroup(id);
                    request.setAttribute("group", group);
                    List<Account> groupMember = dao.getGroupMembers(id);
                    request.setAttribute("groupMember", groupMember);
                    request.getRequestDispatcher("/WEB-INF/viewGroup.jsp").forward(request, response);
                } catch (Exception e) {
                    response.sendRedirect("/error");
                }
                break;
            case "create":
                try {
                    AccountDAO accountDAO = new AccountDAO();
                    request.setAttribute("listAccount", accountDAO.getActiveAccounts());
                    request.getRequestDispatcher("/WEB-INF/createGroup.jsp").forward(request, response);
                } catch (Exception e){
                    e.printStackTrace();
                    response.sendRedirect("/error");
                }

                break;
            default:
                List<ResGroupDTO> groups = dao.getActiveGroups();
                request.setAttribute("groupList", groups);
                request.getRequestDispatcher("/WEB-INF/viewGroups.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        String action = request.getParameter("action");
        action = action == null ? "" : action.toLowerCase().trim();
        switch (action) {
            case "create":
                try {
                    FileService fileService = new FileService(getServletContext());
                    Part filePart = request.getPart("coverImage");
                    String fileName = filePart.getSubmittedFileName();
                    if (!isImageFile(fileName)) {
                        request.setAttribute("msg", "Only image files are allowed.");
                        doGet(request, response);
                        return;
                    }
                    try (InputStream fileContent = filePart.getInputStream()) {
                        String savedPath = fileService.saveFile(fileName, fileContent);
                        String groupName = request.getParameter("groupName");
                        String groupDescription = request.getParameter("groupDescription");
                        ReqGroupDTO group = new ReqGroupDTO(groupName, groupDescription, savedPath);
                        String[] managerIds = request.getParameterValues("managerIds") == null ? new String[0] : request.getParameterValues("managerIds");
                        for(String managerId : managerIds) {
                            group.addManager(Integer.parseInt(managerId));
                        }
                        if(dao.createGroup(group)){
                            request.setAttribute("msg", "Created group successfully.");
                        } else {
                            request.setAttribute("msg", "Failed to create group.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.sendRedirect("/error");
                    return;
                }
                break;
            case "disband":
                try {
                    int id = Integer.parseInt(request.getParameter("groupId"));
                    if(dao.disbandGroup(id)){
                        request.setAttribute("msg", "Disbanded group successfully.");
                    } else {
                        request.setAttribute("msg", "Failed to remove group.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.sendRedirect("/error");
                    return;
                }
                break;
            default:
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