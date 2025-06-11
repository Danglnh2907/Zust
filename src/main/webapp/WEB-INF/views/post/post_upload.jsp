<%@ page import="dto.RespPostDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="dto.RespPostDTO" %>
<%--
  Created by IntelliJ IDEA.
  User: Asus
  Date: 6/7/2025
  Time: 9:54 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>Post upload</title>
        <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
    </head>
    <body>
        <div class="upload-container">
            <h1>Create a New Post</h1>
            <select id="post_privacy">
                <option>Public</option>
                <option>Private</option>
                <option>Friend</option>
            </select>
            <!-- This div will be replaced by the Quill editor -->
            <div id="editor-container">
                <div id="editor"></div>
            </div>
            <button type="submit" class="post-button">Post</button>
        </div>
        <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/post.js"></script>
    </body>
</html>