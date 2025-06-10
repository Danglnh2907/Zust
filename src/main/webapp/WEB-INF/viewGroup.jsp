<%@ page import="dto.ResGroupDTO" %><%--
  Created by IntelliJ IDEA.
  User: hoqua
  Date: 6/10/2025
  Time: 3:40 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Group</title>
</head>
<body>
<h1>View Group</h1>
<%
    if (request.getAttribute("group") == null){
%>

    <p>There is no data</p>
<%
    } else {
        ResGroupDTO group = (ResGroupDTO) request.getAttribute("group");
%>
    <img src="<%= group.getImage()%>" alt="Group Image">
    <p><%= group.getName()%></p>
    <p><%= group.getDescription()%></p>
    <p><%= group.getCreate_date()%></p>
    <p><%= group.getStatus()%></p>
    <p><%= group.getNumberParticipants()%></p>
    <p><%= group.getNumberPosts()%></p>
<%
    }
%>
</body>
</html>
