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
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GroupDAO dao = new GroupDAO();
        List<ResGroupDTO> groups = dao.getInactiveGroups();
        request.setAttribute("groups", groups);
        request.getRequestDispatcher("/WEB-INF/views/group_request.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}