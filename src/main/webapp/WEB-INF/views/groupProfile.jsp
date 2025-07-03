<%--
  Created by IntelliJ IDEA.
  User: thang
  Date: 6/18/2025
  Time: 5:44 PM
  Refactored for a modern UI.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="dto.GroupProfileDTO" %>
<%@ page import="dto.ResGroupDTO" %>
<%@ page import="model.Account" %>
<%
    // Get the group object from the request. It might be null.
    GroupProfileDTO group = (GroupProfileDTO) request.getAttribute("group");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zust - Edit Group Profile</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/updateprofilegroup.css">
</head>
<body>
<div class="container">

    <header class="page-header">
        <a href="#" class="logo">Zust</a>
    </header>

    <div class="back-link-container">
        <a href="${pageContext.request.contextPath}/groupManager?groupId=1" class="back-link">
            <i class="fas fa-arrow-left"></i>
            <span>Back</span>
        </a>
    </div>

    <%-- Display error or success messages if they exist --%>
    <% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-error"><%= request.getAttribute("error") %></div>
    <% } %>
    <% if (request.getAttribute("message") != null) { %>
    <div class="alert alert-message"><%= request.getAttribute("message") %></div>
    <% } %>

    <div class="profile-layout">
        <!-- Profile Display Card (Left Side) -->
        <div class="card profile-display">
            <h2>Current Profile</h2>
            <% if (group != null) { %>
            <div class="avatar-wrapper">
                <% if (group.getAvatarPath() != null && !group.getAvatarPath().isEmpty()) { %>
                <img src="<%= request.getContextPath() + "/static/images/" + group.getAvatarPath() %>" alt="Group Avatar" class="group-avatar" />

                <% } else { %>
                <div class="no-avatar">No Avatar</div>
                <% } %>
            </div>
            <div class="profile-info">
                <p><strong>Group Name:</strong> <%= group.getGroupName() %></p>
                <p><strong>Description:</strong> <%= group.getDescription() != null ? group.getDescription() : "N/A" %></p>
                <p><strong>Status:</strong> <%= group.getStatus() %></p>
            </div>
            <% } else { %>
            <p>No group information available.</p>
            <% } %>
        </div>

        <!-- Update Form Card (Right Side) -->
        <div class="card update-form">
            <h2>Update Group Profile</h2>
            <form action="<%= request.getContextPath() %>/groupProfile" method="post" enctype="multipart/form-data">
                <%-- Hidden field to pass the group ID --%>
                <input type="hidden" name="groupId" value="<%= group != null ? group.getGroupId() : "" %>">

                <div class="form-group">
                    <label for="groupName">Group Name</label>
                    <input type="text" id="groupName" name="groupName" value="<%= group != null ? group.getGroupName() : "" %>" required>
                </div>

                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea id="description" name="description" rows="4"><%= group != null ? group.getDescription() : "" %></textarea>
                </div>

                <div class="form-group">
                    <label>Status</label>
                    <div class="radio-group">
                        <label for="active">
                            <input type="radio" id="active" name="status" value="active" <% if (group != null && "active".equals(group.getStatus())) { %>checked<% } %>>
                            <span class="radio-custom"></span>
                            Active
                        </label>
                        <label for="banned">
                            <input type="radio" id="banned" name="status" value="banned" <% if (group != null && "banned".equals(group.getStatus())) { %>checked<% } %>>
                            <span class="radio-custom"></span>
                            Banned
                        </label>
                        <label for="deleted">
                            <input type="radio" id="deleted" name="status" value="deleted" <% if (group != null && "deleted".equals(group.getStatus())) { %>checked<% } %>>
                            <span class="radio-custom"></span>
                            Deleted
                        </label>
                    </div>
                </div>

                <div class="form-group">
                    <label for="avatar">Change Avatar</label>
                    <input type="file" id="avatar" name="avatar" accept="image/*">
                </div>

                <div class="form-group">
                    <input type="submit" value="Update Profile" class="btn-submit">
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>