<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f7f6; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
        .register-container { background-color: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); width: 400px; text-align: center; }
        h1 { color: #333; margin-bottom: 20px; }
        .form-group { margin-bottom: 15px; text-align: left; }
        .form-group label { display: block; margin-bottom: 5px; color: #555; }
        .form-group input[type="text"],
        .form-group input[type="password"],
        .form-group input[type="email"],
        .form-group input[type="tel"],
        .form-group input[type="date"],
        .form-group textarea,
        .form-group select,
        .form-group input[type="file"] { /* Added file input */
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        .form-group input[type="radio"] { margin-right: 5px; }
        .btn-primary { width: 100%; padding: 10px; background-color: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; margin-top: 10px; }
        .btn-primary:hover { background-color: #218838; }
        .error-message { color: red; margin-top: 10px; }
        .login-link { margin-top: 20px; font-size: 0.9em; }
        .login-link a { color: #007bff; text-decoration: none; }
        .login-link a:hover { text-decoration: underline; }
    </style>
</head>
<body>
<div class="register-container">
    <h1>Register New Account</h1>
    <c:if test="${not empty errorMessage}">
        <p class="error-message">${errorMessage}</p>
    </c:if>

    <form action="${pageContext.request.contextPath}/register" method="POST" enctype="multipart/form-data">
        <div class="form-group">
            <label for="username">Username: <span style="color: red;">*</span></label>
            <input type="text" id="username" name="username" value="${account.username != null ? account.username : ''}" required>
        </div>
        <div class="form-group">
            <label for="password">Password: <span style="color: red;">*</span></label>
            <input type="password" id="password" name="password" required>
        </div>
        <div class="form-group">
            <label for="fullname">Full Name: <span style="color: red;">*</span></label>
            <input type="text" id="fullname" name="fullname" value="${account.fullname != null ? account.fullname : ''}" required>
        </div>
        <div class="form-group">
            <label for="email">Email: <span style="color: red;">*</span></label>
            <input type="email" id="email" name="email" value="${account.email != null ? account.email : ''}" required>
        </div>
        <div class="form-group">
            <label for="phone">Phone (optional):</label>
            <input type="tel" id="phone" name="phone" value="${account.phone != null ? account.phone : ''}">
        </div>
        <div class="form-group">
            <label>Gender (optional):</label>
            <input type="radio" id="male" name="gender" value="false" <c:if test="${account.gender != null && !account.gender}">checked</c:if>>
            <label for="male">Male</label>
            <input type="radio" id="female" name="gender" value="true" <c:if test="${account.gender != null && account.gender}">checked</c:if>>
            <label for="female">Female</label>
        </div>
        <div class="form-group">
            <label for="dob">Date of Birth (optional):</label>
            <input type="date" id="dob" name="dob" value="${account.dob != null ? account.dob : ''}">
        </div>
        <div class="form-group">
            <label for="avatar">Profile Picture (optional):</label>
            <input type="file" id="avatar" name="avatar" accept="image/*">
        </div>
        <div class="form-group">
            <label for="bio">Bio (optional):</label>
            <textarea id="bio" name="bio" rows="3">${account.bio != null ? account.bio : ''}</textarea>
        </div>
        <button type="submit" class="btn-primary">Register</button>
    </form>
    <div class="login-link">
        Already have an account? <a href="${pageContext.request.contextPath}/login">Login here</a>
    </div>
</div>
</body>
</html>
