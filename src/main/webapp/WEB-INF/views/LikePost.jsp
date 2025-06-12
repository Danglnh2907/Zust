<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="vn">
<head>
    <title>Zust - Like & Repost</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body {
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            background: #f4f4f4;
            margin: 0;
            padding: 20px;
        }

        h2 {
            text-align: center;
            color: #333;
        }

        .post-container {
            max-width: 800px;
            margin: 0 auto;
        }

        .post {
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            padding: 20px;
            margin-bottom: 20px;
            transition: transform 0.2s ease-in-out;
        }

        .post:hover {
            transform: translateY(-3px);
        }

        .post-content {
            font-size: 18px;
            margin-bottom: 10px;
        }

        .post-meta {
            font-size: 14px;
            color: #666;
            margin-bottom: 15px;
        }

        .buttons {
            display: flex;
            gap: 15px;
        }

        .buttons button {
            flex: 1;
            padding: 10px;
            font-size: 14px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            transition: background 0.3s, color 0.3s;
        }

        .like-button {
            background: #eeeeee;
        }

        .like-button.liked {
            background: #ffebee;
            color: #d32f2f;
            font-weight: bold;
        }

        .repost-button {
            background: #e0f7fa;
        }

        .repost-button.reposted {
            background: #a5d6a7;
            color: #2e7d32;
            font-weight: bold;
        }
    </style>
</head>
<body>
<h2>📢 Like & Repost Posts</h2>
<div class="post-container">
    <%
        java.util.List<Object> posts = (java.util.List<Object>) request.getAttribute("posts");
        if (posts == null || posts.isEmpty()) {
    %>
    <p>No posts available.</p>
    <%
    } else {
        for (int i = 0; i < posts.size(); i += 2) {
            dto.PostWithLikeDTO likeDto = (dto.PostWithLikeDTO) posts.get(i);
            dto.PostWithRepostDTO repostDto = (dto.PostWithRepostDTO) posts.get(i + 1);
            model.Post post = likeDto.getPost();
    %>
    <div class="post">
        <div class="post-content"><%= post.getPostContent() %></div>
        <div class="post-meta">👤 User ID: <%= post.getAccount().getId() %></div>
        <div class="buttons">
            <button id="likeButton-<%= post.getId() %>"
                    class="like-button <%= likeDto.isLiked() ? "liked" : "" %>"
                    onclick="toggleLike(<%= post.getId() %>)">
                👍 <%= likeDto.isLiked() ? "Unlike" : "Like" %> (<span id="likeCount-<%= post.getId() %>"><%= likeDto.getLikeCount() %></span>)
            </button>
            <button id="repostButton-<%= post.getId() %>"
                    class="repost-button <%= repostDto.isReposted() ? "reposted" : "" %>"
                    onclick="toggleRepost(<%= post.getId() %>)">
                🔁 <%= repostDto.isReposted() ? "Unrepost" : "Repost" %> (<span id="repostCount-<%= post.getId() %>"><%= repostDto.getRepostCount() %></span>)
            </button>
        </div>
    </div>
    <%
            }
        }
    %>
</div>

<script>
    function toggleLike(postId) {
        $.ajax({
            url: 'likePost',
            type: 'POST',
            data: { postId: postId },
            success: function(response) {
                try {
                    const data = JSON.parse(response);
                    if (data.error) {
                        alert('Error: ' + data.error);
                        return;
                    }
                    $('#likeCount-' + postId).text(data.likeCount);
                    const btn = $('#likeButton-' + postId);
                    btn.text((data.isLiked ? 'Unlike' : 'Like') + ' (' + data.likeCount + ')');
                    btn.toggleClass('liked', data.isLiked);
                } catch (e) {
                    alert("Parse error: " + e.message);
                }
            }
        });
    }

    function toggleRepost(postId) {
        $.ajax({
            url: 'repost',
            type: 'POST',
            data: { postId: postId },
            success: function(response) {
                try {
                    const data = JSON.parse(response);
                    if (data.error) {
                        alert('Error: ' + data.error);
                        return;
                    }
                    $('#repostCount-' + postId).text(data.repostCount);
                    const btn = $('#repostButton-' + postId);
                    btn.text((data.isReposted ? 'Unrepost' : 'Repost') + ' (' + data.repostCount + ')');
                    btn.toggleClass('reposted', data.isReposted);
                } catch (e) {
                    alert("Parse error: " + e.message);
                }
            }
        });
    }
</script>
</body>
</html>
