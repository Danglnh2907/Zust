package controller;

import dao.CreateGroupRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(
        name = "SendCreateGroupRequestServlet",
        value = "/sendCreateGroupRequest"
)
public class SendCreateGroupRequestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            request.getRequestDispatcher("WEB-INF/sendCreateGroupRequest.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CreateGroupRequestDAO dao = new CreateGroupRequestDAO();
        int accountId = Integer.parseInt(request.getParameter("accountId"));
        String content = request.getParameter("content");
        if(dao.sendCreateGroupRequest(content, accountId)) {
                request.setAttribute("msg", "Successful group request");
                doGet(request, response);
        } else {
                request.setAttribute("msg", "Error creating group request");
                doGet(request, response);
        }
    }
}