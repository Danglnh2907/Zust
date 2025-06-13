<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ page import="java.time.LocalDateTime, java.time.format.DateTimeFormatter" %>
<%@ page import="dto.ResGroupDTO" %>
<%@ page import="model.Account" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Group Management</title>

    <style>
        :root {
            --primary-color: #FF852F; --text-color-dark: #1c1c1c; --text-color-light: #65676b;
            --border-color: #e0e0e0; --bg-color-light: #f0f2f5; --bg-color-white: #ffffff;
            --status-active-bg: #e7f3ff; --status-active-text: #1877f2;
            --status-banned-bg: #fffbe2; --status-banned-text: #f7b928;
            --status-deleted-bg: #fde2e4; --status-deleted-text: #d90429;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg-color-light); margin: 0; padding: 20px;
        }
        .container { max-width: 95%; margin: 0 auto; }
        .dashboard-container {
            background-color: var(--bg-color-white); border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.08); overflow: hidden;
        }
        .dashboard-header {
            padding: 20px 25px; border-bottom: 1px solid var(--border-color);
        }
        .dashboard-header h1 { font-size: 24px; margin: 0; }
        .dashboard-content { overflow-x: auto; } /* Allow horizontal scrolling on small screens */
        .group-table { width: 100%; border-collapse: collapse; font-size: 14px; min-width: 1200px; }
        .group-table th, .group-table td {
            padding: 12px 15px; text-align: left; border-bottom: 1px solid var(--border-color);
            vertical-align: middle;
        }
        .group-table thead th {
            background-color: #f9fafb; font-weight: 600; color: var(--text-color-light);
            font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px;
        }
        .group-table tbody tr:hover { background-color: #f7f7f7; }

        /* Specific Column Styles */
        .group-info { display: flex; align-items: center; gap: 12px; }
        .group-avatar { width: 45px; height: 45px; border-radius: 50%; object-fit: cover; }

        /* Description with Read More */
        .description-full { display: none; }
        .read-more { color: var(--primary-color); font-weight: 600; cursor: pointer; text-decoration: none; }

        /* Manager List */
        .manager-list { display: flex; flex-wrap: wrap; gap: -8px; }
        .manager-avatar {
            width: 32px; height: 32px; border-radius: 50%; object-fit: cover;
            border: 2px solid white; margin-left: -8px;
            transition: transform 0.2s; cursor: pointer;
        }
        .manager-avatar:hover { transform: scale(1.2); z-index: 10; }

        /* Status Badge */
        .status-badge { display: inline-block; padding: 4px 10px; border-radius: 20px; font-weight: 500; }
        .status-active { background-color: var(--status-active-bg); color: var(--status-active-text); }
        .status-banned { background-color: var(--status-banned-bg); color: var(--status-banned-text); }
        .status-deleted { background-color: var(--status-deleted-bg); color: var(--status-deleted-text); }

        /* Action buttons */
        .action-cell { display: flex; flex-direction: column; gap: 8px; }
        .action-btn {
            display: block; width: 100%; text-align: center; text-decoration: none;
            padding: 6px 10px; border-radius: 5px; font-size: 13px; font-weight: 500;
            border: 1px solid transparent; transition: background-color 0.2s, color 0.2s;
        }
        .btn-view { background-color: #e7f3ff; color: #1877f2; }
        .btn-view:hover { background-color: #d0e7ff; }
        .btn-manage { background-color: #f0fdf4; color: #16a34a; }
        .btn-manage:hover { background-color: #dcfce7; }
        .btn-disband {
            background-color: #fef2f2; color: #ef4444; border: none;
            font-family: inherit; font-size: 13px; font-weight: 500; cursor: pointer;
        }
        .btn-disband:hover { background-color: #fee2e2; }

        .no-data-message { text-align: center; padding: 40px; color: var(--text-color-light); }
    </style>
</head>
<body>
<%
    List<ResGroupDTO> groupList = (List<ResGroupDTO>) request.getAttribute("groupList");

%>

<div class="container">
    <div class="dashboard-container">
        <div class="dashboard-header">
            <h1>Group Management Dashboard</h1>
        </div>
        <div class="dashboard-content">
            <table class="group-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Group Info</th>
                    <th>Description</th>
                    <th>Created Date</th>
                    <th>Managers</th>
                    <th>Status</th>
                    <th>Members</th>
                    <th>Posts</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (groupList == null || groupList.isEmpty()) {
                %>
                <tr>
                    <td colspan="9">
                        <p class="no-data-message">No groups found in the system.</p>
                    </td>
                </tr>
                <%
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                    for (ResGroupDTO group : groupList) {
                %>
                <tr>
                    <td><strong><%= group.getId() %></strong></td>
                    <td>
                        <div class="group-info">
                            <img src="<%= group.getImage() %>" alt="Group Avatar" class="group-avatar">
                            <span><%= group.getName() %></span>
                        </div>
                    </td>
                    <td>
                        <%
                            String desc = group.getDescription();
                            int previewLength = 50;
                            if (desc != null && desc.length() > previewLength) {
                        %>
                        <span class="description-preview">
                                                <%= desc.substring(0, previewLength) %>...
                                                <a href="javascript:void(0);" class="read-more" onclick="toggleDescription(this)">more</a>
                                            </span>
                        <span class="description-full">
                                                <%= desc %>
                                                <a href="javascript:void(0);" class="read-more" onclick="toggleDescription(this)">less</a>
                                            </span>
                        <% } else { %>
                        <%= desc == null ? "" : desc %>
                        <% } %>
                    </td>
                    <td><%= group.getCreateDate().format(formatter) %></td>
                    <td>
                        <div class="manager-list">
                            <% for (Account manager : group.getManagers()) { %>
                            <img src="<%= manager.getAvatar() %>" alt="<%= manager.getFullname() %>" title="<%= manager.getFullname() %> (@<%= manager.getUsername() %>)" class="manager-avatar">
                            <% } %>
                        </div>
                    </td>
                    <td>
                        <%
                            String statusClass = "";
                            if ("Active".equalsIgnoreCase(group.getStatus())) statusClass = "status-active";
                            else if ("Banned".equalsIgnoreCase(group.getStatus())) statusClass = "status-banned";
                            else if ("Deleted".equalsIgnoreCase(group.getStatus())) statusClass = "status-deleted";
                        %>
                        <span class="status-badge <%= statusClass %>"><%= group.getStatus() %></span>
                    </td>
                    <td><%= group.getNumberParticipants() %></td>
                    <td><%= group.getNumberPosts() %></td>
                    <td>
                        <div class="action-cell">
                            <a href="group?action=view&id=<%= group.getId() %>" class="action-btn btn-view">View Details</a>
                            <a href="manage?id=<%= group.getId() %>" class="action-btn btn-manage">Assign Manager</a>
                            <form action="group?action=disband" method="POST" onsubmit="return confirm('Are you sure you want to disband this group? This action cannot be undone.');">
                                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                <button type="submit" class="action-btn btn-disband">Disband</button>
                            </form>
                        </div>
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

<script>
    function toggleDescription(linkElement) {
        const container = linkElement.closest('td');
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
</script>
</body>
</html>