<%--
  Created by IntelliJ IDEA.
  User: Asus
  Date: 6/7/2025
  Time: 9:54 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>Title</title>
        <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
        <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
        <style>
            #editor { height: 300px; }
            .error { color: red; }
            .success { color: green; }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="row">
                <!-- Editor section -->
                <div class="col-sm-6">
                    <h2>Create a New Post</h2>
                    <form id="postForm">
                        <div id="editor"></div>
                        <label for="post_privacy">Choose privacy level: </label>
                        <select name="post_privacy" id="post_privacy">
                            <option value="public">Public</option>
                            <option value="friend">Friend</option>
                            <option value="private">Private</option>
                        </select>
                        <button type="button" onclick="submitContent()">Create Post</button>
                    </form>
                    <div id="message"></div>
                </div>

                <!-- Posts section -->
                <div class="col-sm-6">
                    Posts
                </div>
            </div>
        </div>


        <script>
            // Initialize Quill editor
            const quill = new Quill('#editor', {
                theme: 'snow',
                modules: {
                    toolbar: [
                        ['bold', 'italic', 'underline'],
                        ['image', 'link'],
                        [{ 'list': 'ordered' }, { 'list': 'bullet' }]
                    ]
                }
            });

            async function submitContent() {
                // Clear previous messages
                const messageDiv = document.getElementById('message');
                messageDiv.innerHTML = '';

                // Validate input
                const htmlContent = quill.root.innerHTML.trim();
                if (htmlContent === '<p><br></p>') {
                    messageDiv.innerHTML = '<p class="error">Post content cannot be empty.</p>';
                    return;
                }

                // Extract hashtags from plain text content
                const textContent = quill.root.innerText;
                const hashtagRegex = /#\w+/g;
                const hashtags = Array.from(textContent.match(hashtagRegex) || []);

                // Create FormData
                const formData = new FormData();
                formData.append('htmlContent', htmlContent);
                formData.append('hashtags', JSON.stringify(hashtags));
                formData.append('post_privacy', document.getElementById('post_privacy').value);

                // Find and append images
                const images = quill.root.querySelectorAll('img[src^="data:image"]');
                for (let i = 0; i < images.length; i++) {
                    const imgSrc = images[i].src;
                    const blob = await fetch(imgSrc).then(res => res.blob());
                    formData.append(`image${i}`, blob, `image${i}.png`);
                }

                // Send to Servlet
                try {
                    const response = await fetch('/zust/post?action=create', {
                        method: 'POST',
                        body: formData
                    });
                    if (response.ok) {
                        messageDiv.innerHTML = '<p class="success">Post created successfully!</p>';
                        quill.setContents([]); // Clear editor
                        document.getElementById('post_privacy').value = 'public'; // Reset privacy
                    } else {
                        messageDiv.innerHTML = '<p class="error">Failed to create post.</p>';
                    }
                } catch (error) {
                    console.error('Error:', error);
                    messageDiv.innerHTML = '<p class="error">Error: ' + error.message + '</p>';
                }
            }
        </script>
  </body>
</html>
