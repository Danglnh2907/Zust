<%--
  Created by IntelliJ IDEA.
  User: thang
  Date: 6/19/2025
  Time: 11:22 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="dto.ResGroupDTO" %>
<%@ page import="model.Account" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zust - Group Management</title>

    <!-- Font Imports -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
    <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
          crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
            crossorigin="anonymous"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/groupmanager.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">
</head>
<body>
<div class="container">
    <!-- Sidebar -->
    <aside class="slide_bar">
        <div class="logo">
            <h1>Zust</h1>
        </div>
        <nav class="menu">
            <a href="${pageContext.request.contextPath}/post" class="active"><i class="fas fa-home"></i> Home</a>
            <a href="#"><i class="fas fa-user"></i> My Profile</a>
            <a href="#"><i class="fas fa-plus-square"></i> Create Request</a>
        </nav>
    </aside>

    <%
        ResGroupDTO groupInfo = (ResGroupDTO) request.getAttribute("groupInfo");
    %>
    <!-- Main Content -->
    <main class="main-content">
        <!-- Group Info Section -->
        <section class="group-card">
            <div class="background_img">
                <img src="${pageContext.request.contextPath}/static/images/<%= groupInfo.getImage() %>" alt="Group Cover"
                     class="cover-img" />
                <button class="edit-banner" onclick="window.location.href='${pageContext.request.contextPath}/groupProfile?groupId=1'">
                    <i class="fas fa-pencil-alt"></i> Edit
                </button>
            </div>
            <div class="group-info-body">
                <div class="group-header">
                    <div class="group-title">
                        <h1><%= groupInfo.getName() %></h1>
                        <p><%= groupInfo.getDescription() %></p>
                    </div>
                    <div class="group-buttons">
<%--                        <button class="edit-button"><i class="fas fa-pencil-alt"></i> Edit</button>--%>
<%--                        <button class="joined-button">Joined</button>--%>
<%--    trạng thái tham nha nhosm--%>
                        <button class="invite-button"><i class="fas fa-user-plus"></i> Invite</button>
                        <button class="share-button"><i class="fas fa-share"></i> Share</button>
                    </div>
                </div>
<%--                <nav class="group-tabs">--%>
<%--                    <a href="${pageContext.request.contextPath}/groupManager?groupId=2" class="active">Discussion</a>--%>
<%--                    <a href="#">Members</a>--%>
<%--                    <a href="${pageContext.request.contextPath}/joinRequest?groupId=${groupId}">Joining Request</a>--%>
<%--                    <a href="${pageContext.request.contextPath}/approvePost?groupId=2" onclick="event.stopPropagation();">Pending Posts</a>--%>
<%--                    <a href="${pageContext.request.contextPath}/viewFeedback?groupId=2" onclick="event.stopPropagation();">View Feedbacks</a>--%>
<%--                    <div class="tab-actions">--%>

<%--                        <button class="more-options-btn"><i class="fas fa-ellipsis-h"></i></button>--%>
<%--                    </div>--%>
<%--                </nav>--%>

                    <nav class="group-tabs">
                        <a href="${pageContext.request.contextPath}/groupManager?groupId=1" class="active">Discussion</a>
                        <a href="${pageContext.request.contextPath}/viewMembers?groupId=<%= groupInfo.getId() %>" >Members</a>
                        <a href="${pageContext.request.contextPath}/joinRequest?groupId=<%= groupInfo.getId() %>">Joining Request</a>
                        <a href="${pageContext.request.contextPath}/approvePost?groupId=<%= groupInfo.getId() %>">Pending Posts</a>
                        <a href="${pageContext.request.contextPath}/reportGroupPost?groupId=${groupId}" >Reported Content</a>
                        <a href="${pageContext.request.contextPath}/viewFeedback?groupId=<%= groupInfo.getId() %>">View Feedbacks</a>
                    </nav>
