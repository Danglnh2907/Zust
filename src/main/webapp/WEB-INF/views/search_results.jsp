<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="h" uri="http://zust.com/functions" %>
<%@ page import="model.Account, model.Post, model.Group" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Search Results - Zust</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
        rel="stylesheet"
        integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
        crossorigin="anonymous">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">

  <style>
    body {
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
      background: #f8f9fa;
    }

    .search-results-page {
      max-width: 800px;
      margin: 0 auto;
      padding: 20px;
      background: white;
      min-height: 100vh;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }

    .post-content-preview {
      background: #f8f9fa;
      border-left: 3px solid #1877f2;
      padding: 12px;
      border-radius: 0 6px 6px 0;
      margin-top: 8px;
      font-style: italic;
      color: #65676b;
      line-height: 1.4;
    }

    .post-html-badge {
      display: inline-block;
      background: #e3f2fd;
      color: #1976d2;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 10px;
      font-weight: 500;
      margin-left: 8px;
      vertical-align: top;
    }

    .content-toggle {
      color: #1877f2;
      cursor: pointer;
      text-decoration: none;
      font-size: 12px;
      margin-top: 8px;
      display: inline-block;
      padding: 4px 8px;
      border: 1px solid #1877f2;
      border-radius: 4px;
      transition: all 0.2s ease;
    }

    .content-toggle:hover {
      background: #1877f2;
      color: white;
      text-decoration: none;
    }

    .html-content {
      display: none;
      margin-top: 12px;
      padding: 12px;
      background: #ffffff;
      border: 1px solid #e4e6ea;
      border-radius: 6px;
      max-height: 300px;
      overflow-y: auto;
      box-shadow: inset 0 1px 3px rgba(0,0,0,0.1);
    }

    .html-content.show {
      display: block;
      animation: slideDown 0.3s ease-out;
    }

    @keyframes slideDown {
      from {
        max-height: 0;
        opacity: 0;
      }
      to {
        max-height: 300px;
        opacity: 1;
      }
    }

    .search-form {
      display: flex;
      gap: 12px;
      margin-bottom: 24px;
      padding: 16px;
      background: #f8f9fa;
      border-radius: 8px;
    }

    .search-form input[type="text"] {
      flex-grow: 1;
      padding: 12px 16px;
      border: 2px solid #e4e6ea;
      border-radius: 8px;
      font-size: 15px;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      transition: all 0.2s ease;
    }

    .search-form input[type="text"]:focus {
      outline: none;
      border-color: #1877f2;
      box-shadow: 0 0 0 2px rgba(24, 119, 242, 0.2);
    }

    .search-form button {
      padding: 12px 20px;
      background: #1877f2;
      color: white;
      border: none;
      border-radius: 8px;
      font-weight: 600;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      cursor: pointer;
      transition: background 0.2s ease;
    }

    .search-form button:hover {
      background: #166fe5;
    }

    .search-header {
      border-bottom: 1px solid #e4e6ea;
      padding-bottom: 16px;
      margin-bottom: 24px;
    }

    .search-title {
      color: #1c1e21;
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
    }

    .search-subtitle {
      color: #65676b;
      font-size: 15px;
    }

    .results-category {
      margin-bottom: 32px;
    }

    .category-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid #e4e6ea;
    }

    .category-title {
      color: #1c1e21;
      font-size: 18px;
      font-weight: 600;
      display: flex;
      align-items: center;
      margin: 0;
    }

    .category-title i {
      margin-right: 8px;
      color: #1877f2;
    }

    .category-count {
      color: #65676b;
      font-size: 14px;
      background: #f0f2f5;
      padding: 4px 8px;
      border-radius: 12px;
    }

    .view-more-link {
      color: #1877f2;
      text-decoration: none;
      font-size: 14px;
      font-weight: 500;
      padding: 6px 12px;
      border-radius: 6px;
      transition: background 0.2s ease;
    }

    .view-more-link:hover {
      background: #f0f2f5;
      text-decoration: none;
      color: #1877f2;
    }

    .result-item-large {
      background: #f8f9fa;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;
      border: 1px solid #e4e6ea;
      transition: all 0.2s ease;
    }

    .result-item-large:hover {
      background: #e9ecef;
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    }

    .result-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      object-fit: cover;
      background: #e4e6ea;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #65676b;
      font-size: 18px;
      flex-shrink: 0;
    }

    .result-avatar.group {
      border-radius: 8px;
    }

    .result-title {
      font-size: 18px;
      font-weight: 600;
      color: #1c1e21;
      margin-bottom: 4px;
    }

    .result-subtitle {
      font-size: 14px;
      color: #65676b;
      margin-bottom: 8px;
    }

    .result-content {
      font-size: 15px;
      color: #1c1e21;
      line-height: 1.4;
    }

    .back-link {
      color: #1877f2;
      text-decoration: none;
      font-weight: 500;
      display: inline-flex;
      align-items: center;
      margin-bottom: 20px;
      padding: 8px 12px;
      border-radius: 6px;
      transition: background 0.2s ease;
    }

    .back-link:hover {
      background: #f0f2f5;
      text-decoration: none;
      color: #1877f2;
    }

    .back-link i {
      margin-right: 8px;
    }

    .no-results {
      text-align: center;
      padding: 40px 20px;
      color: #65676b;
      font-size: 16px;
      background: #f8f9fa;
      border-radius: 8px;
      border: 1px solid #e4e6ea;
    }

    .no-results i {
      font-size: 24px;
      margin-bottom: 12px;
      display: block;
    }

    .badge {
      font-size: 11px;
      padding: 4px 8px;
    }
  </style>
