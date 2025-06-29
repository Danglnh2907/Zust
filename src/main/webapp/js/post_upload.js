document.addEventListener('DOMContentLoaded', () => {
    // --- Define and Register Custom Blot (Unchanged) ---
    const BlockEmbed = Quill.import('blots/block/embed');

    class DeletableImage extends BlockEmbed {
        static create(value) {
            const node = super.create();
            node.classList.add('image-blot-container');
            const image = document.createElement('img');
            image.setAttribute('src', value);
            image.setAttribute('alt', 'User uploaded content');
            const button = document.createElement('button');
            button.innerHTML = '×';
            button.classList.add('image-delete-btn');
            button.setAttribute('type', 'button');
            node.appendChild(image);
            node.appendChild(button);
            return node;
        }

        static value(node) {
            return node.querySelector('img').src;
        }
    }

    DeletableImage.blotName = 'deletable-image';
    DeletableImage.tagName = 'div';
    Quill.register(DeletableImage);

    // --- Initialize Quill ---
    const quill = new Quill('#editor', {
        modules: {toolbar: '#toolbar'},
        placeholder: 'What\'s on your mind?',
        theme: 'snow'
    });

    // --- Override Image Handler (Unchanged) ---
    const toolbar = quill.getModule('toolbar');
    toolbar.addHandler('image', imageHandler);

    function imageHandler() {
        const input = document.createElement('input');
        input.setAttribute('type', 'file');
        input.setAttribute('accept', 'image/*');
        input.click();
        input.onchange = () => {
            const file = input.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    const range = quill.getSelection(true);
                    quill.insertEmbed(range.index, 'deletable-image', e.target.result, Quill.sources.USER);
                    quill.setSelection(range.index + 1, Quill.sources.SILENT);
                };
                reader.readAsDataURL(file);
            }
        };
    }

    // --- Event Listener for Deleting Images (Unchanged) ---
    const editorContainer = document.querySelector('#editor');
    editorContainer.addEventListener('click', (e) => {
        if (e.target && e.target.matches('.image-delete-btn')) {
            const blotNode = e.target.closest('.image-blot-container');
            if (blotNode) {
                const blot = Quill.find(blotNode);
                if (blot) {
                    blot.remove();
                }
            }
        }
    });

    // --- NEW: Function to group images into grids ---
    const editor = quill.root;

    function updateImageGrids() {
        // First, unwrap any existing grids to handle edits
        editor.querySelectorAll('.image-grid-wrapper').forEach(wrapper => {
            [...wrapper.childNodes].forEach(child => {
                wrapper.parentNode.insertBefore(child, wrapper);
            });
            wrapper.parentNode.removeChild(wrapper);
        });

        let consecutiveImages = [];
        [...editor.childNodes].forEach(node => {
            if (node.classList && node.classList.contains('image-blot-container')) {
                consecutiveImages.push(node);
            } else {
                if (consecutiveImages.length > 1) {
                    const wrapper = document.createElement('div');
                    wrapper.classList.add('image-grid-wrapper');
                    editor.insertBefore(wrapper, consecutiveImages[0]);
                    consecutiveImages.forEach(imgNode => wrapper.appendChild(imgNode));
                }
                consecutiveImages = [];
            }
        });
        // Check for a trailing group of images at the end of the editor
        if (consecutiveImages.length > 1) {
            const wrapper = document.createElement('div');
            wrapper.classList.add('image-grid-wrapper');
            editor.insertBefore(wrapper, consecutiveImages[0]);
            consecutiveImages.forEach(imgNode => wrapper.appendChild(imgNode));
        }
    }

    // --- NEW: Custom Privacy Dropdown Logic ---
    const privacySelector = document.getElementById('privacy-selector');
    const privacyIconDisplay = document.getElementById('privacy-icon-display');
    const privacyDisplayText = document.getElementById('privacy-display-text');
    const privacyDropdownMenu = document.getElementById('privacy-dropdown-menu');
    const privacyOptions = document.querySelectorAll('.privacy-dropdown-option');

    let currentPrivacy = 'public';

    function getPrivacyIconHTML(privacy) {
        switch (privacy) {
            case 'friend':
                return `<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`;
            case 'private':
                return `<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`;
            case 'public':
            default:
                return `<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>`;
        }
    }

    function getPrivacyDisplayText(privacy) {
        switch (privacy) {
            case 'friend':
                return 'Friends';
            case 'private':
                return 'Private';
            case 'public':
            default:
                return 'Public';
        }
    }

    function updatePrivacyDisplay() {
        privacyIconDisplay.innerHTML = getPrivacyIconHTML(currentPrivacy);
        privacyDisplayText.textContent = getPrivacyDisplayText(currentPrivacy);

        // Update selected state in dropdown
        privacyOptions.forEach(option => {
            option.classList.toggle('selected', option.dataset.value === currentPrivacy);
        });
    }

    function toggleDropdown() {
        const isOpen = privacySelector.classList.contains('open');
        if (isOpen) {
            closeDropdown();
        } else {
            openDropdown();
        }
    }

    function openDropdown() {
        privacySelector.classList.add('open');
        privacyDropdownMenu.classList.add('open');
        document.addEventListener('click', handleOutsideClick);
    }

    function closeDropdown() {
        privacySelector.classList.remove('open');
        privacyDropdownMenu.classList.remove('open');
        document.removeEventListener('click', handleOutsideClick);
    }

    function handleOutsideClick(event) {
        if (!privacySelector.contains(event.target)) {
            closeDropdown();
        }
    }

    // Event listeners
    privacySelector.addEventListener('click', (e) => {
        if (!e.target.closest('.privacy-dropdown-option')) {
            toggleDropdown();
        }
    });

    privacyOptions.forEach(option => {
        option.addEventListener('click', (e) => {
            e.stopPropagation();
            currentPrivacy = option.dataset.value;
            updatePrivacyDisplay();
            closeDropdown();
        });
    });

    // Initialize
    updatePrivacyDisplay();


    // --- Post Button and Feed Logic ---
    const postButton = document.getElementById('post-button');
    const postsFeed = document.getElementById('posts-feed');

    quill.on('text-change', () => {
        const hasContent = quill.getLength() > 1 || quill.getContents().ops.some(op => op.insert['deletable-image']);
        postButton.disabled = !hasContent;
        // Update the grid layout after any change
        setTimeout(updateImageGrids, 0);
    });

    postButton.addEventListener('click', () => {
        // Get the editor content
        let contentHTML = quill.root.innerHTML;

        // Clean up unnecessary elements
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = contentHTML;

        // Remove image-grid-wrapper and empty paragraphs
        const gridWrappers = tempDiv.getElementsByClassName('image-grid-wrapper');
        Array.from(gridWrappers).forEach(wrapper => wrapper.remove());

        // Remove delete buttons
        const deleteButtons = tempDiv.getElementsByClassName('image-delete-btn');
        Array.from(deleteButtons).forEach(button => button.remove());

        // Remove empty paragraphs and trailing line breaks
        const cleanUpElements = () => {
            let removed = false;
            // Remove paragraphs that only contain <br>
            const emptyParagraphs = tempDiv.querySelectorAll('p:only-child:has(br), p:empty');
            emptyParagraphs.forEach(p => {
                p.remove();
                removed = true;
            });
            // Remove trailing <p><br></p>
            if (tempDiv.lastElementChild &&
                tempDiv.lastElementChild.tagName === 'P' &&
                (!tempDiv.lastElementChild.textContent.trim() ||
                    tempDiv.lastElementChild.innerHTML === '<br>')) {
                tempDiv.lastElementChild.remove();
                removed = true;
            }
            return removed;
        };

        // Run cleanup until no more elements are removed
        while (cleanUpElements()) {
            // Continue cleaning until no changes are made
        }

        // Get cleaned HTML
        contentHTML = tempDiv.innerHTML;


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
        //formData.append('post_privacy', document.getElementById('post_privacy').value);
        formData.append('post_privacy', currentPrivacy);
        console.log(currentPrivacy);
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
            fetch('/zust/post?action=create', {
                method: 'POST',
                body: formData
            })
                .then(response => {
                    if (response.status === 201) { // Status Created
                        window.location.href = '/zust/post';
                    } else {
                        throw new Error('Failed to create post');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Failed to create post. Please try again.');
                });
        });

        // Optional: Clear the editor after posting
        quill.setContents([{insert: '\n'}]);
    });
});
