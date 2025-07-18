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
import java.util.UUID;

@WebServlet("/groupManager")
public class DiscussionController extends HttpServlet {
    private final DiscussionPostDAO discussionPostDAO = new DiscussionPostDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy groupId từ request
        String groupIdParam = req.getParameter("groupId");
        int groupId = (groupIdParam != null) ? Integer.parseInt(groupIdParam) : 1;
        System.out.println("Group ID: " + groupId);

        // Lấy danh sách bài post từ DAO
        List<DiscussionPostDTO> posts = discussionPostDAO.getAllPostsByGroupId(groupId);
        System.out.println("Posts size: " + (posts != null ? posts.size() : "null"));
        req.setAttribute("posts", posts);
        // Lấy thông tin nhóm (đã có DAO + DTO đầy đủ)
        GroupDAO groupDAO = new GroupDAO();
        ResGroupDTO groupInfo = groupDAO.getGroup(groupId);
        req.setAttribute("groupInfo", groupInfo);

        String csrfToken = UUID.randomUUID().toString();
        req.setAttribute("csrfToken", csrfToken);
        req.getSession().setAttribute("csrfToken", csrfToken);

        // Chuyển hướng đến groupManager.jsp
        req.getRequestDispatcher("/WEB-INF/views/groupManager.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Placeholder cho xử lý POST (có thể mở rộng sau)
        resp.sendRedirect(req.getContextPath() + "/groupManager?groupId=1");
    }
}