<%--                <div class="tab-actions" style="position: relative;">--%>
<%--                    <button class="more-options-btn"><i class="fas fa-ellipsis-h"></i></button>--%>
<%--                    <div class="options-menu-tab" style="display: none; position: absolute; top: 100%; right: 0; background-color: white; border: 1px solid #ddd; border-radius: 5px; box-shadow: 0 2px 8px rgba(0,0,0,0.15); z-index: 1000;">--%>
<%--                        <a href="#" class="dropdown-item">Disband Group</a>--%>
<%--                        <a href="#" class="dropdown-item">Assign Managers to Group</a>--%>
<%--                        <a href="#">Report Content</a>--%>
<%--                    </div>--%>
<%--                </div>--%>
                </div>
        </section>

        <!-- Discussion Section -->
        <section class="post-card" id="discussion">
            <div class="create-post">
                <img src="https://i.imgur.com/g3v1Y1B.jpg" alt="User Avatar" class="user-avatar">
                <input type="text" placeholder="Write something..." class="post-input">
            </div>
            <c:forEach var="post" items="${posts}">
                <div class="post">
                    <div class="post-header">
                        <div class="post-author">
                            <img src="${pageContext.request.contextPath}/static/images/${post.avatar}" alt="${post.username} Avatar" class="post-avatar">
                            <div class="post-user-info">
                                <span class="post-user-name">${post.username}</span>
                                <span class="post-timestamp">${post.lastModified}</span>
                            </div>
                        </div>
                        <div class="post-options">
                            <button class="options-btn"><i class="fas fa-ellipsis-h"></i></button>
                            <div class="options-menu">
                                <a href="#" class="edit">Edit</a>
                                <a href="#" class="delete">Delete</a>
                            </div>
                        </div>
                    </div>

                    <div class="post-content">
                        <p>${post.postContent}</p>
                        <c:if test="${not empty post.images}">
                            <div class="post-media">
                                <div class="carousel-track">
                                    <c:forEach var="image" items="${post.images}">
                                        <img src="${pageContext.request.contextPath}/static/images/${image}" alt="Post Image" class="carousel-slide">
                                    </c:forEach>
                                </div>
                                <button class="carousel-btn prev"><</button>
                                <button class="carousel-btn next">></button>
                            </div>
                        </c:if>
                    </div>
                    <div class="post-actions">
                        <div class="action-btn-group like-btn-group">
                            <button class="action-btn like-btn">
                                <svg class="icon icon-heart-outline" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
                                <svg class="icon icon-heart-filled" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="none"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                                <span class="count like-count">${post.likeCount}</span>
                            </button>
                        </div>
                        <div class="action-btn-group comment-btn-group">
                            <button class="action-btn">
                                <svg class="icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path></svg>
                                <span class="count comment-count">${post.commentCount}</span>
                            </button>
                        </div>
                        <div class="action-btn-group repost-btn-group">
                            <button class="action-btn">
                                <svg class="icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="17 1 21 5 17 9"></polyline><path d="M3 11V9a4 4 0 0 1 4-4h14"></path><polyline points="7 23 3 19 7 15"></polyline><path d="M21 13v2a4 4 0 0 1-4 4H3"></path></svg>
                                <span class="count repost-count">${post.repostCount}</span>
                            </button>
                        </div>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty posts}">
                <p>Chưa có bài post nào trong nhóm.</p>
            </c:if>
        </section>



        <!-- Placeholder Sections -->
        <section class="post-card" id="members" style="display: none;">
            <h2 class="placeholder-title">Members (${group.memberCount})</h2>
            <div class="list-container">
                <div class="list-item">
                    <div class="post-author">
                        <img src="https://i.imgur.com/g3v1Y1B.jpg" alt="Shaan Alam Avatar">
                        <div class="author-info">
                            <span>Shaan Alam</span>
                            <small>Admin</small>
                        </div>
                    </div>
                    <button class="action-btn remove-btn">Remove</button>
                </div>
            </div>
        </section>
    </main>
</div>

<script src="${pageContext.request.contextPath}/js/groupmanager.js"></script>
<script src="${pageContext.request.contextPath}/js/search.js"></script>
</body>
</html>