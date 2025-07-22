<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="model.Account" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create New Group</title>

    <!-- Internal CSS -->
    <style>
        :root {
            --primary-color: #FF852F;
            --primary-color-dark: #E67222;
            --text-color-dark: #1c1c1c;
            --text-color-light: #555;
            --border-color: #e0e0e0;
            --bg-color-light: #f9f9f9;
            --bg-color-white: #ffffff;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg-color-light); margin: 0; padding: 40px 20px;
            display: flex; justify-content: center; align-items: flex-start;
        }

        .form-container {
            background-color: var(--bg-color-white); border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1); width: 100%;
            max-width: 700px; padding: 32px; box-sizing: border-box;
        }

        .form-header h1 {
            font-size: 28px; color: var(--text-color-dark); margin: 0 0 30px 0;
            text-align: center; font-weight: 700;
        }

        .form-group { margin-bottom: 25px; }

        .form-group label, .form-group .label-title {
            display: block; font-weight: 600; color: var(--text-color-dark);
            margin-bottom: 8px; font-size: 16px;
        }

        .form-control {
            width: 100%; padding: 12px 15px; border: 1px solid var(--border-color);
            border-radius: 8px; font-size: 16px; box-sizing: border-box;
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        .form-control:focus {
            outline: none; border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(255, 133, 47, 0.2);
        }

        textarea.form-control { resize: vertical; min-height: 120px; }

        /* Cover Image Upload */
        .cover-image-upload {
            border: 2px dashed var(--border-color); border-radius: 8px; padding: 20px;
            text-align: center; cursor: pointer; transition: background-color 0.2s, border-color 0.2s;
        }
        .cover-image-upload:hover { border-color: var(--primary-color); background-color: #fff8f2; }
        .cover-image-upload input[type="file"] { display: none; }
        #image-preview { margin-top: 15px; max-height: 250px; width: 100%; display: none; }
        #preview-img { width: 100%; height: 100%; max-height: 250px; object-fit: cover; border-radius: 8px; }
        .upload-icon { font-size: 24px; color: var(--primary-color); }
        .upload-text { color: var(--text-color-light); font-weight: 500; }

        /* --- Manager Selection Styling --- */
        /* Search input wrapper */
        .manager-search-wrapper {
            margin-bottom: 15px;
        }

        .manager-list {
            max-height: 300px; overflow-y: auto; border: 1px solid var(--border-color);
            border-radius: 8px; padding: 8px;
        }

        .manager-item-label {
            display: block; /* Important for filtering show/hide */
        }

        .manager-item {
            display: flex; align-items: center; padding: 10px; border-radius: 6px;
            cursor: pointer; transition: background-color 0.2s; border: 2px solid transparent;
        }
        .manager-item:hover { background-color: #f5f5f5; }
        .manager-item-label:has(input:checked) {
            background-color: #fff8f2; border-color: var(--primary-color);
        }

        .manager-item input[type="checkbox"] {
            margin-right: 15px; width: 18px; height: 18px; accent-color: var(--primary-color);
        }
        .manager-avatar {
            width: 45px; height: 45px; border-radius: 50%; object-fit: cover; margin-right: 15px;
        }
        .manager-info { display: flex; flex-direction: column; }
        .manager-fullname { font-weight: 600; color: var(--text-color-dark); }
        .manager-username { font-size: 14px; color: var(--text-color-light); }

        /* Message for when no search results are found */
        .no-results-message {
            text-align: center;
            padding: 20px;
            color: var(--text-color-light);
            font-style: italic;
        }

        /* Submit Button */
        .submit-btn {
            width: 100%; padding: 15px;
            background: linear-gradient(90deg, var(--primary-color), var(--primary-color-dark));
            color: #ffffff; border: none; border-radius: 8px; font-size: 18px;
            font-weight: bold; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s;
        }
        .submit-btn:hover {
            transform: translateY(-2px); box-shadow: 0 6px 15px rgba(255, 133, 47, 0.4);
        }

        .action-buttons-group {
            display: flex;
            gap: 15px; /* Khoảng cách giữa 2 nút */
            margin-top: 40px; /* Thêm khoảng cách với trường bên trên */
        }
        /* Style chung cho cả 2 nút */
        .btn {
            flex: 1; /* Hai nút sẽ chia đều không gian */
            padding: 15px;
            border-radius: 8px;
            font-size: 18px;
            font-weight: bold;
            cursor: pointer;
            text-align: center;
            text-decoration: none; /* Loại bỏ gạch chân cho thẻ <a> */
            transition: all 0.2s ease;
        }
        .btn:hover {
            transform: translateY(-2px);
        }

        /* Nút Create (chính) */
        .submit-btn {
            background: linear-gradient(90deg, var(--primary-color), var(--primary-color-dark));
            color: #ffffff;
            border: none;
        }
        .submit-btn:hover {
            box-shadow: 0 6px 15px rgba(255, 133, 47, 0.4);
        }

        /* Nút Back (phụ) */
        .back-btn {
            background-color: var(--bg-color-white);
            color: var(--primary-color);
            border: 2px solid var(--primary-color);
        }
        .back-btn:hover {
            background-color: #fff8f2; /* Màu nền cam rất nhạt khi hover */
            box-shadow: 0 6px 15px rgba(0, 0, 0, 0.05);
        }

        .char-counter {
            display: block; /* Make it appear on its own line */
            text-align: right; /* Align to the right */
            font-size: 0.85em;
            color: #6c757d; /* A muted gray color */
            margin-top: 5px;
        }

        .char-counter.is-maxed {
            color: red;
            font-weight: bold;
        }
    </style>
</head>
<body>

<div class="form-container">
    <div class="form-header">
        <h1>Create a New Group</h1>
    </div>

    <% int accountId = (int)request.getAttribute("accountId"); %>
    <form action="${pageContext.request.contextPath}/group?action=create" method="POST" enctype="multipart/form-data">
        <input type="hidden" name="creatorId" value="<%= accountId%>">
        <div class="form-group">
            <label for="coverImage" class="label-title">Cover Image</label>
            <label for="coverImage" class="cover-image-upload">
                <input type="file" id="coverImage" name="coverImage" accept="image/*">
                <div class="upload-icon"></div>
                <div class="upload-text">Click to upload an image</div>
            </label>
            <div id="image-preview"><img id="preview-img" src="#" alt="Image Preview"/></div>
        </div>

        <div class="form-group">
            <label for="groupName" class="label-title">Group Name</label>
            <input type="text" id="groupName" name="groupName" class="form-control" placeholder="e.g., 'Computer engineer group'" required maxlength="100">
        </div>

        <div class="form-group">
            <label for="groupDesc" class="label-title">Group Description</label>
            <textarea id="groupDesc" name="groupDescription" class="form-control" placeholder="What is this group about?" minlength="10"></textarea>
        </div>
        <div class="form-group action-buttons-group">
            <a href="${pageContext.request.contextPath}/" class="btn back-btn">Back to Home</a>
            <button type="submit" class="btn submit-btn">Create Group</button>
        </div>

    </form>

</div>

<!-- JavaScript for Image Preview and Manager Filter -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        const groupNameInput = document.getElementById('groupName');

        if (groupNameInput) {
            const maxLength = groupNameInput.getAttribute('maxlength');
            const charCounter = document.createElement('small');
            charCounter.className = 'char-counter';
            groupNameInput.insertAdjacentElement('afterend', charCounter);
            function updateCounter() {
                const currentLength = groupNameInput.value.length;
                charCounter.textContent = currentLength + '/' + maxLength;
                if (currentLength >= maxLength) {
                    charCounter.classList.add('is-maxed');
                } else {
                    charCounter.classList.remove('is-maxed');
                }
            }
            updateCounter();
            groupNameInput.addEventListener('input', updateCounter);
        } else {
            console.error("Error: Input element with id 'groupName' was not found.");
        }
    });
    document.getElementById('coverImage').addEventListener('change', function(event) {
        const previewContainer = document.getElementById('image-preview');
        const previewImage = document.getElementById('preview-img');
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                previewImage.src = e.target.result;
                previewContainer.style.display = 'block';
            }
            reader.readAsDataURL(file);
        } else {
            previewContainer.style.display = 'none';
        }
    });

    <%
            if(request.getAttribute("msg") != null){
        %>
    alert("<%= request.getAttribute("msg")%>");
    <%
        }
    %>
</script>
</body>
</html>