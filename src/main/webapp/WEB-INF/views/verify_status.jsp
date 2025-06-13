<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Zust - Email Verification</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 50%, #000000 100%);
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      overflow-x: hidden;
    }
    body::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background:
              radial-gradient(circle at 20% 80%, rgba(255, 165, 0, 0.1) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(255, 140, 0, 0.15) 0%, transparent 50%),
              radial-gradient(circle at 40% 40%, rgba(255, 69, 0, 0.08) 0%, transparent 50%);
      animation: float 20s ease-in-out infinite;
    }
    @keyframes float {
      0%, 100% { transform: translateY(0px) rotate(0deg); }
      50% { transform: translateY(-20px) rotate(180deg); }
    }
    .container {
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(20px);
      border-radius: 24px;
      border: 1px solid rgba(255, 165, 0, 0.2);
      box-shadow:
              0 32px 64px rgba(0, 0, 0, 0.3),
              inset 0 1px 0 rgba(255, 165, 0, 0.1),
              0 0 0 1px rgba(255, 255, 255, 0.1);
      padding: 48px;
      width: 100%;
      max-width: 480px;
      position: relative;
      z-index: 1;
      animation: slideIn 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
      text-align: center;
    }
    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateY(50px) scale(0.95);
      }
      to {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }
    .logo {
      text-align: center;
      margin-bottom: 40px;
    }
    .logo h1 {
      background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 50%, #ffa726 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      font-size: 3.5rem;
      font-weight: 800;
      margin-bottom: 8px;
      letter-spacing: -2px;
    }
    .logo p {
      color: #666;
      font-size: 1.1rem;
      font-weight: 500;
    }
    h1 {
      margin-bottom: 20px;
      font-size: 1.8rem;
      font-weight: 700;
    }
    h1.success {
      color: #16a34a;
    }
    h1.error {
      color: #dc2626;
    }
    .message {
      font-size: 1.2rem;
      margin-bottom: 20px;
      line-height: 1.6;
      color: #666;
    }
    .action-link {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 12px 20px;
      background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
      color: white;
      text-decoration: none;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 600;
      transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    }
    .action-link:hover {
      transform: translateY(-3px);
      box-shadow: 0 20px 40px rgba(255, 107, 53, 0.4);
    }
    .action-link:active {
      transform: translateY(-1px);
    }
    .secondary-link {
      display: block;
      margin-top: 16px;
      color: #666;
      text-decoration: none;
      font-weight: 600;
      transition: all 0.3s ease;
    }
    .secondary-link:hover {
      color: #ff6b35;
    }
    @media (max-width: 480px) {
      .container {
        margin: 20px;
        padding: 32px 24px;
      }
      .logo h1 {
        font-size: 2.8rem;
      }
    }
  </style>
</head>
<body>
<div class="container">
  <div class="logo">
    <h1>Zust</h1>
    <p>Connect, Share, Discover</p>
  </div>
  <c:if test="${isSuccess}">
    <h1 class="success">Verification Successful!</h1>
    <p class="message"><c:out value="${statusMessage}"/></p>
    <a href="${pageContext.request.contextPath}/auth" class="action-link">Go to Login</a>
  </c:if>
  <c:if test="${!isSuccess}">
    <h1 class="error">Verification Failed!</h1>
    <p class="message"><c:out value="${statusMessage}"/></p>
    <a href="${pageContext.request.contextPath}/auth" class="action-link">Try Registering Again</a>
    <a href="${pageContext.request.contextPath}/auth" class="secondary-link">Go to Login</a>
  </c:if>
</div>
</body>
</html>
