/*==== Post displaying scripting ===*/
/*==================================*/
document.addEventListener('DOMContentLoaded', () => {

    //Options Menu (3 dots) toggle
    const optionsBtns = document.querySelectorAll('.options-btn');
    const optionsMenus = document.querySelectorAll('.options-menu');
    optionsBtns.forEach((btn, index) => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            optionsMenus[index].classList.toggle('show');
        });
    }); //Show menu if click 3-dot button
    window.addEventListener('click', () => {
        optionsMenus.forEach(menu => {
            if (menu.classList.contains('show')) {
                menu.classList.remove('show');
            }
        });
    }); //Close menu if click outside

    //Image Carousel Logic
    const tracks = document.querySelectorAll('.carousel-track');
    tracks.forEach(track => {
        const slides = Array.from(track.children);
        const nextButton = track.parentElement.querySelector('.carousel-btn.next');
        const prevButton = track.parentElement.querySelector('.carousel-btn.prev');

        if (slides.length > 1) {
            const slideWidth = slides[0].getBoundingClientRect().width;
            let currentIndex = 0;

            // Arrange the slides next to one another
            const setSlidePosition = (slide, index) => {
                slide.style.left = slideWidth * index + 'px';
            };
            slides.forEach(setSlidePosition);

            const moveToSlide = (currentTrack, targetIndex) => {
                currentTrack.style.transform = 'translateX(-' + (slideWidth * targetIndex) + 'px)';
                currentIndex = targetIndex;
                updateNavButtons();
            }

            const updateNavButtons = () => {
                prevButton.classList.remove('hidden');
                nextButton.classList.remove('hidden');
                if (currentIndex === 0) {
                    prevButton.classList.add('hidden');
                } else if (currentIndex === slides.length - 1) {
                    nextButton.classList.add('hidden');
                }
            }

            // Initially hide the prev button
            updateNavButtons();

            // When I click right, move slides to the right
            nextButton.addEventListener('click', e => {
                if (currentIndex < slides.length - 1) {
                    moveToSlide(track, currentIndex + 1);
                }
            });

            // When I click left, move slides to the left
            prevButton.addEventListener('click', e => {
                if (currentIndex > 0) {
                    moveToSlide(track, currentIndex - 1);
                }
            });
        } else {
            // Hide buttons if only one or no images
            nextButton.classList.add('hidden');
            prevButton.classList.add('hidden');
        }
    });

    //Lightbox Logic (for full image preview)
    const lightbox = document.getElementById('lightbox');
    const lightboxImg = lightbox.querySelector('.lightbox-image');
    const closeBtn = lightbox.querySelector('.lightbox-close');

    function openLightbox(imageSrc) {
        lightboxImg.src = imageSrc;
        lightbox.classList.add('active');
        document.body.classList.add('lightbox-open');
    }

    function closeLightbox() {
        lightbox.classList.remove('active');
        document.body.classList.remove('lightbox-open');
        lightboxImg.src = ''; // Clear the source
    }

    document.querySelectorAll('.carousel-slide').forEach(img => {
        img.addEventListener('click', () => openLightbox(img.src));
    }); //Add event listener to all images
    lightbox.addEventListener('click', (e) => {
        if (e.target === lightbox) {
            closeLightbox();
        }
    }); //Close if click outside
    closeBtn.addEventListener('click', closeLightbox); //Close if click close ('X') button
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && lightbox.classList.contains('active')) {
            closeLightbox();
        }
    }); //Close lightbox if press Esc

    /*=== Buttons actions ===*/
    //Initialize editor
    const modal = new bootstrap.Modal(document.getElementById('modal'));
    const quill = new Quill('#edit-editor', {
        modules: {
            toolbar: [
                ['bold', 'italic', 'underline'],
                [{'list': 'ordered'}, {'list': 'bullet'}],
                ['link', 'image']
            ]
        },
        theme: 'snow'
    });
    let postId;

    //Like Button Logic (toggle animation and send request to server)
    const likeBtns = document.querySelectorAll('.like-btn');
    const likeCounts = document.querySelectorAll('.like-count');
    likeBtns.forEach((btn, index) => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const isLiked = btn.classList.contains('liked');
            let likeCount = parseInt(likeCounts[index].textContent);

            postId = e.target.closest(".post").dataset.postId;

            if (!isLiked) { //If not liked before, send like request
                //Called to server
                fetch(`/zust/post?action=like&postID=${postId}`, {
                    method: "POST"
                })
                    .then(resp => {
                        if (resp.status === 200) {
                            likeCount++;
                            likeCounts[index].textContent = String(likeCount);
                            btn.classList.add("liked");
                        }
                    })
                    .catch(error => console.log(error))
            } else { //If already like, send unlike request
                //Called to server
                fetch(`/zust/post?action=unlike&postID=${postId}`, {
                    method: "POST"
                })
                    .then(resp => {
                        if (resp.status === 200) {
                            likeCount--;
                            likeCounts[index].textContent = String(likeCount);
                            //Remove the liked class
                            btn.classList.remove("liked");
                        }
                    })
                    .catch(error => console.log(error))
            }
        });
    });

    //Repost button logic
    const repostBtns = document.querySelectorAll(".repost-btn");
    repostBtns.forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            postId = e.target.closest(".post").dataset.postId;

            fetch(`/zust/post?action=repost&postID=${postId}`, {
                method: "POST"
            })
                .then(response => {
                    if (response.status === 200) {
                        alert("repost successfully");
                        window.location.reload();
                    }
                })
                .catch(error => {
                    console.log("Failed to repost!")
                    console.log(error);
                })
        })
    })

    //Comment button logic (open comment in modal)
    const commentBtns = document.querySelectorAll(".comment-btn");
    commentBtns.forEach(btn => {
        btn.addEventListener("click", e => {
            e.preventDefault();

            //Fetch postID from post
            const postElement = e.target.closest(".post");
            postId = postElement.dataset.postId;

            //Fetch data from server
            fetch(`/zust/comment?postID=${postId}`)
                .then(response => {
                    if (response.status === 302) {
                        return response.text();
                    } else if (response.status === 404) {
                        return response.text();
                    }
                })
                .then(html => {
                    document.getElementById("modal-body").innerHTML = html;
                    document.getElementById("modal-title-label").innerText = "Comment section";
                    modal.show();
                    attachListener(postId);
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        })
    })

    //Delete Post Logic
    document.querySelectorAll('.options-menu .delete').forEach(deleteBtn => {
        deleteBtn.addEventListener('click', (e) => {
            e.preventDefault();
            const postElement = e.target.closest('.post');
            const postId = postElement.dataset.postId;

            // Show confirmation dialog
            if (confirm('Are you sure you want to delete this post?')) {
                fetch(`/zust/post?action=delete&postID=${postId}`, {
                    method: 'POST'
                })
                    .then(response => {
                        if (response.ok) {
                            window.location.reload();
                        } else {
                            throw new Error('Failed to delete post');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        alert('Failed to delete post. Please try again.');
                    });
            }
        });
    });

    //Handle edit button clicks
    document.querySelectorAll('.options-menu .edit').forEach(editBtn => {
        editBtn.addEventListener('click', (e) => {
            e.preventDefault();
            //Get the post that triggered the event
            const postElement = e.target.closest('.post');
            //Get postID
            const postId = postElement.dataset.postId;
            //Get post privacy
            let privacy = postElement.dataset.privacy || "public";
            //Get avatar src
            const avatar = postElement.querySelector(".post-avatar").getAttribute("src");
            //Get content
            let content = postElement.querySelector('.post-content').innerHTML;
            // Get images
            const images = [];
            postElement.querySelectorAll(".carousel-slide").forEach(image => {
                images.push(image.getAttribute("src"));
            });

            // Clean content to remove invalid image-blot-container nodes
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = content;
            const invalidBlots = tempDiv.querySelectorAll('.image-blot-container:not(:has(img))');
            invalidBlots.forEach(blot => blot.remove());
            content = tempDiv.innerHTML;

            // Generate modal content
            const modalBody = document.querySelector('#modal-body');
            modalBody.innerHTML = generateComposerHTML(avatar, 'Save Changes');

            // Initialize components
            const quill = initializeQuill(content, images);
            const privacySelector = initializePrivacySelector('privacy-selector', privacy);
            setupSaveButton(quill, 'submit');

            // Set up save button
            const saveButton = document.getElementById('submit');
            saveButton.addEventListener('click', () => {
                submitPost(quill, () => privacySelector.getPrivacy(), 'edit', postId);
            });

            // Show modal
            modal.show();
        });
    });
});

function attachListener(postId) {
    //Comment action (comment button)
    const commentTextarea = document.getElementById('comment-textarea');
    const postCommentBtn = document.getElementById('post-comment-btn');
    let selectedImageBase64 = null;
    function updatePostButtonState() {
        const hasText = commentTextarea.value.trim().length > 0;
        postCommentBtn.disabled = !hasText && !selectedImageBase64;
    }
    commentTextarea.addEventListener('input', updatePostButtonState);

    //Comment action (cancel reply)
    const cancelReplyBtn = document.getElementById('cancel-reply-btn');
    const replyIndicator = document.getElementById('reply-indicator');
    let currentReplyTarget = -1;
    let editingCommentId = null;
    function cancelReply() {
        currentReplyTarget = null;
        replyIndicator.style.display = 'none';
        commentTextarea.placeholder = 'Add a comment...';

        // Reset edit state
        if (editingCommentId) {
            editingCommentId = null;
            postCommentBtn.textContent = 'Post';
            commentTextarea.value = '';
            removeImageBtn.click();
        }
    }
    cancelReplyBtn.addEventListener('click', cancelReply);

    //Comment action (send request to create comment)
    const removeImageBtn = document.getElementById('remove-image-btn');
    postCommentBtn.addEventListener('click', () => {
        const commentText = commentTextarea.value.trim();

        if (editingCommentId) {
            // Handle editing a comment
            if (!commentText && !previewImage.src) return;

            const formData = new FormData();
            formData.append("postID", postId);
            formData.append("content", commentText);

            if (selectedImageBase64) { // A new image was selected
                const mimeType = selectedImageBase64.split(',')[0].split(':')[1].split(';')[0];
                const blob = (base64, mimeType) => {
                    const base64Data = base64.includes(',') ? base64.split(',')[1] : base64;
                    const byteCharacters = atob(base64Data);
                    const byteNumbers = new Array(byteCharacters.length);
                    for (let i = 0; i < byteCharacters.length; i++) {
                        byteNumbers[i] = byteCharacters.charCodeAt(i);
                    }
                    const byteArray = new Uint8Array(byteNumbers);
                    return new Blob([byteArray], { type: mimeType });
                };
                formData.append("image", blob(selectedImageBase64, mimeType), "image." + mimeType.split('/')[1]);
            } else if (previewImage.src) { // An existing image should be kept
                const imageUrl = previewImage.src;
                const filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                formData.append("image", filename);
            }

            fetch(`/zust/comment?action=edit&commentID=${editingCommentId}`, {
                method: "POST",
                body: formData
            })
            .then(response => {
                if (response.ok) {
                    console.log("Update comment successfully");
                    window.location.reload();
                } else {
                    throw new Error('Failed to edit comment');
                }
            })
            .catch(error => {
                console.log(error);
                alert("Error: " + error);
            });

        } else {
            // Handle creating a new comment
            if (!commentText && !selectedImageBase64) return;

            let formData = new FormData();
            formData.append("content", commentText);
            if (selectedImageBase64 !== null) {
                // Extract MIME type from data URL
                const mimeType = selectedImageBase64.split(',')[0].split(':')[1].split(';')[0];
                const blob = (base64, mimeType) => {
                    // Remove data URL prefix if present
                    const base64Data = base64.includes(',') ? base64.split(',')[1] : base64;

                    const byteCharacters = atob(base64Data);
                    const byteNumbers = new Array(byteCharacters.length);

                    for (let i = 0; i < byteCharacters.length; i++) {
                        byteNumbers[i] = byteCharacters.charCodeAt(i);
                    }

                    const byteArray = new Uint8Array(byteNumbers);
                    return new Blob([byteArray], { type: mimeType });
                };
                formData.append("image", blob(selectedImageBase64, mimeType), "image." + mimeType.split('/')[1]);
            }
            formData.append("replyID", currentReplyTarget);
            formData.append("postID", postId)

            fetch('/zust/comment?action=create', {
                method: "POST",
                body: formData
            })
                .then(response => {
                    if (response.status === 201) {
                        console.log("Create comment successfully");
                        window.location.reload();
                    }
                })
                .catch(error => {
                    console.log(error);
                    alert("Error: " + error);
                })
        }

        commentTextarea.value = '';
        removeImageBtn.click();
        cancelReply();
    });

    //Comment section action (like, reply, report)
    const replyToHandle = document.getElementById('reply-to-handle');
    document.getElementById('comment-section').addEventListener('click', (e) => {
        const target = e.target;

        // Like comment button
        const likeBtn = target.closest('.like-btn');
        if (likeBtn) {
            const commentItem = likeBtn.closest(".comment-item");
            if (!commentItem) return;
            const commentID = commentItem.dataset.commentId;
            const isCmtLiked = likeBtn.classList.contains("liked");
            const action = isCmtLiked ? 'unlike' : 'like';

            fetch(`/zust/comment?action=${action}&commentID=${commentID}`, {
                method: "POST"
            })
            .then(response => {
                if (response.status === 201) {
                    const countSpan = likeBtn.querySelector('span');
                    const currentCount = parseInt(countSpan.textContent);
                    if (isCmtLiked) {
                        likeBtn.classList.remove('liked');
                        countSpan.textContent = currentCount - 1;
                    } else {
                        likeBtn.classList.add('liked');
                        countSpan.textContent = currentCount + 1;
                    }
                }
            })
            .catch(error => console.log(error));
            return;
        }

        // Edit comment button
        const editBtn = target.closest(".edit-btn");
        if (editBtn) {
            cancelReply();
            commentTextarea.value = '';
            removeImageBtn.click();

            const commentItem = editBtn.closest('.comment-item');
            if (!commentItem) return;

            editingCommentId = commentItem.dataset.commentId;
            const commentContentEl = commentItem.querySelector('.comment-text');
            const commentImage = commentItem.querySelector('.comment-media img');

            commentTextarea.value = commentContentEl ? commentContentEl.innerText.trim() : '';
            if (commentImage) {
                previewImage.src = commentImage.src;
                imagePreviewContainer.style.display = 'inline-block';
                addImageBtn.disabled = true;
            }

            postCommentBtn.textContent = 'Save';
            commentTextarea.focus();

            // Show editing indicator
            replyToHandle.textContent = 'Editing comment';
            replyIndicator.style.display = 'flex';
            commentTextarea.placeholder = 'Edit your comment...';
            return;
        }

        // Delete comment button
        const deleteBtn = target.closest(".delete-btn");
        if (deleteBtn) {
            const commentItem = deleteBtn.closest(".comment-item");
            if (!commentItem) return;
            const commentID = commentItem.dataset.commentId;

            if (confirm("Are you sure you want to delete this comment?")) {
                fetch(`/zust/comment?action=delete&commentID=${commentID}`, {
                    method: "POST"
                })
                    .then(resp => {
                        if (resp.status === 200) {
                            console.log("Delete comment successfully");
                            window.location.reload();
                        }
                    })
                    .catch(error => console.log(error));
                return;
            }
        }

        // Reply comment button
        const replyBtn = target.closest('.reply-btn');
        if (replyBtn) {
            cancelReply();

            const commentItem = replyBtn.closest('.comment-item');
            if (!commentItem) return;

            const userHandleEl = commentItem.querySelector('.comment-user-handle');
            if (!userHandleEl) return;

            currentReplyTarget = commentItem.dataset.commentId;
            replyToHandle.textContent = userHandleEl.textContent;
            replyIndicator.style.display = 'flex';
            commentTextarea.placeholder = `Replying to ${userHandleEl.textContent}...`;
            commentTextarea.focus();
            return;
        }
    });

    //Image previewer for comment form
    const addImageBtn = document.getElementById('add-comment-image-btn');
    const imagePreviewContainer = document.getElementById('comment-image-preview');
    const previewImage = document.getElementById('preview-image');
    addImageBtn.addEventListener('click', () => {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'image/*';
        input.onchange = () => {
            const file = input.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    selectedImageBase64 = e.target.result;
                    previewImage.src = selectedImageBase64;
                    imagePreviewContainer.style.display = 'inline-block';
                    addImageBtn.disabled = true;
                    updatePostButtonState();
                };
                reader.readAsDataURL(file);
            }
        };
        input.click();
    });
    removeImageBtn.addEventListener('click', () => {
        selectedImageBase64 = null;
        imagePreviewContainer.style.display = 'none';
        previewImage.src = '';
        addImageBtn.disabled = false;
        updatePostButtonState();
    });
}