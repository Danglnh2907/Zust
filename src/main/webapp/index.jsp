<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Social Media App</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            margin-top: 50px;
            background-color: #f4f7f6;
            color: #333;
        }

        .header {
            background-color: #007bff;
            color: white;
            padding: 15px;
            border-radius: 8px 8px 0 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .header h1 {
            margin: 0;
        }

        .nav-links a {
            color: white;
            margin-left: 20px;
            text-decoration: none;
            font-weight: bold;
        }

        .nav-links a:hover {
            text-decoration: underline;
        }

        .content {
            background-color: #fff;
            padding: 20px;
            border-radius: 0 0 8px 8px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            max-width: 800px;
            margin: auto;
        }

        .welcome-message {
            font-size: 1.5em;
            margin-bottom: 20px;
        }

        .action-links a {
            display: inline-block;
            padding: 10px 20px;
            background-color: #28a745;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin: 10px;
        }

        .action-links a:hover {
            background-color: #218838;
        }

        .auth-links a {
            color: #007bff;
            text-decoration: none;
            margin: 0 5px;
        }

        .auth-links a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
<div class="header">
    <h1>My Social App</h1>
    <div class="nav-links">
        <c:if test="${loggedInAccount != null}">
            <span>Hello, ${loggedInAccount.fullname}</span>
            <a href="search">Search</a>
            <a href="logout">Logout</a>
        </c:if>
        <c:if test="${loggedInAccount == null}">
            <a href="login">Login</a>
            <a href="register">Register</a>
        </c:if>
    </div>
</div>
<div class="content">
    <div class="welcome-message">
        <c:choose>
            <c:when test="${loggedInAccount != null}">
                <p>Welcome back, ${loggedInAccount.fullname}!</p>
            </c:when>
            <c:otherwise>
                <p>Welcome to our Social Media App!</p>
                <p>Please log in or register to continue.</p>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="action-links">
        <c:if test="${loggedInAccount != null}">
            <a href="search">Start Searching</a>
            <!-- Add other links for logged-in users -->
        </c:if>
        <c:if test="${loggedInAccount == null}">
            <a href="login">Login Now</a>
            <a href="register">Register for Free</a>
        </c:if>
    </div>
</div>
</body>
</html>
