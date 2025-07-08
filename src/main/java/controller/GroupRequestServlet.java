package controller;

import dao.GroupDAO;
import dto.ResGroupDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(
        name = "GroupRequestServlet",
        value = "/groupRequest"
)
public class GroupRequestServlet extends HttpServlet {
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        List<ResGroupDTO> groups = dao.getInactiveGroups();
        request.setAttribute("groups", groups);
        request.getRequestDispatcher("/WEB-INF/views/group_request.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        try{
            int groupId = Integer.parseInt(request.getParameter("groupId"));
            String action = request.getParameter("action");
            if (action.equals("Accept")) {
                if (dao.acceptGroup(groupId)) {
                    LOGGER.info("Accepted group sucessfully");
                    request.setAttribute("msg", "Accepted group sucessfully");
                } else {
                    LOGGER.info("Accepted group fail");
                    request.setAttribute("msg", "Accepted group fail");
                }
            } else if (action.equals("Reject")) {
                if (dao.rejectGroup(groupId)) {
                    LOGGER.info("Rejected group sucessfully");
                    request.setAttribute("msg", "Rejected group sucessfully");
                }else {
                    LOGGER.info("Rejected group fail");
                    request.setAttribute("msg", "Rejected group fail");
                }
            }
            doGet(request, response);
        }catch (Exception e){
            LOGGER.warning("Failed to get request parameters: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}