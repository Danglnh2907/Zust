package controller;

import dao.AccountDAO;
import model.Account;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import util.service.FileService;

@WebServlet(name = "EditProfileServlet", urlPatterns = {"/editProfile"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50)   // 50MB
public class EditProfileServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, String> responseMap = new HashMap<>();

        Account currentUser = (Account) request.getSession().getAttribute("users");
        if (currentUser == null) {
            responseMap.put("status", "error");
            responseMap.put("message", "User not logged in.");
            out.print("{\"status\":\"" + responseMap.get("status") + "\",\"message\":\"" + responseMap.get("message") + "\"}");
            out.flush();
            return;
        }

        try {
            String fullname = null;
            String phone = null;
            boolean gender = false;
            String dobString = null;
            LocalDate dob = null;
            String bio = null;
            String avatar = null;
            String coverImage = null;

            FileService fileService = new FileService(getServletContext());

            for (Part part : request.getParts()) {
                String fieldName = part.getName();
                String submittedFileName = part.getSubmittedFileName();

                if (submittedFileName == null || submittedFileName.isEmpty()) { // It's a regular form field
                    switch (fieldName) {
                        case "fullname":
                            fullname = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                            break;
                        case "phone":
                            phone = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                            break;
                        case "gender":
                            gender = Boolean.parseBoolean(new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                            break;
                        case "dob":
                            dobString = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                            if (dobString != null && !dobString.isEmpty()) {
                                dob = LocalDate.parse(dobString);
                            }
                            break;
                        case "bio":
                            bio = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                            break;
                        case "avatar": // Hidden field for existing avatar
                            avatar = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                            break;
                        case "coverImage": // Hidden field for existing cover image
                            coverImage = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                            break;
                    }
                } else { // It's a file upload
                    String fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                    if (fileName != null && !fileName.isEmpty()) {
                        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                        fileService.saveFile(uniqueFileName, part.getInputStream());
                        if (fieldName.equals("avatarFile")) {
                            avatar = uniqueFileName;
                        } else if (fieldName.equals("coverImageFile")) {
                            coverImage = uniqueFileName;
                        }
                    }
                }
            }

            // Update the current user object with new data
            currentUser.setFullname(fullname);
            currentUser.setPhone(phone);
            currentUser.setGender(gender);
            currentUser.setDob(dob);
            currentUser.setBio(bio);
            currentUser.setAvatar(avatar);
            currentUser.setCoverImage(coverImage);

            AccountDAO accountDAO = new AccountDAO();
            boolean success = accountDAO.updateAccount(currentUser);

            if (success) {
                // Update the session attribute with the new account data
                request.getSession().setAttribute("users", currentUser);
                request.getSession().setAttribute("userProfile", currentUser);
                responseMap.put("status", "success");
                responseMap.put("message", "Profile updated successfully.");
            } else {
                responseMap.put("status", "error");
                responseMap.put("message", "Failed to update profile in database.");
            }

        } catch (Exception e) {
            responseMap.put("status", "error");
            responseMap.put("message", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.print("{\"status\":\"" + responseMap.get("status") + "\",\"message\":\"" + responseMap.get("message") + "\"}");
            out.flush();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // For GET requests, simply forward to the user profile page or handle as needed
        // This servlet primarily handles POST for updates
        response.sendRedirect(request.getContextPath() + "/profile?userId=" + ((Account) request.getSession().getAttribute("users")).getId());
    }
}
