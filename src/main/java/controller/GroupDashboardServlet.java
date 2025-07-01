package controller;

import dto.ResGroupDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.MultipartConfig;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

import dao.GroupDAO;

import model.Account;

@WebServlet(
        name = "GroupDashboardServlet",
        value = "/groupDashboard"
)
@MultipartConfig(maxFileSize = 1024 * 1024 * 5)
public class GroupDashboardServlet extends HttpServlet {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        List<ResGroupDTO> groups = dao.getActiveGroups();
        request.setAttribute("groupList", groups);
        request.getRequestDispatcher("/WEB-INF/views/group_dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        String action = request.getParameter("action");
        if(action.equalsIgnoreCase("Disband")) {
            try {
                int id = Integer.parseInt(request.getParameter("groupId"));
                if (dao.disbandGroup(id)) {
                    request.setAttribute("msg", "Disbanded group successfully.");
                    LOGGER.log(Level.INFO, "Disbanded group successfully.");
                } else {
                    request.setAttribute("msg", "Failed to disband group.");
                    LOGGER.log(Level.INFO, "Failed to disband group.");
                }
            } catch (Exception e) {
                LOGGER.warning("Failed to disband group: " + e.getMessage());
                response.sendRedirect("/error");
                return;
            }
        }
        doGet(request, response);
    }
}