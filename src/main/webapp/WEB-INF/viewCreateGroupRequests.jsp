<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="dto.ResCreateGroupRequestDTO" %> <%-- Import lớp model của bạn --%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Group Requests</title>

    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: #f0f2f5; margin: 0; padding: 20px;
        }
        .dashboard-container {
            background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            width: 100%; max-width: 1200px; margin: 0 auto; padding: 24px; box-sizing: border-box;
        }
        .dashboard-header h1 {
            font-size: 28px; color: #1c1e21; margin: 0 0 24px 0;
            padding-bottom: 16px; border-bottom: 1px solid #dddfe2;
        }
        .requests-table { width: 100%; border-collapse: collapse; font-size: 15px; }
        .requests-table th, .requests-table td {
            padding: 12px 16px; text-align: left; border-bottom: 1px solid #dddfe2; vertical-align: middle;
        }
        .requests-table thead th { background-color: #f5f6f7; font-weight: 600; color: #606770; }
        .col-id { width: 5%; }
        .col-requester { width: 20%; }
        .col-content { width: 40%; }
        .col-date { width: 15%; }
        .col-actions { width: 20%; text-align: center; }
        .requester-info { display: flex; align-items: center; }
        .avatar-img {
            width: 40px; height: 40px; border-radius: 50%; object-fit: cover;
            margin-right: 12px; border: 1px solid #dddfe2;
        }
        .requester-name { font-weight: 500; color: #050505; }
        .content-full { display: none; }
        .read-more-link {
            color: #1877f2; cursor: pointer; font-weight: 500;
            text-decoration: none; display: inline-block; margin-left: 4px;
        }
        .read-more-link:hover { text-decoration: underline; }
        .action-buttons a {
            padding: 8px 16px; border-radius: 6px; color: #ffffff; font-weight: bold; text-decoration: none;
            margin: 0 4px; display: inline-block; border: none; cursor: pointer; transition: opacity 0.2s;
        }
        .action-buttons a:hover { opacity: 0.85; }
        .btn-approve { background-color: #31a24c; }
        .btn-reject { background-color: #fa383e; }
        .no-data-message { text-align: center; padding: 40px; color: #606770; font-size: 16px; }

        .action-forms-container {
            display: flex;
            justify-content: center;
            gap: 8px; /* Creates space between the buttons */
        }
        .action-form {
            margin: 0; /* Reset default form margin */
        }
        .action-button {
            padding: 8px 16px; border-radius: 6px; color: #ffffff; font-weight: bold; text-decoration: none;
            border: none; cursor: pointer; transition: opacity 0.2s;
            font-size: 14px;
        }
        .action-button:hover { opacity: 0.85; }
        .btn-approve { background-color: #31a24c; }
        .btn-reject { background-color: #fa383e; }
        .no-data-message { text-align: center; padding: 40px; color: #606770; font-size: 16px; }
    </style>
</head>
<body>

<div class="dashboard-container">
    <div class="dashboard-header">
        <h1>Group Creation Requests</h1>
    </div>
    <%
        if(request.getAttribute("msg") != null){
    %>
    <p>Message: <%= request.getAttribute("msg")%></p>
    <%
        }
    %>
    <table class="requests-table">
        <thead>
        <tr>
            <th class="col-id">ID</th>
            <th class="col-requester">Requester</th>
            <th class="col-content">Content</th>
            <th class="col-date">Date Created</th>
            <th class="col-actions">Actions</th>
        </tr>
        </thead>
        <tbody>
        <%
            List<ResCreateGroupRequestDTO> requestList = (List<ResCreateGroupRequestDTO>) request.getAttribute("createGroupRequests");

            if (requestList == null || requestList.isEmpty()) {
        %>
        <tr>
            <td colspan="5" class="no-data-message">
                There are no pending group requests at the moment.
            </td>
        </tr>
        <%
        } else {
            for (ResCreateGroupRequestDTO req : requestList) {
        %>
        <tr>
            <td><strong><%= req.getId() %></strong></td>
            <td>
                <div class="requester-info">
                    <img src="<%= req.getAccountAvatar() %>" alt="Avatar" class="avatar-img">
                    <span class="requester-name"><%= req.getAccountName() %></span>
                </div>
            </td>
            <td>
                <%
                    String content = req.getContent();
                    int maxLength = 100;

                    if (content != null && content.length() > maxLength) {
                %>
                <span class="content-preview">
                                            <%= content.substring(0, maxLength) %>...
                                            <a href="javascript:void(0);" class="read-more-link" onclick="toggleContent(this)">read more</a>
                                        </span>
                <span class="content-full">
                                            <%= content %>
                                            <a href="javascript:void(0);" class="read-more-link" onclick="toggleContent(this)">read less</a>
                                        </span>
                <%
                } else {
                %>
                <%= content == null ? "" : content %>
                <%
                    }
                %>
            </td>
            <td><%= req.getCreateDate() %></td>
            <td class="col-actions">
                <div class="action-buttons">
                    <form action="createGroupRequest" method="POST" class="action-form">
                        <input type="hidden" name="id" value="<%= req.getId() %>">
                        <input type="hidden" name="action" value="approve">
                        <button type="submit" class="action-button btn-approve">Approve</button>
                    </form>

                    <!-- Reject Form -->
                    <form action="createGroupRequest" method="POST" class="action-form">
                        <input type="hidden" name="id" value="<%= req.getId() %>">
                        <input type="hidden" name="action" value="reject">
                        <button type="submit" class="action-button btn-reject">Reject</button>
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

<script>
    function toggleContent(linkElement) {
        const parentTd = linkElement.closest('td');
        if (!parentTd) return;

        const preview = parentTd.querySelector('.content-preview');
        const full = parentTd.querySelector('.content-full');

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