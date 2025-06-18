<%@ page import="dto.RespPostDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Account" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>View posts</title>

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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">
</head>

<body>
<!-- Edit Post Modal -->
<div class="modal fade" id="edit_post" data-bs-backdrop="static" data-bs-keyboard="false"
     tabindex="-1" aria-labelledby="edit_post_label" aria-hidden="true">
    <div class="modal-dialog modal-dialog-scrollable modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="edit_post_label">Edit Post</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"
                        aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <select class="form-select mb-3" aria-label="Post privacy" id="edit_post_privacy">
                    <option value="public">Public</option>
                    <option value="private">Private</option>
                    <option value="friend">Friend</option>
                </select>
                <div id="edit-editor-container">
                    <div id="edit-editor"></div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary" id="save-edit">Save changes</button>
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
                <li><a href="#" class="active">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                         viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                         stroke-linecap="round" stroke-linejoin="round">
                        <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                        <polyline points="9 22 9 12 15 12 15 22"></polyline>
                    </svg>
                    <span>Home</span>
                </a></li>
                <li><a href="#">
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
            <a class="create-post-btn" href="${pageContext.request.contextPath}/post?action=create">Create Post</a>

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
            <a href="#" class="nav-profile">
                <img src="${pageContext.request.contextPath}/static/images/<%=linkAvatar%>" alt="User Profile Picture">
                <span><%=account.getFullname()%></span>
            </a>
        </header>

        <!-- Feed of Posts -->
        <div class="feed">
            <% ArrayList<RespPostDTO> posts = (ArrayList<RespPostDTO>) request.getAttribute("posts");
                if (posts == null || posts.isEmpty()) {
                    out.println("<p style=\"color: red;\">No posts found</p>");
                } else {
                    for (RespPostDTO post : posts) {
                        out.println(post);
                    }
                } %>
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
<script src="${pageContext.request.contextPath}/js/post.js"></script>
<script src="${pageContext.request.contextPath}/js/search.js"></script>
</body>
</html>
