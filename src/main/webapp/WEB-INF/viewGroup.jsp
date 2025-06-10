<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="dto.ResGroupDTO" %>

<%-- Lấy đối tượng 'group' từ request và ép kiểu --%>
<%
    ResGroupDTO group = (ResGroupDTO) request.getAttribute("group");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>
        <% if (group != null) { %>
        <%= group.getName() %>
        <% } else { %>
        Group Not Found
        <% } %>
    </title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap');

        body {
            font-family: 'Poppins', sans-serif;
            background-color: #f0f2f5;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
        }

        .container {
            background-color: #ffffff;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 550px;
            box-sizing: border-box;
        }

        h1 {
            text-align: center;
            color: #1c1e21;
            margin-top: 0;
            margin-bottom: 10px;
        }

        .subtitle {
            text-align: center;
            color: #606770;
            margin-bottom: 30px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            font-weight: 600;
            color: #333;
            margin-bottom: 8px;
        }

        input[type="text"],
        textarea,
        input[type="file"] {
            width: 100%;
            padding: 12px;
            border: 1px solid #dddfe2;
            border-radius: 6px;
            font-size: 16px;
            box-sizing: border-box;
            transition: border-color 0.3s;
        }

        input[type="text"]:focus,
        textarea:focus {
            outline: none;
            border-color: #1877f2;
            box-shadow: 0 0 0 2px rgba(24, 119, 242, 0.2);
        }

        textarea {
            resize: vertical; /* Allow vertical resizing */
        }

        small {
            display: block;
            margin-top: 5px;
            color: #606770;
            font-size: 13px;
        }

        .btn-create {
            width: 100%;
            padding: 15px;
            background-color: #1877f2;
            border: none;
            border-radius: 6px;
            color: #ffffff;
            font-size: 18px;
            font-weight: 600;
            cursor: pointer;
            transition: background-color 0.3s;
        }

        .btn-create:hover {
            background-color: #166fe5;
        }

        .success-message {
            padding: 15px;
            background-color: #e9f6ec;
            color: #0b822a;
            border: 1px solid #bce2c7;
            border-radius: 6px;
            text-align: center;
            margin-bottom: 20px;
        }

        /* --- Styles for View Group Page --- */
        .main-content {
            width: 100%;
            max-width: 850px;
            margin: 20px auto;
        }

        .group-container {
            background-color: #ffffff;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.12);
            overflow: hidden; /* Important for border-radius on children */
        }

        .group-header {
            position: relative;
            height: 350px;
        }

        .cover-photo {
            width: 100%;
            height: 100%;
            object-fit: cover; /* Ensures the image covers the area without distortion */
        }

        .group-info-overlay {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            padding: 20px 30px;
            color: #ffffff;
            background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0) 100%);
        }

        .group-info-overlay h1 {
            text-align: left;
            color: #ffffff;
            margin: 0 0 5px 0;
            font-size: 2.5em;
            text-shadow: 1px 1px 3px rgba(0,0,0,0.5);
        }

        .group-stats {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 1em;
        }

        .group-stats .separator {
            opacity: 0.7;
        }

        .group-status {
            padding: 4px 8px;
            border-radius: 4px;
            font-weight: 600;
            font-size: 0.8em;
        }

        .group-status.public {
            background-color: rgba(45, 136, 255, 0.8);
        }

        .group-status.private {
            background-color: rgba(108, 117, 125, 0.8);
        }

        .group-body {
            padding: 20px 30px;
        }

        .group-details h2 {
            font-size: 1.5em;
            color: #1c1e21;
            border-bottom: 1px solid #e9ebee;
            padding-bottom: 10px;
            margin-bottom: 15px;
        }

        .group-details .description {
            font-size: 1.1em;
            line-height: 1.6;
            color: #333;
        }

        .detail-item {
            margin-top: 25px;
        }

        .detail-item h3 {
            font-size: 1.1em;
            color: #606770;
            margin-bottom: 5px;
        }
        .detail-item p {
            font-size: 1em;
            color: #1c1e21;
            margin: 0;
        }

        /* --- Styles for No Data Page --- */
        .no-data-container {
            text-align: center;
            padding: 50px;
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.12);
        }

        .no-data-container h1 {
            color: #dc3545;
        }

        .no-data-container p {
            color: #606770;
            font-size: 1.1em;
        }

        .btn-link {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 20px;
            background-color: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            font-weight: 500;
        }

        .btn-link:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>

<div class="main-content">
    <% if (group == null) { %>
    <div class="no-data-container">
        <h1>Oops! Group Not Found</h1>
        <p>The group you are looking for does not exist or has been removed.</p>
        <a href="createGroup.jsp" class="btn-link">Create a New Group</a>
    </div>
    <% } else { %>
    <div class="group-container">
        <div class="group-header">
            <img src="<%= group.getImage() %>" alt="Group Cover Photo" class="cover-photo">
            <div class="group-info-overlay">
                <h1><%= group.getName() %></h1>
                <div class="group-stats">
                    <span class="group-status"><%= group.getStatus() %> Group</span>
                    <span class="separator">•</span>
                    <span><strong><%= NumberFormat.getInstance(Locale.US).format(group.getNumberParticipants()) %></strong> members</span>
                    <span class="separator">•</span>
                    <!-- 7. Post Count -->
                    <span><strong><%= group.getNumberPosts() %></strong> posts</span>
                </div>
            </div>
        </div>

        <div class="group-body">
            <div class="group-details">
                <h2>About this group</h2>
                <!-- 3. Description -->
                <p class="description"><%= group.getDescription() %></p>

                <div class="detail-item">
                    <h3><i class="icon">📅</i> Created On</h3>
                    <p><%= group.getCreateDate() %></p>
                </div>
            </div>
        </div>
    </div>
    <% } %>
</div>

</body>
</html>