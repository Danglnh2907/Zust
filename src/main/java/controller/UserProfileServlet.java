package controller;

import dao.AccountDAO;
import dao.GroupDAO;
import dao.PostDAO;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import model.RespPostDTO;
import model.Account;
import model.FriendRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import org.mindrot.jbcrypt.BCrypt;
import util.service.FileService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

@WebServlet(name = "UserProfileServlet", urlPatterns = {"/profile"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50)   // 50MB
public class UserProfileServlet extends HttpServlet {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * /profile?userId=USER_ID
         * /profile?action=change_password
         */
        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("users");
        if (account == null) {
            response.sendRedirect("/zust/auth");
            return;
        }

        //If provided action, serve the change password page
        String action = request.getParameter("action");
        if (action != null && action.equals("change_password")) {
            request.setAttribute("account", account);
            request.getRequestDispatcher("/WEB-INF/views/change_password.jsp").forward(request, response);
            return;
        }

        //If provided userID, serve the profile page
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            logger.info("Serve user profile with ID: " + userId);
            AccountDAO accountDAO = new AccountDAO();
            Account userProfile = accountDAO.getAccountById(userId);

            if (userProfile != null) {
                PostDAO postDAO = new PostDAO();
                int requesterId = account.getId();

                //Get total posts from the current requester
                ArrayList<RespPostDTO> totalPosts = postDAO.getPosts(requesterId, userProfile.getId());

                //Filter between post and repost
                ArrayList<RespPostDTO> posts = postDAO.getPosts(requesterId, userId);
                ArrayList<RespPostDTO> reposts = new ArrayList<>();
                for (RespPostDTO post : totalPosts) {
                    if (post.getRepost() != null) {
                        reposts.add(post);
                    } else {
                        posts.add(post);
                    }
                }
                request.setAttribute("posts", posts);
                request.setAttribute("reposts", reposts);

                //Get list of friends of the current user profile that requester is visited
                List<Account> friends = accountDAO.getFriends(userProfile.getId());
                request.setAttribute("friends", friends);

                //If the requester is currently visit their own page, get the list of friend request
                if (Objects.equals(account.getId(), userProfile.getId())) {
                    List<FriendRequest> friendRequests = accountDAO.getFriendRequests(account.getId());
                    request.setAttribute("friendRequests", friendRequests);
                }
                //If the requester is currently visit other page
                //get the list of user that are friend with requester,
                //and the list of user that requester has sent friend requests but the other side still not reply
                else {
                    boolean areFriends = accountDAO.areFriends(account.getId(), userProfile.getId());
                    boolean friendRequestPending = accountDAO.isFriendRequestPending(account.getId(), userProfile.getId());
                    request.setAttribute("areFriends", areFriends);
                    request.setAttribute("friendRequestPending", friendRequestPending);
                }

                //Get list of joined groups (for UI render)
                request.setAttribute("group", (new GroupDAO()).getJoinedGroups(account.getId()));

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
        /*
         * /profile?action=edit
         * /profile?action=change_password
         */

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("users");
        if (account == null) {
            logger.info("Not logged in! Cannot process this request");
            response.sendRedirect("/auth");
            return;
        }

        PrintWriter out = response.getWriter();

        //Get action parameter
        String action = request.getParameter("action");
        switch (action) {
            case "edit" -> {
                Map<String, String> responseMap = new HashMap<>();
                try {
                    String fullname = null;
                    String phone = null;
                    boolean gender = false;
                    LocalDate dob = null;
                    String bio = null;
                    String avatar = null;
                    String coverImage = null;

                    FileService fileService = new FileService();

                    for (Part part : request.getParts()) {
                        String fieldName = part.getName();
                        String submittedFileName = part.getSubmittedFileName();

                        //This is a regular field
                        if (submittedFileName == null || submittedFileName.isEmpty()) {
                            switch (fieldName) {
                                case "fullname" -> {
                                    fullname = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                                    logger.info("Fullname: " + fullname);
                                }
                                case "phone" -> {
                                    phone = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                                    logger.info("Phone: " + phone);
                                }
                                case "gender" -> {
                                    gender = Boolean.parseBoolean(new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                                    logger.info("Gender: " + gender);
                                }
                                case "dob" -> {
                                    String dobStr = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                                    if (!dobStr.isEmpty()) {
                                        dob = LocalDate.parse(dobStr);
                                    }
                                    logger.info("Dob: " + dob);
                                }
                                case "bio" -> {
                                    bio = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                                    logger.info("Bio: " + bio);
                                }
                                case "avatar" -> {
                                    avatar = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                                    logger.info("Avatar: " + avatar);
                                }
                                case "coverImage" -> {
                                    coverImage = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                                    logger.info("Cover Image: " + coverImage);
                                }
                            }
                        }
                        //This is file upload
                        else {
                            String fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                            if (!fileName.isEmpty()) {
                                File uploadDir = new File(fileService.getLocationPath() + File.separator + "images");
                                String newFileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + "_" + fileName;
                                logger.info("Saving file at: " + uploadDir.getAbsolutePath() + File.separator + newFileName);
                                part.write(uploadDir.getAbsolutePath() + File.separator + newFileName);

                                if (fieldName.equals("avatarFile")) {
                                    avatar = newFileName;
                                } else if (fieldName.equals("coverImageFile")) {
                                    coverImage = newFileName;
                                }
                            }
                        }

                        //Update new data to account
                        account.setFullname(fullname);
                        account.setPhone(phone);
                        account.setGender(gender);
                        account.setDob(dob);
                        account.setAvatar(avatar);
                        account.setCoverImage(coverImage);

                        //Update data back in database
                        boolean success = (new AccountDAO()).updateAccount(account);
                        if (success) {
                            logger.info("Successfully updated profile data");

                            // Update the session attribute with the new account data
                            request.getSession().setAttribute("users", account);
                            request.getSession().setAttribute("userProfile", account);
                            responseMap.put("status", "success");
                            responseMap.put("message", "Profile updated successfully.");
                        } else {
                            logger.info("Failed to update profile data");

                            // Add error message and status
                            responseMap.put("status", "error");
                            responseMap.put("message", "Failed to update profile in database.");
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Failed to edit profile: " + e.getMessage());

                    responseMap.put("status", "error");
                    responseMap.put("message", "An error occurred: " + e.getMessage());
                } finally {
                    out.print("{\"status\":\"" + responseMap.get("status") + "\",\"message\":\"" + responseMap.get("message") + "\"}");
                    out.flush();
                }
            }
            case "change_password" -> {
                String currentPassword = request.getParameter("currentPassword");
                String newPassword = request.getParameter("newPassword");
                String confirmPassword = request.getParameter("confirmPassword");

                // Input validation
                if (currentPassword == null || newPassword == null || confirmPassword == null ||
                    currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    request.setAttribute("error", "All fields are required.");
                    request.getRequestDispatcher("change_password.jsp").forward(request, response);
                    return;
                }

                // Check if the confirmed password is the same with the new password provided
                if (!newPassword.equals(confirmPassword)) {
                    request.setAttribute("error", "New password and confirm password do not match.");
                    request.getRequestDispatcher("change_password.jsp").forward(request, response);
                    return;
                }

                // Password strength validation
                // At least 8 characters long, includes an uppercase letter, a lowercase letter, and a number.
                String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$";
                if (!newPassword.matches(passwordRegex)) {
                    request.setAttribute("error", "New password does not meet the requirements. It must be at least 8 characters long and include an uppercase letter, a lowercase letter, and a number.");
                    request.getRequestDispatcher("change_password.jsp").forward(request, response);
                    return;
                }

                boolean success = (new AccountDAO()).changePassword(
                        account.getId(), currentPassword, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                if (success) {
                    logger.info("Successfully changed password");
                    response.sendRedirect("/zust/profile?userId=" + account.getId());
                } else {
                    logger.warning("Failed to change password");
                    request.setAttribute("error", "Failed to change password. Current password might be " +
                                                  "incorrect or a database error occurred. Please try again.");
                    response.sendRedirect("/zust/profile?action=change_password");
                }
            }
        }
    }
}

