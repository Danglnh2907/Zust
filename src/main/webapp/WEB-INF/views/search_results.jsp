<%@ page import="model.Account" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Group" %>
<%@ page import="dao.PostDAO" %>
<%@ page import="model.Post" %><%--
  Created by IntelliJ IDEA.
  User: Asus
  Date: 7/5/2025
  Time: 9:09 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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

        <style>
            /* Card for grouping results */
            .results-card {
                background-color: var(--white);
                border: 1px solid var(--light-gray);
                border-radius: var(--border-radius);
                overflow: hidden;
            }

            .results-header {
                padding: 16px 20px;
                border-bottom: 1px solid var(--light-gray);
            }

            .results-header h2 {
                margin: 0;
                font-size: 1.25rem;
            }

            .entity-list {
                display: flex;
                flex-direction: column;
            }

            /* Individual User/Group Item */
            .entity-item {
                display: flex;
                align-items: center;
                gap: 16px;
                padding: 16px 20px;
                border-bottom: 1px solid var(--light-gray);
            }

            .entity-item:last-child {
                border-bottom: none;
            }

            .entity-avatar {
                width: 48px;
                height: 48px;
                border-radius: 50%;
                flex-shrink: 0;
            }

            a {
                color: black;
                text-decoration: none;
            }

            .entity-info {
                flex-grow: 1;
                min-width: 0;
                /* Prevents text overflow issues in flexbox */
            }

            .entity-name {
                font-weight: bold;
                font-size: 1rem;
            }

            .entity-bio {
                font-size: 0.9rem;
                color: var(--dark-gray);
                margin-top: 4px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .entity-action-btn {
                background-color: transparent;
                color: var(--accent-color);
                border: 1px solid var(--accent-color);
                border-radius: 9999px;
                padding: 8px 16px;
                font-weight: bold;
                cursor: pointer;
                flex-shrink: 0;
                transition: background-color 0.2s, color 0.2s;
            }

            .entity-action-btn:hover {
                background-color: var(--accent-color);
                color: var(--white);
            }

            .show-more-footer {
                padding: 16px;
                text-align: center;
                border-top: 1px solid var(--light-gray);
            }

            .show-more-footer a {
                color: var(--accent-color);
                text-decoration: none;
                font-weight: bold;
            }
        </style>
    </head>
    <body>
        <!-- Modal -->
        <div class="modal fade" id="modal" data-bs-keyboard="false"
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
                        <%Account currentUser = (Account) request.getSession().getAttribute("users");%>
                        <li><a href="${pageContext.request.contextPath}/profile?userId=<%=currentUser.getId()%>">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                                 viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                                 stroke-linecap="round" stroke-linejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                            <span>My Profile</span>
                        </a></li>
                        <li><a href="${pageContext.request.contextPath}/createGroup">
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
                <div class="groups-header">
                    <h2>My Groups</h2>
                    <span class="groups-count">19</span>
                </div>
                <div class="group-list">
                    <div class="group-item">
                        <img src="https://i.pravatar.cc/150?u=group1" alt="Group Avatar">
                        <div class="group-item-info">
                            <span>Websters Shivaji</span>
                            <span class="members">764 members</span>
                        </div>
                    </div>
                    <div class="group-item">
                        <img src="https://i.pravatar.cc/150?u=group2" alt="Group Avatar">
                        <div class="group-item-info">
                            <span>Enactus Shivaji</span>
                            <span class="members">804 members</span>
                        </div>
                    </div>
                    <div class="group-item">
                        <img src="https://i.pravatar.cc/150?u=group3" alt="Group Avatar">
                        <div class="group-item-info">
                            <span>Women Development...</span>
                            <span class="members">104 members</span>
                        </div>
                    </div>
                </div>
                <button class="see-all-btn">See All</button>
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
                        <a href="#" class="nav-profile">
                            <img src="${pageContext.request.contextPath}/static/images/<%=linkAvatar%>"
                                 alt="User Profile Picture">
                            <span><%=account.getFullname()%></span>
                        </a>
                        <div class="dropdown-menu">
                            <a href="${pageContext.request.contextPath}/logout">Log out</a>
                        </div>
                    </div>
                </header>

                <!-- Search result -->
                <div class="feed">
                    <%
                        String keyword = (String) request.getAttribute("keyword");
                        Map<String, List<?>> searchRes = (Map<String, List<?>>) request.getAttribute("searchResults");
                        List<Account> users = (List<Account>) searchRes.get("users");
                        List<Group> groups = (List<Group>) searchRes.get("groups");
                    %>
                    <!-- Users Results Card -->
                    <div class="results-card">
                        <div class="results-header">
                            <h2>People</h2>
                        </div>
                        <div class="entity-list">
                            <%
                                //Display users (max to 5)
                                for (int i = 0; i < 5 && i < users.size(); i++) {
                                    Account acc = users.get(i);
                            %>
                            <div class="entity-item">
                                <img class="entity-avatar"
                                     src="${pageContext.request.contextPath}/static/images/<%= acc.getAvatar() %>"
                                     alt="User Avatar">
                                <div class="entity-info">
                                    <div class="entity-name">
                                        <a href="${pageContext.request.contextPath}/profile?userId=<%= acc.getId() %>"><%= acc.getUsername() %></a>
                                    </div>
                                    <div class="entity-bio"><%= acc.getBio() != null ? acc.getBio() : "" %>
                                    </div>
                                </div>
                                <button class="entity-action-btn" onclick="sendFriendRequest(<%= acc.getId() %>)">Add Friend</button>
                            </div>
                            <%
                                }
                            %>
                        </div>
                        <%
                            //Display the show more button if more than 5 users found
                            if (users.size() > 5) {
                        %>
                        <div class="show-more-footer">
                            <a href="${pageContext.request.contextPath}/search?action=viewMore&category=users&keyword=<%=keyword%>">
                                Show more results
                            </a>
                        </div>
                        <%
                            }
                        %>
                    </div>

                    <!-- Groups Results Card -->
                    <div class="results-card">
                        <div class="results-header">
                            <h2>Group</h2>
                        </div>
                        <div class="entity-list">
                            <%
                                //Display groups (max to 5)
                                for (int i = 0; i < 5 && i < groups.size(); i++) {
                                    Group grp = groups.get(i);
                            %>
                            <div class="entity-item">
                                <img class="entity-avatar"
                                     src="${pageContext.request.contextPath}/static/images/<%= grp.getGroupCoverImage() %>"
                                     alt="User Avatar">
                                <div class="entity-info">
                                    <div class="entity-name"><%= grp.getGroupName() %>
                                    </div>
                                    <div class="entity-bio"><%= grp.getGroupDescription() != null ? grp.getGroupDescription() : "" %>
                                    </div>
                                </div>
                                <button class="entity-action-btn">Join group</button>
                            </div>
                            <%
                                }
                            %>
                        </div>
                        <%
                            //Display the show more button if more than 5 groups found
                            if (groups.size() > 5) {
                        %>
                        <div class="show-more-footer">
                            <a href="${pageContext.request.contextPath}/search?action=viewMore&category=groups&keyword=<%=keyword%>">
                                Show more results
                            </a>
                        </div>
                        <%
                            }
                        %>
                    </div>

                    <!-- Posts Results -->
                    <h3 class="posts-results-header">Related Posts</h3>
                    <%
                        //Display posts
                        if (searchRes.get("posts_content").isEmpty() && searchRes.get("posts_hashtag").isEmpty()) {
                            out.println("<p style=\"color: red;\">No posts found</p>");
                        } else {
                            PostDAO dao = new PostDAO();
                            for (Object post : searchRes.get("posts_content")) {
                                out.println(dao.getPost(((Post) post).getId(), account.getId()));
                            }

                            for (Object post : searchRes.get("posts_hashtag")) {
                                out.println(dao.getPost(((Post) post).getId(), account.getId()));
                            }
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
        <script src="${pageContext.request.contextPath}/js/composer.js"></script>
        <script src="${pageContext.request.contextPath}/js/search.js"></script>
        <script>
            function sendFriendRequest(userId) {
                fetch('${pageContext.request.contextPath}/friend_request', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: new URLSearchParams({
                        'action': 'send',
                        'userId': userId,
                        'content': ''
                    })
                })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success') {
                        alert('Friend request sent!');
                    } else {
                        alert('Error: ' + data.message);
                    }
                })
                .catch(error => console.error('Error:', error));
            }
        </script>
    </body>
</html>
