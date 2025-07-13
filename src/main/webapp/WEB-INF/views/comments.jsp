<%--
  Created by IntelliJ IDEA.
  User: giaph
  Date: 6/14/2025
  Time: 10:15 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Comments Section</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/comments.css">
    <script>
        window.contextPath = '${pageContext.request.contextPath}';
    </script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap" rel="stylesheet">
</head>
<body>
<div class="comments-section-container">
    <h3 class="comments-heading">Comments:</h3>

    <c:choose>
        <c:when test="${empty comments}">
            <p>No comments found.</p>
        </c:when>
        <c:otherwise>
            <div class="comments-list">
                <c:forEach var="comment" items="${comments}">
                    <div class="comment-item ${not empty comment.replyCommentId ? 'is-reply' : ''}">
                        <img class="comment-avatar" src="https://i.pravatar.cc/150?u=shaanalam"
                             alt="${comment.username}'s Avatar"
                             onerror="this.src='/images/avatars/default.png'">
                        <div class="comment-content">
                            <div class="comment-bubble" id="comment-bubble-${comment.commentId}">
                                <div>
                                    <div class="comment-author">${comment.username}</div>
                                    <p class="comment-text" id="comment-text-${comment.commentId}">${comment.commentContent}</p>
                                    <c:if test="${not empty comment.commentImage}">
                                        <img class="comment-image" id="comment-image-${comment.commentId}"
                                             src="${pageContext.request.contextPath}/comment-image/images/${comment.commentImage}" alt="${comment.username}'s Comment Image">
                                    </c:if>
                                </div>
                                <button class="comment-menu-button" aria-label="Comment options"
                                        data-comment-id="${comment.commentId}">
                                    <span>•••</span>
                                </button>
                                <div class="comment-menu" id="comment-menu-${comment.commentId}">
                                    <a href="#" class="edit" data-comment-id="${comment.commentId}" data-post-id="${postId}">Edit</a>
                                    <a href="#" class="delete" data-comment-id="${comment.commentId}">Delete</a>
                                </div>
                            </div>
                            <div class="edit-form" id="edit-form-${comment.commentId}">
                                <form id="edit-comment-form-${comment.commentId}" enctype="multipart/form-data">
                                    <input type="hidden" name="postId" value="${postId}">
                                    <input type="hidden" name="replyCommentId" value="${comment.replyCommentId}">
                                    <textarea class="comment-input" name="commentContent"
                                              required>${comment.commentContent}</textarea>
                                    <input type="file" id="edit-comment-image-${comment.commentId}"
                                           name="commentImage" accept="image/*" style="display: none;">
                                    <div class="form-actions">
                                        <button type="button" onclick="submitEditForm(${comment.commentId})">Save</button>
                                        <button type="button" class="cancel"
                                                onclick="hideEditForm(${comment.commentId})">Cancel</button>
                                    </div>
                                </form>
                            </div>
                            <div class="comment-actions">
                                <span>${comment.likeCount} Likes</span>
                                <a href="#" onclick="likeComment(${comment.commentId})">Like</a>
                                <a href="#" onclick="showReplyForm(${comment.commentId}, ${postId})">Reply</a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

    <div class="comment-composer-container">
        <div class="comment-composer">
            <img class="comment-avatar" src="https://i.pravatar.cc/150?u=shaanalam"
                 alt="Current User Avatar" onerror="this.src='/images/avatars/default.png'">
            <form action="${pageContext.request.contextPath}/comment?action=create" method="post"
                  enctype="multipart/form-data" class="comment-input-area" id="comment-create-form">
                <input type="hidden" name="postId" value="${postId}">
                <input type="hidden" name="replyCommentId" id="replyCommentId" value="">
                <textarea class="comment-input" name="commentContent"
                          placeholder="Write comment..."></textarea>
                <div class="comment-input-icons">
                    <label for="comment-image-input">
                        <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAANCAYAAACdKY9CAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAADXSURBVHgBlZFPCwFRFMXHn81QiIXNrBRZUfIB2CoL+Yo+g52UjWRHWZgFC5ZEFmpYOIczeb1Gk1e/13v33nP/vOc4f66EcXZBISb+mjYuPXCIEVRMwR34oAum4BwhKCUtQxNsQM2wNUAxvISC0MDMrLrQndWyoO98Znw7PVA2MmZAB2zln4M1GLLNFLaWnJ7Yq3+2NVGSABzBgBly4KReR873pewXY8ySgqcM7Lstfq0gocFcZdyBW0Qgq694MH+a/VdBHjw0ly8fP3VsC+xFcV0xFzCj8QXLwybH+TNMlwAAAABJRU5ErkJggg=="
                             alt="Attachment icon">
                    </label>
                    <input type="file" id="comment-image-input" name="commentImage" accept="image/*" style="display: none;">
                    <button type="submit" class="post-button">
                        <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAANCAYAAACdKY9CAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAB5SURBVHgBnY6xCYBADEVPsHIFG7F0BCsbC53iNrN2A7nm3EQ3cIQzSiIfPCHnhwf54X8SY36qNok6CEe02sJEBMZrih2UBMf7qAp+K3wUn4sZlK5lCd4SI/iFGHJY9EQFvoF5J2bwdzD2zsaXXrLaoMhrg6JVG0zWCZtAJOrIQN5tAAAAAElFTkSuQmCC"
                             alt="Send icon">
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
<script src="${pageContext.request.contextPath}/js/comments.js"></script>
</body>
</html>