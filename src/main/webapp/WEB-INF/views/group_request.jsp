<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="dto.ResGroupDTO" %>
<%@ page import="model.Account" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    List<ResGroupDTO> groups = (List<ResGroupDTO>) request.getAttribute("groups");
    String currentPage = "creategroup";
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Group Requests - Zust Dashboard</title>
    <!-- Font Awesome for Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        /* --- Base styles from dashboard (unchanged) --- */
        :root {
            --orange: #FF852F;
            --black: #1a1a1a;
            --white: #FFFFFF;
            --light-gray: #f0f2f5;
            --text-color: #333;
            --green: #28a745;
            --red: #dc3545;
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

        /* --- Page-Specific Styles for the Requests Table --- */
        .page-header {
            margin-bottom: 30px;
        }
        .page-header h1 {
            color: var(--black);
            font-size: 2rem;
            margin-bottom: 5px;
        }
        .page-header p {
            color: #777;
        }

        .requests-table-container {
            background-color: var(--white);
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.05);
            overflow: hidden; /* Ensures border-radius is respected by the table */
        }

        .table-requests {
            width: 100%;
            border-collapse: collapse;
        }
        .table-requests th, .table-requests td {
            padding: 15px 20px;
            text-align: left;
            border-bottom: 1px solid #e0e0e0;
            vertical-align: middle;
        }
        .table-requests th {
            background-color: #f8f9fa;
            color: #555;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.85em;
        }
        .table-requests tr:last-child td {
            border-bottom: none;
        }

        /* Group Info Cell */
        .group-info { display: flex; align-items: flex-start; }
        .group-info .cover-image { width: 120px; height: 70px; object-fit: cover; border-radius: 6px; margin-right: 15px; background-color: #eee; }
        .group-info .cover-image:hover {
            transform: scale(1.1);
        }
        .group-info .text-details .group-name { font-weight: bold; color: var(--black); margin-bottom: 5px; }
        .group-info .text-details .group-desc { font-size: 0.9em; color: #666; max-width: 300px; }

        /* Creator Info Cell */
        .creator-info { display: flex; align-items: center; }
        .creator-info .avatar { width: 40px; height: 40px; border-radius: 50%; margin-right: 12px; object-fit: cover; }
        .creator-info .avatar:hover {
            transform: scale(1.1);
        }
        .creator-info .creator-name { font-weight: 500; color: var(--black); }
        .creator-info .creator-username { font-size: 0.85em; color: #777; }

        .action-btn {
            text-decoration: none;
            color: white;
            padding: 8px 15px;
            border-radius: 5px;
            font-size: 0.9em;
            font-weight: 500;
            transition: opacity 0.2s;
            margin-right: 8px;
        }
        .action-btn:hover { opacity: 0.8; }
        .btn-accept { background-color: var(--green); }
        .btn-reject { background-color: var(--red); }
        .no-data-message { text-align: center; padding: 40px; color: var(--text-color-light); }

        .description-full { display: none; }
        .read-more { color: var(--primary-color); font-weight: 600; cursor: pointer; text-decoration: none; }

        .image-modal {
            display: none; /* Hidden by default */
            position: fixed;
            z-index: 1000;
            left: 0; top: 0;
            width: 100%; height: 100%;
            background-color: rgba(0, 0, 0, 0.85);
            display: none; /* Re-set to be sure */
            align-items: center;
            justify-content: center;
        }

        .modal-content {
            margin: auto;
            display: block;
            max-width: 80%;
            max-height: 80%;
            border-radius: 5px;
            animation: zoomIn 0.3s ease;
        }

        .modal-close {
            position: absolute;
            top: 20px; right: 40px;
            color: #f1f1f1;
            font-size: 40px;
            font-weight: bold;
            transition: 0.3s;
            cursor: pointer;
        }
        .modal-close:hover { color: #bbb; }

        #modal-caption {
            text-align: center;
            color: #ccc;
            padding: 15px 0;
            font-size: 1.2rem;
            animation: fadeIn 0.5s ease;
        }

        @keyframes zoomIn {
            from {transform: scale(0.5);}
            to {transform: scale(1);}
        }
        @keyframes fadeIn {
            from {opacity: 0;}
            to {opacity: 1;}
        }
    </style>
</head>
<body>

<!-- ======================= Sidebar (Reused) ======================= -->
<aside class="sidebar">
    <div class="logo">Zust</div>
    <ul class="nav-menu">
        <li><a href="dashboard"><span class="icon"><i class="fas fa-chart-pie"></i></span><span>Statistic</span></a></li>
        <li><a href="accountDashboard"><span class="icon"><i class="fas fa-users"></i></span><span>User</span></a></li>
        <li class="active"><a href="groupRequest"><span class="icon"><i class="fas fa-plus-square"></i></span><span>Group Request</span></a></li>
        <li><a href="groupDashboard"><span class="icon"><i class="fas fa-user-friends"></i></span><span>Group</span></a></li>
        <li><a href="reportPost"><span class="icon"><i class="fas fa-flag"></i></span><span>Report</span></a></li>
    </ul>
</aside>

<!-- ======================= Main Content ======================= -->
<main class="main-content">
    <header class="page-header">
        <h1>Group Creation Requests</h1>
        <p>Review and approve or reject new group submissions.</p>
        <%
            if(request.getAttribute("msg") != null){
        %>
        <p style="font-weight: bold; color: black"><%= request.getAttribute("msg")%></p>
        <%
            }
        %>
    </header>
    <div class="requests-table-container">
        <table class="table-requests">
            <thead>
            <tr>
                <th>Group Info</th>
                <th>Created By</th>
                <th>Date</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (groups == null || groups.isEmpty()) {
            %>
            <tr>
                <td colspan="4">
                    <p class="no-data-message">There is no pending group.</p>
                </td>
            </tr>
            <%
            } else {
            %>
            <% for (ResGroupDTO group : groups) { %>
            <tr>
                <%-- Group Info Cell --%>
                <td>
                    <div class="group-info">
                        <%-- Null check for cover image --%>
                        <% if (group.getImage() != null) { %>
                        <img src="${pageContext.request.contextPath}/static/images/<%= group.getImage() %>" alt="Group Cover" class="cover-image clickable-image">
                        <% } else { %>
                        <img src="https://via.placeholder.com/120x70/EEEEEE/AAAAAA?text=No+Image" alt="Placeholder" class="cover-image clickable-image">
                        <% } %>
                        <div class="text-details">
                            <%-- Null check for group name --%>
                            <div class="group-name">
                                <%= (group.getName() != null) ? group.getName() : "Unnamed Group" %>
                            </div>
                            <%-- Null check for description --%>
                            <div class="group-desc">
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
                            </div>
                        </div>
                    </div>
                </td>

                <%-- Created By Cell --%>
                <td>
                    <div class="creator-info">
                        <%-- Nested null check: first check if creator object exists, then check its properties --%>
                        <% Account creator = group.getManagers().get(0);
                            if (creator != null) { %>
                        <%-- Null check for avatar --%>
                        <% if (creator.getAvatar() != null) { %>
                        <img src="${pageContext.request.contextPath}/static/images/<%= creator.getAvatar() %>" alt="Creator Avatar" class="avatar clickable-image">
                        <% } else { %>
                        <img src="https://via.placeholder.com/40/EEEEEE/AAAAAA?text=?" alt="Placeholder Avatar" class="avatar">
                        <% } %>
                        <div>
                            <%-- Null check for full name --%>
                            <div class="creator-name">
                                <%= (creator.getFullname() != null) ? creator.getFullname() : "Unknown Name" %>
                            </div>
                            <%-- Null check for username --%>
                            <div class="creator-username">
                                @<%= (creator.getUsername() != null) ? creator.getUsername() : "unknown" %>
                            </div>
                        </div>
                        <% } else { %>
                        <%-- Fallback if the entire creator object is null --%>
                        <img src="https://via.placeholder.com/40/EEEEEE/AAAAAA?text=?" alt="Placeholder Avatar" class="avatar">
                        <div>
                            <div class="creator-name">Deleted User</div>
                            <div class="creator-username">@unknown</div>
                        </div>
                        <% } %>
                    </div>
                </td>

                <%-- Date Cell --%>
                <td>
                    <%-- Null check for create date --%>
                    <%= (group.getCreateDate() != null) ? group.getCreateDate().format(formatter) : "N/A" %>
                </td>

                <%-- Actions Cell --%>
                <td class="actions">
                    <form action="groupRequest" method="post">
                        <input type="hidden" name="groupId" value="<%= group.getId()%>">
                        <input type="submit" name="action" value="Accept" class="action-btn btn-accept">
                        <input type="submit" name="action" value="Reject" class="action-btn btn-reject">
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
</main>
<div id="imageModal" class="image-modal">
    <span class="modal-close">×</span>
    <img class="modal-content" id="modalImage">
</div>
<!-- ======================= JAVASCRIPT ENHANCEMENTS ======================= -->
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

    const modal = document.getElementById("imageModal");
    const modalImg = document.getElementById("modalImage");
    const closeBtn = document.querySelector(".modal-close");
    // Get all clickable avatars
    const avatars = document.querySelectorAll(".clickable-image");

    // Loop through all avatars and add a click event
    avatars.forEach(avatar => {
        avatar.addEventListener('click', function() {
            modal.style.display = "flex"; // Use flex for centering
            modalImg.src = this.src;
        });
    });

    // Function to close the modal
    function closeModal() {
        modal.style.display = "none";
    }

    // Add click events to close the modal
    closeBtn.addEventListener('click', closeModal);

    // Also close modal when clicking on the background
    window.addEventListener('click', function(event) {
        if (event.target == modal) {
            closeModal();
        }
    });
</script>
</body>
</html>