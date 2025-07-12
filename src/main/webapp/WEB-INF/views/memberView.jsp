<%--
  Created by IntelliJ IDEA.
  User: thang
  Date: 7/4/2025
  Time: 9:24 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zust - View Member List</title>
    <!-- Import fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Import external libraries -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/groupmanager.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pending_posts.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/view-members.css">
    <style>
        .post-actions-icon {
            display: flex;
            align-items: center;
        }
        .dropdown-menu {
            min-width: 150px;
        }
        .dropdown-item:hover {
            background-color: #f8f9fa;
        }
    </style>
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

    <!-- Main content -->
    <main class="main-content">
        <c:choose>
            <c:when test="${groupInfo != null && groupId != null}">
                <!-- Group information section -->
                <section class="group-card">
                    <div class="background_img">
                        <img src="${pageContext.request.contextPath}/static/images/${groupInfo.image != null ? groupInfo.image : 'default-group.png'}"
                             alt="Group cover image" class="cover-img"/>
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
                            <a href="${pageContext.request.contextPath}/viewMembers?groupId=${groupId}" class="active">Members</a>
                            <a href="${pageContext.request.contextPath}/joinRequest?groupId=${groupId}">Join Requests</a>
                            <a href="${pageContext.request.contextPath}/approvePost?groupId=${groupId}">Pending Posts</a>
                            <a href="${pageContext.request.contextPath}/reportGroupPost?groupId=${groupId}">Reported Content</a>
                            <a href="${pageContext.request.contextPath}/viewFeedback?groupId=${groupId}">View Feedback</a>
<%--                            <div class="tab-actions">--%>
<%--                                <button class="more-options-btn"><i class="fas fa-ellipsis-h"></i></button>--%>
<%--                            </div>--%>
                        </nav>
                    </div>
                </section>

                <!-- Member list section -->
                <section class="post-card" id="member-view">
                    <h2>Members: ${fn:length(members)} peoples</h2>
                    <c:if test="${not empty message}">
                        <div class="message success">${message}</div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="message error">${error}</div>
                    </c:if>
                    <c:choose>
                        <c:when test="${empty members}">
                            <p>This group has no members yet..</p>
                        </c:when>
                        <c:otherwise>

                                <div class="search-container">
                                    <i class="fas fa-search search-icon"></i>
                                    <input type="text" placeholder="Search for members..." />
                                    <button type="button" class="search-button">Search</button>
                                </div>

                                <!-- Single card containing all members -->
                                <div class="member-card">
                                    <c:forEach var="dto" items="${members}">
                                        <div class="member-item">
                                            <div class="post-author">
                                                <img src="${pageContext.request.contextPath}/static/images/${dto.account.avatar != null ? dto.account.avatar : 'default-avatar.png'}"
                                                     alt="Avatar của ${dto.account.username}" class="post-avatar" onerror="this.src='${pageContext.request.contextPath}/static/images/default-avatar.png';">
                                                <div class="post-user-info">
                                                    <span class="post-user-name">${dto.account.username}</span>
                                                    <span class="post-user-fullname">${dto.account.fullname}</span>
                                                </div>
                                            </div>
                                            <div class="post-actions-icon">
                                                <!-- Dropdown menu with three dots -->
                                                <div class="dropdown">
                                                    <button class="btn icon-button" type="button" id="memberMenu${dto.participate.id.accountId}"
                                                            data-bs-toggle="dropdown" aria-expanded="false">
                                                        <i class="fas fa-ellipsis-h"></i>
                                                    </button>
                                                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="memberMenu${dto.participate.id.accountId}">
                                                        <li>
                                                            <form action="${pageContext.request.contextPath}/viewMembers" method="post" style="margin: 0;">
                                                                <input type="hidden" name="accountId" value="${dto.participate.id.accountId}">
                                                                <input type="hidden" name="groupId" value="${groupId}">
                                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                                <input type="hidden" name="action" value="remove">
                                                                <button type="submit" class="dropdown-item" onclick="return confirm('Are you sure you want to remove this member from the group?')">Kick member</button>
                                                            </form>
                                                        </li>
                                                        <li>
                                                            <form action="${pageContext.request.contextPath}/viewMembers" method="post" style="margin: 0;">
                                                                <input type="hidden" name="accountId" value="${dto.participate.id.accountId}">
                                                                <input type="hidden" name="groupId" value="${groupId}">
                                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                                <input type="hidden" name="action" value="promote">
                                                                <button type="submit" class="dropdown-item" onclick="return confirm('Are you sure you want to invite this member as a manager?')">Invite as manager</button>
                                                            </form>
                                                        </li>
                                                    </ul>
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
                <div class="message error">Lỗi: groupId là bắt buộc.</div>
            </c:otherwise>
        </c:choose>
    </main>
</div>

<!-- Script -->
<script src="${pageContext.request.contextPath}/js/groupmanager.js"></script>
<script src="${pageContext.request.contextPath}/js/search.js"></script>
</body>
</html>