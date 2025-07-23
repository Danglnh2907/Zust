<%@ page import="model.RespPostDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="model.Account" %>
<%@ page import="java.util.List" %>
<%@ page import="model.FriendRequest" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Profile</title>

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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/composer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/comment.css">
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

<!-- Top Navigation Bar -->
<header class="top-navbar">
    <!-- Logo -->
    <a class="logo" href="${pageContext.request.contextPath}/">Zust</a>
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

<div class="app-layout">
    <main class="main-content">
        <div class="profile-cover">
            <%
                Account profile = (Account) request.getSession().getAttribute("userProfile");
                String profileAvatar = profile.getAvatar();
                String profileCoverImage = profile.getCoverImage();
            %>
            <img class="profile-cover-image"
                 src="${pageContext.request.contextPath}/static/images/<%=profileCoverImage%>"
                 alt="Profile cover image">

            <div class="profile-header-details">
                <div class="profile-avatar-bio">
                    <img class="profile-avatar"
                         src="${pageContext.request.contextPath}/static/images/<%=profileAvatar%>" alt="User Avatar">
                    <div class="profile-bio">
                        <h2 class="profile-name"><%=profile.getFullname()%>
                        </h2>
                        <p class="profile-short-bio"><%=profile.getBio() == null ? "" : profile.getBio()%>
                        </p>
                    </div>
                </div>
                <div class="profile-actions">
                    <%-- Add Friend Button --%>
                    <% Account currentUser = (Account) request.getSession().getAttribute("users");
                        if (currentUser != null && currentUser.getId().equals(profile.getId())) { %>
                    <button class="btn edit-profile-btn" data-bs-toggle="modal" data-bs-target="#editProfileModal">
                        <i class="fa-solid fa-user-edit"></i> Edit Profile
                    </button>
                    <a class="btn change-password-btn" href="${pageContext.request.contextPath}/change_password">
                        <i class="fa-solid fa-change-password"></i> Change Password
                    </a>
                    <% } else {
                        if (currentUser != null && !currentUser.getId().equals(profile.getId())) {
                        Boolean areFriends = (Boolean) request.getAttribute("areFriends");
                        Boolean friendRequestPending = (Boolean) request.getAttribute("friendRequestPending");

                        if (areFriends != null && areFriends) { %>
                    <button class="btn add-friend-btn unfriend-btn" data-user-id="<%= profile.getId() %>">
                        <i class="fa-solid fa-user-minus"></i> Unfriend
                    </button>
                    <% } else if (friendRequestPending != null && friendRequestPending) { %>
                    <button class="btn add-friend-btn pending-btn" disabled>
                        <i class="fa-solid fa-clock"></i> Request Sent
                    </button>
                    <% } else { %>
                    <button class="btn add-friend-btn" data-bs-toggle="modal" data-bs-target="#friendRequestModal">
                        <i class="fa-solid fa-user-plus"></i> Add Friend
                    </button>
                    <% }
                    %>
                        <a class="btn report-btn" href="${pageContext.request.contextPath}/report?type=account&id=<%= profile.getId()%>">
                            <i class="fas fa-flag"></i>Report
                        </a>
                    <%
                    }
                    }%>
                </div>
            </div>

            <!-- Friend Request Modal -->
            <div class="modal fade" id="friendRequestModal" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="friendRequestModalLabel">Send Friend Request</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <form>
                                <div class="mb-3">
                                    <label for="friendRequestContent" class="col-form-label">Message:</label>
                                    <textarea class="form-control" id="friendRequestContent" rows="3"
                                              placeholder="Write a message..."></textarea>
                                </div>
                            </form>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <button type="button" class="btn btn-primary" id="sendFriendRequestBtn">Send Request
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <jsp:include page="edit_profile.jsp"/>

            <div class="about-section">
                <div class="about-info">
                    <h3>About</h3>
                    <ul class="info-list">
                        <li>
                            <i class="fa-solid fa-user"></i> <%=profile.getGender() == null ? "Not set" : (profile.getGender() ? "Male" : "Female")%>
                        </li>
                        <li><i class="fa-solid fa-calendar-days"></i>
                            Born <%=profile.getDob() == null ? "Not set" : profile.getDob()%>
                        </li>
                        <li><i class="fa-solid fa-envelope"></i> <%=profile.getEmail()%>
                        </li>
                        <li>
                            <i class="fa-solid fa-phone"></i> <%=profile.getPhone() != null ? profile.getPhone() : "Not set"%>
                        </li>
                        <li>
                            <button class="social-credit-btn">Social Credit: <%=profile.getCredit()%>
                            </button>
                        </li>
                    </ul>
                </div>
                <div class="about-feed">

                    <div class="feed-tabs">
                        <a href="#posts" class="tab-link active-tab">Posts</a>
                        <a href="#reposts" class="tab-link inactive-tab">Reposts</a>
                        <a href="#friends" class="tab-link inactive-tab">Friends</a>
                        <div class="active-indicator"></div>
                    </div>

                    <div class="tab-content-container">
                        <!-- Posts Content -->
                        <div id="posts" class="tab-pane active">
                            <% ArrayList<RespPostDTO> posts = (ArrayList<RespPostDTO>) request.getAttribute("posts");
                                if (posts == null || posts.isEmpty()) {
                                    out.println("<p style=\"padding: 20px; text-align: center; color: #6c757d;\">This user has not made any posts yet.</p>");
                                } else {
                                    for (RespPostDTO post : posts) {
                                        out.println(post);
                                    }
                                } %>
                        </div>

                        <!-- Reposts Content -->
                        <div id="reposts" class="tab-pane">
                            <p style="padding: 20px; text-align: center; color: #6c757d;">Reposts will be shown
                                here.</p>
                        </div>

                        <!-- Friends Content -->
                        <div id="friends" class="tab-pane">

                            <c:if test="${not empty friendRequests}">
                                <div class="friend-requests-container" style="margin-bottom: 20px;">
                                    <h2>Friend Requests</h2>
                                    <c:forEach var="request" items="${friendRequests}">
                                        <div class="friend-request">
                                            <div class="user-profile">
                                                <img src="${pageContext.request.contextPath}/static/images/${request.sendAccount.avatar}"
                                                     alt="User Avatar" class="avatar">
                                                <div style="display: flex; flex-direction: column">
                                                    <span class="name">${request.sendAccount.username}</span>
                                                    <span class="request-content"
                                                          style="font-size: 14px; color: #65676b;">${request.friendRequestContent}</span>
                                                </div>
                                            </div>
                                            <div class="actions">
                                                <button class="btn btn-accept"
                                                        onclick="handleRequest(${request.id}, ${request.sendAccount.id}, 'accept')">
                                                    Accept
                                                </button>
                                                <button class="btn btn-decline"
                                                        onclick="handleRequest(${request.id}, ${request.sendAccount.id}, 'reject')">
                                                    Decline
                                                </button>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:if>

                            <div class="friends-list-container">
                                <h3>Friends</h3>
                                <c:if test="${empty friends}">
                                    <p style="padding: 20px; text-align: center; color: #6c757d;">No friends to
                                        show.</p>
                                </c:if>
                                <ul>
                                    <c:forEach var="friend" items="${friends}">
                                        <li class="friend-item">
                                            <a href="${pageContext.request.contextPath}/profile?userId=${friend.id}"
                                               style="text-decoration: none; color: inherit; display: flex; align-items: center;">
                                                <div class="avatar-wrapper">
                                                    <img class="avatar"
                                                         src="${pageContext.request.contextPath}/static/images/${friend.avatar}"
                                                         alt="${friend.fullname} avatar">
                                                </div>
                                                <span class="name">${friend.fullname}</span>
                                            </a>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
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
<script src="${pageContext.request.contextPath}/js/search.js"></script>
<script src="${pageContext.request.contextPath}/js/composer.js"></script>
<script src="${pageContext.request.contextPath}/js/comments.js"></script>
<script>
    // JavaScript for Profile Tabs
    document.addEventListener('DOMContentLoaded', function () {
        const tabs = document.querySelectorAll('.feed-tabs .tab-link');
        const panes = document.querySelectorAll('.about-feed .tab-pane');
        const indicator = document.querySelector('.feed-tabs .active-indicator');

        // Function to set the indicator position
        const setIndicator = (element) => {
            indicator.style.left = `${element.offsetLeft}px`;
            indicator.style.width = `${element.offsetWidth}px`;
        };

        // Set initial indicator position for the active tab
        const initialActiveTab = document.querySelector('.feed-tabs .active-tab');
        if (initialActiveTab) {
            setIndicator(initialActiveTab);
        }

        tabs.forEach(tab => {
            tab.addEventListener('click', function (event) {
                event.preventDefault();

                // 1. Update Tab Classes
                tabs.forEach(t => {
                    t.classList.remove('active-tab');
                    t.classList.add('inactive-tab');
                });
                this.classList.remove('inactive-tab');
                this.classList.add('active-tab');

                // 2. Update Content Panes
                const targetPaneId = this.getAttribute('href').substring(1);
                panes.forEach(p => {
                    p.classList.remove('active');
                    if (p.id === targetPaneId) {
                        p.classList.add('active');
                    }
                });

                // 3. Move the indicator
                setIndicator(this);
            });
        });

        // Optional: Recalculate indicator on window resize
        window.addEventListener('resize', () => {
            const currentActiveTab = document.querySelector('.feed-tabs .active-tab');
            if (currentActiveTab) {
                setIndicator(currentActiveTab);
            }
        });
    });
