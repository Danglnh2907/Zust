<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ page import="model.Account" %>

<%
    List<Account> accountList = (List<Account>) request.getAttribute("accountList");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Dashboard - Zust</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
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

        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        .page-header h1 { color: var(--black); font-size: 2rem; margin: 0; }
        .header-controls { display: flex; align-items: center; gap: 20px; }
        .search-box { display: flex; align-items: center; background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 0 10px; }
        .search-box i { color: #999; }
        .search-box input { border: none; outline: none; padding: 10px; font-size: 1rem; width: 250px; }

        .page-tabs { display: flex; border-bottom: 2px solid #e0e0e0; margin-bottom: 30px; }
        .tab-item { padding: 10px 20px; cursor: pointer; font-weight: 600; color: #777; border-bottom: 3px solid transparent; transform: translateY(2px); transition: color 0.2s, border-color 0.2s; }
        .tab-item.active { color: var(--orange); border-color: var(--orange); }

        .data-table-container { background-color: var(--white); border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); overflow-x: auto; }
        .data-table { width: 100%; min-width: 1200px; border-collapse: collapse; }
        .data-table th, .data-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #e0e0e0; vertical-align: middle; white-space: nowrap; }
        .data-table th { background-color: #f8f9fa; color: #555; font-weight: 600; font-size: 0.8em; text-transform: uppercase; }
        .user-info { display: flex; align-items: center; gap: 10px; }
        .user-info .avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; cursor: pointer; }
        .user-info .user-name { font-weight: 600; }
        .user-info .user-username { font-size: 0.9em; color: #777; }

        .bio-cell { white-space: normal; max-width: 250px;}
        .bio-cell .bio-content { white-space: normal; max-width: 250px;}
        .read-more-btn { background: none; border: none; padding: 0; font: inherit; color: var(--orange); text-decoration: underline; cursor: pointer; margin-left: 5px; font-size: 0.9em; }
        .status-badge { padding: 5px 12px; border-radius: 20px; font-size: 0.8em; font-weight: bold; text-transform: capitalize; }
        .status-active { background-color: #e4f8eb; color: var(--green); }
        .status-banned { background-color: #ffeeed; color: var(--red); }
        .actions .btn-ban { background-color: var(--red); color: white; border: none; padding: 6px 12px; border-radius: 5px; cursor: pointer; }

        .hidden { display: none; }

        /* Modal Styles (Reused) */
        .image-modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.85); align-items: center; justify-content: center; }
        .modal-content { max-width: 80%; max-height: 80%; border-radius: 5px; animation: zoomIn 0.3s ease; }
        .modal-close { position: absolute; top: 20px; right: 40px; color: #f1f1f1; font-size: 40px; font-weight: bold; cursor: pointer; }
        @keyframes zoomIn { from {transform: scale(0.5);} to {transform: scale(1);} }
    </style>
</head>
<body>

<!-- Sidebar -->
<aside class="sidebar">
    <div class="logo">Zust</div>
    <ul class="nav-menu">
        <li><a href="dashboard"><span class="icon"><i class="fas fa-chart-pie"></i></span><span>Statistic</span></a></li>
        <li class="active"><a href="accountDashboard"><span class="icon"><i class="fas fa-users"></i></span><span>User</span></a></li>
        <li><a href="groupRequest"><span class="icon"><i class="fas fa-plus-square"></i></span><span>Group Request</span></a></li>
        <li><a href="groupDashboard"><span class="icon"><i class="fas fa-user-friends"></i></span><span>Group</span></a></li>
        <li><a href="reportPost"><span class="icon"><i class="fas fa-flag"></i></span><span>Report</span></a></li>
    </ul>
</aside>

<!-- Main Content -->
<main class="main-content">
    <div class="page-header">
        <h1>User Dashboard</h1>
        <%
            if(request.getAttribute("msg") != null){
        %>
        <p>Message: <%= request.getAttribute("msg")%></p>
        <%
            }
        %>
        <div class="header-controls">
            <div class="search-box">
                <i class="fas fa-search"></i>
                <input type="text" id="searchInput" placeholder="Search by name or username...">
            </div>
        </div>
    </div>

    <% if (accountList == null || accountList.isEmpty()) { %>
    <div class="no-data-message">
        <div class="icon"><i class="fas fa-search"></i></div>
        <h2>No Users Found</h2>
        <p>There are no user to display at this time.</p>
    </div>
    <% } else { %>

    <div class="page-tabs">
        <div class="tab-item active" data-tab="active">Active Users</div>
<%--        <div class="tab-item" data-tab="banned">Banned Users</div>--%>
    </div>

    <div class="data-table-container">
        <table class="data-table">
            <thead>
            <tr>
                <th>ID</th><th>User</th><th>Contact</th><th>Details</th>
                <th>Bio</th><th>Credit</th><%--<th>Status</th>--%><th>Action</th>
            </tr>
            </thead>
            <tbody id="userTableBody">
            <% for (Account user : accountList) { %>
            <%-- The data-search-term contains all text we want to search through --%>
            <tr data-status="<%= user.getAccountStatus() %>" data-search-term="<%= user.getFullname().toLowerCase() %> <%= user.getUsername().toLowerCase() %>">
                <td><%= user.getId() %></td>
                <td>
                    <div class="user-info">
                        <img src="${pageContext.request.contextPath}/static/images/<%= user.getAvatar() %>" alt="Avatar" class="avatar clickable-avatar" data-username="<%= user.getFullname() %>">
                        <div>
                            <div class="user-name"><%= user.getFullname() %></div>
                            <div class="user-username">@<%= user.getUsername() %></div>
                        </div>
                    </div>
                </td>
                <td>
                    <div><%= user.getEmail() %></div>
                    <div style="color: #666;"><%= user.getPhone() != null ? user.getPhone() : "N/A" %></div>
                </td>
                <td>
                    <div><strong>Gender:</strong> <%= user.getGender() != null ? (user.getGender() ? "Male" : "Female") : "N/A" %></div>
                    <div><strong>DOB:</strong> <%= user.getDob() != null ? user.getDob() : "N/A" %></div>
                </td>
                <td class="bio-cell">
                    <%
                        String bio = user.getBio();
                        if (bio != null && bio.length() > 50) {
                            // If the bio is long, create the full structure
                            String shortBio = bio.substring(0, 50) + "...";
                    %>
                    <span class="short-text bio-content"><%= shortBio %></span>
                    <span class="full-text bio-content hidden"><%= bio %></span>
                    <button class="read-more-btn">Read More</button>
                    <%
                    } else {
                        // Otherwise, just display the content or "N/A"
                    %>
                    <%= (bio != null) ? bio : "N/A" %>
                    <%
                        }
                    %>
                </td>
                <td><%= user.getCredit() %></td>
<%--                <td><span class="status-badge status-<%= user.getAccountStatus()%>"><%= user.getAccountStatus() %></span></td>--%>
                <td class="actions">
                    <form action="accountDashboard" method="post" onsubmit="return confirm('Are you sure you want to ban this user? This action cannot be undone.');">
                        <input type="hidden" name="id" value="<%= user.getId()%>">
                        <input type="hidden" name="action" value="ban">
                        <button class="btn-ban">Ban</button>
                    </form>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>
</main>

<!-- Image Modal -->
<div id="imageModal" class="image-modal">
    <span class="modal-close">×</span>
    <img class="modal-content" id="modalImage">
    <div id="modal-caption" style="color: #ccc; text-align: center; padding: 15px 0;"></div>
</div>

<!-- JavaScript for Interactivity -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        const tabs = document.querySelectorAll('.tab-item');
        const searchInput = document.getElementById('searchInput');
        const tableRows = document.querySelectorAll('#userTableBody tr');

        // --- Main Filter Function ---
        function filterUsers() {
            const activeTab = document.querySelector('.tab-item.active').dataset.tab;
            const searchTerm = searchInput.value.toLowerCase();

            tableRows.forEach(row => {
                const rowStatus = row.dataset.status;
                const rowSearchTerm = row.dataset.searchTerm;

                const tabMatch = (rowStatus === activeTab);
                const searchMatch = (rowSearchTerm.includes(searchTerm));

                if (tabMatch && searchMatch) {
                    row.classList.remove('hidden');
                } else {
                    row.classList.add('hidden');
                }
            });
        }

        // --- Event Listeners for Tabs and Search ---
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                filterUsers();
            });
        });

        searchInput.addEventListener('input', filterUsers);

        // --- "Read More" for Bio ---
        document.querySelectorAll('.read-more-btn').forEach(button => {
            button.addEventListener('click', function(event) {
                event.stopPropagation(); // Prevent other clicks

                // Find the text spans RELATIVE to the button that was clicked
                const cell = this.closest('.bio-cell');
                const shortText = cell.querySelector('.short-text');
                const fullText = cell.querySelector('.full-text');

                // Toggle the 'hidden' class on both spans
                shortText.classList.toggle('hidden');
                fullText.classList.toggle('hidden');

                // Update the button's text
                if (fullText.classList.contains('hidden')) {
                    this.textContent = 'Read More';
                } else {
                    this.textContent = 'Read Less';
                }
            });
        });

        // --- Image Modal Logic ---
        const modal = document.getElementById("imageModal");
        const modalImg = document.getElementById("modalImage");
        const captionText = document.getElementById("modal-caption");

        document.querySelectorAll('.clickable-avatar').forEach(avatar => {
            avatar.addEventListener('click', function() {
                modal.style.display = "flex";
                modalImg.src = this.src;
                captionText.textContent = this.dataset.username;
            });
        });

        function closeModal() { modal.style.display = "none"; }
        modal.querySelector('.modal-close').addEventListener('click', closeModal);
        window.addEventListener('click', e => { if (e.target == modal) closeModal(); });

        // --- Initial filter on page load ---
        filterUsers();
    });
</script>
</body>
</html>