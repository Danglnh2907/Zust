<%@ page import="model.InteractGroupDTO" %>
<%@ page import="model.RespPostDTO" %>
<%@ page import="model.JoinGroupRequestDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Account" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<% InteractGroupDTO group = (InteractGroupDTO) request.getAttribute("group"); %>
<% List<RespPostDTO> pendingPosts = (List<RespPostDTO>) request.getAttribute("pendingPosts"); %>
<% List<RespPostDTO> groupPosts = (List<RespPostDTO>) request.getAttribute("posts"); %>

<!-- JOIN REQUEST PAGE (GROUP MANAGER) -->

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><%= group != null ? group.getName() : "Group" %> - Zust</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
              rel="stylesheet"
              integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
              crossorigin="anonymous">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/group.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/notification.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/composer.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/comment.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">
        <style>
            .post-feed-section {
                margin-top: 20px;
            }

            .feed-header {
                font-size: 1.2rem;
                font-weight: 600;
                margin-bottom: 15px;
            }

            .no-data-message-post {
                background-color: white;
                border-radius: 8px;
                padding: 40px;
                text-align: center;
                color: #777;
            }

            .feed-container {
                display: flex;
                flex-direction: column;
                gap: 15px;
            }

            .join-requests-section {
                margin-top: 20px;
            }

            .join-request-list {
                display: flex;
                flex-direction: column;
                gap: 15px;
            }

            .join-request-item {
                background-color: white;
                border-radius: 8px;
                padding: 15px;
                display: flex;
                flex-direction: column;
                gap: 10px;
                border: 1px solid #e0e0e0;
            }

            .request-user-info {
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .request-user-info .avatar {
                width: 40px;
                height: 40px;
                border-radius: 50%;
            }

            .user-details {
                display: flex;
                flex-direction: column;
            }

            .user-details .username {
                font-weight: 600;
            }

            .user-details .fullname, .user-details .request-date {
                font-size: 0.9rem;
                color: #777;
            }

            .request-content {
                padding: 10px;
                background-color: #f9f9f9;
                border-radius: 5px;
            }

            .request-actions {
                display: flex;
                gap: 10px;
            }

            .btn-success, .btn-danger {
                padding: 5px 15px;
                font-size: 0.9rem;
            }

            .alert-danger {
                margin: 20px;
                padding: 15px;
                border-radius: 5px;
            }
        </style>
    </head>

    <body>
        <div class="modal fade" id="modal" data-bs-keyboard="false"
             tabindex="-1" aria-labelledby="modal-label" aria-hidden="true">
            <div class="modal-dialog modal-dialog-scrollable modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="modal-title-label"></h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                                aria-label="Close"></button>
                    </div>
                    <div class="modal-body" id="modal-body"></div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>


        <div class="app-layout">
            <aside class="left-sidebar">
                <div class="logo">Zust</div>
                <nav class="sidebar-nav">
                    <ul>
                        <li><a href="${pageContext.request.contextPath}/" class="active">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                                 viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                                 stroke-linecap="round" stroke-linejoin="round">
                                <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                                <polyline points="9 22 9 12 15 12 15 22"></polyline>
                            </svg>
                            <span>Home</span>
                        </a></li>
                        <% Account currentUser = (Account) request.getSession().getAttribute("users"); %>
                        <li><a href="${pageContext.request.contextPath}/profile?userId=<%= currentUser.getId() %>">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                                 viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                                 stroke-linecap="round" stroke-linejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                            <span>My Profile</span>
                        </a></li>
                        <li><a href="${pageContext.request.contextPath}/group?action=create">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                                 viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                                 stroke-linecap="round" stroke-linejoin="round">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                <polyline points="14 2 14 8 20 8"></polyline>
                                <line x1="12" y1="18" x2="12" y2="12"></line>
                                <line x1="9" y1="15" x2="15" y2="15"></line>
                            </svg>
                            <span>Create Group</span>
                        </a></li>
                    </ul>
                </nav>
                <div class="sidebar-divider"></div>

                <% List<InteractGroupDTO> myGroups = (List<InteractGroupDTO>) request.getAttribute("joinedGroups"); %>
                <div class="groups-header">
                    <h2>My Groups</h2>
                    <span class="groups-count"><%= myGroups != null ? myGroups.size() : 0 %></span>
                </div>
                <div class="scrollable-group-list">
                    <% if (myGroups != null && !myGroups.isEmpty()) { %>
                    <div class="group-list">
                        <% for (InteractGroupDTO currentGroup : myGroups) { %>
                        <% String status = currentGroup.getStatus() != null ? currentGroup.getStatus().toLowerCase() : "unknown"; %>
                        <a href="${pageContext.request.contextPath}/group?id=<%= currentGroup.getId() %>"
                           class="group-link">
                            <div class="group-item">
                                <img src="${pageContext.request.contextPath}/static/images/<%= currentGroup.getCoverImage() %>"
                                     alt="Group Avatar">
                                <div class="group-item-info">
                                    <span><%= currentGroup.getName() %></span>
                                    <span class="members"><%= currentGroup.getMemberCount() %> members</span>
                                    <span class="status-badge status-<%= status %>"><%= status %></span>
                                </div>
                            </div>
                        </a>
                        <% } %>
                    </div>
                    <% } else { %>
                    <div class="no-data-message">
                        <div class="icon"><i class="fas fa-search"></i></div>
                        <h2>No Groups Found</h2>
                    </div>
                    <% } %>
                </div>
                <button class="see-all-btn" onclick="location.href='group'">See All</button>
            </aside>

            <main class="main-content">
                <!-- Top Navigation Bar -->
                <header class="top-navbar">
                    <a class="create-post-btn" href="${pageContext.request.contextPath}/post?action=create">Create
                        Post</a>

                    <!-- Live Search Container -->
                    <div class="search-container">
                        <div class="search-bar">
                            <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                                 viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round"
                                 stroke-linejoin="round">
                                <circle cx="11" cy="11" r="8"></circle>
                                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                            </svg>
                            <input type="search" id="liveSearchInput" placeholder="Search Zust..." autocomplete="off">
                            <div class="search-loading" id="searchLoading" style="display: none;">
                                <div class="spinner"></div>
                            </div>
                        </div>

                        <!-- Live Search Dropdown -->
                        <div class="live-search-dropdown" id="liveSearchDropdown">
                            <div class="search-results-container">
                                <!-- Results will be loaded here dynamically -->
                            </div>
                        </div>
                    </div>
                    <%
                        Account account = (Account) request.getSession().getAttribute("users");
                        String linkAvatar = account.getAvatar();
                    %>
                    <div class="nav-profile-container">
                        <!-- Notification button -->
                        <div class="notification-container">
                            <button class="notification-btn" id="notification-btn" aria-label="Notifications">
                                <i class="fas fa-bell"></i>
                                <span class="notification-badge hidden" id="notification-badge"></span>
                            </button>
                            <div class="notification-dropdown" id="notification-dropdown">
                                <div class="notification-header">
                                    <h3>Notifications</h3>
                                    <button id="mark-all-read-btn">Mark all as read</button>
                                </div>
                                <div class="notification-list" id="notification-list">
                                    <!-- Data will be populated by JS here -->
                                </div>
                            </div>
                        </div>

                        <!-- Profile and dropdown for logout here -->
                        <a href="#" class="nav-profile">
                            <img src="${pageContext.request.contextPath}/static/images/<%=linkAvatar%>"
                                 alt="User Profile Picture">
                            <span><%=account.getFullname()%></span>
                        </a>
                        <div class="dropdown-menu">
                            <a class="dropdown-item" href="${pageContext.request.contextPath}/logout">Log out</a>
                        </div>
                    </div>
                </header>

                <% String error = request.getParameter("error"); %>
                <% if (error != null) { %>
                <div class="alert alert-danger" role="alert">
                    <%= error %>
                </div>
                <% } %>

                <% if (group == null) { %>
                <div class="no-data-message-post" style="margin-top: 20px;">
                    <h2>Group Not Found</h2>
                    <p>The requested group does not exist, or it was deleted.</p>
                </div>
                <% } else { %>
                <% InteractGroupDTO.InteractStatus interactStatus = group.getInteractStatus(); %>
                <div class="group-header">
                    <div class="group-cover-image clickable-cover"
                         style="background-image: url('${pageContext.request.contextPath}/static/images/<%= group.getCoverImage() %>');"
                         data-image-url="${pageContext.request.contextPath}/static/images/<%= group.getCoverImage() %>">
                    </div>
                    <div class="group-info-bar">
                        <div class="group-title-stats">
                            <h1><%= group.getName() %>
                            </h1>
                            <div class="group-stats">
                                <span><i class="fas fa-users"></i> <%= group.getMemberCount() %> Members</span>
                                <span><i class="fas fa-eye"></i> <%= group.getStatus() %></span>
                            </div>
                        </div>
                        <div class="group-actions">
                            <% if (interactStatus == InteractGroupDTO.InteractStatus.UNJOINED) { %>
                            <button type="button" class="btn btn-join" id="openJoinModal"
                                    data-group-id="<%= group.getId() %>">Join Group
                            </button>
                            <% } else if (interactStatus == InteractGroupDTO.InteractStatus.JOINED || interactStatus == InteractGroupDTO.InteractStatus.MANAGER) { %>
                            <form method="POST" style="display:inline;">
                                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                <input type="hidden" name="action" value="leave">
                                <button type="submit" class="btn btn-leave">Leave Group</button>
                            </form>
                            <% } else if (interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>
                            <form method="POST" style="display:inline;">
                                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                <input type="hidden" name="action" value="disband">
                                <button type="submit" class="btn btn-leave">Disband Group</button>
                            </form>
                            <% } else if (interactStatus == InteractGroupDTO.InteractStatus.SENT) { %>
                            <form method="POST" style="display:inline;">
                                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                <input type="hidden" name="action" value="cancel_request">
                                <button type="submit" class="btn btn-cancel">Cancel Request</button>
                            </form>
                            <% } %>
                            <% if (interactStatus == InteractGroupDTO.InteractStatus.MANAGER || interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>
                            <a href="${pageContext.request.contextPath}/groupProfile?groupId=<%= group.getId() %>"
                               class="btn btn-edit">Edit Profile</a>
                            <% } else if (interactStatus == InteractGroupDTO.InteractStatus.JOINED) { %>
                            <button type="button" class="btn btn-feedback" id="openFeedbackModal">Send Feedback</button>
                            <% } %>
                        </div>
                    </div>
                    <div class="group-description">
                        <% String desc = group.getDescription();
                            if (desc != null && desc.length() > 200) { %>
                        <span class="short-desc"><%= desc.substring(0, 200) %>...</span>
                        <span class="full-desc hidden"><%= desc %></span>
                        <button class="read-more-btn">Read More</button>
                        <% } else { %>
                        <%= desc != null ? desc : "No description provided." %>
                        <% } %>
                    </div>
                </div>

                <div class="group-tabs">
                    <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>"
                       class="tab-item <%= request.getParameter("tag") == null || "discussion".equals(request.getParameter("tag")) ? "active" : "" %>">Discussion</a>
                    <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=members"
                       class="tab-item <%= "members".equals(request.getParameter("tag")) ? "active" : "" %>">Members</a>
                    <% if (interactStatus == InteractGroupDTO.InteractStatus.MANAGER || interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>
                    <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=requests"
                       class="tab-item <%= "requests".equals(request.getParameter("tag")) ? "active" : "" %>">Joining
                        Request</a>
                    <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=pending"
                       class="tab-item <%= "pending".equals(request.getParameter("tag")) ? "active" : "" %>">Pending
                        Post</a>
                    <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=feedback"
                       class="tab-item <%= "feedback".equals(request.getParameter("tag")) ? "active" : "" %>">View
                        Feedback</a>
                    <% } %>
                    <% if (interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>
                    <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=report"
                       class="tab-item">Report Content</a>
                    <% } %>
                </div>

                <div class="create-post-bar">
                    <a href="${pageContext.request.contextPath}/post?action=create&groupId=<%= group.getId() %>"
                       class="create-post-link">What's on your mind?</a>
                </div>

                <% if ("requests".equals(request.getParameter("tag")) && (interactStatus == InteractGroupDTO.InteractStatus.MANAGER || interactStatus == InteractGroupDTO.InteractStatus.LEADER)) { %>
                <div class="join-requests-section">
                    <h2 class="feed-header">Joining Requests</h2>
                    <div class="feed-container">
                        <% List<JoinGroupRequestDTO> joinRequests = (List<JoinGroupRequestDTO>) request.getAttribute("joinRequests"); %>
                        <% if (joinRequests == null || joinRequests.isEmpty()) { %>
                        <div class="no-data-message-post">
                            <h3>No Join Requests</h3>
                            <p>There are no pending requests to join this group.</p>
                        </div>
                        <% } else { %>
                        <div class="join-request-list">
                            <% for (JoinGroupRequestDTO joinRequest : joinRequests) { %>
                            <div class="join-request-item">
                                <div class="request-user-info">
                                    <img src="${pageContext.request.contextPath}/static/images/<%= joinRequest.getAvatar() %>"
                                         alt="User Avatar" class="avatar">
                                    <div class="user-details">
                                        <span class="username"><%= joinRequest.getUsername() %></span>
                                        <span class="fullname"><%= joinRequest.getFullname() %></span>
                                        <span class="request-date">Requested on: <%= joinRequest.getCreatedAt() != null ? joinRequest.getCreatedAt() : "N/A" %></span>
                                    </div>
                                </div>
                                <div class="request-content">
                                    <p><%= joinRequest.getMessage() != null ? joinRequest.getMessage() : "No message provided." %>
                                    </p>
                                </div>
                                <div class="request-actions">
                                    <form method="POST" style="display:inline;">
                                        <input type="hidden" name="action" value="approve">
                                        <input type="hidden" name="requestId"
                                               value="<%= joinRequest.getRequesterID() %>">
                                        <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                        <button type="submit" class="btn btn-success">Approve</button>
                                    </form>
                                    <form method="POST" style="display:inline;">
                                        <input type="hidden" name="action" value="reject">
                                        <input type="hidden" name="requestId"
                                               value="<%= joinRequest.getRequesterID() %>">
                                        <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                        <button type="submit" class="btn btn-danger">Reject</button>
                                    </form>
                                </div>
                            </div>
                            <% } %>
                        </div>
                        <% } %>
                    </div>
                </div>
                <% } else { %>
                <% if (pendingPosts != null && !pendingPosts.isEmpty()) { %>
                <div class="post-feed-section">
                    <h2 class="feed-header">Your Pending Posts</h2>
                    <div class="feed">
                        <% for (RespPostDTO post : pendingPosts) { %>
                        <%= post %>
                        <% } %>
                    </div>
                </div>
                <% } %>
                <div class="post-feed-section">
                    <h2 class="feed-header">Group Discussion</h2>
                    <div class="feed">
                        <% if (groupPosts == null || groupPosts.isEmpty()) { %>
                        <div class="no-data-message-post">
                            <h3>It's quiet in here...</h3>
                            <p>Be the first to share something in this group!</p>
                        </div>
                        <% } else { %>
                        <% for (RespPostDTO post : groupPosts) { %>
                        <%= post %>
                        <% } %>
                        <% } %>
                    </div>
                </div>
                <% } %>
                <% } %>
            </main>
        </div>

        <!-- Lightbox HTML structure -->
        <div class="lightbox-overlay" id="lightbox">
            <button class="lightbox-close">×</button>
            <img class="lightbox-image" src="" alt="Full-screen image view">
        </div>

        <!-- Live Search iframe for loading results -->
        <iframe id="liveSearchFrame"
                style="display: none; position: absolute; left: -9999px; width: 1px; height: 1px;"
                name="liveSearchFrame"
                title="Search Results Loader">
        </iframe>

        <!-- Feedback Modal -->
        <div id="feedbackModal" class="modal">
            <div class="modal-content-wrapper">
                <div class="modal-header">
                    <h2>Send Feedback to Managers</h2>
                    <button class="modal-close">×</button>
                </div>
                <form id="feedbackForm" method="POST">
                    <div class="modal-body">
                        <p>Your feedback will be sent privately to the group's leadership.</p>
                        <textarea name="feedbackContent" required placeholder="Type your feedback here..."></textarea>
                        <input type="hidden" name="action" value="send_feedback">
                        <input type="hidden" name="groupId" value="<%= group != null ? group.getId() : "0" %>">
                    </div>
                    <div class="modal-footer">
                        <button type="submit" class="btn-submit">Send</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Join Request Modal -->
        <div id="joinRequestModal" class="modal">
            <div class="modal-content-wrapper">
                <div class="modal-header">
                    <h2>Send Join Request</h2>
                    <button class="modal-close">×</button>
                </div>
                <form id="joinForm" method="POST">
                    <div class="modal-body">
                        <p>You can include an optional message to the group manager(s).</p>
                        <textarea name="joinMessage"
                                  placeholder="E.g., Hi, I'm interested in joining because..."></textarea>
                        <input type="hidden" name="action" value="join">
                        <input type="hidden" id="modalGroupId" name="groupId" value="">
                    </div>
                    <div class="modal-footer">
                        <button type="submit" class="btn-submit join-btn">Send Request</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Image Modal -->
        <div id="imageModal" class="modal">
            <button class="modal-close">×</button>
            <img class="modal-content-image" id="modalImage" src="" alt="Group Cover Image">
        </div>

        <script>
            document.addEventListener('DOMContentLoaded', function () {
                const descContainer = document.querySelector('.group-description');
                if (descContainer) {
                    const readMoreBtn = descContainer.querySelector('.read-more-btn');
                    if (readMoreBtn) {
                        readMoreBtn.addEventListener('click', function () {
                            const shortText = descContainer.querySelector('.short-desc');
                            const fullText = descContainer.querySelector('.full-desc');
                            shortText.classList.toggle('hidden');
                            fullText.classList.toggle('hidden');
                            this.textContent = fullText.classList.contains('hidden') ? 'Read More' : 'Read Less';
                        });
                    }
                }

                const feedbackModal = document.getElementById('feedbackModal');
                const joinModal = document.getElementById('joinRequestModal');
                const imageModal = document.getElementById('imageModal');
                const allModals = document.querySelectorAll('#feedbackModal, #joinRequestModal, #imageModal');

                function closeModal(modal) {
                    if (modal) modal.style.display = 'none';
                }

                allModals.forEach(modal => {
                    const closeBtn = modal.querySelector('.modal-close');
                    if (closeBtn) {
                        closeBtn.addEventListener('click', () => closeModal(modal));
                    }
                });
                window.addEventListener('click', (event) => {
                    allModals.forEach(modal => {
                        if (event.target === modal) closeModal(modal);
                    });
                });

                const openFeedbackBtn = document.getElementById('openFeedbackModal');
                if (openFeedbackBtn) {
                    openFeedbackBtn.addEventListener('click', () => {
                        if (feedbackModal) feedbackModal.style.display = 'flex';
                    });
                }

                const openJoinBtn = document.getElementById('openJoinModal');
                if (openJoinBtn) {
                    openJoinBtn.addEventListener('click', (event) => {
                        const groupId = event.currentTarget.dataset.groupId;
                        if (joinModal) {
                            joinModal.querySelector('#modalGroupId').value = groupId;
                            joinModal.style.display = 'flex';
                        }
                    });
                }

                document.querySelectorAll('.read-more-btn').forEach(button => {
                    button.addEventListener('click', function (event) {
                        event.preventDefault();
                        event.stopPropagation();
                        const parent = this.closest('.group-card-desc');
                        const shortText = parent.querySelector('.short-desc');
                        const fullText = parent.querySelector('.full-desc');
                        shortText.classList.toggle('hidden');
                        fullText.classList.toggle('hidden');
                        this.textContent = fullText.classList.contains('hidden') ? 'more' : 'less';
                    });
                });

                const coverImageElement = document.querySelector('.clickable-cover');
                if (coverImageElement) {
                    coverImageElement.addEventListener('click', function () {
                        document.getElementById('modalImage').src = this.dataset.imageUrl;
                        imageModal.style.display = 'flex';
                    });
                }

                // Profile dropdown menu logic
                const profileNav = document.querySelector('.nav-profile');
                const dropdownMenu = document.querySelector('.dropdown-menu');

                profileNav.addEventListener('click', (e) => {
                    e.stopPropagation();
                    dropdownMenu.classList.toggle('show');
                });

                window.addEventListener('click', (e) => {
                    if (!e.target.matches('.nav-profile, .nav-profile *')) {
                        if (dropdownMenu.classList.contains('show')) {
                            dropdownMenu.classList.remove('show');
                        }
                    }
                });
            });
        </script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
                crossorigin="anonymous"></script>
        <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/composer.js"></script>
        <script src="${pageContext.request.contextPath}/js/search.js"></script>
        <script src="${pageContext.request.contextPath}/js/notification.js"></script>
    </body>
</html>