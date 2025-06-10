<%--
  Created by IntelliJ IDEA.
  User: hoqua
  Date: 6/10/2025
  Time: 2:12 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Create Group</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>

</head>
<body>
    <h1>Create New Group</h1>
    <form action="group?action=create" method="post" enctype="multipart/form-data">
        <label>Group Name</label>
        <input name="name" maxlength="100" type="text" placeholder="Enter Group Name" required>
        <label>Group Description</label>
        <input name="description" type="text" required>
        <label>Cover Image</label>
        <input name="image" type="file"  accept="image/*">
        <input type="submit" value="submit">
    </form>
    <a href="group?action=view&id=1">Link</a>
    <%
    if(request.getAttribute("msg") != null){
    %>

    <p><%= request.getAttribute("msg")%></p>

    <%
    }
    %>
</body>
</html>