</script>
<script>
    function handleRequest(requestId, senderId, action) {
        fetch('${pageContext.request.contextPath}/friend_request', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'requestId=' + requestId + '&senderId=' + senderId + '&action=' + action
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                    location.reload();
                } else {
                    alert(data.message);
                }
            })
            .catch(error => console.error('Error:', error));
    }

    document.addEventListener('DOMContentLoaded', function () {
        const sendFriendRequestBtn = document.getElementById('sendFriendRequestBtn');
        if (sendFriendRequestBtn) {
            sendFriendRequestBtn.addEventListener('click', function () {
                const profileId = <%= profile.getId() %>;
                const content = document.getElementById('friendRequestContent').value;

                fetch('/zust/friend_request', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'userId=' + profileId + '&content=' + encodeURIComponent(content) + '&action=send'
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.status === 'success') {
                            const friendRequestModal = bootstrap.Modal.getInstance(document.getElementById('friendRequestModal'));
                            friendRequestModal.hide();

                            const button = document.querySelector('.add-friend-btn');
                            button.innerHTML = '<i class="fa-solid fa-clock"></i> Request Sent';
                            button.classList.add('pending-btn');
                            button.disabled = true;
                        } else {
                            alert('Error: ' + data.message);
                        }
                    })
                    .catch(error => console.error('Error:', error));
            });
        }

        const unfriendBtn = document.querySelector('.unfriend-btn');
        if (unfriendBtn) {
            unfriendBtn.addEventListener('click', function () {
                const profileId = <%= profile.getId() %>;
                fetch('/zust/friend_request', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'userId=' + profileId + '&action=unfriend'
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.status === 'success') {
                            location.reload();
                        } else {
                            alert('Error: ' + data.message);
                        }
                    })
                    .catch(error => console.error('Error:', error));
            });
        }

        // Edit Profile Modal Logic
        const editProfileModal = document.getElementById('editProfileModal');
        if (editProfileModal) {
            editProfileModal.addEventListener('show.bs.modal', function (event) {
                const profileData = {
                    id: <%= profile.getId() %>,
                    fullname: '<%= profile.getFullname() %>',
                    username: '<%= profile.getUsername() %>',
                    email: '<%= profile.getEmail() %>',
                    phone: '<%= profile.getPhone() != null ? profile.getPhone() : "" %>',
                    gender: <%= profile.getGender() %>,
                    dob: '<%= profile.getDob() != null ? profile.getDob().toString() : "" %>',
                    bio: '<%= profile.getBio() != null ? profile.getBio() : "" %>',
                    avatar: '<%= profile.getAvatar() != null ? profile.getAvatar() : "" %>',
                    coverImage: '<%= profile.getCoverImage() != null ? profile.getCoverImage() : "" %>'
                };

                document.getElementById('editFullname').value = profileData.fullname;
                document.getElementById('editUsername').value = profileData.username;
                document.getElementById('editEmail').value = profileData.email;
                document.getElementById('editPhone').value = profileData.phone || '';
                document.getElementById('editBio').value = profileData.bio || '';
                document.getElementById('editAvatar').value = ''; // Clear file input
                document.getElementById('currentAvatar').value = profileData.avatar || '';
                document.getElementById('editCoverImage').value = ''; // Clear file input
                document.getElementById('currentCoverImage').value = profileData.coverImage || '';

                if (profileData.gender) {
                    document.getElementById('editMale').checked = true;
                } else {
                    document.getElementById('editFemale').checked = true;
                }

                if (profileData.dob) {
                    document.getElementById('editDob').value = profileData.dob;
                } else {
                    document.getElementById('editDob').value = '';
                }
            });

            document.getElementById('saveProfileChanges').addEventListener('click', function () {
                const formData = new FormData();

                formData.append('fullname', document.getElementById('editFullname').value);
                formData.append('username', document.getElementById('editUsername').value);
                formData.append('email', document.getElementById('editEmail').value);
                formData.append('phone', document.getElementById('editPhone').value);
                formData.append('gender', document.querySelector('input[name="gender"]:checked').value);
                formData.append('dob', document.getElementById('editDob').value);
                formData.append('bio', document.getElementById('editBio').value);

                const avatarFile = document.getElementById('editAvatar').files[0];
                if (avatarFile) {
                    formData.append('avatarFile', avatarFile);
                } else {
                    formData.append('avatar', document.getElementById('currentAvatar').value);
                }

                const coverImageFile = document.getElementById('editCoverImage').files[0];
                if (coverImageFile) {
                    formData.append('coverImageFile', coverImageFile);
                } else {
                    formData.append('coverImage', document.getElementById('currentCoverImage').value);
                }

                fetch('/zust/profile?action=edit', { // Assuming a servlet mapped to /zust/editProfile
                    method: 'POST',
                    body: formData
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.status === 'success') {
                            alert('Profile updated successfully!');
                            bootstrap.Modal.getInstance(editProfileModal).hide();
                            location.reload(); // Reload page to reflect changes
                        } else {
                            alert('Error updating profile: ' + data.message);
                        }
                    })
                    .catch(error => console.error('Error:', error));
            });
        }
    });
</script>
</body>
</html>