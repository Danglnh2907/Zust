package controller;

import dao.CreateGroupRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(
        name = "CreateGroupRequestServlet",
        value = "/createGroupRequest"
)
public class CreateGroupRequestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CreateGroupRequestDAO dao = new CreateGroupRequestDAO();
        request.setAttribute("createGroupRequests", dao.getCreateGroupRequests());
        request.getRequestDispatcher("WEB-INF/viewCreateGroupRequests.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CreateGroupRequestDAO dao = new CreateGroupRequestDAO();
        String action = request.getParameter("action");
        if(action.equals("rejected")) {

        }else if(action.equals("accepted")) {

        }
    }
}