// Define DeletableImage Blot for Quill
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
        const img = node.querySelector('img');
        return img ? img.src : null; // Return null if no img element
    }
}

DeletableImage.blotName = 'deletable-image';
DeletableImage.tagName = 'div';
Quill.register(DeletableImage);

// Initialize Quill editor
function initializeQuill(content, images) {
    // Clean up existing Quill instance if any
    const editor = document.querySelector('#editor');
    if (editor.__quill) {
        editor.__quill = null; // Clear Quill instance
        editor.innerHTML = ''; // Clear editor content
    }

    const quill = new Quill(`#editor`, {
        modules: {toolbar: `#toolbar`},
        theme: 'snow'
    });

    // Append text content into Quill editor (edit) or add placeholder (create)
    if (content) {
        // Validate and clean content to remove invalid image-blot-container nodes
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = content;
        const invalidBlots = tempDiv.querySelectorAll('.image-blot-container:not(:has(img))');
        invalidBlots.forEach(blot => blot.remove());
        quill.root.innerHTML = tempDiv.innerHTML;
    } else {
        quill.placeholder = `What's in your mind....`;
    }

    // Append images as DeletableImage blots
    if (images.length > 0) {
        let position = quill.getLength() - 1; // Before final newline
        images.forEach(src => {
            quill.insertEmbed(position, 'deletable-image', src, Quill.sources.SILENT);
            position += 1; // Increment position for next image
        });
    }

    // Function to group images into grids
    function updateImageGrids() {
        const editor = quill.root;
        // Remove existing grids
        const existingWrappers = editor.querySelectorAll('.image-grid-wrapper');
        existingWrappers.forEach(wrapper => {
            const children = [...wrapper.childNodes];
            children.forEach(child => wrapper.parentNode.insertBefore(child, wrapper));
            wrapper.remove();
        });

        // Group consecutive images
        const nodes = Array.from(editor.childNodes);
        let consecutiveImages = [];
        let insertBeforeNode = null;

        nodes.forEach((node, index) => {
            if (node.classList && node.classList.contains('image-blot-container')) {
                consecutiveImages.push({node, index});
                insertBeforeNode = nodes[index + 1] || null;
            } else {
                if (consecutiveImages.length > 1) {
                    const wrapper = document.createElement('div');
                    wrapper.classList.add('image-grid-wrapper');
                    editor.insertBefore(wrapper, consecutiveImages[0].node);
                    consecutiveImages.forEach(({node}) => wrapper.appendChild(node));
                }
                consecutiveImages = [];
                insertBeforeNode = null;
            }
        });

        // Handle trailing group
        if (consecutiveImages.length > 1) {
            const wrapper = document.createElement('div');
            wrapper.classList.add('image-grid-wrapper');
            editor.insertBefore(wrapper, consecutiveImages[0].node);
            consecutiveImages.forEach(({node}) => wrapper.appendChild(node));
        }
    }

    // Run grid update on content changes and initially
    quill.on('text-change', () => {
        setTimeout(updateImageGrids, 0);
    });
    setTimeout(updateImageGrids, 0);

    const toolbar = quill.getModule('toolbar');
    toolbar.addHandler('image', () => {
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
    });

    //Event Listener for Deleting Images
    document.querySelector("#editor").addEventListener('click', (e) => {
        if (e.target && e.target.matches('.image-delete-btn')) {
            const blotNode = e.target.closest('.image-blot-container');
            if (blotNode) {
                const blot = Quill.find(blotNode);
                if (blot) blot.remove();
            }
        }
    });

    return quill;
}

