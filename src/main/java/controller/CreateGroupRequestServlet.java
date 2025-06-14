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
        request.setAttribute("createGroupRequests", dao.getSendedCreateGroupRequests());
        request.getRequestDispatcher("WEB-INF/views/viewCreateGroupRequests.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CreateGroupRequestDAO dao = new CreateGroupRequestDAO();
        String action = request.getParameter("action");
        int id = Integer.parseInt(request.getParameter("id"));
        if(action.equals("reject")) {
            try {
                if (dao.rejectCreateGroupRequest(id)){
                    request.setAttribute("msg", "Request rejected");
                } else {
                    request.setAttribute("msg", "Request rejected fail");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("/error");
            }

        }else if(action.equals("approve")) {
            try {
                int senderId = dao.getSenderId(id);
                    if(dao.approveCreateGroupRequest(id, senderId)) {
                        request.setAttribute("msg", "Request approved");
                    } else {
                        request.setAttribute("msg", "Request approved fail");
                    }

            }catch (Exception e){
                e.printStackTrace();
                response.sendRedirect("/error");
            }
        }
        doGet(request, response);
    }
}