package controller;

import dao.GroupDAO;
import dto.ResGroupDTO;
import dao.DiscussionPostDAO;
import dto.DiscussionPostDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/groupManager")
public class DiscussionController extends HttpServlet {
    private final DiscussionPostDAO discussionPostDAO = new DiscussionPostDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy groupId từ request
        String groupIdParam = req.getParameter("groupId");
        int groupId = (groupIdParam != null) ? Integer.parseInt(groupIdParam) : 2; // Mặc định groupId = 1

        // Lấy danh sách bài post từ DAO
        List<DiscussionPostDTO> posts = discussionPostDAO.getAllPostsByGroupId(groupId);
        req.setAttribute("posts", posts);
        // Lấy thông tin nhóm (đã có DAO + DTO đầy đủ)
        GroupDAO groupDAO = new GroupDAO();
        ResGroupDTO groupInfo = groupDAO.getGroup(groupId);
        req.setAttribute("groupInfo", groupInfo);

        // Chuyển hướng đến groupManager.jsp
        req.getRequestDispatcher("/WEB-INF/views/groupManager.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Placeholder cho xử lý POST (có thể mở rộng sau)
        resp.sendRedirect(req.getContextPath() + "/groupManager?groupId=2");
    }
}