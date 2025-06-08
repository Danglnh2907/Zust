<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Search Results</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f7f6; }
    .container { max-width: 800px; margin: auto; background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
    h1, h2 { text-align: center; color: #333; }
    .search-form { display: flex; gap: 10px; margin-bottom: 20px; }
    .search-form input[type="text"] { flex-grow: 1; padding: 10px; border: 1px solid #ccc; border-radius: 4px; }
    .search-form button { padding: 10px 15px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .search-form button:hover { background-color: #0056b3; }
    .error-message { color: red; margin-top: 10px; text-align: center; }
    .results-section { margin-top: 20px; border-top: 1px solid #eee; padding-top: 20px; }
    .result-group { margin-bottom: 30px; }
    .result-group h3 { background-color: #e9ecef; padding: 8px 15px; border-radius: 4px; margin-bottom: 15px; color: #007bff; }
    .result-item { background-color: #f8f9fa; padding: 10px; border-radius: 4px; margin-bottom: 10px; border: 1px solid #eee; }
    .result-item h4 { margin-top: 0; margin-bottom: 5px; color: #333; }
    .no-results { color: #666; text-align: center; }
    .back-link { display: block; text-align: center; margin-top: 20px; }
    .back-link a { color: #007bff; text-decoration: none; }
    .back-link a:hover { text-decoration: underline; }
  </style>
</head>
<body>
<div class="container">
  <h1>Search Social Media</h1>

  <form action="${pageContext.request.contextPath}/search" method="GET" class="search-form">
    <input type="text" name="keyword" placeholder="Enter keyword..." value="${keyword != null ? keyword : ''}">
    <button type="submit">Search</button>
  </form>

  <c:if test="${not empty errorMessage}">
    <p class="error-message">${errorMessage}</p>
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
