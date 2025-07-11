package controller;

import dao.AccountDAO;
import model.Account;
import model.FriendRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "FriendRequestServlet", urlPatterns = {"/friend_request"})
public class FriendRequestServlet extends HttpServlet {

    private AccountDAO accountDAO;

    @Override
    public void init() throws ServletException {
        accountDAO = new AccountDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Account currentUser = (Account) request.getSession().getAttribute("users");
        if (currentUser == null) {
            response.sendRedirect("login.jsp"); // Redirect to login if not logged in
            return;
        }

        List<FriendRequest> friendRequests = accountDAO.getFriendRequests(currentUser.getId());
        request.setAttribute("friendRequests", friendRequests);
        request.getRequestDispatcher("/WEB-INF/views/friend_requests.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String action = request.getParameter("action");
            Account currentUser = (Account) request.getSession().getAttribute("users");

            if (currentUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\":\"error\",\"message\":\"You must be logged in to perform this action.\"}");
                return;
            }

            if ("send".equals(action)) {
                int userId = Integer.parseInt(request.getParameter("userId"));
                String content = request.getParameter("content");

                if (currentUser.getId() == userId) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"You cannot send a friend request to yourself.\"}");
                    return;
                }

                if (accountDAO.isFriendRequestPending(currentUser.getId(), userId)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"Friend request already sent.\"}");
                    return;
                }

                if (accountDAO.areFriends(currentUser.getId(), userId)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"You are already friends.\"}");
                    return;
                }
                accountDAO.createFriendRequest(currentUser.getId(), userId, content);
                response.getWriter().write("{\"status\":\"success\",\"message\":\"Friend request sent successfully.\"}");

            } else if ("accept".equals(action)) {
                int requestId = Integer.parseInt(request.getParameter("requestId"));
                int senderId = Integer.parseInt(request.getParameter("senderId"));
                accountDAO.updateFriendRequestStatus(requestId, "accepted");
                accountDAO.addFriend(currentUser.getId(), senderId);
                response.getWriter().write("{\"status\":\"success\",\"message\":\"Friend request accepted.\"}");

            } else if ("reject".equals(action)) {
                int requestId = Integer.parseInt(request.getParameter("requestId"));
                accountDAO.updateFriendRequestStatus(requestId, "rejected");
                response.getWriter().write("{\"status\":\"success\",\"message\":\"Friend request rejected.\"}");

            } else if ("unfriend".equals(action)) {
                int userId = Integer.parseInt(request.getParameter("userId"));
                boolean success = accountDAO.unfriend(currentUser.getId(), userId);
                if (success) {
                    response.getWriter().write("{\"status\":\"success\",\"message\":\"Friend removed successfully.\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"Failed to remove friend. You may not be friends.\"}");
                }
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid user ID.\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"An unexpected error occurred.\"}");
            e.printStackTrace();
        }
    }
}