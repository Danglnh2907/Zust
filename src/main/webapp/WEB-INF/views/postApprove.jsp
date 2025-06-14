<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%-- Cấu hình trang JSP để sử dụng UTF-8 và ngôn ngữ Java --%>
<%@ page import="dto.PostApprovalDTO" %> <%-- Import lớp PostApprovalDTO dùng để truyền dữ liệu bài viết và tài khoản --%>
<%@ page import="java.util.List" %> <%-- Import lớp List để xử lý danh sách bài viết --%>
<%@ page import="model.Post" %> <%-- Import lớp Post đại diện cho bài đăng --%>
<%@ page import="model.Account" %> <%-- Import lớp Account đại diện cho người dùng đăng bài --%>

<html>
<head>
    <title>Post Approval</title> <%-- Tiêu đề trang hiển thị trên tab trình duyệt --%>
    <style>
        /* CSS cho giao diện bảng duyệt bài */
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: #f9f9f9;
        }
        h2 { color: #333; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        th, td {
            padding: 12px;
            border: 1px solid #ccc;
            text-align: left;
        }
        th {
            background-color: #eee;
        }
        .btn {
            padding: 6px 12px;
            margin-right: 5px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        .approve {
            background-color: #4CAF50;
            color: white;
        }
        .disapprove {
            background-color: #f44336;
            color: white;
        }
        .message {
            margin-top: 15px;
            padding: 10px;
            background-color: #e7f3fe;
            color: #31708f;
            border: 1px solid #bce8f1;
            border-radius: 4px;
        }
    </style>
</head>
<body>

<h2>Pending Posts for Approval</h2> <%-- Tiêu đề chính hiển thị trên trang --%>

<%
    // Lấy thông báo thành công hoặc lỗi (nếu có) từ request
    String message = (String) request.getAttribute("message");
    String error = (String) request.getAttribute("error");
    if (message != null) {
%>
<div class="message"><%= message %></div> <%-- Hiển thị thông báo thành công --%>
<%
} else if (error != null) {
%>
<div class="message" style="background-color:#fdd; color:#a00; border-color:#f99;"><%= error %></div> <%-- Hiển thị thông báo lỗi --%>
<%
    }

    // Lấy danh sách bài viết chờ duyệt từ request
    List<PostApprovalDTO> posts = (List<PostApprovalDTO>) request.getAttribute("posts");

    // Kiểm tra nếu không có bài nào thì hiển thị thông báo
    if (posts == null || posts.isEmpty()) {
%>
<p>No posts awaiting approval.</p> <%-- Thông báo không có bài chờ duyệt --%>
<%
} else {
%>

<table>
    <tr>
        <th>Post ID</th>       <%-- Cột hiển thị ID bài đăng --%>
        <th>Content</th>       <%-- Cột hiển thị nội dung bài đăng --%>
        <th>Status</th>        <%-- Cột hiển thị trạng thái bài (ví dụ: pending, approved...) --%>
<%--        <th>Author</th>        &lt;%&ndash; Cột hiển thị người đăng bài &ndash;%&gt;--%>
        <th>Action</th>        <%-- Cột chứa nút duyệt / từ chối --%>
    </tr>
    <%
        // Duyệt từng bài viết trong danh sách
        for (PostApprovalDTO dto : posts) {
            Post post = dto.getPost(); // Lấy đối tượng Post từ DTO
//            Account acc = dto.getAccount(); // Lấy tài khoản người đăng bài (nếu cần dùng)
    %>
    <tr>
        <td><%= post.getId() %></td> <%-- Hiển thị ID bài viết --%>
        <td><%= post.getPostContent() %></td> <%-- Hiển thị nội dung bài viết --%>
        <td><%= post.getPostStatus() %></td> <%-- Hiển thị trạng thái bài viết --%>
        <%--        <td><%= acc.getUsername() %></td> --%> <%-- (comment) Nếu muốn hiển thị tên người đăng bài --%>
        <td>
            <%-- Form gửi yêu cầu duyệt bài (Approve) --%>
            <form action="<%= request.getContextPath() %>/approvePost" method="post" style="display:inline;">
                <input type="hidden" name="postId" value="<%= post.getId() %>"> <%-- ID bài viết gửi lên --%>
                <input type="hidden" name="action" value="approve"> <%-- Hành động duyệt bài --%>
                <button type="submit" class="btn approve">Approve</button> <%-- Nút duyệt --%>
            </form>

            <%-- Form gửi yêu cầu từ chối bài (Reject) --%>
            <form action="<%= request.getContextPath() %>/approvePost" method="post" style="display:inline;">
                <input type="hidden" name="postId" value="<%= post.getId() %>"> <%-- ID bài viết gửi lên --%>
                <input type="hidden" name="action" value="disapprove"> <%-- Hành động từ chối bài --%>
                <button type="submit" class="btn disapprove">Reject</button> <%-- Nút từ chối --%>
            </form>
        </td>
    </tr>
    <%
        } // Kết thúc vòng lặp
    %>
</table>

<%
    } // Kết thúc else (nếu có bài viết chờ duyệt)
%>

</body>
</html>
