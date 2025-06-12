<%--
  Created by IntelliJ IDEA.
  User: thang
  Date: 6/8/2025
  Time: 11:04 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Zust - Repost</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        .post { border: 1px solid #ccc; padding: 10px; margin: 10px 0; }
        .reposted { background-color: #d0f0d0; }
    </style>
</head>
<body>
<h2>Repost</h2>
<div class="post-container">
    <c:if test="${empty posts}">
        <p>No posts available.</p>
    </c:if>
    <c:forEach var="postDto" items="${posts}">
        <div class="post">
            <p><strong>${postDto.post.postContent}</strong></p>
            <p>Posted by: User ID ${postDto.post.account.id}</p>
            <p>Reposts: <span id="repostCount-${postDto.post.id}">${postDto.repostCount}</span></p>
            <button onclick="toggleRepost(${postDto.post.id})"
                    id="repostButton-${postDto.post.id}"
                    class="${postDto.isReposted ? 'reposted' : ''}">
                    ${postDto.isReposted ? 'Unrepost' : 'Repost'}
            </button>
        </div>
    </c:forEach>
</div>

<script>
    function toggleRepost(postId) {
        $.ajax({
            url: 'repost',
            type: 'POST',
            data: { postId: postId },
            success: function(response) {
                var data = JSON.parse(response);
                $('#repostCount-' + postId).text(data.repostCount);
                var button = $('#repostButton-' + postId);
                if (data.isReposted) {
                    button.text('Unrepost').addClass('reposted');
                } else {
                    button.text('Repost').removeClass('reposted');
                }
            },
            error: function() {
                alert('Failed to toggle repost');
            }
        });
    }
</script>
</body>
</html>
