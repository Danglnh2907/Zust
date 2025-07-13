<%--
  Created by IntelliJ IDEA.
  User: thang
  Date: 6/25/2025
  Time: 2:05 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Zust - Feedback Approval</title>
  <!-- Font Imports -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <!-- External Libraries -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
          integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
  <!-- Custom Styles -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/groupmanager.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pending_posts.css">
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
        <!-- Feedback Approval Section -->
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
              <a href="${pageContext.request.contextPath}/viewMembers?groupId=${groupId}" >Members</a>
              <a href="${pageContext.request.contextPath}/joinRequest?groupId=${groupId}">Joining Request</a>
              <a href="${pageContext.request.contextPath}/approvePost?groupId=${groupId}">Pending Posts</a>
              <a href="${pageContext.request.contextPath}/reportGroupPost?groupId=${groupId}">Reported Content</a>
              <a href="${pageContext.request.contextPath}/viewFeedback?groupId=${groupId}" class="active">View Feedbacks</a>
            </nav>
          </div>
        </section>

        <section class="post-card" id="feedback-approval">
          <h2>Feedbacks for Approval</h2>
          <c:if test="${not empty message}">
            <div class="message success">${message}</div>
          </c:if>
          <c:if test="${not empty error}">
            <div class="message error">${error}</div>
          </c:if>
          <c:choose>
            <c:when test="${empty feedbacks}">
              <p>No feedbacks awaiting approval.</p>
            </c:when>
            <c:otherwise>
              <div class="pending-posts-container">
                <c:forEach var="dto" items="${feedbacks}">
                  <div class="pending-post-card">
                    <div class="post-header">
                      <div class="post-author">
                        <img src="${pageContext.request.contextPath}/static/images/${dto.account.avatar}" alt="${dto.account.username} Avatar" class="post-avatar" onerror="this.src='${pageContext.request.contextPath}/static/images/default-avatar.png';">
                        <div class="post-user-info">
                          <span class="post-user-name">${dto.account.username}</span>
                        </div>
                      </div>
                    </div>
                    <div class="post-content">
                      <p>${dto.feedback.feedbackGroupContent}</p>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </section>
      </c:when>
      <c:otherwise>
        <div class="message error">Error: Group ID is required.</div>
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