// Initialize privacy selector
function initializePrivacySelector(selectorId, initialPrivacy = 'public', disableSelector = false) {
    const privacySelector = document.getElementById(selectorId);
    const privacyIconDisplay = privacySelector.querySelector('#privacy-icon-display');
    const privacyDisplayText = privacySelector.querySelector('#privacy-display-text');
    const privacyDropdownMenu = privacySelector.querySelector('#privacy-dropdown-menu');
    const privacyOptions = privacyDropdownMenu.querySelectorAll('.privacy-dropdown-option');
    let currentPrivacy = initialPrivacy;

    function getPrivacyIconHTML(privacy) {
        switch (privacy) {
            case 'friend':
                return `
                    <svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" 
                        stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                        <circle cx="9" cy="7" r="4"/>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                    </svg>`;
            case 'private':
                return `
                    <svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" 
                        stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                        <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>`;
            case 'public':
            default:
                return `
                    <svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" 
                        stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="2" y1="12" x2="22" y2="12"/>
                        <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                    </svg>`;
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
        privacyOptions.forEach(option => {
            option.classList.toggle('selected', option.dataset.value === currentPrivacy);
        });
    }

    function toggleDropdown() {
        const isOpen = privacySelector.classList.contains('open');
        if (isOpen) {
            privacySelector.classList.remove('open');
            privacyDropdownMenu.classList.remove('open');
        } else {
            privacySelector.classList.add('open');
            privacyDropdownMenu.classList.add('open');
        }
    }

    if (!disableSelector) {
        privacySelector.addEventListener('click', toggleDropdown);
        privacyOptions.forEach(option => {
            option.addEventListener('click', (e) => {
                e.stopPropagation();
                currentPrivacy = option.dataset.value;
                updatePrivacyDisplay();
                toggleDropdown();
            });
        });
    }  else {
        privacySelector.classList.add('disabled-privacy-selector');
        privacySelector.style.pointerEvents = 'none';
    }
        updatePrivacyDisplay();
        return {
            getPrivacy: () => currentPrivacy,
            setPrivacy: (privacy) => {
                currentPrivacy = privacy;
                updatePrivacyDisplay();
            }
        };
}

// Setup save/post button
function setupSaveButton(quill, buttonId) {
    const button = document.getElementById(buttonId);
    quill.on('text-change', () => {
        const hasContent = quill.getLength() > 1 || quill.getContents().ops.some(op => op.insert['deletable-image']);
        button.disabled = !hasContent;
    });
}

// Submit post
function submitPost(quill, getPrivacy, action, postId = null, groupId = null) {
    let contentHTML = quill.root.innerHTML;
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = contentHTML;

    // Remove image-grid-wrapper before submission
    const gridWrappers = tempDiv.querySelectorAll('.image-grid-wrapper');
    gridWrappers.forEach(wrapper => {
        const children = [...wrapper.childNodes];
        children.forEach(child => wrapper.parentNode.insertBefore(child, wrapper));
        wrapper.remove();
    });
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

    contentHTML = tempDiv.innerHTML;

    //Gather data
    const formData = new FormData();
    formData.append('htmlContent', contentHTML);
    formData.append('post_privacy', getPrivacy());
    if (groupId !== null && groupId !== -1) {
        formData.append('group_id', groupId);
    }

    const images = quill.root.querySelectorAll('img');
    const imagePromises = [];
    let imageCount = 0;

    for (let i = 0; i < images.length; i++) {
        const img = images[i];
        if (img.src.startsWith('data:image')) {
            imagePromises.push(
                fetch(img.src)
                    .then(res => res.blob())
                    .then(blob => {
                        formData.append(`image${imageCount}`, blob, `image${imageCount}.png`);
                        imageCount++;
                    })
            );
        } else {
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

    let url = '/zust/post?action=' + action;
    if (action === 'edit' && postId) {
        url += '&postID=' + postId;
    }

    Promise.all(imagePromises).then(() => {
        fetch(url, {
            method: 'POST',
            body: formData
        }).then(response => {
            if (response.ok) {
                if (action === "create") {
                    console.log("Create post success");
                    sessionStorage.setItem('shouldReload', 'true');
                    window.history.back();
                } else {
                    window.location.reload();
                }
            } else {
                throw new Error('Failed to ' + action + ' post');
            }
        }).catch(error => {
            console.error('Error:', error);
            alert('Failed to ' + action + ' post. Please try again.');
        });
    });
}

// Generate composer HTML for the modal
function generateComposerHTML(avatarSrc, buttonText, groupId) {
    return `
        <div class="composer-container">
            <div class="composer-body">
                <img class="composer-avatar" src="${avatarSrc}" alt="Your Avatar">
                <div class="editor-wrapper">
                    <div class="composer-privacy">
                        <div class="privacy-selector-wrapper" id="privacy-selector">
                            <span id="privacy-icon-display"></span>
                            <span class="privacy-display-text" id="privacy-display-text">Public</span>
                            <div class="privacy-dropdown-menu" id="privacy-dropdown-menu">
                                <button class="privacy-dropdown-option" data-value="public">
                                    <svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" 
                                    stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="12" cy="12" r="10"></circle>
                                        <line x1="2" y1="12" x2="22" y2="12"></line>
                                        <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 
                                        15.3 0 0 1 4-10z"></path>
                                    </svg>
                                    Public
                                </button>
                                <button class="privacy-dropdown-option" data-value="friend">
                                    <svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" 
                                    stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                        <circle cx="9" cy="7" r="4"></circle>
                                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                                    </svg>
                                    Friends
                                </button>
                                <button class="privacy-dropdown-option" data-value="private">
                                    <svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" 
                                    stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                                        <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                                    </svg>
                                    Private
                                </button>
                            </div>
                        </div>
                    </div>
                    <div id="editor"></div>
                </div>
            </div>
            <div class="composer-actions">
                <div id="toolbar">
                    <button class="ql-bold"></button>
                    <button class="ql-italic"></button>
                    <button class="ql-underline"></button>
                    <button class="ql-list" value="ordered"></button>
                    <button class="ql-list" value="bullet"></button>
                    <button class="ql-link"></button>
                    <button class="ql-image"></button>
                </div>
                <input type="hidden" id="groupIdHidden" name="group_id" value="${groupId}">
                <button class="post-button" id="submit" disabled>${buttonText}</button>
            </div>
        </div>
    `;
}