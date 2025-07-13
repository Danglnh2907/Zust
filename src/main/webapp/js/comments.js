/*
 * Handle all comment-related client-side interactions:
 * - Initialize event listeners for comment creation, editing, deletion, and replies
 * - Manage form validation and image previews
 * - Dynamically create and update comment elements
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log("DOM fully loaded, setting up event listeners");

    // Initialize DOM element references
    const commentsList = document.querySelector('.comments-list');
    const commentForm = document.getElementById('comment-create-form');
    const commentInput = document.querySelector('.comment-input');
    const commentImageInput = document.getElementById('comment-image-input');
    const submitButton = commentForm.querySelector('button[type="submit"]');
    let previewImage = null;

    // Check if commentsList exists, log error if not
    if (!commentsList) {
        console.error("commentsList not found. Using document delegation.");
    }

    // Set up single event delegation for all comment interactions
    document.addEventListener('click', (e) => {
        console.log("Click event captured, target:", e.target, "currentTarget:", e.currentTarget);

        // Handle menu button click to toggle comment options menu
        const menuButton = e.target.closest('.comment-menu-button');
        if (menuButton) {
            e.preventDefault();
            e.stopPropagation();
            const commentId = menuButton.getAttribute('data-comment-id');
            console.log("Menu button clicked for commentId:", commentId, "Button:", menuButton);
            const menu = document.getElementById('comment-menu-' + commentId);
            if (menu) {
                console.log("Menu found:", menu);
                setTimeout(() => {
                    const allMenus = document.querySelectorAll('.comment-menu');
                    allMenus.forEach(m => m.classList.remove('show'));
                    menu.classList.toggle('show');
                    console.log("Menu toggle state:", menu.classList.contains('show'));
                }, 0);
            } else {
                console.error("Menu not found for commentId:", commentId);
            }
            return;
        }

        // Handle edit button click to show edit form
        const editBtn = e.target.closest('.comment-menu .edit');
        if (editBtn) {
            e.preventDefault();
            const commentId = editBtn.getAttribute('data-comment-id');
            const postId = editBtn.getAttribute('data-post-id');
            console.log("Edit clicked for commentId:", commentId, "postId:", postId, "Edit Button:", editBtn);
            showEditForm(commentId, postId);
            return;
        }

        // Handle delete button click to remove a comment
        const deleteBtn = e.target.closest('.comment-menu .delete');
        if (deleteBtn) {
            e.preventDefault();
            const commentId = deleteBtn.getAttribute('data-comment-id');
            console.log("Delete clicked for commentId:", commentId, "Delete Button:", deleteBtn);
            deleteComment(commentId);
            return;
        }
    });

    // Close all comment menus when clicking outside
    window.addEventListener('click', (e) => {
        const allMenus = document.querySelectorAll('.comment-menu');
        allMenus.forEach(menu => {
            if (menu.classList.contains('show') && !e.target.closest('.comment-menu') && !e.target.closest('.comment-menu-button')) {
                console.log("Closing menu for commentId:", menu.id.replace('comment-menu-', ''));
                menu.classList.remove('show');
            }
        });
    });

    // Validate comment form to enable/disable submit button
    function validateForm() {
        const hasText = commentInput.value.trim().length > 0;
        const hasImage = commentImageInput.files.length > 0;
        submitButton.disabled = !hasText && !hasImage;
    }

    commentInput.addEventListener('input', validateForm);
    commentImageInput.addEventListener('change', validateForm);

    // Handle comment form submission
    commentForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const formData = new FormData(commentForm);
        formData.append('action', 'create');

        // Log FormData for debugging
        for (let [key, value] of formData.entries()) {
            console.log(key, value);
        }

        fetch(window.contextPath + '/comment', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    const comment = data.comment;
                    const commentsList = document.querySelector('.comments-list');
                    if (commentsList) {
                        const newComment = createCommentElement(comment);
                        commentsList.insertBefore(newComment, commentsList.firstChild);
                        console.log("New comment inserted with commentId:", comment.commentId);
                    } else {
                        console.error("commentsList not found during insertion.");
                    }
                    commentForm.reset();
                    document.getElementById('replyCommentId').value = '';
                    document.querySelector('.comment-input').placeholder = 'Write comment...';
                    validateForm(); // Re-enable button after reset
                } else {
                    alert('Failed to create comment: ' + (data.message || 'Unknown error'));
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Failed to create comment. Please try again.');
            });
    });

    // Handle image preview for comment input
    const commentInputArea = document.querySelector('.comment-input-area');

    commentImageInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file && file.type.startsWith('image/')) {
            const reader = new FileReader();
            if (previewImage) previewImage.remove();
            reader.onload = (event) => {
                previewImage = document.createElement('img');
                previewImage.className = 'comment-preview-image';
                previewImage.src = event.target.result;
                previewImage.style.maxWidth = '100px';
                previewImage.style.maxHeight = '100px';
                previewImage.style.borderRadius = '8px';
                previewImage.style.marginTop = '0.5rem';
                previewImage.style.marginLeft = '0.75rem';
                commentInputArea.appendChild(previewImage);
            };
            reader.readAsDataURL(file);
        } else if (previewImage) {
            previewImage.remove();
            previewImage = null;
        }
    });

    // Clear image preview when form is reset or submitted
    commentForm.addEventListener('reset', () => {
        if (previewImage) {
            previewImage.remove();
            previewImage = null;
        }
    });
    commentForm.addEventListener('submit', () => {
        if (previewImage) {
            previewImage.remove();
            previewImage = null;
        }
    });

    // Perform initial form validation
    validateForm();
});

/*
 * Create a DOM element for a new comment
 * @param {Object} comment - Comment data including id, content, image, etc.
 * @returns {HTMLElement} - The constructed comment element
 */
