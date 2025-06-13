<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Zust - Search Results</title>
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
      max-width: 800px;
      position: relative;
      z-index: 1;
      animation: slideIn 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
      margin: 20px;
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
    h1, h2 {
      text-align: center;
      color: #333;
      margin-bottom: 24px;
    }
    h1 {
      font-size: 2.5rem;
      font-weight: 700;
    }
    h2 {
      font-size: 1.8rem;
      font-weight: 600;
    }
    .search-form {
      display: flex;
      gap: 16px;
      margin-bottom: 32px;
    }
    .search-form input[type="text"] {
      flex-grow: 1;
      padding: 16px 20px;
      border: 2px solid #e0e0e0;
      border-radius: 12px;
      font-size: 16px;
      transition: all 0.3s ease;
      background: #fafafa;
      color: #333;
    }
    .search-form input[type="text"]::placeholder {
      color: #999;
    }
    .search-form input[type="text"]:focus {
      outline: none;
      border-color: #ff6b35;
      background: white;
      box-shadow: 0 0 0 4px rgba(255, 107, 53, 0.1);
      transform: translateY(-2px);
    }
    .search-form button {
      padding: 16px 24px;
      border: none;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
      color: white;
      transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    }
    .search-form button:hover {
      transform: translateY(-3px);
      box-shadow: 0 20px 40px rgba(255, 107, 53, 0.4);
    }
    .search-form button:active {
      transform: translateY(-1px);
    }
    .error-message {
      color: #dc2626;
      font-size: 1.2rem;
      margin-bottom: 24px;
      text-align: center;
      background: rgba(239, 68, 68, 0.1);
      padding: 16px;
      border-radius: 12px;
      border: 1px solid rgba(239, 68, 68, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
    }
    .results-section {
      margin-top: 32px;
      border-top: 1px solid #e0e0e0;
      padding-top: 32px;
    }
    .result-group {
      margin-bottom: 40px;
    }
    .result-group h3 {
      background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
      color: white;
      padding: 12px 20px;
      border-radius: 12px;
      margin-bottom: 24px;
      font-size: 1.4rem;
      font-weight: 600;
    }
    .result-item {
      background: rgba(255, 255, 255, 0.95);
      padding: 20px;
      border-radius: 12px;
      margin-bottom: 16px;
      border: 1px solid rgba(255, 165, 0, 0.2);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
      transition: transform 0.3s ease;
    }
    .result-item:hover {
      transform: translateY(-4px);
    }
    .result-item h4 {
      margin: 0 0 8px;
      color: #333;
      font-size: 1.2rem;
      font-weight: 600;
    }
    .result-item p {
      color: #666;
      margin-bottom: 8px;
      line-height: 1.6;
    }
    .result-item small {
      color: #999;
      font-size: 0.9rem;
    }
    .no-results {
      color: #666;
      text-align: center;
      font-size: 1.1rem;
      padding: 20px;
      background: rgba(255, 255, 255, 0.95);
      border-radius: 12px;
      border: 1px solid #e0e0e0;
    }
    .back-link {
      text-align: center;
      margin-top: 32px;
    }
    .back-link a {
      color: #ff6b35;
      text-decoration: none;
      font-weight: 600;
      padding: 12px 20px;
      border-radius: 8px;
      transition: all 0.3s ease;
    }
    .back-link a:hover {
      background: #f5f5f5;
      color: #ff8c42;
      transform: translateY(-2px);
    }
    @media (max-width: 480px) {
      .container {
        margin: 16px;
        padding: 32px 24px;
      }
      .logo h1 {
        font-size: 2.8rem;
      }
      .search-form {
        flex-direction: column;
        gap: 12px;
      }
      .search-form input[type="text"],
      .search-form button {
        width: 100%;
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
  <h1>Search Social Media</h1>

  <form action="${pageContext.request.contextPath}/search" method="GET" class="search-form">
    <input type="text" name="keyword" placeholder="Enter keyword..." value="${keyword != null ? keyword : ''}">
    <button type="submit">Search</button>
  </form>

  <c:if test="${not empty errorMessage}">
    <p class="error-message"><span>❌</span>${errorMessage}</p>
  </c:if>

  <c:if test="${not empty keyword && empty errorMessage}">
    <div class="results-section">
      <h2>Results for "${keyword}"</h2>

      <c:set var="anyResultsFound" value="${false}" />

        <%-- Users Section --%>
      <div class="result-group">
        <h3>Users (${searchResults.users.size()})</h3>
        <c:choose>
          <c:when test="${not empty searchResults.users}">
            <c:set var="anyResultsFound" value="${true}" />
            <c:forEach var="account" items="${searchResults.users}">
              <div class="result-item">
                <h4>${account.fullname} (@${account.username})</h4>
                <p>Email: ${account.email}</p>
                <c:if test="${not empty account.bio}">
                  <p>Bio: ${account.bio}</p>
                </c:if>
              </div>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <p class="no-results">No users found.</p>
          </c:otherwise>
        </c:choose>
      </div>

        <%-- Posts by Content Section --%>
      <div class="result-group">
        <h3>Posts (by Content) (${searchResults.posts_content.size()})</h3>
        <c:choose>
          <c:when test="${not empty searchResults.posts_content}">
            <c:set var="anyResultsFound" value="${true}" />
            <c:forEach var="post" items="${searchResults.posts_content}">
              <div class="result-item">
                <h4>Post ID: ${post.id} <c:if test="${post.account != null}"> by ${post.account.username}</c:if></h4>
                <p>${post.postContent}</p>
                <c:if test="${post.group != null}">
                  <p>In Group: ${post.group.groupName}</p>
                </c:if>
                <small>Created: ${post.postCreateDate}</small>
              </div>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <p class="no-results">No posts found by content.</p>
          </c:otherwise>
        </c:choose>
      </div>

        <%-- Posts by Hashtag Section --%>
      <div class="result-group">
        <h3>Posts (by Hashtag) (${searchResults.posts_hashtag.size()})</h3>
        <c:choose>
          <c:when test="${not empty searchResults.posts_hashtag}">
            <c:set var="anyResultsFound" value="${true}" />
            <c:forEach var="post" items="${searchResults.posts_hashtag}">
              <div class="result-item">
                <h4>Post ID: ${post.id} <c:if test="${post.account != null}"> by ${post.account.username}</c:if></h4>
                <p>${post.postContent}</p>
                <c:if test="${post.group != null}">
                  <p>In Group: ${post.group.groupName}</p>
                </c:if>
                <small>Created: ${post.postCreateDate}</small>
              </div>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <p class="no-results">No posts found by hashtag.</p>
          </c:otherwise>
        </c:choose>
      </div>

        <%-- Groups Section --%>
      <div class="result-group">
        <h3>Groups (${searchResults.groups.size()})</h3>
        <c:choose>
          <c:when test="${not empty searchResults.groups}">
            <c:set var="anyResultsFound" value="${true}" />
            <c:forEach var="group" items="${searchResults.groups}">
              <div class="result-item">
                <h4>${group.groupName}</h4>
                <p>${group.groupDescription}</p>
                <small>Created: ${group.groupCreateDate}</small>
              </div>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <p class="no-results">No groups found.</p>
          </c:otherwise>
        </c:choose>
      </div>

      <c:if test="${!anyResultsFound}">
        <p class="no-results">No results found for "${keyword}" across all categories.</p>
      </c:if>
    </div>
  </c:if>
  <div class="back-link">
    <a href="${pageContext.request.contextPath}/">Back to Home</a>
  </div>
</div>
</body>
</html>
