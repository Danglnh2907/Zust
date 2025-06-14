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