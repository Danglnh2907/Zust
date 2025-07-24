<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Group" %>
<%
    // Get the group object from the request. It might be null.
    Group group = (Group) request.getAttribute("group");
%>

<!-- EDIT GROUP PROFILE PAGE (GROUP MANAGER) -->

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
        <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>" class="back-link">
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
                <% if (group.getGroupCoverImage() != null && !group.getGroupCoverImage().isEmpty()) { %>
                <img src="<%= request.getContextPath() + "/static/images/" + group.getGroupCoverImage() %>"
                     alt="Group Avatar" class="group-avatar" />
                <% } else { %>
                <div class="no-avatar">No Avatar</div>
                <% } %>
            </div>
            <div class="profile-info">
                <p><strong>Group Name:</strong> <%= group.getGroupName() %></p>
                <p><strong>Description:</strong> <%= group.getGroupDescription() != null ? group.getGroupDescription() : "N/A" %></p>
            </div>
            <% } else { %>
            <p>No group information available.</p>
            <% } %>
        </div>

        <!-- Update Form Card (Right Side) -->
        <div class="card update-form">
            <h2>Update Group Profile</h2>
            <form action="<%= request.getContextPath() %>/group?action=edit" method="post" enctype="multipart/form-data">
                <%-- Hidden field to pass the group ID --%>
                <input type="hidden" name="groupId" value="<%= group.getId() %>">

                <div class="form-group">
                    <label for="groupName">Group Name</label>
                    <input type="text" id="groupName" name="groupName" value="<%= group.getGroupName() %>" required>
                </div>

                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea id="description" name="description" rows="4"><%= group.getGroupDescription() %></textarea>
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