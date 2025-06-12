package controller;

import dao.GroupDAO;
import dto.GroupProfileDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@WebServlet("/groupProfile")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5MB
public class GroupProfileServlet extends HttpServlet {
    private final GroupDAO groupDAO = new GroupDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
        if (accountId == null) {
            accountId = 1003; // Set tạm accountId để test
            req.getSession().setAttribute("accountId", accountId);
            System.out.println("Set temporary accountId: " + accountId + " for testing");
        }

        String groupIdParam = req.getParameter("groupId");
        if (groupIdParam == null || groupIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Group ID is required");
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid group ID");
            return;
        }

        GroupProfileDTO group = groupDAO.getGroupProfile(groupId, accountId);
        if (group == null) {
            group = new GroupProfileDTO(); // Tạo DTO tạm nếu không tìm thấy
            group.setGroupId(groupId);
            group.setGroupName("Test Group " + groupId);
            group.setDescription("Temporary group for testing");
            group.setStatus("public");
            group.setCreatedBy(accountId);
            group.setGroupCreateDate(LocalDateTime.now());
            System.out.println("Temporary group created for groupId: " + groupId + " with accountId: " + accountId);
        } else {
            System.out.println("Group found: " + group.getGroupName() + " for groupId: " + groupId);
        }

        req.setAttribute("group", group);
        req.getRequestDispatcher("/WEB-INF/views/groupProfile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
        if (accountId == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        String groupIdParam = req.getParameter("groupId");
        if (groupIdParam == null || groupIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Group ID is required");
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid group ID");
            return;
        }

        GroupProfileDTO dto = new GroupProfileDTO();
        dto.setGroupId(groupId);
        String groupName = req.getParameter("groupName");
        String description = req.getParameter("description");
        String status = req.getParameter("status");

        // Kiểm tra dữ liệu đầu vào
        if (groupName == null || groupName.trim().isEmpty()) {
            dto.addError("Group name is required");
        } else {
            dto.setGroupName(groupName.trim());
        }
        if (description == null) description = "";
        dto.setDescription(description.trim());
        if (status == null || (!"public".equalsIgnoreCase(status) && !"private".equalsIgnoreCase(status))) {
            dto.setStatus("public");
        } else {
            dto.setStatus(status.trim());
        }

        // Xử lý upload avatar
        String avatarPath = null;
        Part avatarPart = req.getPart("avatar");
        if (avatarPart != null && avatarPart.getSize() > 0) {
            String uploadDir = req.getServletContext().getRealPath("/uploads");
            if (uploadDir == null) {
                dto.addError("Upload directory not found");
            } else {
                Path uploadPath = Paths.get(uploadDir);
                try {
                    Files.createDirectories(uploadPath);
                    String fileName = UUID.randomUUID().toString() + "_" + avatarPart.getSubmittedFileName();
                    Path filePath = uploadPath.resolve(fileName);
                    avatarPart.write(filePath.toString());
                    avatarPath = "/uploads/" + fileName;
                } catch (IOException e) {
                    dto.addError("Failed to upload avatar: " + e.getMessage());
                    System.err.println("Avatar upload error: " + e.getMessage());
                }
            }
        }
        dto.setAvatarPath(avatarPath);

        // Cập nhật database
        boolean success = groupDAO.updateGroupProfile(dto, accountId);
        if (!dto.getErrors().isEmpty()) {
            success = false; // Đặt success = false nếu có lỗi
        }
        if (success) {
            req.setAttribute("message", "Group profile updated successfully");
            // Cập nhật lại group từ database để hiển thị dữ liệu mới
            GroupProfileDTO updatedGroup = groupDAO.getGroupProfile(groupId, accountId);
            if (updatedGroup != null) {
                req.setAttribute("group", updatedGroup);
            }
        } else {
            req.setAttribute("error", "Failed to update group profile: " + String.join(", ", dto.getErrors()));
        }
        req.setAttribute("group", dto); // Giữ dto để hiển thị dữ liệu đã nhập
        req.getRequestDispatcher("/WEB-INF/views/groupProfile.jsp").forward(req, resp);
    }
}