<%@ page import="model.Account" %>
<%@ page import="model.RespPostDTO" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="model.RespCommentDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.InteractGroupDTO" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<!-- FULL POST PAGE -->

<html>
    <head>
        <title>Title</title>
        <!-- Font Imports -->
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

        <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
              rel="stylesheet"
              integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
              crossorigin="anonymous">

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/composer.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/comment.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/group.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/notification.css">
    </head>
    <body>
        <%
            Account account = (Account) request.getAttribute("account");
            if (account == null) {
                request.getRequestDispatcher("/auth").forward(request, response);
            }

            String message = (String) request.getAttribute("message");
            if (message != null) {
                out.println(message);
                return;
            }

            RespPostDTO post = (RespPostDTO) request.getAttribute("post");

            //Get total comments and comment
            int totalComments = (Integer) request.getAttribute("total_comments");
            LinkedHashMap<RespCommentDTO, ArrayList<RespCommentDTO>> comments =
                    (LinkedHashMap<RespCommentDTO, ArrayList<RespCommentDTO>>) request.getAttribute("comments");
        %>

        <!-- Modal -->
        <div class="modal fade" id="modal" data-bs-backdrop="static" data-bs-keyboard="false"
             tabindex="-1" aria-labelledby="modal-label" aria-hidden="true">
            <div class="modal-dialog modal-dialog-scrollable modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="modal-title-label"></h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                                aria-label="Close"></button>
                    </div>
                    <div class="modal-body" id="modal-body">
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="app-layout">
            <!-- Left Sidebar -->
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
                        <li><a href="${pageContext.request.contextPath}/profile?userId=<%=account.getId()%>">
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

                <% List<InteractGroupDTO> groups = (List<InteractGroupDTO>) request.getAttribute("joinedGroups");%>
                <div class="groups-header">
                    <h2>My Groups</h2>
                    <span class="groups-count"><%= groups != null ? groups.size() : 0 %></span>
                </div>
                <div class="scrollable-group-list">
                    <%
                        if (groups != null && !groups.isEmpty()) {
                    %>
                    <div class="group-list">
                        <%
                            for (InteractGroupDTO group : groups) {
                                String status = group.getStatus() != null ? group.getStatus().toLowerCase() : "unknown";
                        %>
                        <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>" class="group-link">
                            <div class="group-item">
                                <img src="${pageContext.request.contextPath}/static/images/<%= group.getCoverImage()%>"
                                     alt="Group Avatar">
                                <div class="group-item-info">
                                    <span><%= group.getName()%></span>
                                    <span class="members"><%= group.getMemberCount()%> members</span>
                                    <span class="status-badge status-<%= status %>"><%= status %></span>
                                </div>
                            </div>
                        </a>
                        <%
                            }
                        %>
                    </div>
                    <%
                    } else {
                    %>
                    <div class="no-data-message">
                        <div class="icon"><i class="fas fa-search"></i></div>
                        <h2>No Groups Found</h2>
                    </div>
                    <%
                        }
                    %>
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

                <div class="feed">
                    <!-- Post -->
                    <%
                        if (post != null) {
                            out.println(post);
                    %>

                            <!-- Comment section -->
                            <div class="comment-section" id="comment-section">
                                <h2>Comments (<span id="comment-count"><%= totalComments %></span>)</h2>

                                <div class="comment-form">
                                    <div class="comment-form-body">
                                        <img class="post-avatar"
                                             src="${pageContext.request.contextPath}/static/images/<%=account.getAvatar()%>"
                                             alt="Your Avatar">
                                        <div class="comment-form-main">
                                            <div class="reply-indicator" id="reply-indicator">
                                                <span>Replying to <b id="reply-to-handle"></b></span>
                                                <button class="cancel-reply-btn" id="cancel-reply-btn"
                                                        title="Cancel reply">×
                                                </button>
                                            </div>
                                            <textarea id="comment-textarea" placeholder="Add a comment..."></textarea>
                                            <div class="comment-image-preview" id="comment-image-preview">
                                                <button
                                                        class="remove-image-btn" id="remove-image-btn">×
                                                </button>
                                                <img id="preview-image"
                                                     src="" alt="Image preview"></div>
                                            <div class="comment-form-actions">
                                                <button class="icon-btn" id="add-comment-image-btn" title="Add image">
                                                    <svg
                                                            xmlns="http://www.w3.org/2000/svg" width="20" height="20"
                                                            viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                                                            stroke-linecap="round" stroke-linejoin="round">
                                                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                                        <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                                        <polyline points="21 15 16 10 5 21"></polyline>
                                                    </svg>
                                                </button>
                                                <button class="post-comment-btn" id="post-comment-btn" disabled>Post
                                                    Comment
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="comment-list" id="comment-list">
                                    <%
                                        for (Map.Entry<RespCommentDTO, ArrayList<RespCommentDTO>> entry : comments.entrySet()) {
                                            out.println(entry.getKey());
                                            for (RespCommentDTO reply : entry.getValue()) {
                                                out.println(reply);
                                            }
                                        }
                                    %>
                                </div>
                            </div>
                    <%
                        }
                    %>
                </div>
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

        <!-- Load JS files at the end -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
                crossorigin="anonymous"></script>
        <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/post.js"></script>
        <script src="${pageContext.request.contextPath}/js/comment.js"></script>
        <script src="${pageContext.request.contextPath}/js/composer.js"></script>
        <script src="${pageContext.request.contextPath}/js/search.js"></script>
        <script src="${pageContext.request.contextPath}/js/notification.js"></script>
        <script>
            document.addEventListener("DOMContentLoaded", () => {
                attachListener(<%= post.getPostId() %>)
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
    </body>
</html>
