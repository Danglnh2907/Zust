<%--
  Created by IntelliJ IDEA.
  User: thang
  Date: 6/25/2025
  Time: 3:18 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zust - Join Request Approval</title>
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
                <!-- Join Request Approval Section -->
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
                            <a href="#">Members</a>
                            <a href="${pageContext.request.contextPath}/joinRequest?groupId=${groupId}" class="active">Joining Request</a>
                            <a href="${pageContext.request.contextPath}/approvePost?groupId=${groupId}">Pending Posts</a>
                            <a href="${pageContext.request.contextPath}/viewFeedback?groupId=${groupId}">View Feedbacks</a>
                            <div class="tab-actions">
                                <button class="more-options-btn"><i class="fas fa-ellipsis-h"></i></button>
                            </div>
                        </nav>
                    </div>
                </section>

                <section class="post-card" id="join-request-approval">
                    <h2>Join Requests for Group ID: ${groupId}</h2>
                    <c:if test="${not empty message}">
                        <div class="message success">${message}</div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="message error">${error}</div>
                    </c:if>
                    <c:choose>
                        <c:when test="${empty requests}">
                            <p>No join requests awaiting approval.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="pending-posts-container">
                                <c:forEach var="dto" items="${requests}">
                                    <div class="pending-post-card">
                                        <div class="post-header">
                                            <div class="post-author">
                                                <img src="${pageContext.request.contextPath}/static/images/${dto.account.avatar}" alt="${dto.account.username} Avatar" class="post-avatar" onerror="this.src='${pageContext.request.contextPath}/static/images/default-avatar.png';">
                                                <div class="post-user-info">
                                                    <span class="post-user-name">${dto.account.username} </span>
                                                </div>
                                            </div>
                                            <div class="post-actions-icon">
                                                <form action="${pageContext.request.contextPath}/joinRequest" method="post" style="display:inline;">
                                                    <input type="hidden" name="requestId" value="${dto.request.id}">
                                                    <input type="hidden" name="action" value="approve">
                                                    <input type="hidden" name="groupId" value="${groupId}">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                    <button type="submit" class="icon-button approve-icon" title="Approve">
                                                        <i class="fas fa-check"></i>
                                                    </button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/joinRequest" method="post" style="display:inline;">
                                                    <input type="hidden" name="requestId" value="${dto.request.id}">
                                                    <input type="hidden" name="action" value="disapprove">
                                                    <input type="hidden" name="groupId" value="${groupId}">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                    <button type="submit" class="icon-button disapprove-icon" title="Reject">
                                                        <i class="fas fa-times"></i>
                                                    </button>
                                                </form>
                                            </div>
                                        </div>
                                        <div class="post-content">
                                            <p>${dto.request.joinGroupRequestContent}</p>
                                            <small>Requested on: <fmt:formatDate value="${dto.request.joinGroupRequestDate}" pattern="dd MMM yyyy HH:mm:ss" /></small>
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
<script src="${pageContext.request.contextPath}/js/groupmanager.js"></script>
<script src="${pageContext.request.contextPath}/js/search.js"></script>
</body>
</html>
