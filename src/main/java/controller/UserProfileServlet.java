package controller;

import dao.AccountDAO;
import dao.PostDAO;
import dto.RespPostDTO;
import model.Account;
import model.FriendRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "UserProfileServlet", urlPatterns = {"/profile"})
public class UserProfileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            AccountDAO accountDAO = new AccountDAO();
            Account userProfile = accountDAO.getAccountById(userId);

            if (userProfile != null) {
                PostDAO postDAO = new PostDAO();
                Account currentUser = (Account) request.getSession().getAttribute("users");
                int requesterId = (currentUser != null) ? currentUser.getId() : 0;
                ArrayList<RespPostDTO> posts = postDAO.getPosts(requesterId, userProfile.getId());
                request.setAttribute("posts", posts);

                List<Account> friends = accountDAO.getFriends(userProfile.getId());
                request.setAttribute("friends", friends);

                if (currentUser != null && currentUser.getId() == userProfile.getId()) {
                    List<FriendRequest> friendRequests = accountDAO.getFriendRequests(currentUser.getId());
                    request.setAttribute("friendRequests", friendRequests);
                } else if (currentUser != null) {
                    boolean areFriends = accountDAO.areFriends(currentUser.getId(), userProfile.getId());
                    boolean friendRequestPending = accountDAO.isFriendRequestPending(currentUser.getId(), userProfile.getId());
                    request.setAttribute("areFriends", areFriends);
                    request.setAttribute("friendRequestPending", friendRequestPending);
                }

                request.getSession().setAttribute("userProfile", userProfile);
                request.getRequestDispatcher("/WEB-INF/views/user_profile.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AccountDAO accountDAO = new AccountDAO();
        Account account = (Account) request.getSession().getAttribute("users");

        String fullname = request.getParameter("fullname");
        String bio = request.getParameter("bio");
        String phone = request.getParameter("phone");
        LocalDate dob = LocalDate.parse(request.getParameter("dob"));

        account.setFullname(fullname);
        account.setBio(bio);
        account.setPhone(phone);
        account.setDob(dob);

        accountDAO.updateAccount(account);

        response.sendRedirect(request.getContextPath() + "/profile?userId=" + account.getId());
    }
}

