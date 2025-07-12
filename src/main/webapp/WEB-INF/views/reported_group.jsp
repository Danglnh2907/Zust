<%--
  Created by IntelliJ IDEA.
  User: thang
  Date: 7/12/2025
  Time: 5:48 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" contentType="width=device-width, initial-scale=1.0">
  <title>Zust - Reported Content</title>
  <!-- Font Imports -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <!-- External Libraries -->
  <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
  <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
          integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
  <!-- Custom Styles -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/groupmanager.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pending_posts.css"> <!-- Reuse style for similarity -->
</head>
<body>
<div class="container">
  <!-- Sidebar -->
  <aside class="slide_bar">
    <div class="logo">
      <h1>Zust</h1>
    </div>
    <nav class="menu">
      <a href="${pageContext.request.contextPath}/post"><i class="fas fa-home"></i> Home</a>
      <a href="#"><i class="fas fa-user"></i> My Profile</a>
      <a href="#"><i class="fas fa-plus-square"></i> Create Request</a>
    </nav>
  </aside>

  <!-- Main Content -->
  <main class="main-content">
    <c:choose>
      <c:when test="${groupInfo != null && groupId != null}">
        <!-- Group Info Section -->
        <section class="group-card">
          <div class="background_img">
            <img src="${pageContext.request.contextPath}/static/images/${groupInfo.image}" alt="Group Cover" class="cover-img"/>
            <button class="edit-banner" onclick="window.location.href='${pageContext.request.contextPath}/groupProfile?groupId=${groupId}'">
              <i class="fas fa-pencil-alt"></i> Edit
            </button>
          </div>
          <div class="group-info-body">
            <div class="group-header">
              <div class="group-title">
                <h1>${groupInfo.name}</h1>
                <p>${groupInfo.description}</p>
              </div>
              <div class="group-buttons">
                <button class="invite-button"><i class="fas fa-user-plus"></i> Invite</button>
                <button class="share-button"><i class="fas fa-share"></i> Share</button>
              </div>
            </div>
            <nav class="group-tabs">
              <a href="${pageContext.request.contextPath}/groupManager?groupId=${groupId}">Discussion</a>
              <a href="${pageContext.request.contextPath}/viewMembers?groupId=${groupId}">Members</a>
              <a href="${pageContext.request.contextPath}/joinRequest?groupId=${groupId}">Joining Request</a>
              <a href="${pageContext.request.contextPath}/approvePost?groupId=${groupId}">Pending Posts</a>
              <a href="${pageContext.request.contextPath}/reportGroupPost?groupId=${groupId}" class="active">Reported Content</a>
              <a href="${pageContext.request.contextPath}/viewFeedback?groupId=${groupId}">View Feedbacks</a>
            </nav>
          </div>
        </section>

        <!-- Reported Content Section -->
        <section class="post-card" id="reported-content">
          <h2>Reported Contents</h2>
          <c:if test="${not empty message}">
            <div class="message success">${message}</div>
          </c:if>
          <c:if test="${not empty error}">
            <div class="message error">${error}</div>
          </c:if>
          <c:choose>
            <c:when test="${empty reportPostList}">
              <p>No reported contents available.</p>
            </c:when>
            <c:otherwise>
              <div class="pending-posts-container"> <!-- Reuse class for similar styling -->
                <c:forEach var="dto" items="${reportPostList}">
                  <div class="pending-post-card"> <!-- Reuse class for report card -->
                    <div class="post-header">
                      <div class="post-author">
                        <img src="${pageContext.request.contextPath}/static/images/${dto.account.avatar}" alt="${dto.account.username} Avatar" class="post-avatar">
                        <div class="post-user-info">
                          <span class="post-user-name">${dto.account.username} (Reporter)</span>
                          <span class="report-date">${dto.reportCreateDate}</span>
                        </div>
                      </div>
                      <!-- Action buttons for accept/dismiss -->
                      <div class="post-actions-icon">
                        <!-- Accept form (delete post and send notification) -->
                        <form action="${pageContext.request.contextPath}/reportGroupPost" method="post" style="display:inline;">
                          <input type="hidden" name="action" value="accept">
                          <input type="hidden" name="reportId" value="${dto.reportId}">
                          <input type="hidden" name="reporterId" value="${dto.account.id}">
                          <input type="hidden" name="reportedPostId" value="${dto.post.postId}">
                          <input type="hidden" name="groupId" value="${groupId}">
                          <input type="hidden" name="csrfToken" value="${csrfToken}">
                          <!-- Optional: Field for suspension message -->
                          <input type="text" name="suspensionMessage" placeholder="Notification message" style="display:none;"> <!-- Có thể hiển thị nếu cần input -->
                          <button type="submit" class="icon-button approve-icon" title="Accept Report">
                            <i class="fas fa-check"></i>
                          </button>
                        </form>
                        <!-- Dismiss form (reject report) -->
                        <form action="${pageContext.request.contextPath}/reportGroupPost" method="post" style="display:inline;">
                          <input type="hidden" name="action" value="dismiss">
                          <input type="hidden" name="reportId" value="${dto.reportId}">
                          <input type="hidden" name="groupId" value="${groupId}">
                          <input type="hidden" name="csrfToken" value="${csrfToken}">
                          <button type="submit" class="icon-button disapprove-icon" title="Dismiss Report">
                            <i class="fas fa-times"></i>
                          </button>
                        </form>
                      </div>
                    </div>

                    <div class="post-content">
                      <h4>Report Content:</h4>
                      <p>${dto.reportContent}</p>
                      <h4>Reported Post:</h4>
                      <p>${dto.post.postContent}</p>
                      <c:if test="${not empty dto.post.images}">
                        <div class="post-media">
                          <div class="carousel-track">
                            <c:forEach var="image" items="${dto.post.images}">
                              <img src="${pageContext.request.contextPath}/static/images/${image}" alt="Post Image" class="carousel-slide">
                            </c:forEach>
                          </div>
                          <button class="carousel-btn prev"><</button>
                          <button class="carousel-btn next">></button>
                        </div>
                      </c:if>
                      <c:if test="${not empty dto.post.hashtags}">
                        <div class="hashtags">
                          <c:forEach var="hashtag" items="${dto.post.hashtags}">
                            <span>#${hashtag}</span>
                          </c:forEach>
                        </div>
                      </c:if>
                      <div class="post-stats">
                        <span><i class="fas fa-heart"></i> ${dto.post.likeCount}</span>
                        <span><i class="fas fa-comment"></i> ${dto.post.commentCount}</span>
                        <span><i class="fas fa-retweet"></i> ${dto.post.repostCount}</span>
                      </div>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </section>
      </c:when>
      <c:otherwise>
        <div class="message error">Error: Group information not found.</div>
      </c:otherwise>
    </c:choose>
  </main>
</div>

<!-- Scripts -->
<script src="${pageContext.request.contextPath}/js/groupmanager.js"></script>
<script src="${pageContext.request.contextPath}/js/search.js"></script>
</body>
</html>
