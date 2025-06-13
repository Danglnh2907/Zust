<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ page import="java.time.LocalDateTime, java.time.Duration" %>
<%@ page import="dto.ResGroupDTO" %>
<%@ page import="model.Account" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>View Group Details</title>

    <!-- Internal CSS -->
    <style>
        :root {
            --primary-color: #FF852F; --text-color-dark: #1c1c1c; --text-color-light: #65676b;
            --border-color: #ced0d4; --bg-color-light: #f0f2f5; --bg-color-white: #ffffff;
            --status-active-bg: #e7f3ff; --status-active-text: #1877f2;
            --status-banned-bg: #fffbe2; --status-banned-text: #f7b928;
            --status-deleted-bg: #fde2e4; --status-deleted-text: #d90429;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg-color-light); margin: 0; padding: 0;
        }
        .group-view-container {
            max-width: 1100px; margin: 20px auto;
        }
        /* Cover Image Section */
        .cover-image-wrapper {
            background-color: #dcdcdc; border-radius: 8px; overflow: hidden;
            height: 350px; position: relative;
        }
        .cover-image { width: 100%; height: 100%; object-fit: cover; }

        /* Main Layout (2 columns) */
        .group-layout {
            display: flex; gap: 20px; margin-top: -80px; padding: 0 20px;
            position: relative; z-index: 2;
        }
        .main-content {
            flex: 2; /* Takes 2/3 of the space */
        }
        .sidebar {
            flex: 1; /* Takes 1/3 of the space */
            margin-top: 80px; /* Aligns with the bottom of the cover image space */
        }

        /* Header in main content */
        .group-header {
            background-color: var(--bg-color-white); padding: 20px;
            border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        }
        .group-name {
            display: flex; align-items: center; gap: 10px;
            font-size: 28px; font-weight: 700; color: var(--text-color-dark); margin: 0;
        }

        /* Status Badge */
        .status-badge {
            font-size: 13px; font-weight: 600; padding: 4px 10px; border-radius: 20px;
        }
        .status-active { background-color: var(--status-active-bg); color: var(--status-active-text); }
        .status-banned { background-color: var(--status-banned-bg); color: var(--status-banned-text); }
        .status-deleted { background-color: var(--status-deleted-bg); color: var(--status-deleted-text); }

        /* Description Box */
        .info-box {
            background-color: var(--bg-color-white); padding: 20px; border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-top: 20px;
        }
        .box-title {
            font-size: 18px; font-weight: 700; color: var(--text-color-dark);
            margin: 0 0 15px 0; padding-bottom: 10px; border-bottom: 1px solid #eee;
        }
        .group-description {
            color: var(--text-color-dark); line-height: 1.6; font-size: 16px;
        }
        .description-full { display: none; }
        .read-more {
            color: var(--primary-color); font-weight: 600; cursor: pointer;
            text-decoration: none; display: inline-block; margin-left: 5px;
        }
        .read-more:hover { text-decoration: underline; }

        /* Sidebar Widgets */
        .stat-item {
            display: flex; align-items: center; gap: 12px; margin-bottom: 15px; font-size: 15px;
        }
        .stat-icon { font-size: 20px; color: var(--text-color-light); }
        .stat-value { color: var(--text-color-dark); font-weight: 500; }

        .admin-list .admin-item {
            display: flex; align-items: center; gap: 12px; margin-bottom: 12px;
        }
        .admin-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
        .admin-info .fullname { font-weight: 600; color: var(--text-color-dark); }
        .admin-info .username { font-size: 13px; color: var(--text-color-light); }

        /* Message for when group is not found */
        .not-found-message {
            text-align: center; padding: 50px; font-size: 20px; color: var(--text-color-light);
        }

        .assign-admin-btn {
            width: 100%; border: 1px solid var(--primary-color); color: var(--primary-color);
            background-color: var(--bg-color-white); padding: 8px; border-radius: 6px;
            font-weight: 600; cursor: pointer; transition: background-color 0.2s, color 0.2s;
            margin-top: 10px;
        }
        .assign-admin-btn:hover { background-color: var(--primary-color); color: white; }

        /* Modal Overlay */
        .modal-overlay {
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background-color: rgba(0, 0, 0, 0.6); z-index: 1000;
            display: none; justify-content: center; align-items: center;
        }
        /* Modal Content */
        .modal-content {
            background-color: var(--bg-color-white); border-radius: 8px;
            width: 90%; max-width: 500px; max-height: 90vh;
            display: flex; flex-direction: column; box-shadow: 0 5px 15px rgba(0,0,0,0.3);
        }
        .modal-header {
            padding: 15px 20px; border-bottom: 1px solid #eee;
            display: flex; justify-content: space-between; align-items: center;
        }
        .modal-title { font-size: 20px; font-weight: 700; margin: 0; }
        .modal-close-btn { font-size: 28px; font-weight: bold; color: #aaa; cursor: pointer; border: none; background: none; }
        .modal-body { padding: 20px; overflow-y: auto; }
        .modal-footer {
            padding: 15px 20px; border-top: 1px solid #eee;
            display: flex; justify-content: flex-end; gap: 10px;
        }
        .modal-btn {
            padding: 10px 20px; border-radius: 6px; font-weight: 600;
            border: none; cursor: pointer; transition: opacity 0.2s;
        }
        .btn-primary { background-color: var(--primary-color); color: white; }
        .btn-secondary { background-color: #e4e6eb; color: #050505; }
        .modal-btn:hover { opacity: 0.85; }

        /* Styling cho danh sách thành viên trong modal (tái sử dụng từ form tạo group) */
        .member-search-input { width: 100%; padding: 10px; border: 1px solid var(--border-color); border-radius: 6px; margin-bottom: 15px; }
        .member-list { list-style: none; padding: 0; margin: 0; }
        .member-item-label { display: block; }
        .member-item { display: flex; align-items: center; padding: 10px; border-radius: 6px; cursor: pointer; border: 2px solid transparent; }
        .member-item:hover { background-color: #f5f5f5; }
        .member-item-label:has(input:checked) { background-color: #fff8f2; border-color: var(--primary-color); }
        .member-item input[type="checkbox"] { margin-right: 15px; width: 18px; height: 18px; accent-color: var(--primary-color); }

    </style>
</head>
<body>

<%-- MOCK DATA: Xóa phần này khi tích hợp servlet. --%>
<%
    ResGroupDTO group = (ResGroupDTO) request.getAttribute("group");
%>

<div class="group-view-container">
    <% if (group != null) { %>
    <!-- 1. Cover Image -->
    <div class="cover-image-wrapper">
        <img src="<%= group.getImage() %>" alt="Group Cover Image" class="cover-image">
    </div>

    <div class="group-layout">
        <!-- Left Column: Main Content -->
        <div class="main-content">
            <div class="group-header">
                <!-- 2. Group Name & 3. Group Status -->
                <h1 class="group-name">
                    <%= group.getName() %>
                    <%
                        String status = group.getStatus();
                        String statusClass = "";
                        if ("active".equalsIgnoreCase(status)) statusClass = "status-active";
                        else if ("banned".equalsIgnoreCase(status)) statusClass = "status-banned";
                        else if ("deleted".equalsIgnoreCase(status)) statusClass = "status-deleted";
                    %>
                    <span class="status-badge <%= statusClass %>"><%= status.substring(0, 1).toUpperCase() + status.substring(1) %></span>
                </h1>
            </div>

            <!-- 4. Group Description -->
            <div class="info-box">
                <h2 class="box-title">About this group</h2>
                <div class="group-description">
                    <%
                        String description = group.getDescription();
                        int previewLength = 200;
                        if (description != null && description.length() > previewLength) {
                    %>
                    <span class="description-preview">
                                <%= description.substring(0, previewLength) %>...
                                <a href="javascript:void(0);" class="read-more" onclick="toggleDescription(this)">Read More</a>
                            </span>
                    <span class="description-full">
                                <%= description %>
                                <a href="javascript:void(0);" class="read-more" onclick="toggleDescription(this)">Show Less</a>
                            </span>
                    <% } else { %>
                    <%= (description == null || description.isEmpty()) ? "No description provided." : description %>
                    <% } %>
                </div>
            </div>
        </div>

        <!-- Right Column: Sidebar -->
        <div class="sidebar">
            <div class="info-box">
                <h2 class="box-title">Group Info</h2>
                <div class="stats-list">
                    <!-- 5. Existence Time -->
                    <div class="stat-item">
                        <span class="stat-icon"></span> <!-- Clock emoji -->
                        <%
                            LocalDateTime now = LocalDateTime.now();
                            Duration duration = Duration.between(group.getCreateDate(), now);
                            long days = duration.toDays();
                            String existenceTime;
                            if (days > 365) existenceTime = (days / 365) + " years old";
                            else if (days > 30) existenceTime = (days / 30) + " months old";
                            else if (days > 7) existenceTime = (days / 7) + " weeks old";
                            else if (days > 0) existenceTime = days + " days old";
                            else existenceTime = "Created today";
                        %>
                        <span class="stat-value"><%= existenceTime %></span>
                    </div>

                    <!-- 6. Member Count -->
                    <div class="stat-item">
                        <span class="stat-icon"></span> <!-- People emoji -->
                        <span class="stat-value"><%= group.getNumberParticipants() %> members</span>
                    </div>

                    <!-- 7. Post Count -->
                    <div class="stat-item">
                        <span class="stat-icon"></span> <!-- Memo emoji -->
                        <span class="stat-value"><%= group.getNumberPosts() %> posts</span>
                    </div>
                </div>
            </div>

            <!-- 8. Administrators -->
            <div class="info-box">
                <h2 class="box-title">Administrators</h2>
                <div class="admin-list">
                    <%
                        List<Account> admins = group.getManagers();
                        if (admins != null && !admins.isEmpty()) {
                            for (Account admin : admins) {
                    %>
                    <div class="admin-item">
                        <img src="<%= admin.getAvatar() %>" alt="Admin Avatar" class="admin-avatar">
                        <div class="admin-info">
                            <div class="fullname"><%= admin.getFullname() %></div>
                            <div class="username">@<%= admin.getUsername() %></div>
                        </div>
                    </div>
                    <%
                        }
                    } else {
                    %>
                    <p style="color: #888;">No administrators listed.</p>
                    <% } %>
                </div>
                <button class="assign-admin-btn" id="openAssignModalBtn">Assign New Admins</button>
            </div>
        </div>
    </div>
    <% } else { %>
    <p class="not-found-message">Group not found or has been removed.</p>
    <% } %>
</div>
<% if (group != null) { %>
<div class="modal-overlay" id="assignAdminModal">
    <div class="modal-content">
        <form action="manage" method="POST">
            <div class="modal-header">
                <h3 class="modal-title">Assign New Administrators</h3>
                <button type="button" class="modal-close-btn" id="closeAssignModalBtn">×</button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                <input type="text" id="memberSearchInput" class="member-search-input" placeholder="Search members...">

                <ul class="member-list" id="memberListContainer">
                    <%
                        List<Account> groupMember = (List<Account>) request.getAttribute("groupMember");
                        if (groupMember != null && !groupMember.isEmpty()) {
                            for (Account member : groupMember) {
                    %>
                    <li class="member-item-label">
                        <div class="member-item">
                            <input type="checkbox" name="newManagerIds" value="<%= member.getId() %>">
                            <img src="<%= member.getAvatar() %>" alt="Avatar" class="admin-avatar">
                            <div class="admin-info">
                                <div class="fullname"><%= member.getFullname() %></div>
                                <div class="username">@<%= member.getUsername() %></div>
                            </div>
                        </div>
                    </li>
                    <%
                        }
                    } else {
                    %>
                    <li><p style="text-align:center; color:#888;">No other members to assign.</p></li>
                    <%
                        }
                    %>
                    <li id="noResultsMessage" style="display: none; text-align: center; color: #888; padding: 20px;">No members found.</li>
                </ul>
            </div>
            <div class="modal-footer">
                <button type="button" class="modal-btn btn-secondary" id="cancelAssignModalBtn">Cancel</button>
                <button type="submit" class="modal-btn btn-primary">Assign Selected</button>
            </div>
        </form>
    </div>
</div>
<% } %>

<script>
    function toggleDescription(linkElement) {
        const container = linkElement.closest('.group-description');
        if (!container) return;

        const preview = container.querySelector('.description-preview');
        const full = container.querySelector('.description-full');

        if (preview.style.display === 'none') {
            preview.style.display = 'inline';
            full.style.display = 'none';
        } else {
            preview.style.display = 'none';
            full.style.display = 'inline';
        }
    }

    const openModalBtn = document.getElementById('openAssignModalBtn');
    const closeModalBtn = document.getElementById('closeAssignModalBtn');
    const cancelModalBtn = document.getElementById('cancelAssignModalBtn');
    const modalOverlay = document.getElementById('assignAdminModal');

    if (openModalBtn) {
        openModalBtn.addEventListener('click', () => {
            modalOverlay.style.display = 'flex';
        });
    }

    function closeModal() {
        modalOverlay.style.display = 'none';
    }

    closeModalBtn.addEventListener('click', closeModal);
    cancelModalBtn.addEventListener('click', closeModal);
    // Đóng modal khi click ra ngoài
    modalOverlay.addEventListener('click', (event) => {
        if (event.target === modalOverlay) {
            closeModal();
        }
    });

    // Logic lọc danh sách thành viên
    const searchInput = document.getElementById('memberSearchInput');
    const memberListContainer = document.getElementById('memberListContainer');
    const memberItems = memberListContainer.querySelectorAll('.member-item-label');
    const noResultsMessage = document.getElementById('noResultsMessage');

    searchInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase().trim();
        let visibleCount = 0;

        memberItems.forEach(function(item) {
            const fullName = item.querySelector('.fullname').textContent.toLowerCase();
            const username = item.querySelector('.username').textContent.toLowerCase();

            if (fullName.includes(searchTerm) || username.includes(searchTerm)) {
                item.style.display = 'block';
                visibleCount++;
            } else {
                item.style.display = 'none';
            }
        });

        noResultsMessage.style.display = (visibleCount === 0) ? 'block' : 'none';
    });
</script>
</body>
</html>