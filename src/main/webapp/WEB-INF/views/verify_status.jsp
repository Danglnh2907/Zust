<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Email Verification Status</title>
  <style>
    body { font-family: Arial, sans-serif; background-color: #f4f7f6; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
    .status-container { background-color: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); width: 400px; text-align: center; }
    h1 { margin-bottom: 20px; }
    .success-message { color: green; font-size: 1.2em; margin-bottom: 20px; }
    .error-message { color: red; font-size: 1.2em; margin-bottom: 20px; }
    .action-link { display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; font-size: 16px; }
    .action-link:hover { background-color: #0056b3; }
  </style>
</head>
<body>
<div class="status-container">
  <c:if test="${isSuccess}">
    <h1 style="color: green;">Verification Successful!</h1>
    <p class="success-message">${statusMessage}</p>
    <a href="${pageContext.request.contextPath}/login" class="action-link">Go to Login</a>
  </c:if>
  <c:if test="${!isSuccess}">
    <h1 style="color: red;">Verification Failed!</h1>
    <p class="error-message">${statusMessage}</p>
    <a href="${pageContext.request.contextPath}/register" class="action-link">Try Registering Again</a>
    <p>Or <a href="${pageContext.request.contextPath}/login">Go to Login</a></p>
  </c:if>
</div>
</body>
</html>