function createCommentElement(comment) {
    const div = document.createElement('div');
    div.className = 'comment-item' + (comment.replyCommentId ? ' is-reply' : '');
    div.innerHTML = `
        <img class="comment-avatar" src="https://i.pravatar.cc/150?u=shaanalam" alt="${comment.username}'s Avatar" onerror="this.src='/images/avatars/default.png'">
        <div class="comment-content">
            <div class="comment-bubble" id="comment-bubble-${comment.commentId}">
                <div>
                    <div class="comment-author">${comment.username}</div>
                    <p class="comment-text" id="comment-text-${comment.commentId}">${comment.commentContent}</p>
                    ${comment.commentImage ? `<img class="comment-image" id="comment-image-${comment.commentId}" src="${window.contextPath}/comment-image/images/${comment.commentImage}" alt="${comment.username}'s Comment Image">` : ''}
                </div>
                <button class="comment-menu-button" aria-label="Comment options" data-comment-id="${comment.commentId}">
                    <span>•••</span>
                </button>
                <div class="comment-menu" id="comment-menu-${comment.commentId}">
                    <a href="#" class="edit" data-comment-id="${comment.commentId}" data-post-id="${comment.postId}">Edit</a>
                    <a href="#" class="delete" data-comment-id="${comment.commentId}">Delete</a>
                </div>
            </div>
            <div class="edit-form" id="edit-form-${comment.commentId}">
                <form id="edit-comment-form-${comment.commentId}" enctype="multipart/form-data">
                    <input type="hidden" name="postId" value="${comment.postId}">
                    <input type="hidden" name="replyCommentId" value="${comment.replyCommentId}">
                    <textarea class="comment-input" name="commentContent" required>${comment.commentContent}</textarea>
                    <input type="file" id="edit-comment-image-${comment.commentId}" name="commentImage" accept="image/*" style="display: none;">
                    <div class="form-actions">
                        <button type="button" onclick="submitEditForm(${comment.commentId})">Save</button>
                        <button type="button" class="cancel" onclick="hideEditForm(${comment.commentId})">Cancel</button>
                    </div>
                </form>
            </div>
            <div class="comment-actions">
                <span>${comment.likeCount} Likes</span>
                <a href="#" onclick="likeComment(${comment.commentId})">Like</a>
                <a href="#" onclick="showReplyForm(${comment.commentId}, ${comment.postId})">Reply</a>
            </div>
        </div>
    `;
    return div;
}

