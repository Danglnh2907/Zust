document.addEventListener('DOMContentLoaded', () => {
    console.log("DOM fully loaded, setting up event listeners");
    // Options Menu Logic
    const optionsBtns = document.querySelectorAll('.comment-menu-button');
    const optionsMenus = document.querySelectorAll('.comment-menu');

    optionsBtns.forEach((btn, index) => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            const commentId = btn.getAttribute('data-comment-id');
            console.log("Menu button clicked for commentId:", commentId);
            const menu = document.getElementById('comment-menu-' + commentId);
            optionsMenus.forEach(m => m.classList.remove('show'));
            menu.classList.toggle('show');
        });
    });

    // Close menu if clicked outside
    window.addEventListener('click', () => {
        optionsMenus.forEach(menu => {
            if (menu.classList.contains('show')) {
                console.log("Closing menu");
                menu.classList.remove('show');
            }
        });
    });

    // Edit Link Logic
    document.querySelectorAll('.comment-menu .edit').forEach(editBtn => {
        editBtn.addEventListener('click', (e) => {
            e.preventDefault();
            const commentId = editBtn.getAttribute('data-comment-id');
            const postId = editBtn.getAttribute('data-post-id');
            console.log("Edit clicked for commentId:", commentId, "postId:", postId);
            showEditForm(commentId, postId);
        });
    });

    // Delete Link Logic
    document.querySelectorAll('.comment-menu .delete').forEach(deleteBtn => {
        deleteBtn.addEventListener('click', (e) => {
            e.preventDefault();
            const commentId = deleteBtn.getAttribute('data-comment-id');
            console.log("Delete clicked for commentId:", commentId);
            deleteComment(commentId);
        });
    });
});

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

function submitEditForm(commentId) {
    console.log("Submitting edit form for commentId:", commentId);
    const form = document.getElementById('edit-comment-form-' + commentId);
    if (form) {
        const formData = new FormData(form);
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

function likeComment(commentId) {
    console.log("Liking comment " + commentId);
}

function showReplyForm(commentId, postId) {
    document.getElementById('replyCommentId').value = commentId;
    document.querySelector('.comment-input').focus();
    document.querySelector('.comment-input').placeholder = "Reply to comment #" + commentId + "...";
}