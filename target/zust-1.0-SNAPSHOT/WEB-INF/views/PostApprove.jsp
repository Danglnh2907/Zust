<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Approve/Disapprove Posts</title>
  <style>
    .post { border: 1px solid #ccc; padding: 10px; margin: 10px 0; }
    .error { color: red; }
    .success { color: green; }
  </style>
</head>
<body>
<h2>Approve/Disapprove Posts</h2>
<%
  java.util.List<dto.PostApprovalDTO> posts = (java.util.List<dto.PostApprovalDTO>) request.getAttribute("posts");
  String error = (String) request.getAttribute("error");
  String message = (String) request.getAttribute("message");
%>
<% if (error != null) { %>
<p class="error"><%= error %></p>
<% } %>
<% if (message != null) { %>
<p class="success"><%= message %></p>
<% } %>
<% if (posts != null && !posts.isEmpty()) { %>
<% for (dto.PostApprovalDTO postDto : posts) { %>
<div class="post">
  <p>Post ID: <%= postDto.getPost().getId() %></p>
  <p>Content: <%= postDto.getPost().getPostContent() %></p>
  <p>Posted by: <%= postDto.getAccount().getUsername() %> (ID: <%= postDto.getAccount().getId() %>)</p>
  <p>Created at: <%= postDto.getPost().getCreatedAt() %></p>
  <form action="approvePost" method="post">
    <input type="hidden" name="postId" value="<%= postDto.getPost().getId() %>">
    <button type="submit" name="action" value="approve">Approve</button>
    <button type="submit" name="action" value="disapprove">Disapprove</button>
  </form>
</div>
<% } %>
<% } else { %>
<p><%= message != null ? message : "No pending posts to process" %></p>
<% } %>
</body>
</html>