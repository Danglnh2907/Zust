<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<%@ page import="model.Account" %>
<%@ page import="dto.ResGroupDTO" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Group Managers</title>

    <style>
        :root {
            --primary-color: #FF852F; --primary-color-dark: #E67222;
            --text-color-dark: #1c1c1c; --text-color-light: #65676b;
            --border-color: #e0e0e0; --bg-color-light: #f0f2f5; --bg-color-white: #ffffff;
            --status-active-bg: #e7f3ff; --status-active-text: #1877f2;
            --status-inactive-bg: #fde2e4; --status-inactive-text: #d90429;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg-color-light); margin: 0; padding: 20px;
        }
        .container {
            max-width: 1400px; margin: 0 auto;
        }
        .manager-dashboard {
            background-color: var(--bg-color-white); border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.08); overflow: hidden;
        }
        .dashboard-header {
            display: flex; justify-content: space-between; align-items: center;
            padding: 20px 25px; border-bottom: 1px solid var(--border-color);
        }
        .header-title h1 {
            font-size: 24px; font-weight: 700; color: var(--text-color-dark); margin: 0;
        }
        .header-title p {
            font-size: 15px; color: var(--text-color-light); margin: 4px 0 0;
        }
        .header-actions .btn-primary {
            background-color: var(--primary-color); color: white; padding: 10px 20px;
            border-radius: 6px; font-weight: 600; text-decoration: none; border: none; cursor: pointer;
        }
        .dashboard-content { padding: 10px; }
        .manager-table { width: 100%; border-collapse: collapse; font-size: 14px; }
        .manager-table th, .manager-table td {
            padding: 12px 15px; text-align: left; border-bottom: 1px solid var(--border-color);
            vertical-align: middle;
        }
        .manager-table thead th {
            background-color: #f9fafb; font-weight: 600; color: var(--text-color-light);
            font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px;
        }
        .manager-table tbody tr:last-child td { border-bottom: none; }
        .manager-table tbody tr:hover { background-color: #f7f7f7; }
        .user-info { display: flex; align-items: center; gap: 12px; }
        .user-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
        .user-details .fullname { font-weight: 600; color: var(--text-color-dark); }
        .user-details .username { font-size: 13px; color: var(--text-color-light); }
        .status-badge {
            display: inline-block; padding: 4px 10px; border-radius: 20px; font-weight: 500;
        }
        .status-active { background-color: var(--status-active-bg); color: var(--status-active-text); }
        .status-inactive { background-color: var(--status-inactive-bg); color: var(--status-inactive-text); }
        .no-data-message { text-align: center; padding: 40px; color: var(--text-color-light); }

        /* === CSS CHO MODAL (Tái sử dụng) === */
        .modal-overlay {
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background-color: rgba(0, 0, 0, 0.6); z-index: 1000;
            display: none; justify-content: center; align-items: center;
        }
        .modal-content {
            background-color: var(--bg-color-white); border-radius: 8px;
            width: 90%; max-width: 500px; max-height: 90vh;
            display: flex; flex-direction: column; box-shadow: 0 5px 15px rgba(0,0,0,0.3);
        }
        .modal-header, .modal-footer, .modal-body { padding: 20px; }
        .modal-header { border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
        .modal-title { font-size: 20px; font-weight: 700; margin: 0; }
        .modal-close-btn { font-size: 28px; cursor: pointer; border: none; background: none; color: #aaa; }
        .modal-body { overflow-y: auto; }
        .modal-footer { border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
        .modal-btn { padding: 10px 20px; border-radius: 6px; font-weight: 600; border: none; cursor: pointer; }
        .btn-primary { background-color: var(--primary-color); color: white; }
        .btn-secondary { background-color: #e4e6eb; color: #050505; }
        .member-search-input { width: 100%; padding: 10px; border: 1px solid var(--border-color); border-radius: 6px; margin-bottom: 15px; box-sizing: border-box; }
        .member-list { list-style: none; padding: 0; margin: 0; }
        .member-item-label { display: block; }
        .member-item { display: flex; align-items: center; padding: 10px; border-radius: 6px; cursor: pointer; border: 2px solid transparent; }
        .member-item:hover { background-color: #f5f5f5; }
        .member-item-label:has(input:checked) { background-color: #fff8f2; border-color: var(--primary-color); }
        .member-item input[type="checkbox"] { margin-right: 15px; width: 18px; height: 18px; accent-color: var(--primary-color); }
    </style>
</head>
<body>
<%
    ResGroupDTO group = (ResGroupDTO) request.getAttribute("group");
%>

<div class="container">
    <div class="manager-dashboard">
        <div class="dashboard-header">
            <div class="header-title">
                <h1>Group Managers</h1>
                <p>For group: <strong><%= group.getName() %></strong></p>
            </div>
            <div class="header-actions">
                <button class="btn-primary" id="openAssignModalBtn">Assign New Manager</button>
            </div>
        </div>
        <div class="dashboard-content">
            <table class="manager-table">
                <thead>
                <tr>
                    <th>Manager</th>
                    <th>Contact Info</th>
                    <th>Gender</th>
                    <th>Date of Birth</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <%
                    List<Account> currentManagers = (List<Account>) group.getManagers();
                    if (currentManagers == null || currentManagers.isEmpty()) {
                %>
                <tr>
                    <td colspan="5">
                        <p class="no-data-message">This group currently has no managers.</p>
                    </td>
                </tr>
                <%
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                    for (Account manager : currentManagers) {
                %>
                <tr>
                    <td>
                        <div class="user-info">
                            <img src="<%= manager.getAvatar() %>" alt="Avatar" class="user-avatar">
                            <div class="user-details">
                                <div class="fullname"><%= manager.getFullname() %></div>
                                <div class="username">@<%= manager.getUsername() %></div>
                            </div>
                        </div>
                    </td>
                    <td>
                        <div class="user-details">
                            <div class="email"><%= manager.getEmail() %></div>
                            <div class="phone"><%= manager.getPhone() %></div>
                        </div>
                    </td>
                    <td><%= manager.getGender() %></td>
                    <td><%= manager.getDob()%></td>
                    <td>
                        <%
                            String statusClass = "status-inactive";
                            if ("Active".equalsIgnoreCase(manager.getAccountStatus())) {
                                statusClass = "status-active";
                            }
                        %>
                        <span class="status-badge <%= statusClass %>"><%= manager.getAccountStatus() %></span>
                    </td>
                    <td>
                        <form action="manage" METHOD="post">
                            <input type="hidden" value="delete" name="action">
                            <input type="hidden" value="<%= group.getId()%>" name="groupId">
                            <input type="hidden" value="<%= manager.getId()%>" name="managerId">
                            <input type="submit" value="Delete">
                        </form>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal-overlay" id="assignManagerModal">
    <div class="modal-content">
        <form action="manage" method="POST">
            <input type="hidden" value="add" name="action">
            <div class="modal-header">
                <h3 class="modal-title">Assign New Manager(s)</h3>
                <button type="button" class="modal-close-btn" id="closeAssignModalBtn">×</button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                <input type="text" id="memberSearchInput" class="member-search-input" placeholder="Search members by name or username...">

                <ul class="member-list" id="memberListContainer">
                    <%
                        List<Account> assignableMembers = (List<Account>) request.getAttribute("members");
                        if (assignableMembers != null && !assignableMembers.isEmpty()) {
                            for (Account member : assignableMembers) {
                    %>
                    <li class="member-item-label">
                        <div class="member-item">
                            <input type="checkbox" name="newManagerIds" value="<%= member.getId() %>">
                            <img src="<%= member.getAvatar() %>" alt="Avatar" class="user-avatar">
                            <div class="user-details">
                                <div class="fullname"><%= member.getFullname() %></div>
                                <div class="username">@<%= member.getUsername() %></div>
                            </div>
                        </div>
                    </li>
                    <%
                        }
                    } else {
                    %>
                    <li><p class="no-data-message" style="padding: 20px 0;">No other members to assign.</p></li>
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

<script>
    // === JAVASCRIPT CHO MODAL VÀ LỌC ===
    const openModalBtn = document.getElementById('openAssignModalBtn');
    const closeModalBtn = document.getElementById('closeAssignModalBtn');
    const cancelModalBtn = document.getElementById('cancelAssignModalBtn');
    const modalOverlay = document.getElementById('assignManagerModal');

    if (openModalBtn) {
        openModalBtn.addEventListener('click', () => { modalOverlay.style.display = 'flex'; });
    }

    function closeModal() { modalOverlay.style.display = 'none'; }

    closeModalBtn.addEventListener('click', closeModal);
    cancelModalBtn.addEventListener('click', closeModal);
    modalOverlay.addEventListener('click', (event) => {
        if (event.target === modalOverlay) { closeModal(); }
    });

    // Logic lọc danh sách thành viên
    const searchInput = document.getElementById('memberSearchInput');
    if (searchInput) {
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

            noResultsMessage.style.display = (visibleCount === 0 && memberItems.length > 0) ? 'block' : 'none';
        });
    }
</script>
</body>
</html>