<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Process Join Group Requests</title>
    <style>
        .request { border: 1px solid #ccc; padding: 10px; margin: 10px 0; }
        .error { color: red; }
        .success { color: green; }
    </style>
</head>
<body>
<h2>Process Join Group Requests</h2>
<%
    java.util.List<dto.JoinGroupRequestDTO> requests = (java.util.List<dto.JoinGroupRequestDTO>) request.getAttribute("requests");
    String error = (String) request.getAttribute("error");
    String message = (String) request.getAttribute("message");
%>
<% if (error != null) { %>
<p class="error"><%= error %></p>
<% } %>
<% if (message != null) { %>
<p class="success"><%= message %></p>
<% } %>
<% if (requests != null && !requests.isEmpty()) { %>
<% for (dto.JoinGroupRequestDTO requestDto : requests) { %>
<div class="request">
    <p>User ID: <%= requestDto.getAccount().getId() %></p>
    <p>Group ID: <%= requestDto.getGroup().getId() %></p>
    <p>Request Date: <%= requestDto.getRequest().getJoinGroupRequestDate() %></p>
    <p>Status: <%= requestDto.getRequest().getJoinGroupRequestStatus() %></p>
    <form action="processJoinGroupRequest" method="post">
        <input type="hidden" name="requestId" value="<%= requestDto.getRequest().getId() %>">
        <input type="hidden" name="groupId" value="<%= requestDto.getGroup().getId() %>">
        <button type="submit" name="action" value="approve">Approve</button>
        <button type="submit" name="action" value="disapprove">Disapprove</button>
    </form>
</div>
<% } %>
<% } else { %>
<p><%= message != null ? message : "No pending requests to process" %></p>
<% } %>
</body>
</html>