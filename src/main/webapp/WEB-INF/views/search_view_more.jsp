<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page import="model.Account, model.Post, model.Group" %>
<%@ page import="java.util.List" %>
<%@ page import="util.HtmlUtils" %>
<%@ taglib prefix="h" uri="http://zust.com/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Search Results - <c:out value="${category}"/> - Zust</title>
  <!-- Font Imports -->
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

    .search-header {
      border-bottom: 1px solid #e4e6ea;
      padding-bottom: 16px;
      margin-bottom: 24px;
    }

    .search-title {
      color: #1c1e21;
      font-size: 24px;
      font-weight: 700;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      margin-bottom: 8px;
    }

    .search-subtitle {
      color: #65676b;
      font-size: 15px;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }

    .result-item-large {
      background: #f8f9fa;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;
      border: 1px solid #e4e6ea;
      transition: all 0.2s ease;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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

    .result-avatar i {
      font-family: 'Font Awesome 6 Free';
      font-weight: 900;
    }

    .result-title {
      font-size: 18px;
      font-weight: 600;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      color: #1c1e21;
      margin-bottom: 4px;
    }

    .result-subtitle {
      font-size: 14px;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      color: #65676b;
      margin-bottom: 8px;
    }

    .result-content {
      font-size: 15px;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      color: #1c1e21;
      line-height: 1.4;
    }

    .pagination-container {
      text-align: center;
      margin-top: 32px;
      padding-top: 24px;
      border-top: 1px solid #e4e6ea;
    }

    .load-more-btn {
      background: #1877f2;
      color: white;
      border: none;
      padding: 12px 24px;
      border-radius: 6px;
      font-weight: 600;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      cursor: pointer;
      transition: background 0.2s ease;
      text-decoration: none;
      display: inline-block;
    }

    .load-more-btn:hover {
      background: #166fe5;
      color: white;
      text-decoration: none;
    }

    .back-link {
      color: #1877f2;
      text-decoration: none;
      font-weight: 500;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
      font-family: 'Font Awesome 6 Free';
      font-weight: 900;
    }

    .badge {
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      font-size: 11px;
      padding: 4px 8px;
    }

    .no-results {
      text-align: center;
      padding: 40px 20px;
      color: #65676b;
      font-size: 16px;
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }

    .no-results i {
      font-size: 24px;
      margin-bottom: 12px;
      display: block;
      font-family: 'Font Awesome 6 Free';
      font-weight: 900;
    }

    /* Responsive */
    @media (max-width: 768px) {
      .search-results-page {
        padding: 16px;
      }

      .search-title {
        font-size: 20px;
      }

      .result-item-large {
        padding: 12px;
      }

      .result-avatar {
        width: 40px;
        height: 40px;
        font-size: 16px;
      }

      .result-title {
        font-size: 16px;
      }
    }

    @media (max-width: 480px) {
      .search-results-page {
        padding: 12px;
      }

      .search-title {
        font-size: 18px;
      }

      .result-avatar {
        width: 36px;
        height: 36px;
        font-size: 14px;
      }

      .result-title {
        font-size: 15px;
      }

      .result-subtitle,
      .result-content {
        font-size: 13px;
      }
    }
  </style>
</head>
<script>
  function toggleHtmlContent(element) {
    const htmlContent = element.nextElementSibling;
    const isVisible = htmlContent.classList.contains('show');

    if (isVisible) {
      htmlContent.classList.remove('show');
      element.textContent = 'Show full content with formatting';
    } else {
      htmlContent.classList.add('show');
      element.textContent = 'Hide formatting';
    }
  }

  // Add smooth animations for better UX
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

<body>
<%-- Debug information --%>
<c:if test="${param.debug == 'true'}">
  <div class="alert alert-info">
    <h5>Debug Information:</h5>
    <p>Keyword: <strong>${keyword}</strong></p>
    <p>Category: <strong>${category}</strong></p>
    <p>Current Page: <strong>${currentPage}</strong></p>
    <p>Results Per Page: <strong>${resultsPerPage}</strong></p>
    <p>Total Count: <strong>${pagedResults.totalCount}</strong></p>
    <p>Has More: <strong>${pagedResults.hasMore}</strong></p>
    <p>Results Size: <strong>${fn:length(pagedResults.results)}</strong></p>
  </div>
</c:if>
<div class="search-results-page">
  <a href="${pageContext.request.contextPath}/post" class="back-link">
    <i class="fas fa-arrow-left"></i>
    Back to Feed
  </a>

  <div class="search-header">
    <h1 class="search-title">
      <c:choose>
        <c:when test="${category == 'users'}">
          <i class="fas fa-user" style="margin-right: 12px; color: #1877f2;"></i>People
        </c:when>
        <c:when test="${category == 'posts_content'}">
          <i class="fas fa-file-alt" style="margin-right: 12px; color: #1877f2;"></i>Posts
        </c:when>
        <c:when test="${category == 'posts_hashtag'}">
          <i class="fas fa-hashtag" style="margin-right: 12px; color: #1877f2;"></i>Posts with hashtags
        </c:when>
        <c:when test="${category == 'groups'}">
          <i class="fas fa-users" style="margin-right: 12px; color: #1877f2;"></i>Groups
        </c:when>
        <c:otherwise>
          <i class="fas fa-search" style="margin-right: 12px; color: #1877f2;"></i>Search Results
        </c:otherwise>
      </c:choose>
    </h1>
    <p class="search-subtitle">
      Search results for "<c:out value="${keyword}"/>" · ${pagedResults.totalCount} results found
    </p>
  </div>

  <div class="search-results">
    <c:choose>
      <c:when test="${empty pagedResults.results}">
        <div class="no-results">
          <i class="fas fa-search"></i>
          No results found for "<c:out value="${keyword}"/>" in this category.
        </div>
      </c:when>
      <c:otherwise>
        <c:forEach var="item" items="${pagedResults.results}" varStatus="status">
          <div class="result-item-large">
            <c:choose>
              <%-- User Results --%>
              <c:when test="${category == 'users'}">
                <div class="d-flex align-items-center">
                  <div class="result-avatar me-3">
                    <c:choose>
                      <c:when test="${not empty item.avatar}">
                        <img src="${item.avatar}" alt="${item.fullname}" style="width: 100%; height: 100%; border-radius: 50%; object-fit: cover;">
                      </c:when>
                      <c:otherwise>
                        <i class="fas fa-user"></i>
                      </c:otherwise>
                    </c:choose>
                  </div>
                  <div class="flex-grow-1">
                    <div class="result-title">
                      <a href="${pageContext.request.contextPath}/profile?userId=${item.id}" style="text-decoration: none; color: #1c1e21;">
                        <c:out value="${item.fullname}"/>
                      </a>
                    </div>
                    <div class="result-subtitle">@<c:out value="${item.username}"/> · <c:out value="${item.email}"/></div>
                    <c:if test="${not empty item.bio}">
                      <div class="result-content"><c:out value="${item.bio}"/></div>
                    </c:if>
                  </div>
                </div>
              </c:when>

              <%-- Post Results Section trong search_view_more.jsp --%>
              <c:when test="${category == 'posts_content' or category == 'posts_hashtag'}">
                <div>
                  <div class="d-flex align-items-start mb-3">
                    <c:if test="${item.account != null}">
                      <div class="result-avatar me-3" style="width: 36px; height: 36px; font-size: 14px;">
                        <c:choose>
                          <c:when test="${not empty item.account.avatar}">
                            <img src="${item.account.avatar}" alt="${item.account.fullname}"
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
                              <a href="${pageContext.request.contextPath}/profile?userId=${item.account.id}" style="text-decoration: none; color: #1c1e21;">
                                <c:out value="${item.account.fullname}"/>
                              </a>
                            </div>
                            <div class="result-subtitle">@<c:out value="${item.account.username}"/></div>
                          </div>
                          <c:if test="${category == 'posts_hashtag'}">
                            <span class="badge bg-primary"><i class="fas fa-hashtag"></i> hashtag</span>
                          </c:if>
                        </div>
                      </div>
                    </c:if>
                  </div>
                  <div class="post-content">
                    <a href="${pageContext.request.contextPath}/post?postId=${item.id}" style="text-decoration: none; color: #1c1e21;">
                      <div class="result-content">
                        <!-- Replace isHtmlContent check with: -->
                        <c:set var="isHtmlContent" value="${fn:contains(item.postContent, '<') && fn:contains(item.postContent, '>')}" />
                        <c:choose>
                          <c:when test="${isHtmlContent}">
                            <div class="html-content" id="html-content-${item.id}">
                                ${h:sanitizeHtml(item.postContent)}
                            </div>
                          </c:when>
                          <c:otherwise>
                            <c:out value="${h:getPreview(item.postContent, 300)}" />
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </a>
                    <c:if test="${item.group != null}">
                      <small class="text-muted mt-2 d-block">
                        <i class="fas fa-users" style="margin-right: 4px;"></i>
                        Posted in: <c:out value="${item.group.groupName}"/>
                      </small>
                    </c:if>
                  </div>
                </div>
              </c:when>


              <%-- Group Results --%>
              <c:when test="${category == 'groups'}">
                <div class="d-flex align-items-center">
                  <div class="result-avatar group me-3">
                    <c:choose>
                      <c:when test="${not empty item.groupCoverImage}">
                        <img src="${item.groupCoverImage}" alt="${item.groupName}"
                             style="width: 100%; height: 100%; border-radius: 8px; object-fit: cover;">
                      </c:when>
                      <c:otherwise>
                        <i class="fas fa-users"></i>
                      </c:otherwise>
                    </c:choose>
                  </div>
                  <div class="flex-grow-1">
                    <div class="result-title">
                      <a href="${pageContext.request.contextPath}/group?groupId=${item.id}" style="text-decoration: none; color: #1c1e21;">
                        <c:out value="${item.groupName}"/>
                      </a>
                    </div>
                    <c:if test="${not empty item.groupDescription}">
                      <div class="result-content"><c:out value="${item.groupDescription}"/></div>
                    </c:if>
                  </div>
                </div>
              </c:when>
            </c:choose>
          </div>
        </c:forEach>

        <%-- Pagination --%>
        <div class="pagination-container">
          <c:if test="${currentPage > 1}">
            <a href="${pageContext.request.contextPath}/search?action=viewMore&keyword=${keyword}&category=${category}&page=${currentPage - 1}"
               class="btn btn-outline-secondary me-2">
              <i class="fas fa-chevron-left"></i> Previous
            </a>
          </c:if>

          <span class="text-muted mx-3">Page ${currentPage}</span>

          <c:if test="${pagedResults.hasMore}">
            <a href="${pageContext.request.contextPath}/search?action=viewMore&keyword=${keyword}&category=${category}&page=${currentPage + 1}"
               class="btn btn-outline-secondary ms-2">
              Next <i class="fas fa-chevron-right"></i>
            </a>
          </c:if>
        </div>

        <c:if test="${pagedResults.hasMore}">
          <div class="pagination-container">
            <a href="${pageContext.request.contextPath}/search?action=viewMore&keyword=${keyword}&category=${category}&page=${currentPage + 1}"
               class="load-more-btn">
              <i class="fas fa-plus"></i> Load more results
            </a>
          </div>
        </c:if>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
        crossorigin="anonymous"></script>
</body>
</html>
