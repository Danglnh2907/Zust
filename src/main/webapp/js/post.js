/*==== Post displaying scripting ===*/
/*==================================*/
document.addEventListener('DOMContentLoaded', () => {

    // --- Options Menu (3 dots) Logic ---
    const optionsBtns = document.querySelectorAll('.options-btn');
    const optionsMenus = document.querySelectorAll('.options-menu');

    optionsBtns.forEach((btn, index) => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            optionsMenus[index].classList.toggle('show');
        });
    });

    // Close menu if clicked outside
    window.addEventListener('click', () => {
        optionsMenus.forEach(menu => {
            if (menu.classList.contains('show')) {
                menu.classList.remove('show');
            }
        });
    });

    // --- Like Button Logic ---
    const likeBtns = document.querySelectorAll('.like-btn');
    const likeCounts = document.querySelectorAll('.like-count');

    likeBtns.forEach((btn, index) => {
        btn.addEventListener('click', () => {
            const isLiked = btn.classList.toggle('liked');
            let likeCount = parseInt(likeCounts[index].textContent);

            if (isLiked) {
                likeCount++;
            } else {
                likeCount--;
            }

            likeCounts[index].textContent = String(likeCount);
        });
    });

    // --- Image Carousel Logic ---
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

    // --- Edit Post Logic ---
    const editModal = new bootstrap.Modal(document.getElementById('edit_post'));
    const editQuill = new Quill('#edit-editor', {
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

    // Handle edit button clicks
    document.querySelectorAll('.options-menu .edit').forEach(editBtn => {
        editBtn.addEventListener('click', (e) => {
            e.preventDefault();
            const postElement = e.target.closest('.post');
            postId = postElement.dataset.postId;
            
            // Get content with images
            let content = postElement.querySelector('.post-content').innerHTML;
            
            // Get existing images and add them to content
            const images = postElement.querySelectorAll('.carousel-track img');
            images.forEach(img => {
                content += `<p><img src="${img.src}"></p>`;
            });

            // Set editor content with text and images
            editQuill.root.innerHTML = content;

            // Store post data for submission
            const modal = document.getElementById('edit_post');
            modal.dataset.postId = postId;

            // Show modal
            editModal.show();
        });
    });

    // Handle save changes button
    document.getElementById('save-edit').addEventListener('click', async () => {
        const modal = document.getElementById('edit_post');
        const postId = modal.dataset.postId;
        const contentHTML = editQuill.root.innerHTML;
        const privacy = document.getElementById('edit_post_privacy').value;

        const formData = new FormData();
        formData.append('htmlContent', contentHTML);
        formData.append('post_privacy', privacy); // Changed to match create form field name

        // Handle all images in editor
        const images = editQuill.root.querySelectorAll('img');
        const imagePromises = [];
        let imageCount = 0;

        for (let i = 0; i < images.length; i++) {
            const img = images[i];
            if (img.src.startsWith('data:image')) {
                // New images (from upload)
                imagePromises.push(
                    fetch(img.src)
                        .then(res => res.blob())
                        .then(blob => {
                            formData.append(`image${imageCount}`, blob, `image${imageCount}.png`);
                            imageCount++;
                        })
                );
            } else {
                // Existing images (from server)
                const urlParts = img.src.split('/');
                const filename = urlParts[urlParts.length - 1];
                formData.append(`image${imageCount}`, filename);
                imageCount++;
            }
        }

        // Extract hashtags from content
        const hashtagRegex = /#\w+/g;
        const hashtags = Array.from(contentHTML.match(hashtagRegex) || []);
        formData.append('hashtags', hashtags.join(''));

        // Submit after all new images are processed
        Promise.all(imagePromises).then(() => {
            fetch(`/zust/post?action=edit&id=${postId}`, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok) {
                    window.location.reload();
                } else {
                    throw new Error('Failed to edit post');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Failed to edit post. Please try again.');
            });
        });
    });

    // --- Delete Post Logic ---
    document.querySelectorAll('.options-menu .delete').forEach(deleteBtn => {
        deleteBtn.addEventListener('click', (e) => {
            e.preventDefault();
            const postElement = e.target.closest('.post');
            const postId = postElement.dataset.postId;
            
            // Show confirmation dialog
            if (confirm('Are you sure you want to delete this post?')) {
                fetch(`/zust/post?action=delete&id=${postId}`, {
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

    // --- Lightbox Logic ---
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

    // Add click listeners to all carousel images
    document.querySelectorAll('.carousel-slide').forEach(img => {
        img.addEventListener('click', () => openLightbox(img.src));
    });

    // Close lightbox when clicking outside the image
    lightbox.addEventListener('click', (e) => {
        if (e.target === lightbox) {
            closeLightbox();
        }
    });

    // Close lightbox when clicking close button
    closeBtn.addEventListener('click', closeLightbox);

    // Close lightbox when pressing Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && lightbox.classList.contains('active')) {
            closeLightbox();
        }
    });
});

