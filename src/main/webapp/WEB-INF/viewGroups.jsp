<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="dto.ResGroupDTO" %>

<%
    // Lấy danh sách group từ request
    List<ResGroupDTO> groupList = (List<ResGroupDTO>) request.getAttribute("groups");

    NumberFormat nf = NumberFormat.getInstance(Locale.US);
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Group Management</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap');

        body {
            background-color: #f8f9fa; /* A lighter gray for admin pages */
        }

        .dashboard-container {
            width: 90%;
            max-width: 1200px;
            margin: 40px auto;
            background-color: #ffffff;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
            padding: 30px;
        }

        .dashboard-header {
            border-bottom: 1px solid #dee2e6;
            padding-bottom: 20px;
            margin-bottom: 30px;
        }

        .dashboard-header h1 {
            font-size: 2em;
            color: #343a40;
            margin: 0;
        }

        .dashboard-header p {
            font-size: 1.1em;
            color: #6c757d;
            margin: 5px 0 0 0;
        }

        .table-wrapper {
            overflow-x: auto; /* For responsiveness on small screens */
        }

        .admin-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 15px;
        }

        .admin-table th,
        .admin-table td {
            padding: 15px;
            text-align: left;
            border-bottom: 1px solid #e9ecef;
        }

        .admin-table thead th {
            background-color: #f8f9fa;
            font-weight: 600;
            color: #495057;
            text-transform: uppercase;
            font-size: 12px;
        }

        .admin-table tbody tr:hover {
            background-color: #f1f3f5;
        }

        .group-name-cell {
            font-weight: 600;
            color: #007bff;
        }

        /* Status Badges */
        .status-badge {
            padding: 5px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            text-transform: capitalize;
        }

        .status-badge.status-public {
            background-color: #d4edda;
            color: #155724;
        }

        .status-badge.status-private {
            background-color: #f8d7da;
            color: #721c24;
        }

        /* Action Buttons */
        .actions-cell {
            white-space: nowrap;
        }

        .btn-action {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 5px;
            color: white;
            text-decoration: none;
            font-size: 13px;
            font-weight: 500;
            transition: opacity 0.2s;
            margin-right: 5px;
        }

        .btn-action:hover {
            opacity: 0.8;
        }

        .btn-view {
            background-color: #007bff; /* Blue */
        }

        .btn-edit {
            background-color: #28a745; /* Green */
        }

        .btn-delete {
            background-color: #dc3545; /* Red */
        }

        /* Panel for no data */
        .no-data-panel {
            text-align: center;
            padding: 40px;
            border: 2px dashed #e9ecef;
            border-radius: 8px;
        }
        .no-data-panel p {
            font-size: 1.1em;
            color: #6c757d;
        }
    </style>
</head>
<body>
<div class="dashboard-container">
    <header class="dashboard-header">
        <h1>Group Management</h1>
        <p>A list of all groups in the system.</p>
    </header>

    <% if (groupList == null || groupList.isEmpty()) { %>
    <div class="no-data-panel">
        <p>No groups found. Start by <a href="createGroup.jsp">creating a new group</a>!</p>
    </div>
    <% } else { %>
    <div class="table-wrapper">
        <table class="admin-table">
            <thead>
            <tr>
                <th>Group Name</th>
                <th>Members</th>
                <th>Posts</th>
                <th>Status</th>
                <th>Created On</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <%-- Lặp qua danh sách và hiển thị từng group --%>
            <% for (ResGroupDTO group : groupList) { %>
            <tr>
                <td class="group-name-cell"><%= group.getName() %></td>
                <td><%= nf.format(group.getNumberParticipants()) %></td>
                <td><%= nf.format(group.getNumberPosts()) %></td>
                <td>
                                    <span class="status-badge">
                                        <%= group.getStatus() %>
                                    </span>
                </td>
                <td><%= group.getCreateDate()%></td>
                <td class="actions-cell">
                    <a href="${pageContext.request.contextPath}/group?action=view&id=<%= group.getId() %>" class="btn-action btn-view">View</a>
                    <a href="#" class="btn-action btn-edit">Edit</a>
                    <a href="#" onclick="return confirm('Are you sure you want to delete this group?')" class="btn-action btn-delete">Delete</a>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>
</div>
</body>
</html>