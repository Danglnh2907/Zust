package controller;

import dao.AccountDAO;
import dao.GroupDAO;
import dto.ResGroupDTO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Account;

import java.io.IOException;
import java.util.List;

@WebServlet(
        name = "ManageServlet",
        value = "/manage"
)
public class ManageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO groupDAO = new GroupDAO();
        try {
            int groupId = Integer.parseInt(request.getParameter("id"));
            List<Account> members = groupDAO.getGroupMembers(groupId);
            ResGroupDTO group = groupDAO.getGroup(groupId);
            request.setAttribute("group", group);
            request.setAttribute("members", members);
            request.getRequestDispatcher("WEB-INF/views/viewGroupManagers.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect("/error");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        String action = request.getParameter("action");
        int id = Integer.parseInt(request.getParameter("groupId"));
        if(action.equals("add")) {
            try {
                String[] rawManagerIds = request.getParameterValues("newManagerIds") == null ? new String[0] : request.getParameterValues("newManagerIds");
                int[]  managerIds = new int[rawManagerIds.length];
                for(int i = 0; i < rawManagerIds.length; i++) {
                    managerIds[i] = Integer.parseInt(rawManagerIds[i]);
                }
                if(dao.assignManager(id, managerIds)){
                    request.setAttribute("msg", "Assign manager group successfully.");
                } else {
                    request.setAttribute("msg", "Failed to Assign manager group.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("/error");
            }
        } else if(action.equals("delete")) {
            int managerId = Integer.parseInt(request.getParameter("managerId"));
            if(dao.deleteManager(id, managerId)){
                request.setAttribute("msg", "Delete manager group successfully.");
            } else {
                request.setAttribute("msg", "Failed to Delete manager group.");
            }
        }
        response.sendRedirect("manage?id=" + id);
    }
}