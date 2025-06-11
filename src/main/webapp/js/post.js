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
});

/*==== Post editor scripting ===*/
/*==============================*/
document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Quill Editor
    const toolbarOptions = [
        ['bold', 'italic', 'underline'],
        [{'list': 'ordered'}, {'list': 'bullet'}],
        ['link', 'image'] // 'image' will prompt for a URL by default
    ];

    const quill = new Quill('#editor', {
        modules: {
            toolbar: toolbarOptions
        },
        placeholder: 'What\'s happening?',
        theme: 'snow'
    });

    // 2. Set up the Post Button
    const postButton = document.querySelector('.post-button');

    postButton.addEventListener('click', () => {
        const contentHTML = quill.root.innerHTML;

        // Simple validation to prevent empty posts
        if (contentHTML === '<p><br></p>') {
            alert('Please write something before posting!');
            return;
        }

        //Gather data
        const hashtagRegex = /#\w+/g;
        const hashtags = Array.from(contentHTML.match(hashtagRegex) || []);
        const formData = new FormData();
        formData.append('htmlContent', contentHTML);
        formData.append('hashtags', hashtags != null && hashtags.join(""));
        formData.append('post_privacy', document.getElementById('post_privacy').value);
        // Find and append images
        const images = quill.root.querySelectorAll('img[src^="data:image"]');
        const imagePromises = [];

        for (let i = 0; i < images.length; i++) {
            const imgSrc = images[i].src;
            imagePromises.push(
                fetch(imgSrc)
                    .then(res => res.blob())
                    .then(blob => {
                        formData.append(`image${i}`, blob, `image${i}.png`);
                    })
            );
        }

        // Wait for all images to be processed before sending
        Promise.all(imagePromises).then(() => {
            try {
                fetch('/zust/post?action=create', {
                    method: 'POST',
                    body: formData
                }).then(r => r.ok);
            } catch (error) {
                console.error('Error:', error);
            }
        });

        // Optional: Clear the editor after posting
        quill.setContents([{insert: '\n'}]);
    });
});