</head>
<body>
<div class="search-results-page">
  <a href="${pageContext.request.contextPath}/post" class="back-link">
    <i class="fas fa-arrow-left"></i>
    Back to Feed
  </a>

  <div class="search-header">
    <h1 class="search-title">
      <i class="fas fa-search" style="margin-right: 12px; color: #1877f2;"></i>
      Search Results
    </h1>
    <c:if test="${not empty keyword}">
      <p class="search-subtitle">
        Search results for "<c:out value="${keyword}"/>"
      </p>
    </c:if>
  </div>

  <!-- Search Form -->
  <form action="${pageContext.request.contextPath}/search" method="GET" class="search-form">
    <input type="text" name="keyword" placeholder="Search users, posts, groups..." value="${keyword != null ? keyword : ''}">
    <button type="submit">
      <i class="fas fa-search"></i> Search
    </button>
  </form>

  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger">
      <i class="fas fa-exclamation-triangle"></i>
        ${errorMessage}
    </div>
  </c:if>

  <c:choose>
    <c:when test="${not empty keyword}">
      <div class="search-results">
        <c:set var="anyResultsFound" value="${false}" />

        <!-- Users Section -->
        <div class="results-category">
          <div class="category-header">
            <h3 class="category-title">
              <i class="fas fa-user"></i>People
            </h3>
            <div class="d-flex align-items-center gap-2">
              <span class="category-count">${fn:length(searchResults.users)} results</span>
              <c:if test="${fn:length(searchResults.users) > 0}">
                <a href="${pageContext.request.contextPath}/search?action=viewMore&keyword=${keyword}&category=users" class="view-more-link">
                  View all
                </a>
              </c:if>
            </div>
          </div>
          <c:choose>
            <c:when test="${not empty searchResults.users}">
              <c:set var="anyResultsFound" value="${true}" />
              <c:forEach var="account" items="${searchResults.users}" varStatus="status" end="4">
                <div class="result-item-large">
                  <div class="d-flex align-items-center">
                    <div class="result-avatar me-3">
                      <c:choose>
                        <c:when test="${not empty account.avatar}">
                          <img src="${account.avatar}" alt="${account.fullname}" style="width: 100%; height: 100%; border-radius: 50%; object-fit: cover;">
                        </c:when>
                        <c:otherwise>
                          <i class="fas fa-user"></i>
                        </c:otherwise>
                      </c:choose>
                    </div>
                    <div class="flex-grow-1">
                      <div class="result-title">
                        <a href="${pageContext.request.contextPath}/profile?userId=${account.id}" style="text-decoration: none; color: #1c1e21;">
                          <c:out value="${account.fullname}"/>
                        </a>
                      </div>
                      <div class="result-subtitle">@<c:out value="${account.username}"/> · <c:out value="${account.email}"/></div>
                      <c:if test="${not empty account.bio}">
                        <div class="result-content"><c:out value="${account.bio}"/></div>
                      </c:if>
                    </div>
                  </div>
                </div>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <div class="no-results">
                <i class="fas fa-user-slash"></i>
                No users found for "<c:out value="${keyword}"/>".
              </div>
            </c:otherwise>
          </c:choose>
        </div>

        <!-- Posts by Content Section -->
        <div class="results-category">
          <div class="category-header">
            <h3 class="category-title">
              <i class="fas fa-file-alt"></i>Posts
            </h3>
            <div class="d-flex align-items-center gap-2">
              <span class="category-count">${fn:length(searchResults.posts_content)} results</span>
              <c:if test="${fn:length(searchResults.posts_content) > 0}">
                <a href="${pageContext.request.contextPath}/search?action=viewMore&keyword=${keyword}&category=posts_content" class="view-more-link">
                  View all
                </a>
              </c:if>
            </div>
          </div>
          <c:choose>
            <c:when test="${not empty searchResults.posts_content}">
              <c:set var="anyResultsFound" value="${true}" />
              <c:forEach var="post" items="${searchResults.posts_content}" varStatus="status" end="4">
                <div class="result-item-large">
                  <div class="d-flex align-items-start mb-3">
                    <c:if test="${post.account != null}">
                      <div class="result-avatar me-3" style="width: 36px; height: 36px; font-size: 14px;">
                        <c:choose>
                          <c:when test="${not empty post.account.avatar}">
                            <img src="${post.account.avatar}" alt="${post.account.fullname}"
                                 style="width: 100%; height: 100%; border-radius: 50%; object-fit: cover;">
                          </c:when>
                          <c:otherwise>
                            <i class="fas fa-user"></i>
                          </c:otherwise>
                        </c:choose>
                      </div>
                      <div class="flex-grow-1">
                        <div class="result-title" style="font-size: 16px;">
                          <a href="${pageContext.request.contextPath}/profile?userId=${post.account.id}" style="text-decoration: none; color: #1c1e21;">
                            <c:out value="${post.account.fullname}"/>
                          </a>
                        </div>
                        <div class="result-subtitle">@<c:out value="${post.account.username}"/></div>
                      </div>
                    </c:if>
                  </div>
                  <div class="post-content">
                    <a href="${pageContext.request.contextPath}/post?postId=${post.id}" style="text-decoration: none; color: #1c1e21;">
                      <div class="result-content">
                        <c:set var="isHtmlContent" value="${fn:contains(post.postContent, '<') && fn:contains(post.postContent, '>')}" />
                        <c:choose>
                          <c:when test="${isHtmlContent}">
                            <span class="post-html-badge">
                              <i class="fas fa-code"></i> Rich Text
                            </span>
                            <div class="post-content-preview">
                              <c:out value="${h:getPreview(post.postContent, 150)}" />
                            </div>
                            <a href="#" class="content-toggle" onclick="toggleHtmlContent(this); return false;" data-post-id="${post.id}">
                              Show original formatting
                            </a>
                            <div class="html-content" id="html-content-${post.id}">
                                ${h:sanitizeHtml(post.postContent)}
                            </div>
                          </c:when>
                          <c:otherwise>
                            <c:out value="${h:getPreview(post.postContent, 200)}" />
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </a>
                    <c:if test="${post.group != null}">
                      <small class="text-muted mt-2 d-block">
                        <i class="fas fa-users" style="margin-right: 4px;"></i>
                        Posted in: <c:out value="${post.group.groupName}"/>
                      </small>
                    </c:if>
                  </div>
                </div>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <div class="no-results">
                <i class="fas fa-file-excel"></i>
                No posts found for "<c:out value="${keyword}"/>".
              </div>
            </c:otherwise>
          </c:choose>
        </div>

        <!-- Posts by Hashtag Section -->
        <div class="results-category">
          <div class="category-header">
            <h3 class="category-title">
              <i class="fas fa-hashtag"></i>Posts with hashtags
            </h3>
            <div class="d-flex align-items-center gap-2">
              <span class="category-count">${fn:length(searchResults.posts_hashtag)} results</span>
              <c:if test="${fn:length(searchResults.posts_hashtag) > 0}">
                <a href="${pageContext.request.contextPath}/search?action=viewMore&keyword=${keyword}&category=posts_hashtag" class="view-more-link">
                  View all
                </a>
              </c:if>
            </div>
          </div>
          <c:choose>
            <c:when test="${not empty searchResults.posts_hashtag}">
              <c:set var="anyResultsFound" value="${true}" />
              <c:forEach var="post" items="${searchResults.posts_hashtag}" varStatus="status" end="4">
                <div class="result-item-large">
                  <div class="d-flex align-items-start mb-3">
                    <c:if test="${post.account != null}">
                      <div class="result-avatar me-3" style="width: 36px; height: 36px; font-size: 14px;">
                        <c:choose>
                          <c:when test="${not empty post.account.avatar}">
                            <img src="${post.account.avatar}" alt="${post.account.fullname}"
                                 style="width: 100%; height: 100%; border-radius: 50%; object-fit: cover;">
                          </c:when>
                          <c:otherwise>
                            <i class="fas fa-user"></i>
                          </c:otherwise>
                        </c:choose>
                      </div>
                      <div class="flex-grow-1">
                        <div class="d-flex align-items-center justify-content-between">
                          <div>
                            <div class="result-title" style="font-size: 16px;">
                              <a href="${pageContext.request.contextPath}/profile?userId=${post.account.id}" style="text-decoration: none; color: #1c1e21;">
                                <c:out value="${post.account.fullname}"/>
                              </a>
                            </div>
                            <div class="result-subtitle">@<c:out value="${post.account.username}"/></div>
                          </div>
                          <span class="badge bg-primary"><i class="fas fa-hashtag"></i> hashtag</span>
                        </div>
                      </div>
                    </c:if>
                  </div>
                  <div class="post-content">
                    <a href="${pageContext.request.contextPath}/post?postId=${post.id}" style="text-decoration: none; color: #1c1e21;">
                      <div class="result-content">
                        <c:set var="isHtmlContent" value="${fn:contains(post.postContent, '<') && fn:contains(post.postContent, '>')}" />
                        <c:choose>
                          <c:when test="${isHtmlContent}">
                            <span class="post-html-badge">
                              <i class="fas fa-code"></i> Rich Text
                            </span>
                            <div class="post-content-preview">
                              <c:out value="${h:getPreview(post.postContent, 150)}" />
                            </div>
                            <a href="#" class="content-toggle" onclick="toggleHtmlContent(this); return false;" data-post-id="${post.id}">
                              Show original formatting
                            </a>
                            <div class="html-content" id="html-content-${post.id}">
                                ${h:sanitizeHtml(post.postContent)}
                            </div>
                          </c:when>
                          <c:otherwise>
                            <c:out value="${h:getPreview(post.postContent, 200)}" />
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </a>
                    <c:if test="${post.group != null}">
                      <small class="text-muted mt-2 d-block">
                        <i class="fas fa-users" style="margin-right: 4px;"></i>
                        Posted in: <c:out value="${post.group.groupName}"/>
                      </small>
                    </c:if>
                  </div>
                </div>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <div class="no-results">
                <i class="fas fa-hashtag"></i>
                No tagged posts found for "<c:out value="${keyword}"/>".
              </div>
            </c:otherwise>
          </c:choose>
        </div>

        <!-- Groups Section -->
        <div class="results-category">
          <div class="category-header">
            <h3 class="category-title">
              <i class="fas fa-users"></i>Groups
            </h3>
            <div class="d-flex align-items-center gap-2">
              <span class="category-count">${fn:length(searchResults.groups)} results</span>
              <c:if test="${fn:length(searchResults.groups) > 0}">
                <a href="${pageContext.request.contextPath}/search?action=viewMore&keyword=${keyword}&category=groups" class="view-more-link">
                  View all
                </a>
              </c:if>
            </div>
          </div>
          <c:choose>
            <c:when test="${not empty searchResults.groups}">
              <c:set var="anyResultsFound" value="${true}" />
              <c:forEach var="group" items="${searchResults.groups}" varStatus="status" end="4">
                <div class="result-item-large">
                  <div class="d-flex align-items-center">
                    <div class="result-avatar group me-3">
                      <c:choose>
                        <c:when test="${not empty group.groupCoverImage}">
                          <img src="${group.groupCoverImage}" alt="${group.groupName}"
                               style="width: 100%; height: 100%; border-radius: 8px; object-fit: cover;">
                        </c:when>
                        <c:otherwise>
                          <i class="fas fa-users"></i>
                        </c:otherwise>
                      </c:choose>
                    </div>
                    <div class="flex-grow-1">
                      <div class="result-title">
                        <a href="${pageContext.request.contextPath}/group?groupId=${group.id}" style="text-decoration: none; color: #1c1e21;">
                          <c:out value="${group.groupName}"/>
                        </a>
                      </div>
                      <c:if test="${not empty group.groupDescription}">
                        <div class="result-content"><c:out value="${group.groupDescription}"/></div>
                      </c:if>
                    </div>
                  </div>
                </div>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <div class="no-results">
                <i class="fas fa-users-slash"></i>
                No groups found for "<c:out value="${keyword}"/>".
              </div>
            </c:otherwise>
          </c:choose>
        </div>

        <!-- Overall No Results -->
        <c:if test="${!anyResultsFound}">
          <div class="no-results">
            <i class="fas fa-search"></i>
            <h3>No results found</h3>
            <p>We couldn't find anything for "<c:out value="${keyword}"/>" across all categories.</p>
            <p>Try searching with different keywords or check your spelling.</p>
          </div>
        </c:if>
      </div>
    </c:when>
    <c:otherwise>
      <div class="no-results">
        <i class="fas fa-search"></i>
        <h3>Enter a search term</h3>
        <p>Use the search box above to find users, posts, and groups.</p>
      </div>
    </c:otherwise>
  </c:choose>
</div>

<script>
  function toggleHtmlContent(element) {
    const htmlContent = element.nextElementSibling;
    const isVisible = htmlContent.classList.contains('show');

    if (isVisible) {
      htmlContent.classList.remove('show');
      element.textContent = 'Show original formatting';
    } else {
      htmlContent.classList.add('show');
      element.textContent = 'Hide formatting';
    }
  }

  document.addEventListener('DOMContentLoaded', function() {
    const toggles = document.querySelectorAll('.content-toggle');
    toggles.forEach(toggle => {
      toggle.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        toggleHtmlContent(this);
      });
    });
  });
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
        crossorigin="anonymous"></script>
</body>
</html>
