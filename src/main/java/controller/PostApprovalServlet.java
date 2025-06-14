package controller;

// Import các lớp cần thiết
import dao.PostApprovalDAO;
import dto.PostApprovalDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

// Định nghĩa servlet với đường dẫn URL là /approvePost
@WebServlet("/approvePost")
public class PostApprovalServlet extends HttpServlet {
    // Tạo đối tượng DAO để làm việc với cơ sở dữ liệu
    private final PostApprovalDAO postApprovalDAO = new PostApprovalDAO();

    // Xử lý các yêu cầu GET
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        // Lấy thông tin accountId và role từ session
//     Integer ManagerId = (Integer) req.getSession().getAttribute("accountId");
////        String accountRole = (String) req.getSession().getAttribute("accountRole");
//
//        if (ManagerId == null) {
//            ManagerId = 1003; // ID của một tài khoản admin giả định (đang quản lý group nào đó)
//            req.getSession().setAttribute("accountId", ManagerId);
//            System.out.println("Set temporary accountId: " + ManagerId + " for testing");
//        }
//
        String managerIdParam = req.getParameter("managerId");
        System.out.println("DEBUG - managerId = " + managerIdParam);

        int managerId;

        try {
            managerId = Integer.parseInt(managerIdParam);
        } catch (NumberFormatException | NullPointerException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing managerId");
            return;
        }

        // Lấy danh sách các bài post đang chờ duyệt của tài khoản
        List<PostApprovalDTO> pendingPosts = postApprovalDAO.getPendingPosts(managerId);
        System.out.println("Pending posts size in servlet: " + pendingPosts.size());
        if (pendingPosts.isEmpty()) {
            System.out.println("No pending posts found, setting message");
            req.setAttribute("message", "No pending posts to approve");
        } else {
            System.out.println("Found pending posts, setting attribute 'posts'");
            req.setAttribute("posts", pendingPosts);
        }

        // Chuyển tiếp sang trang JSP để hiển thị dữ liệu
        req.getRequestDispatcher("/WEB-INF/views/postApprove.jsp").forward(req, resp);
    }

    // Xử lý các yêu cầu POST (ví dụ: khi bấm duyệt hoặc từ chối bài post)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy lại thông tin người dùng từ session
//        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
//        String accountRole = (String) req.getSession().getAttribute("accountRole");

        // Nếu không phải admin hoặc chưa đăng nhập thì chuyển hướng về trang login
//        if (accountId == null || !"admin".equals(accountRole)) {
//            resp.sendRedirect("login.jsp");
//            return;
//        }

        // Lấy postId và action từ form (ví dụ: approve hoặc disapprove)
        String postIdParam = req.getParameter("postId");
        String action = req.getParameter("action");

        // Kiểm tra đầu vào hợp lệ
        if (postIdParam == null || postIdParam.trim().isEmpty() || action == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID and action are required");
            return;
        }

        int postId;
        try {
            postId = Integer.parseInt(postIdParam); // Chuyển postId thành số
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid post ID");
            return;
        }


        // Chỉ chấp nhận action là "approve" hoặc "disapprove"
        if (!"approve".equalsIgnoreCase(action) && !"disapprove".equalsIgnoreCase(action)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            return;
        }

// Chuyển action thành giá trị đúng với trong DB (published hoặc rejected)
        String normalizedAction = "approve".equalsIgnoreCase(action) ? "published" : "rejected";

// Gọi DAO để xử lý phê duyệt hoặc từ chối bài viết
        boolean success = postApprovalDAO.processPost(postId, normalizedAction ); // gán tạm accountId = 1003 test


        // Nếu thành công, set thông báo thành công
        if (success) {
            req.setAttribute("message", "Post " + action + "d successfully");
        } else {
            req.setAttribute("error", "Failed to process post");
        }

        // Chuyển hướng về lại trang approvePost để load lại danh sách
        resp.sendRedirect(req.getContextPath() + "/"); // tạm chuyển về home

    }

}
