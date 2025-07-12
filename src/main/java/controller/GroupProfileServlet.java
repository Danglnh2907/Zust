// Khai báo package
package controller;

// Import các lớp cần thiết
import dao.GroupProfileDAO;
import util.service.FileService;
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
import java.io.InputStream;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

// Đăng ký servlet với URL /groupProfile
@WebServlet("/groupProfile")
// Cấu hình servlet để xử lý file upload (tối đa 5MB)
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class GroupProfileServlet extends HttpServlet {
    // Khởi tạo DAO để thao tác với database
//    private final GroupProfileDAO groupDAO = new GroupProfileDAO();

    // Xử lý GET request
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GroupProfileDAO groupDAO = new GroupProfileDAO();
        // Lấy accountId từ session, nếu chưa có thì set tạm để test
//        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
//        if (accountId == null) {
//            accountId = 1003; // gán tạm accountId cho mục đích test
//            req.getSession().setAttribute("accountId", accountId);
//            System.out.println("Set temporary accountId: " + accountId + " for testing");
//        }

        // Lấy groupId từ request
        String groupIdParam = req.getParameter("groupId");
        if (groupIdParam == null || groupIdParam.trim().isEmpty()) {
            // Nếu không có groupId, trả lỗi 400
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Group ID is required");
            return;
        }

        // Parse groupId thành số nguyên
        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid group ID");
            return;
        }

        // Lấy thông tin group từ DB
        GroupProfileDTO group = groupDAO.getGroupProfile(groupId);

        // Nếu không tìm thấy group, tạo tạm DTO để test
        if (group == null) {
//            group = new GroupProfileDTO();
//            group.setGroupId(groupId);
//            group.setGroupName("Test Group " + groupId);
//            group.setDescription("Temporary group for testing");
//            group.setStatus("public");
////            group.setCreatedBy(accountId);
//            group.setGroupCreateDate(LocalDateTime.now());
//            System.out.println("Temporary group created for groupId: " + groupId + " with accountId: " );
            System.out.println("Fail " );
            req.setAttribute("error", "Group with ID " + groupId + " not found.");
            req.getRequestDispatcher("/WEB-INF/views/").forward(req, resp); // Hoặc trang lỗi tùy chỉnh
            return;


        } else {
            System.out.println("Group found: " + group.getGroupName() + " for groupId: " + groupId);
        }

        // Gửi dữ liệu group đến JSP
        req.setAttribute("group", group);
        req.getRequestDispatcher("/WEB-INF/views/groupProfile.jsp").forward(req, resp);
    }

    // Xử lý POST request (khi người dùng cập nhật group)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GroupProfileDAO groupDAO = new GroupProfileDAO();
        // Kiểm tra user đã đăng nhập chưa
//        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
//        if (accountId == null) {
//            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
//            return;
//        }

        // Lấy groupId từ request
        String groupIdParam = req.getParameter("groupId");
        if (groupIdParam == null || groupIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Group ID is required");
            return;
        }

        // Parse groupId
        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid group ID");
            return;
        }

        // Tạo DTO mới để chứa dữ liệu cập nhật
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

        // Nếu không có description thì gán rỗng
        if (description == null) description = "";
        dto.setDescription(description.trim());

        // Xử lý status: nếu không hợp lệ thì set mặc định là "public"
        if (status == null || (!"deleted".equalsIgnoreCase(status) && !"active".equalsIgnoreCase(status)&& !"banned".equalsIgnoreCase(status))) {
            dto.setStatus("active");
        } else {
            dto.setStatus(status.trim());
        }

        // Xử lý upload avatar
        String avatarPath = null;
        Part avatarPart = req.getPart("avatar");
        if (avatarPart != null && avatarPart.getSize() > 0) {
            FileService fileService = new FileService(req.getServletContext()); // sử dụng context để lấy thư mục lưu trữ
            try (InputStream input = avatarPart.getInputStream()) {
                String originalFileName = avatarPart.getSubmittedFileName();
                String randomFileName = UUID.randomUUID() + "_" + originalFileName;
                avatarPath = fileService.saveFile(randomFileName, input);
            } catch (IOException e) {
                dto.addError("Failed to upload avatar: " + e.getMessage());
                System.err.println("Avatar upload error: " + e.getMessage());
            }
        }
        dto.setAvatarPath(avatarPath);

        // Gọi DAO để cập nhật profile trong database
        boolean success = groupDAO.updateGroupProfile(dto);

        // Nếu DTO có lỗi thì cập nhật không thành công
        if (!dto.getErrors().isEmpty()) {
            success = false;
        }

        // Xử lý kết quả cập nhật
        if (success) {
            req.setAttribute("message", "Group profile updated successfully");
            // Lấy lại dữ liệu mới từ DB để hiển thị
            GroupProfileDTO updatedGroup = groupDAO.getGroupProfile(groupId);
            if (updatedGroup != null) {
                req.setAttribute("group", updatedGroup);
            }
        } else {
            // Gửi thông báo lỗi
            req.setAttribute("error", "Failed to update group profile: " + String.join(", ", dto.getErrors()));
        }

        // Dù thành công hay thất bại, gửi lại DTO để giữ dữ liệu người dùng nhập
        req.setAttribute("group", dto);
        req.getRequestDispatcher("/WEB-INF/views/groupProfile.jsp").forward(req, resp);
    }
}