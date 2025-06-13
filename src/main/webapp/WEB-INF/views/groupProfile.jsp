<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="dto.GroupProfileDTO" %>
<html>
<head>
  <title>Group Profile</title>
  <style>
    /* Global styles */
    body {
      font-family: Arial, sans-serif;
      background-color: #f5f5f5;
      margin: 0;
      padding: 20px;
    }
    .container {
      max-width: 1000px;
      margin: 0 auto;
    }
    h1 {
      text-align: center;
      color: #333;
    }
    .profile-container {
      display: flex;
      gap: 20px;
      margin-top: 20px;
    }
    .profile-display, .update-form {
      background: white;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      flex: 1;
    }
    .profile-display img {
      border-radius: 50%;
      margin-bottom: 10px;
      display: block;
      margin-left: auto;
      margin-right: auto;
    }
    .profile-display p {
      margin: 10px 0;
    }
    .update-form label {
      display: block;
      margin-top: 10px;
      font-weight: bold;
    }
    .update-form input[type="text"],
    .update-form textarea {
      width: 100%;
      padding: 8px;
      margin: 5px 0 10px 0;
      border: 1px solid #ccc;
      border-radius: 4px;
      box-sizing: border-box;
    }
    .update-form input[type="radio"] {
      margin-right: 5px;
    }
    .update-form input[type="submit"] {
      background: #007bff;
      color: white;
      padding: 10px 20px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      margin-top: 10px;
    }
    .update-form input[type="submit"]:hover {
      background: #0056b3;
    }
    .error {
      color: red;
      margin-bottom: 10px;
    }
    .message {
      color: green;
      margin-bottom: 10px;
    }
    /* Responsive design */
    @media (max-width: 768px) {
      .profile-container {
        flex-direction: column;
      }
      .profile-display, .update-form {
        width: 100%;
      }
    }
  </style>
</head>
<body>
<div class="container">
  <% if (request.getAttribute("error") != null) { %>
  <div class="error"><%= request.getAttribute("error") %></div>
  <% } %>
  <% if (request.getAttribute("message") != null) { %>
  <div class="message"><%= request.getAttribute("message") %></div>
  <% } %>

  <h1>Group Profile</h1>
  <div class="profile-container">
    <div class="profile-display">
      <% GroupProfileDTO group = (GroupProfileDTO) request.getAttribute("group"); %>
      <% if (group != null) { %>
      <% if (group.getAvatarPath() != null && !group.getAvatarPath().isEmpty()) { %>
      <img src="<%= group.getAvatarPath() %>" alt="Group Avatar" width="100">
      <% } %>
      <p><strong>Name:</strong> <%= group.getGroupName() %></p>
      <p><strong>Description:</strong> <%= group.getDescription() %></p>
      <p><strong>Status:</strong> <%= group.getStatus() %></p>
      <% } else { %>
      <p>No group information available.</p>
      <% } %>
    </div>
    <div class="update-form">
      <h2>Update Group Profile</h2>
      <form action="<%= request.getContextPath() %>/groupProfile" method="post" enctype="multipart/form-data">
        <input type="hidden" name="groupId" value="<%= group != null ? group.getGroupId() : "" %>">
        <label for="groupName">Group Name:</label>
        <input type="text" id="groupName" name="groupName" value="<%= group != null ? group.getGroupName() : "" %>" required>
        <label for="description">Description:</label>
        <textarea id="description" name="description"><%= group != null ? group.getDescription() : "" %></textarea>
        <label>Status:</label>
        <div>
          <input type="radio" id="deleted" name="status" value="deleted" <% if (group != null && group.getStatus().equals("deleted")) { %>checked<% } %>>
          <label for="deleted">Deleted</label>
          <input type="radio" id="banned" name="status" value="banned" <% if (group != null && group.getStatus().equals("banned")) { %>checked<% } %>>
          <label for="banned">Banned</label>
          <input type="radio" id="active" name="status" value="active" <% if (group != null && group.getStatus().equals("active")) { %>checked<% } %>>
          <label for="active">Active</label>
        </div>
        <label for="avatar">Avatar:</label>
        <input type="file" id="avatar" name="avatar">
        <input type="submit" value="Update">
      </form>
    </div>
  </div>
</div>
</body>

