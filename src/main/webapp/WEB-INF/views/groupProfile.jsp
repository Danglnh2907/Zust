<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">
<head>
  <title>Group Profile</title>
  <style>
    body {
      font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
      background-color: #f4f6f8;
      margin: 0;
      padding: 40px;
      display: flex;
      justify-content: center;
    }

    .container {
      background: #ffffff;
      border-radius: 10px;
      padding: 30px;
      max-width: 600px;
      width: 100%;
      box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    }

    h2 {
      text-align: center;
      color: #333;
      margin-bottom: 30px;
    }

    .form-group {
      margin-bottom: 20px;
    }

    label {
      display: block;
      font-weight: 600;
      margin-bottom: 8px;
      color: #333;
    }

    input[type="text"],
    textarea,
    select {
      width: 100%;
      padding: 10px;
      border: 1px solid #ccc;
      border-radius: 6px;
      font-size: 14px;
      transition: border-color 0.3s ease;
    }

    input[type="text"]:focus,
    textarea:focus,
    select:focus {
      border-color: #007bff;
      outline: none;
    }

    textarea {
      resize: vertical;
      min-height: 80px;
    }

    input[type="file"] {
      border: none;
    }

    img {
      display: block;
      margin-top: 10px;
      border-radius: 6px;
      max-height: 120px;
    }

    .error {
      background-color: #ffe6e6;
      color: #d8000c;
      padding: 10px;
      border-radius: 6px;
      margin-bottom: 20px;
    }

    .success {
      background-color: #e6ffed;
      color: #2e7d32;
      padding: 10px;
      border-radius: 6px;
      margin-bottom: 20px;
    }

    button[type="submit"] {
      width: 100%;
      padding: 12px;
      background-color: #007bff;
      color: #fff;
      border: none;
      border-radius: 6px;
      font-size: 16px;
      cursor: pointer;
      transition: background-color 0.3s ease;
    }

    button[type="submit"]:hover {
      background-color: #0056b3;
    }
  </style>
</head>
<body>
<div class="container">
  <h2>✏️ Edit Group Profile</h2>
  <%
    dto.GroupProfileDTO group = (dto.GroupProfileDTO) request.getAttribute("group");
    String error = (String) request.getAttribute("error");
    String message = (String) request.getAttribute("message");
  %>
  <% if (error != null) { %>
  <div class="error"><%= error %></div>
  <% } %>
  <% if (message != null) { %>
  <div class="success"><%= message %></div>
  <% } %>

  <form action="groupProfile" method="post" enctype="multipart/form-data">
    <input type="hidden" name="groupId"
           value="<%= group != null ? group.getGroupId() : (request.getParameter("groupId") != null ? request.getParameter("groupId") : "") %>">

    <div class="form-group">
      <label for="groupName">Group Name</label>
      <input type="text" id="groupName" name="groupName" required
             value="<%= group != null ? group.getGroupName() : "" %>">
    </div>

    <div class="form-group">
      <label for="description">Description</label>
      <textarea id="description" name="description"><%= group != null ? group.getDescription() : "" %></textarea>
    </div>

    <div class="form-group">
      <label for="avatar">Avatar</label>
      <input type="file" id="avatar" name="avatar" accept="image/*">
      <% if (group != null && group.getAvatarPath() != null) { %>
      <img src="<%= group.getAvatarPath() %>" alt="Current Avatar">
      <% } %>
    </div>

    <div class="form-group">
      <label for="status">Status</label>
      <select id="status" name="status">
        <option value="public" <%= group != null && "public".equals(group.getStatus()) ? "selected" : "" %>>Public</option>
        <option value="private" <%= group != null && "private".equals(group.getStatus()) ? "selected" : "" %>>Private</option>
      </select>
    </div>

    <button type="submit">Update Profile</button>
  </form>
</div>
</body>
</html>
