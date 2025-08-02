<%@ page import="model.Account" %>
<%@ page import="model.InteractGroupDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="model.RespPostDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="model.FriendRequest" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<!-- USER PROFILE PAGE -->

<html>
    <head>
        <title>User profile</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <!-- Font Imports -->
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

        <!-- Bootstrap import -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
              rel="stylesheet"
              integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
              crossorigin="anonymous">

        <!-- Quill editor import -->
        <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">

        <!-- Custom CSS import -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/comment.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/composer.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/notification.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/group.css">

        <!-- Custom CSS for this file specifically -->
        <style>
            :root {
                --primary-color: #f56a00;
                --primary-hover-color: #e05a00;
                --secondary-color: #f0f2f5;
                --border-color: #ddd;
                --text-color: #333;
                --text-secondary: #666;
            }

            .profile-layout {
                display: grid;
                grid-template-columns: minmax(0, 1fr) 350px; /* Adjust grid to be more flexible */
                gap: 20px;
                margin-top: 20px;
                padding: 20px;
            }

            .profile-main {
                /* The tab content will go here */
            }

            .profile-side {
                /* The about card will go here */
            }

            .about-card {
                background-color: #fff;
                border-radius: 8px;
                padding: 20px;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            }

            .about-card h3 {
                font-size: 18px;
                font-weight: 700;
                border-bottom: 1px solid var(--border-color);
                padding-bottom: 10px;
                margin-bottom: 15px;
            }

            .about-card ul {
                list-style: none;
                padding: 0;
            }

            .about-card li {
                display: flex;
                align-items: center;
                gap: 10px;
                margin-bottom: 12px;
                font-size: 15px;
            }

            .about-card li i {
                width: 20px;
                text-align: center;
                color: var(--text-secondary);
            }

            .profile-info-container {
                padding: 24px;
                display: flex;
                align-items: center;
                margin-top: -80px;
                position: relative;
                background: rgba(255, 255, 255, 0.8);
                backdrop-filter: blur(10px);
                border-bottom-left-radius: 8px;
                border-bottom-right-radius: 8px;
                margin-left: 20px;
                margin-right: 20px;
            }

            .profile-header {
                background-color: white;
                border-radius: 8px;
                margin-bottom: 20px;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                overflow: hidden;
            }

            .cover-image-container {
                height: 250px;
                background-color: #e9ecef;
            }

            .cover-image {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .profile-info-container {
                padding: 0 24px 24px;
                display: flex;
                align-items: flex-end;
                margin-top: -80px;
                position: relative;
            }

            .profile-avatar-container {
                width: 160px;
                height: 160px;
                border-radius: 50%;
                border: 5px solid white;
                overflow: hidden;
                background-color: white;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                z-index: 1;
            }

            .profile-avatar {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .profile-details {
                margin-left: 24px;
                flex-grow: 1;
                padding-bottom: 10px;
            }

            .profile-name {
                font-size: 28px;
                font-weight: 700;
                color: var(--text-color);
                margin: 0;
            }

            .profile-bio {
                font-size: 16px;
                color: var(--text-secondary);
                margin-top: 4px;
            }

            .profile-stats {
                margin-top: 12px;
                display: flex;
                gap: 24px;
                font-size: 16px;
            }

            .stat-count {
                font-weight: 600;
                color: var(--text-color);
            }

            .profile-actions {
                display: flex;
                gap: 10px;
                padding-bottom: 10px;
            }

            .profile-actions .btn {
                font-weight: 600;
            }

            .profile-actions .btn-primary {
                background-color: var(--primary-color);
                border-color: var(--primary-color);
            }

            .profile-actions .btn-primary:hover {
                background-color: var(--primary-hover-color);
                border-color: var(--primary-hover-color);
            }

            .profile-content {
                background-color: white;
                border-radius: 8px;
                padding: 20px;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            }

            .profile-tabs {
                display: flex;
                border-bottom: 1px solid var(--border-color);
                margin-bottom: 20px;
            }

            .tab-link {
                padding: 10px 20px;
                cursor: pointer;
                border: none;
                background: none;
                font-size: 16px;
                font-weight: 600;
                color: var(--text-secondary);
                position: relative;
            }

            .tab-link.active {
                color: var(--primary-color);
            }

            .tab-link.active::after {
                content: '';
                position: absolute;
                bottom: -1px;
                left: 0;
                width: 100%;
                height: 3px;
                background-color: var(--primary-color);
            }

            .tab-content {
                display: none;
            }

            .tab-content.active {
                display: block;
            }

            .friends-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
                gap: 16px;
            }

            .friend-card {
                display: flex;
                flex-direction: column;
                align-items: center;
                text-align: center;
                padding: 16px;
                border: 1px solid var(--border-color);
                border-radius: 8px;
                transition: box-shadow 0.2s ease, transform 0.2s ease;
            }

            .friend-card:hover {
                box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                transform: translateY(-2px);
            }

            .friend-card img {
                width: 80px;
                height: 80px;
                border-radius: 50%;
                object-fit: cover;
                margin-bottom: 10px;
            }

            .friend-card a {
                text-decoration: none;
                color: var(--text-color);
                font-weight: 600;
                word-break: break-word;
            }

            .friend-card a:hover {
                color: var(--primary-color);
            }

            .friend-requests-list {
                display: flex;
                flex-direction: column;
                gap: 12px;
            }

            .friend-request-item {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 12px;
                border: 1px solid var(--border-color);
                border-radius: 8px;
            }

            .fr-user-info {
                display: flex;
                align-items: center;
                gap: 12px;
            }

            .fr-user-info img {
                width: 50px;
                height: 50px;
                border-radius: 50%;
                object-fit: cover;
            }

            .fr-user-info a {
                text-decoration: none;
                color: var(--text-color);
                font-weight: 600;
            }

            .fr-actions {
                display: flex;
                gap: 8px;
            }
        </style>
    </head>
    <body>
        <!-- Bootstrap modal -->
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

        <!-- Fetch data from request attribute -->
        <%
            Account acc = (Account) request.getSession().getAttribute("users");
            ArrayList<InteractGroupDTO> joinedGroups = (ArrayList<InteractGroupDTO>) request.getAttribute("joinedGroups");
        %>

        <!-- App layout -->
        <div class="app-layout">
            <!-- Left sidebar -->
            <aside class="left-sidebar">
                <!-- Home/My Profile/Create Group section -->
                <div class="logo">Zust</div>
                <nav class="sidebar-nav">
                    <ul>
                        <li>
                            <a href="${pageContext.request.contextPath}/">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                                     viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                                     stroke-linecap="round" stroke-linejoin="round">
                                    <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                                    <polyline points="9 22 9 12 15 12 15 22"></polyline>
                                </svg>
                                <span>Home</span>
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/profile?userId=<%=acc.getId()%>" class="active">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                                     viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                                     stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                    <circle cx="12" cy="7" r="4"></circle>
                                </svg>
                                <span>My Profile</span>
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/group?action=create">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                                     viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                                     stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                    <polyline points="14 2 14 8 20 8"></polyline>
                                    <line x1="12" y1="18" x2="12" y2="12"></line>
                                    <line x1="9" y1="15" x2="15" y2="15"></line>
                                </svg>
                                <span>Create Group</span>
                            </a>
                        </li>
                    </ul>
                </nav>
                <div class="sidebar-divider"></div>

                <!-- Joined group section -->
                <div class="groups-header">
                    <h2>My Groups</h2>
                    <span class="groups-count"><%= joinedGroups != null ? joinedGroups.size() : 0 %></span>
                </div>
                <div class="scrollable-group-list">
                    <%
                        if (joinedGroups != null && !joinedGroups.isEmpty()) {
                    %>
                    <div class="group-list">
                        <%
                            for (InteractGroupDTO currentGroup : joinedGroups) {
                                String status = currentGroup.getStatus() != null ?
                                        currentGroup.getStatus().toLowerCase() : "unknown";
                                String groupAvatar = currentGroup.getCoverImage();
                        %>
                        <a href="${pageContext.request.contextPath}/group?id=<%= currentGroup.getId() %>"
                           class="group-link">
                            <div class="group-item">
                                <img src="${pageContext.request.contextPath}/static/images/<%= groupAvatar %>"
                                     alt="Group Avatar">
                                <div class="group-item-info">
                                    <span><%= currentGroup.getName()%></span>
                                    <span class="members"><%= currentGroup.getMemberCount()%> members</span>
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

            <!-- Main content: profile data -->
            <main class="main-content">
                <!-- Top navigation bar -->
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
                        String linkAvatar = acc.getAvatar();
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
                            <span><%=acc.getFullname()%></span>
                        </a>
                        <div class="dropdown-menu">
                            <a class="dropdown-item" href="${pageContext.request.contextPath}/logout">Log out</a>
                        </div>
                    </div>
                </header>

                <!-- Profile section: profile data -->
                <%
                    //List of this profile's posts (can be empty here, but not null)
                    ArrayList<RespPostDTO> posts = (ArrayList<RespPostDTO>) request.getAttribute("posts");

                    //List of this profile's reposts (can be empty here, but not null)
                    ArrayList<RespPostDTO> reposts = (ArrayList<RespPostDTO>) request.getAttribute("reposts");

                    //Get list of friend of this profile (can be empty here, not null)
                    List<Account> friends = (List<Account>) request.getAttribute("friends");

                    //Get the list of friend request - people that sent friend request to the current profile
                    //(can be empty, not null) -> if the current visitor is visit their own profile
                    List<FriendRequest> friendRequests = (List<FriendRequest>) request.getAttribute("friendRequests");

                    //If the visitor is visiting other profile, there are 2 states:
                    //Visitor is friend with the current profile
                    //Visitor has sent request to the current profile, but the request is still pending
                    //If one is true, the other side must be false
                    boolean areFriends = request.getAttribute("areFriends") != null && (boolean) request.getAttribute("areFriends");
                    boolean isFriendRequestPending = request.getAttribute("friendRequestPending") != null && (boolean) request.getAttribute("friendRequestPending");

                    //Get the current profile
                    Account userProfile = (Account) session.getAttribute("profile");
                %>

                <div class="profile-header">
                    <div class="cover-image-container">
                        <img src="${pageContext.request.contextPath}/static/images/<%= userProfile.getCoverImage() != null ? userProfile.getCoverImage() : "cover.jpg" %>"
                             alt="Cover Image" class="cover-image">
                    </div>
                    <div class="profile-info-container">
                        <div class="profile-avatar-container">
                            <img src="${pageContext.request.contextPath}/static/images/<%= userProfile.getAvatar() != null ? userProfile.getAvatar() : "user.png" %>"
                                 alt="User Avatar" class="profile-avatar">
                        </div>
                        <div class="profile-details">
                            <h1 class="profile-name"><%= userProfile.getUsername() %>
                            </h1>
                            <p class="profile-bio"><%= userProfile.getBio() != null ? userProfile.getBio() : "No bio available." %>
                            </p>
                            <div class="profile-stats">
                                <span><span class="stat-count"><%= posts.size() %></span> Posts</span>
                                <span><span class="stat-count"><%= friends.size() %></span> Friends</span>
                            </div>
                        </div>
                        <div class="profile-actions">
                            <% if (acc.getId().equals(userProfile.getId())) { %>
                            <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#editProfileModal">
                                Edit Profile
                            </button>
                            <a href="${pageContext.request.contextPath}/profile?action=change_password"
                               class="btn btn-secondary">Change Password</a>
                            <% } else { %>
                            <% if (areFriends) { %>
                            <button class="btn btn-secondary" disabled>Friends</button>
                            <button class="btn btn-danger unfriend-btn" data-user-id="<%= userProfile.getId() %>">
                                Unfriend
                            </button>
                            <% } else if (isFriendRequestPending) { %>
                            <button class="btn btn-secondary" disabled>Request Pending</button>
                            <% } else { %>
                            <button class="btn btn-primary add-friend-btn" data-bs-toggle="modal"
                                    data-bs-target="#addFriendModal" data-user-id="<%= userProfile.getId() %>">Add
                                Friend
                            </button>
                            <% } %>
                            <a href="${pageContext.request.contextPath}/report?type=account&id=<%= userProfile.getId() %>"
                               class="btn btn-danger">Report</a>
                            <% } %>
                        </div>
                    </div>
                </div>

                <div class="profile-layout">
                    <div class="profile-main">
                        <div class="profile-content">
                            <div class="profile-tabs">
                                <button class="tab-link active" onclick="openTab(event, 'posts')">Posts</button>
                                <button class="tab-link" onclick="openTab(event, 'reposts')">Reposts</button>
                                <button class="tab-link" onclick="openTab(event, 'friends')">Friends</button>
                                <% if (acc.getId().equals(userProfile.getId())) { %>
                                <button class="tab-link" onclick="openTab(event, 'friend-requests')">Friend Requests
                                </button>
                                <% } %>
                            </div>

                            <div id="posts" class="tab-content active">
                                <div class="feed">
                                    <% if (posts.isEmpty()) { %>
                                    <p>No posts to display.</p>
                                    <% } else {
                                        for (RespPostDTO post : posts) { %>
                                    <%= post.toString() %>
                                    <% }
                                    } %>
                                </div>
                            </div>

                            <div id="reposts" class="tab-content">
                                <div class="feed">
                                    <% if (reposts == null || reposts.isEmpty()) { %>
                                    <p>No reposts to display.</p>
                                    <% } else {
                                        for (RespPostDTO post : reposts) { %>
                                    <%= post.toString() %>
                                    <% }
                                    } %>
                                </div>
                            </div>

                            <div id="friends" class="tab-content">
                                <div class="friends-grid">
                                    <% if (friends.isEmpty()) { %>
                                    <p>No friends to display.</p>
                                    <% } else {
                                        for (Account friend : friends) { %>
                                    <div class="friend-card">
                                        <img src="${pageContext.request.contextPath}/static/images/<%= friend.getAvatar() != null ? friend.getAvatar() : "user.png" %>"
                                             alt="<%= friend.getFullname() %>'s avatar">
                                        <a href="${pageContext.request.contextPath}/profile?userId=<%= friend.getId() %>"><%= friend.getFullname() %>
                                        </a>
                                    </div>
                                    <% }
                                    } %>
                                </div>
                            </div>

                            <% if (acc.getId().equals(userProfile.getId())) { %>
                            <div id="friend-requests" class="tab-content">
                                <div class="friend-requests-list">
                                    <% if (friendRequests == null || friendRequests.isEmpty()) { %>
                                    <p>No friend requests.</p>
                                    <% } else {
                                        for (FriendRequest fr : friendRequests) { %>
                                    <div class="friend-request-item" id="fr-<%= fr.getId() %>">
                                        <div class="fr-user-info">
                                            <img src="${pageContext.request.contextPath}/static/images/<%= fr.getSendAccount().getAvatar() != null ? fr.getSendAccount().getAvatar() : "user.png" %>"
                                                 alt="<%= fr.getSendAccount().getFullname() %>'s avatar">
                                            <a href="${pageContext.request.contextPath}/profile?userId=<%= fr.getSendAccount().getId() %>"><%= fr.getSendAccount().getFullname() %>
                                            </a>
                                        </div>
                                        <div class="fr-actions">
                                            <button class="btn btn-success accept-fr-btn"
                                                    data-request-id="<%= fr.getId() %>"
                                                    data-sender-id="<%= fr.getSendAccount().getId() %>">Accept
                                            </button>
                                            <button class="btn btn-danger reject-fr-btn"
                                                    data-request-id="<%= fr.getId() %>">Reject
                                            </button>
                                        </div>
                                    </div>
                                    <% }
                                    } %>
                                </div>
                            </div>
                            <% } %>
                        </div>
                    </div>
                    <div class="profile-side">
                        <div class="about-card">
                            <h3>About</h3>
                            <ul>
                                <li>
                                    <i class="fas fa-user"></i>
                                    <span>
                                        <%
                                            String fullname = userProfile.getFullname();
                                            fullname = (fullname != null && !fullname.equals("null") && !fullname.isEmpty())
                                                    ? fullname : "N/A";
                                            out.println(fullname);
                                        %>
                                    </span>
                                </li>
                                <li>
                                    <i class="fas fa-venus-mars"></i>
                                    <span>
                                        <%= userProfile.getGender() == null ? "N/A" :
                                                (userProfile.getGender() ? "Male" : "Female") %>
                                    </span>
                                </li>
                                <li>
                                    <i class="fas fa-birthday-cake"></i>
                                    <span>
                                        <%= userProfile.getDob() != null ? userProfile.getDob().toString() : "N/A" %>
                                    </span>
                                </li>
                                <li>
                                    <i class="fas fa-phone"></i>
                                    <span>
                                        <%
                                            String phone = userProfile.getPhone();
                                            phone = (phone != null && !phone.equals("null") && !phone.isEmpty()) ? phone : "N/A";
                                            out.println(phone);
                                        %>
                                    </span>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </main>
        </div>

        <!-- Edit Profile Modal -->
        <div class="modal fade" id="editProfileModal" tabindex="-1" aria-labelledby="editProfileModalLabel"
             aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="editProfileModalLabel">Edit Profile</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form id="editProfileForm" action="${pageContext.request.contextPath}/profile?action=edit"
                              method="post" enctype="multipart/form-data">
                            <div class="mb-3">
                                <label for="fullname" class="form-label">Full Name</label>
                                <input type="text" class="form-control" id="fullname" name="fullname"
                                       value="<%= acc.getFullname() %>">
                            </div>
                            <div class="mb-3">
                                <label for="phone" class="form-label">Phone</label>
                                <input type="text" class="form-control" id="phone" name="phone"
                                       value="<%= acc.getPhone() != null ? acc.getPhone() : "" %>">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Gender</label>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="gender" id="male"
                                           value="<%= acc.getGender() != null ? (acc.getGender() ? "checked" : "") : "true" %>">
                                    <label class="form-check-label" for="male">Male</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="gender" id="female"
                                           value="<%= acc.getGender() != null ? (acc.getGender() ? "checked" : "") : "false" %>">
                                    <label class="form-check-label" for="female">Female</label>
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="dob" class="form-label">Date of Birth</label>
                                <input type="date" class="form-control" id="dob" name="dob"
                                       value="<%= acc.getDob() != null ? acc.getDob().toString() : "" %>">
                            </div>
                            <div class="mb-3">
                                <label for="bio" class="form-label">Bio</label>
                                <textarea class="form-control" id="bio" name="bio"
                                          rows="3"><%= acc.getBio() != null ? acc.getBio() : "" %></textarea>
                            </div>
                            <div class="mb-3">
                                <label for="avatarFile" class="form-label">Avatar</label>
                                <input class="form-control" type="file" id="avatarFile" name="avatarFile">
                            </div>
                            <div class="mb-3">
                                <label for="coverImageFile" class="form-label">Cover Image</label>
                                <input class="form-control" type="file" id="coverImageFile" name="coverImageFile">
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        <button type="submit" form="editProfileForm" class="btn btn-primary" style="background-color: #f56a00">
                            Save changes
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Add Friend Modal -->
        <div class="modal fade" id="addFriendModal" tabindex="-1" aria-labelledby="addFriendModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="addFriendModalLabel">Send Friend Request</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form id="addFriendForm">
                            <div class="mb-3">
                                <label for="friendRequestMessage" class="form-label">Message (optional)</label>
                                <textarea class="form-control" id="friendRequestMessage" rows="3"></textarea>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-primary" id="sendFriendRequestBtn" style="background-color: #f56a00">
                            Send Request
                        </button>
                    </div>
                </div>
            </div>
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

        <script>
            function openTab(evt, tabName) {
                let i, tabContent, tabLink;
                tabContent = document.getElementsByClassName("tab-content");
                for (i = 0; i < tabContent.length; i++) {
                    tabContent[i].style.display = "none";
                }
                tabLink = document.getElementsByClassName("tab-link");
                for (i = 0; i < tabLink.length; i++) {
                    tabLink[i].className = tabLink[i].className.replace(" active", "");
                }
                document.getElementById(tabName).style.display = "block";
                evt.currentTarget.className += " active";
            }

            document.addEventListener('DOMContentLoaded', () => {
                // Activate the first tab by default
                const firstTab = document.querySelector('.tab-link');
                if (firstTab) {
                    firstTab.click();
                }

                // Handle edit profile form submission
                const editProfileForm = document.getElementById('editProfileForm');
                if (editProfileForm) {
                    editProfileForm.addEventListener('submit', function (event) {
                        event.preventDefault();
                        const formData = new FormData(this);
                        fetch(this.action, {
                            method: this.method,
                            body: formData
                        })
                            .then(response => response.json())
                            .then(data => {
                                if (data.status === 'success') {
                                    alert(data.message);
                                    location.reload();
                                } else {
                                    alert('Error: ' + data.message);
                                }
                            })
                            .catch(error => {
                                console.error('Error:', error);
                                alert('An error occurred while updating profile.');
                            });
                    });
                }

                // Add friend functionality
                const addFriendBtn = document.querySelector('.add-friend-btn');
                const sendFriendRequestBtn = document.getElementById('sendFriendRequestBtn');
                if (addFriendBtn && sendFriendRequestBtn) {
                    const addFriendModal = new bootstrap.Modal(document.getElementById('addFriendModal'));

                    sendFriendRequestBtn.addEventListener('click', function () {
                        const userId = addFriendBtn.dataset.userId;
                        const message = document.getElementById('friendRequestMessage').value;

                        const params = new URLSearchParams();
                        params.append('action', 'send');
                        params.append('userId', userId);
                        params.append('content', message);

                        fetch('${pageContext.request.contextPath}/friend_request', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                            body: params
                        }).then(response => response.json())
                            .then(data => {
                                if (data.status === 'success') {
                                    addFriendModal.hide();
                                    addFriendBtn.textContent = 'Request Pending';
                                    addFriendBtn.disabled = true;
                                    addFriendBtn.removeAttribute('data-bs-toggle');
                                    addFriendBtn.removeAttribute('data-bs-target');
                                } else {
                                    alert(data.message);
                                }
                            });
                    });
                }

                // Unfriend functionality
                const unfriendBtn = document.querySelector('.unfriend-btn');
                if (unfriendBtn) {
                    unfriendBtn.addEventListener('click', function () {
                        const userId = this.dataset.userId;
                        if (confirm('Are you sure you want to unfriend this user?')) {
                            const params = new URLSearchParams();
                            params.append('action', 'unfriend');
                            params.append('userId', userId);

                            fetch('${pageContext.request.contextPath}/friend_request', {
                                method: 'POST',
                                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                                body: params
                            }).then(response => response.json())
                                .then(data => {
                                    if (data.status === 'success') {
                                        location.reload();
                                    } else {
                                        alert(data.message);
                                    }
                                });
                        }
                    });
                }

                // Friend request accept/reject functionality
                document.querySelectorAll('.accept-fr-btn, .reject-fr-btn').forEach(button => {
                    button.addEventListener('click', function () {
                        const requestId = this.dataset.requestId;
                        const senderId = this.dataset.senderId;
                        const action = this.classList.contains('accept-fr-btn') ? 'accept' : 'reject';

                        const params = new URLSearchParams();
                        params.append('action', action);
                        params.append('requestId', requestId);
                        if (action === 'accept') {
                            params.append('senderId', senderId);
                        }

                        fetch('${pageContext.request.contextPath}/friend_request', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                            body: params
                        }).then(response => response.json())
                            .then(data => {
                                if (data.status === 'success') {
                                    document.getElementById('fr-' + requestId).remove();
                                } else {
                                    alert(data.message);
                                }
                            });
                    });
                });
            });
        </script>
        <script>
            window.addEventListener('DOMContentLoaded', () => {
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
        <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
                crossorigin="anonymous"></script>
        <script src="${pageContext.request.contextPath}/js/post.js"></script>
        <script src="${pageContext.request.contextPath}/js/comments.js"></script>
        <script src="${pageContext.request.contextPath}/js/composer.js"></script>
        <script src="${pageContext.request.contextPath}/js/search.js"></script>
        <script src="${pageContext.request.contextPath}/js/notification.js"></script>
    </body>
</html>
