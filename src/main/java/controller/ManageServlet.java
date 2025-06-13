package controller;

import dao.GroupDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(
        name = "ManageServlet",
        value = "/manage"
)
public class ManageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("group");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        try {
            int id = Integer.parseInt(request.getParameter("groupId"));
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
        doGet(request, response);
    }
}