/*
 * Show the edit form for a specific comment
 * @param {number} commentId - The ID of the comment to edit
 * @param {number} postId - The ID of the post the comment belongs to
 */
function showEditForm(commentId, postId) {
    console.log("Showing edit form for commentId:", commentId, "postId:", postId);
    const bubble = document.getElementById('comment-bubble-' + commentId);
    const form = document.getElementById('edit-form-' + commentId);
    if (bubble && form) {
        bubble.style.display = 'none';
        form.style.display = 'flex';
    } else {
        console.error("Element not found: bubble or form for commentId:", commentId);
    }
}

/*
 * Hide the edit form and show the comment content
 * @param {number} commentId - The ID of the comment being edited
 */
function hideEditForm(commentId) {
    console.log("Hiding edit form for commentId:", commentId);
    const bubble = document.getElementById('comment-bubble-' + commentId);
    const form = document.getElementById('edit-form-' + commentId);
    if (bubble && form) {
        bubble.style.display = 'flex';
        form.style.display = 'none';
    } else {
        console.error("Element not found: bubble or form for commentId:", commentId);
    }
}

/*
 * Submit the edit form for a comment
 * @param {number} commentId - The ID of the comment being edited
 */
function submitEditForm(commentId) {
    console.log("Submitting edit form for commentId:", commentId);
    const form = document.getElementById('edit-comment-form-' + commentId);
    if (form) {
        const

            formData = new FormData(form);
        formData.append('action', 'edit');
        formData.append('id', commentId);

        fetch(window.contextPath + '/comment', {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    document.getElementById('comment-text-' + commentId).textContent =
                        formData.get('commentContent');
                    const imageInput = document.getElementById('edit-comment-image-' + commentId);
                    if (imageInput.files.length > 0) {
                        const imageUrl = URL.createObjectURL(imageInput.files[0]);
                        let imageElement = document.getElementById('comment-image-' + commentId);
                        if (!imageElement) {
                            imageElement = document.createElement('img');
                            imageElement.id = 'comment-image-' + commentId;
                            imageElement.className = 'comment-image';
                            imageElement.alt = 'Comment Image';
                            document.getElementById('comment-text-' + commentId).after(imageElement);
                        }
                        imageElement.src = imageUrl;
                    }
                    hideEditForm(commentId);
                } else {
                    alert('Failed to edit comment: ' + (data.message || 'Unknown error'));
                }
            })
            .catch(error => {
                console.error('Edit error:', error);
                alert('Error editing comment');
            });
    } else {
        console.error("Form not found for commentId:", commentId);
    }
}

/*
 * Delete a comment after user confirmation
 * @param {number} commentId - The ID of the comment to delete
 */
function deleteComment(commentId) {
    if (!confirm('Are you sure you want to delete this comment?')) {
        return;
    }

    const formData = new FormData();
    formData.append('action', 'delete');
    formData.append('id', commentId);

    fetch(window.contextPath + '/comment', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const commentItem = document.getElementById('comment-bubble-' + commentId).closest('.comment-item');
                if (commentItem) {
                    commentItem.remove();
                } else {
                    console.error("Comment item not found for commentId:", commentId);
                }
            } else {
                alert('Failed to delete comment: ' + (data.message || 'Unknown error'));
            }
        })
        .catch(error => {
            console.error('Delete error:', error);
            alert('Error deleting comment');
        });
}

/*
 * Handle liking a comment
 * @param {number} commentId - The ID of the comment to like
 */
function likeComment(commentId) {
    console.log("Liking comment " + commentId);
}

/*
 * Show the reply form for a specific comment
 * @param {number} commentId - The ID of the comment to reply to
 * @param {number} postId - The ID of the post the comment belongs to
 */
function showReplyForm(commentId, postId) {
    document.getElementById('replyCommentId').value = commentId;
    document.querySelector('.comment-input').focus();
    document.querySelector('.comment-input').placeholder = "Reply to comment #" + commentId + "...";
}