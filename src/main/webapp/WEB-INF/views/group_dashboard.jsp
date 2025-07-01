<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="dto.ResGroupDTO" %>
<%@ page import="model.Account" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    List<ResGroupDTO> groupList = (List<ResGroupDTO>) request.getAttribute("groupList");
    String currentPage = "group";
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Group Dashboard - Zust</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        /* --- Base Styles (Consistent with other pages) --- */
        :root {
            --orange: #FF852F; --black: #1a1a1a; --white: #FFFFFF; --light-gray: #f0f2f5;
            --green: #28a745; --red: #dc3545; --yellow: #ffc107; --blue: #007bff;
        }
        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        body { background-color: var(--light-gray); display: flex; }
        .sidebar { width: 260px; background-color: var(--black); color: var(--white); height: 100vh; padding: 25px; display: flex; flex-direction: column; position: fixed; left: 0; top: 0; }
        .sidebar .logo { font-size: 2.5rem; font-weight: bold; text-align: center; margin-bottom: 40px; color: var(--orange); letter-spacing: 2px; }
        .sidebar .nav-menu { list-style-type: none; flex-grow: 1; }
        .sidebar .nav-menu li a { display: flex; align-items: center; color: var(--white); text-decoration: none; padding: 15px 20px; margin-bottom: 10px; border-radius: 8px; transition: background-color 0.3s, color 0.3s; }
        .sidebar .nav-menu li a .icon { margin-right: 15px; font-size: 1.2rem; width: 20px; text-align: center; }
        .sidebar .nav-menu li a:hover { background-color: var(--orange); }
        .sidebar .nav-menu li.active a { background-color: var(--orange); font-weight: 600; }
        .main-content { margin-left: 260px; padding: 40px; width: calc(100% - 260px); }
        .page-header { margin-bottom: 30px; }
        .page-header h1 { color: var(--black); font-size: 2rem; margin-bottom: 5px; }
        .page-header p { color: #777; }

        /* --- Table & Page-Specific Styles --- */
        .data-table-container { background-color: var(--white); border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); overflow: hidden; }
        .data-table { width: 100%; border-collapse: collapse; }
        .data-table th, .data-table td { padding: 15px; text-align: left; border-bottom: 1px solid #e0e0e0; vertical-align: top; }
        .data-table th { background-color: #f8f9fa; color: #555; font-weight: 600; text-transform: uppercase; font-size: 0.85em; }
        .data-table tr:last-child td { border-bottom: none; }

        /* Group Info Cell */
        .group-info { display: flex; align-items: flex-start; gap: 15px; }
        .group-info .cover-image { width: 120px; height: 70px; object-fit: cover; border-radius: 6px; flex-shrink: 0; cursor: pointer; transition: transform 0.2s; }
        .group-info .cover-image:hover { transform: scale(1.05); }
        .group-info .text-details .group-name { font-weight: bold; color: var(--black); margin-bottom: 5px; }
        .group-info .text-details .group-desc { font-size: 0.9em; color: #666; }
        .read-more-btn { background: none; border: none; padding: 0; font: inherit; color: var(--orange); text-decoration: underline; cursor: pointer; margin-left: 5px; font-size: 0.9em; font-weight: 600; }

        /* Manager List Cell */
        .manager-list { display: flex; flex-direction: column; gap: 12px; }
        .manager-item { display: flex; align-items: center; gap: 10px; }
        .manager-item .avatar { width: 35px; height: 35px; border-radius: 50%; object-fit: cover; cursor: pointer; transition: transform 0.2s; }
        .manager-item .avatar:hover { transform: scale(1.1); }
        .manager-item .manager-name { font-weight: 500; }
        .manager-item .manager-username { font-size: 0.85em; color: #777; }

        /* Stats Cell */
        .stats-info { display: flex; flex-direction: column; gap: 8px; font-size: 0.9em; }
        .stats-info div { display: flex; align-items: center; gap: 8px; color: #555; }
        .stats-info i { color: var(--orange); }

        /* Status & Action Styles */
        .status-badge { padding: 5px 12px; border-radius: 20px; font-size: 0.8em; font-weight: bold; text-transform: capitalize; display: inline-block; }
        .status-active { background-color: #e4f8eb; color: var(--green); }
        .status-inactive { background-color: #f3f4f6; color: #6b7280; }
        .status-banned, .status-deleted { background-color: #ffeeed; color: var(--red); }
        .actions { display: flex; flex-direction: column; gap: 8px; }
        .actions .btn { text-decoration: none; color: white; padding: 6px 12px; border-radius: 5px; font-size: 0.85em; font-weight: 500; transition: opacity 0.2s; display: inline-flex; align-items: center; justify-content: center; gap: 5px; }
        .btn-assign { background-color: var(--blue); }
        .btn-disband { background-color: var(--red); }

        /* No Data & Modal Styles (Reused) */
        .no-data-message { background-color: var(--white); border-radius: 10px; padding: 60px 40px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .no-data-message .icon { font-size: 3.5rem; color: var(--orange); margin-bottom: 20px; }
        .no-data-message h2 { font-size: 1.5rem; color: var(--black); margin-bottom: 10px; }
        .no-data-message p { color: #777; font-size: 1rem; }
        .image-modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.85); align-items: center; justify-content: center; }
        .modal-content { margin: auto; display: block; max-width: 80%; max-height: 80%; border-radius: 5px; animation: zoomIn 0.3s ease; }
        .modal-close { position: absolute; top: 20px; right: 40px; color: #f1f1f1; font-size: 40px; font-weight: bold; transition: 0.3s; cursor: pointer; }
        .modal-close:hover { color: #bbb; }
        #modal-caption { text-align: center; color: #ccc; padding: 15px 0; font-size: 1.2rem; animation: fadeIn 0.5s ease; }
        @keyframes zoomIn { from {transform: scale(0.5);} to {transform: scale(1);} }
        @keyframes fadeIn { from {opacity: 0;} to {opacity: 1;} }
    </style>
</head>
<body>

<!-- Sidebar -->
<aside class="sidebar">
    <div class="logo">Zust</div>
    <ul class="nav-menu">
        <li><a href="dashboard"><span class="icon"><i class="fas fa-chart-pie"></i></span><span>Statistic</span></a></li>
        <li><a href="accountDashboard"><span class="icon"><i class="fas fa-users"></i></span><span>User</span></a></li>
        <li><a href="notification"><span class="icon"><i class="fas fa-bell"></i></span><span>Notification</span></a></li>
        <li><a href="groupRequest"><span class="icon"><i class="fas fa-plus-square"></i></span><span>Group Request</span></a></li>
        <li class="active"><a href="groupDashboard"><span class="icon"><i class="fas fa-user-friends"></i></span><span>Group</span></a></li>
        <li><a href="reportPost"><span class="icon"><i class="fas fa-flag"></i></span><span>Report</span></a></li>
    </ul>
</aside>

<!-- Main Content -->
<main class="main-content">
    <header class="page-header">
        <h1>Group Dashboard</h1>
        <p>Overview and management of all groups on the platform.</p>
        <%
            if(request.getAttribute("msg") != null){
        %>
        <p>Message: <%= request.getAttribute("msg")%></p>
        <%
            }
        %>
    </header>

    <%-- Check if the list of groups is null or empty before rendering table --%>
    <% if (groupList == null || groupList.isEmpty()) { %>
    <div class="no-data-message">
        <div class="icon"><i class="fas fa-search"></i></div>
        <h2>No Groups Found</h2>
        <p>There are no groups to display at this time.</p>
    </div>
    <% } else { %>
    <div class="data-table-container">
        <table class="data-table">
            <thead>
            <tr>
                <th>Group Info</th>
                <th>Managers</th>
                <th>Status</th>
                <th>Stats</th>
                <th>Created</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <% for (ResGroupDTO group : groupList) { %>
            <tr>
                <%-- Group Info Cell --%>
                <td>
                    <div class="group-info">
                        <img src="${pageContext.request.contextPath}/static/images/<%= (group.getImage())%>"
                             alt="Group Cover"
                             class="cover-image clickable-image"
                             data-caption="<%= group.getName() != null ? group.getName() : "Group Cover" %>">
                        <div class="text-details">
                            <div class="group-name"><%= (group.getName() != null) ? group.getName() : "Unnamed Group" %></div>
                            <div class="group-desc"><%= (group.getDescription() != null) ? group.getDescription() : "No description provided." %></div>
                        </div>
                    </div>
                </td>
                <%-- Managers Cell --%>
                <td>
                    <div class="manager-list">
                        <% List<Account> managers = group.getManagers();
                            if (managers != null && !managers.isEmpty()) {
                                for (Account manager : managers) { %>
                        <div class="manager-item">
                            <img src="${pageContext.request.contextPath}/static/images/<%= manager.getAvatar()%>"
                                 alt="Manager Avatar"
                                 class="avatar clickable-image"
                                 data-caption="<%= manager.getFullname() != null ? manager.getFullname() : "Manager" %>">
                            <div>
                                <div class="manager-name"><%= manager.getFullname() != null ? manager.getFullname() : "N/A" %></div>
                                <div class="manager-username">@<%= manager.getUsername() != null ? manager.getUsername() : "unknown" %></div>
                            </div>
                        </div>
                        <%  }
                        } else { %>
                        <span style="color: #999; font-size: 0.9em;">No Managers</span>
                        <% } %>
                    </div>
                </td>
                <%-- Status Cell --%>
                <td><span class="status-badge status-<%= group.getStatus() != null ? group.getStatus() : "unknown" %>"><%= group.getStatus() != null ? group.getStatus() : "Unknown" %></span></td>
                <%-- Stats Cell --%>
                <td>
                    <div class="stats-info">
                        <div><i class="fas fa-users"></i> <%= group.getNumberParticipants() %> Members</div>
                        <div><i class="fas fa-pen-to-square"></i> <%= group.getNumberPosts() %> Posts</div>
                    </div>
                </td>
                <%-- Create Date Cell --%>
                <td><%= group.getCreateDate() != null ? group.getCreateDate().format(formatter) : "N/A" %></td>
                <%-- Actions Cell --%>
                <td class="actions">
<%--                    <a href="#" class="btn btn-assign"><i class="fas fa-user-plus"></i>Assign</a>--%>
                    <form action="manage?id=<%= group.getId()%>" method="post" onsubmit="return confirm('Are you sure you want to ban this group? This action cannot be undone.');">
                        <input type="hidden" name="groupId" value="<%= group.getId()%>">
                        <input type="submit" name="action" value="ban" class="btn btn-disband" >
                    </form>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>
</main>

<!-- Image Modal (Reused for all images) -->
<div id="imageModal" class="image-modal">
    <span class="modal-close">×</span>
    <img class="modal-content" id="modalImage">
    <div id="modal-caption"></div>
</div>

<!-- JavaScript for Interactivity -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        // --- Logic for the Image Modal ---
        const modal = document.getElementById("imageModal");
        const modalImg = document.getElementById("modalImage");
        const captionText = document.getElementById("modal-caption");
        const closeBtn = document.querySelector(".modal-close");

        document.querySelectorAll('.clickable-image').forEach(image => {
            image.addEventListener('click', function(event) {
                event.stopPropagation(); // Stop event from bubbling up
                modal.style.display = "flex";
                modalImg.src = this.src;
                captionText.innerHTML = this.dataset.caption;
            });
        });

        function closeModal() {
            modal.style.display = "none";
        }
        closeBtn.addEventListener('click', closeModal);
        window.addEventListener('click', e => { if (e.target == modal) closeModal(); });

        // --- Logic for "Read More" Description ---
        const descriptionLimit = 70;
        document.querySelectorAll('.group-desc').forEach(descElement => {
            const fullText = descElement.textContent.trim();
            if (fullText.length > descriptionLimit) {
                const shortText = fullText.substring(0, descriptionLimit) + '...';
                descElement.innerHTML = `
                        <span class="short-desc">${shortText}</span>
                        <span class="full-desc" style="display: none;">${fullText}</span>
                        <button class="read-more-btn">Read More</button>
                    `;
                descElement.querySelector('.read-more-btn').addEventListener('click', function(event) {
                    event.stopPropagation();
                    const shortSpan = this.parentElement.querySelector('.short-desc');
                    const fullSpan = this.parentElement.querySelector('.full-desc');
                    const isHidden = fullSpan.style.display === 'none';
                    fullSpan.style.display = isHidden ? 'inline' : 'none';
                    shortSpan.style.display = isHidden ? 'none' : 'inline';
                    this.textContent = isHidden ? 'Read Less' : 'Read More';
                });
            }
        });

        // --- Prevent action buttons from triggering parent clicks (good practice) ---
        document.querySelectorAll('.actions .btn').forEach(button => {
            button.addEventListener('click', e => e.stopPropagation());
        });
    });
</script>
</body>
</html>