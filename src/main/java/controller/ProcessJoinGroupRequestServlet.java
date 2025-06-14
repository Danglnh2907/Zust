// Khai báo package và các import cần thiết
package controller;

import dao.JoinGroupRequestDAO;
import dto.JoinGroupRequestDTO;
import model.Account;
import model.Group;
import model.JoinGroupRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Định nghĩa servlet để xử lý yêu cầu tham gia nhóm
@WebServlet("/processJoinGroupRequest")
public class ProcessJoinGroupRequestServlet extends HttpServlet {
    // Khởi tạo DAO để thao tác với dữ liệu yêu cầu tham gia nhóm
    private final JoinGroupRequestDAO joinGroupRequestDAO = new JoinGroupRequestDAO();

    // Xử lý GET request - hiển thị danh sách các yêu cầu đang chờ xử lý
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy accountId từ session, nếu chưa có thì giả lập để test
//        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
//        if (accountId == null) {
//            accountId = 1004; // Giả lập cho mục đích kiểm thử
//            req.getSession().setAttribute("accountId", accountId);
//        }

        // Lấy groupId từ request parameter
        String groupIdParam = req.getParameter("groupId");
        System.out.println("[DEBUG] doGet - groupIdParam: " + groupIdParam);
        if (groupIdParam == null || groupIdParam.trim().isEmpty()) {
            System.out.println("[ERROR] doGet - Group ID is missing");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Group ID is required");
            return;
        }

        // Parse groupId và xử lý lỗi nếu có
        int groupId;
        try {
            groupId = Integer.parseInt(groupIdParam);
            System.out.println("[DEBUG] doGet - Parsed groupId: " + groupId);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid group ID");
            return;
        }

        // Lấy danh sách yêu cầu tham gia nhóm đang chờ duyệt
        List<JoinGroupRequest> requests = joinGroupRequestDAO.getPendingJoinRequests(groupId);
        System.out.println("[DEBUG] doGet - Number of requests: " + (requests != null ? requests.size() : "null"));
        List<JoinGroupRequestDTO> requestDtos = new ArrayList<>();

        // Chuyển đổi danh sách model thành DTO để truyền tới view
        for (JoinGroupRequest request : requests) {
//            Account account = new Account();
//            account.setId(request.getAccount().getId());

            Group group = new Group();
            group.setId(request.getGroup().getId());

            requestDtos.add(new JoinGroupRequestDTO(request, group));
        }
        System.out.println("[DEBUG] doGet - Number of requestDtos: " + requestDtos.size());
        // Gán danh sách vào request attribute và forward đến JSP để hiển thị
        req.setAttribute("requests", requestDtos);
        req.getRequestDispatcher("/WEB-INF/views/ProcessJoinGroupRequest.jsp").forward(req, resp);
    }

    // Xử lý POST request - duyệt hoặc từ chối yêu cầu tham gia nhóm
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy accountId từ session
//        Integer accountId = (Integer) req.getSession().getAttribute("accountId");
//        if (accountId == null) {
//            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
//            return;
//        }

        // Lấy các tham số cần thiết từ form (requestId và action)
        String requestIdParam = req.getParameter("requestId");
        String action = req.getParameter("action");
        String groupIdParam = req.getParameter("groupId");
        System.out.println("[DEBUG] doPost - requestId: " + requestIdParam + ", action: " + action + ", groupId: " + groupIdParam);
        // Kiểm tra tính hợp lệ của tham số
        if (requestIdParam == null || requestIdParam.trim().isEmpty() || action == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request ID and action are required");
            return;
        }

        // Parse requestId và xử lý lỗi nếu sai định dạng
        int requestId;
        try {
            requestId = Integer.parseInt(requestIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request ID");
            return;
        }

        // Kiểm tra action là approve hay disapprove
        if (!"approve".equalsIgnoreCase(action) && !"disapprove".equalsIgnoreCase(action)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");

            return;
        }

        // Gọi DAO để thực hiện xử lý tương ứng
        boolean success = false;
        if ("approve".equalsIgnoreCase(action)) {
            success = joinGroupRequestDAO.approveJoinRequest(requestId);
            System.out.println("[DEBUG] doPost - Approve requestId " + requestId + ": " + success);
        } else if ("disapprove".equalsIgnoreCase(action)) {
            success = joinGroupRequestDAO.disapproveJoinRequest(requestId);
            System.out.println("[DEBUG] doPost - Disapprove requestId " + requestId + ": " + success);
        }

        // Gán thông báo cho người dùng nếu cần (có thể sử dụng trong view sau này)
        if (success) {
            req.setAttribute("message", "Request " + (action.equalsIgnoreCase("approve") ? "approved" : "disapproved") + " successfully");
        } else {
            req.setAttribute("error", "Failed to process request");
        }

        // Chuyển hướng trở lại trang danh sách yêu cầu

        if (groupIdParam != null && !groupIdParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/processJoinGroupRequest?groupId=" + groupIdParam);
        } else {
            resp.sendRedirect(req.getContextPath() + "/processJoinGroupRequest");
        }
    }
}
