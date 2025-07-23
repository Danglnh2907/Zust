<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Change Password</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/change_password.css">
</head>
<body>
    <div class="container">
        <div class="form-container">
            <h1>Change Your Password</h1>
            <p>To protect your account, choose a strong password that you haven't used before.</p>

            <% 
                String error = (String) request.getAttribute("error");
                String success = (String) request.getAttribute("success");
                if (error != null) {
            %>
                <div id="message-container" class="message-error"><%= error %></div>
            <% } else if (success != null) { %>
                <div id="message-container" class="message-success"><%= success %></div>
            <% } else { %>
                <div id="message-container"></div>
            <% } %>

            <form id="changePasswordForm" action="profile?action=change_password" method="post">
                <div class="form-group">
                    <label for="current-password">Current Password</label>
                    <input type="password" id="current-password" name="currentPassword" required>
                </div>

                <div class="form-group">
                    <label for="new-password">New Password</label>
                    <input type="password" id="new-password" name="newPassword" required>
                    <small class="password-requirements">Must be at least 8 characters long and include an uppercase letter, a lowercase letter, and a number.</small>
                </div>

                <div class="form-group">
                    <label for="confirm-password">Confirm New Password</label>
                    <input type="password" id="confirm-password" name="confirmPassword" required>
                </div>

                <button type="submit" class="submit-btn">Update Password</button>
            </form>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/change_password.js"></script>
</body>
</html>