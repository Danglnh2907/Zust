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
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
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
          </div>
          <div class="group-info-body">
            <div class="group-header">
              <div class="group-title">
                <h1>${groupInfo.name}</h1>
                <p>${groupInfo.description}</p>
              </div>
              <div class="group-buttons">
                <button class="invite-button" onclick="window.location.href='${pageContext.request.contextPath}/groupProfile?groupId=${groupId}'"><i class="fas fa-pen"></i> Edit</button>
                <button class="Disban-group" onclick="disbandGroup(${groupId})"><i class="fas fa-ban"></i> Disband Group</button>
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
          <h2 class="mb-4">Reported Contents</h2>
          <c:if test="${not empty message}">
            <div class="alert alert-success" role="alert">${message}</div>
          </c:if>
          <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert">${error}</div>
          </c:if>
          <c:choose>
            <c:when test="${empty reportPostList}">
              <p class="text-muted">No reported contents available.</p>
            </c:when>
            <c:otherwise>
              <div class="pending-posts-container">
                <c:forEach var="dto" items="${reportPostList}">
                  <article class="card mb-4 shadow-sm">
                    <div class="card-header d-flex justify-content-between align-items-center bg-light">
                      <div class="d-flex align-items-center">
                        <img src="${pageContext.request.contextPath}/static/images/${dto.account.avatar}" alt="${dto.account.username} Avatar" class="post-avatar rounded-circle me-3" style="width: 40px; height: 40px; object-fit: cover;">
                        <div class="post-user-info">
                          <span class="post-user-name fw-bold">${dto.account.username} (Reporter)</span>
                          <time class="report-date text-muted small d-block" datetime="${dto.reportCreateDate}">${dto.reportCreateDate}</time>
                        </div>
                      </div>
                      <div class="post-actions-icon btn-group" role="group" aria-label="Report Actions">
                        <!-- Accept form (delete post and send notification) -->
                        <form action="${pageContext.request.contextPath}/reportGroupPost" method="post" style="display:inline;">
                          <input type="hidden" name="action" value="accept">
                          <input type="hidden" name="reportId" value="${dto.reportId}">
                          <input type="hidden" name="reporterId" value="${dto.account.id}">
                          <input type="hidden" name="reportedPostId" value="${dto.post.postId}">
                          <input type="hidden" name="groupId" value="${groupId}">
                          <input type="hidden" name="csrfToken" value="${csrfToken}">
                          <!-- Optional: Field for suspension message -->
                          <input type="text" name="suspensionMessage" placeholder="Notification message" class="form-control d-none">
                          <button type="submit" class="btn btn-sm btn-success approve-icon" title="Accept Report" aria-label="Accept Report">
                            <i class="fas fa-check"></i> Accept
                          </button>
                        </form>
                        <!-- Dismiss form (reject report) -->
                        <form action="${pageContext.request.contextPath}/reportGroupPost" method="post" style="display:inline;">
                          <input type="hidden" name="action" value="dismiss">
                          <input type="hidden" name="reportId" value="${dto.reportId}">
                          <input type="hidden" name="groupId" value="${groupId}">
                          <input type="hidden" name="csrfToken" value="${csrfToken}">
                          <button type="submit" class="btn btn-sm btn-danger disapprove-icon" title="Dismiss Report" aria-label="Dismiss Report">
                            <i class="fas fa-times"></i> Dismiss
                          </button>
                        </form>
                      </div>
                    </div>
                    <div class="card-body">
                      <h5 class="card-title">Report Content:</h5>
                      <p class="card-text">${dto.reportContent}</p>
                      <h5 class="card-title mt-3">Reported Post:</h5>
                      <p class="card-text">${dto.post.postContent}</p>
                      <c:if test="${not empty dto.post.images}">
                        <div id="carousel-${dto.reportId}" class="carousel slide mb-3" data-bs-ride="carousel">
                          <div class="carousel-inner">
                            <c:forEach var="image" items="${dto.post.images}" varStatus="status">
                              <div class="carousel-item ${status.first ? 'active' : ''}">
                                <img src="${pageContext.request.contextPath}/static/images/${image}" class="d-block w-100" alt="Post Image" style="max-height: 400px; object-fit: contain;">
                              </div>
                            </c:forEach>
                          </div>
                          <button class="carousel-control-prev" type="button" data-bs-target="#carousel-${dto.reportId}" data-bs-slide="prev">
                            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                            <span class="visually-hidden">Previous</span>
                          </button>
                          <button class="carousel-control-next" type="button" data-bs-target="#carousel-${dto.reportId}" data-bs-slide="next">
                            <span class="carousel-control-next-icon" aria-hidden="true"></span>
                            <span class="visually-hidden">Next</span>
                          </button>
                        </div>
                      </c:if>
                      <c:if test="${not empty dto.post.hashtags}">
                        <div class="hashtags mb-2">
                          <c:forEach var="hashtag" items="${dto.post.hashtags}">
                            <span class="badge bg-primary me-1">#${hashtag}</span>
                          </c:forEach>
                        </div>
                      </c:if>
                      <div class="post-stats d-flex justify-content-start text-muted small">
                        <span class="me-3"><i class="fas fa-heart me-1"></i> ${dto.post.likeCount}</span>
                        <span class="me-3"><i class="fas fa-comment me-1"></i> ${dto.post.commentCount}</span>
                        <span><i class="fas fa-retweet me-1"></i> ${dto.post.repostCount}</span>
                      </div>
                    </div>
                  </article>
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
<script>
  function disbandGroup(groupId) {
    if (confirm("Are you sure you want to disband this group?")) {
      var csrfToken = '${csrfToken}';
      var contextPath = '${pageContext.request.contextPath}';
      fetch(contextPath + '/disbandGroup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'groupId=' + groupId + '&csrfToken=' + csrfToken
      })
              .then(response => {
                if (!response.ok) {
                  throw new Error('Network response was not ok: ' + response.statusText);
                }
                return response.json();
              })
              .then(data => {
                if (data.success) {
                  alert('Group disbanded successfully.');
                  window.location.href = contextPath + '/post';
                } else {
                  alert('Failed to disband group: ' + (data.error || 'Unknown error'));
                }
              })
              .catch(error => {
                console.error('Error:', error);
                alert('Error occurred during disband: ' + error.message);
              });
    }
  }
</script>
<script src="${pageContext.request.contextPath}/js/groupmanager.js"></script>
<script src="${pageContext.request.contextPath}/js/search.js"></script>
</body>
</html>