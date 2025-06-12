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
    </style>
</head>
<body>

<%-- Dữ liệu mẫu --%>
<%
    if (request.getAttribute("listAccount") == null) {
        List<Account> mockList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Account acc = new Account();
            acc.setId(i);
            acc.setAvatar("avatar" + i);
            acc.setUsername("usename" + i);
            acc.setFullname("name" + i);
            mockList.add(acc);
        }
        request.setAttribute("listAccount", mockList);
    }
%>

<div class="form-container">
    <div class="form-header">
        <h1>Create a New Group</h1>
    </div>

    <form action="group?action=create" method="POST" enctype="multipart/form-data">

        <!-- 1. Cover Image -->
        <div class="form-group">
            <label for="coverImage" class="label-title">Cover Image</label>
            <label for="coverImage" class="cover-image-upload">
                <input type="file" id="coverImage" name="coverImage" accept="image/*">
                <div class="upload-icon"></div>
                <div class="upload-text">Click to upload an image</div>
            </label>
            <div id="image-preview"><img id="preview-img" src="#" alt="Image Preview"/></div>
        </div>

        <!-- 2. Group Name -->
        <div class="form-group">
            <label for="groupName" class="label-title">Group Name</label>
            <input type="text" id="groupName" name="groupName" class="form-control" placeholder="e.g., 'Photography Enthusiasts'" required>
        </div>

        <!-- 3. Group Description -->
        <div class="form-group">
            <label for="groupDesc" class="label-title">Group Description</label>
            <textarea id="groupDesc" name="groupDescription" class="form-control" placeholder="What is this group about?"></textarea>
        </div>

        <!-- 4. Select Managers -->
        <div class="form-group">
            <div class="label-title">Select Manager(s)</div>

            <!-- Search/Filter Input Field -->
            <div class="manager-search-wrapper">
                <input type="text" id="managerSearchInput" class="form-control" placeholder="Search by name or username...">
            </div>

            <div class="manager-list" id="managerListContainer">
                <%
                    List<Account> accounts = (List<Account>) request.getAttribute("listAccount");
                    if (accounts == null || accounts.isEmpty()) {
                %>
                <p style="text-align:center; color:#888;">No users available to select as manager.</p>
                <%
                } else {
                    for (Account acc : accounts) {
                %>
                <label class="manager-item-label">
                    <div class="manager-item">
                        <input type="checkbox" name="managerIds" value="<%= acc.getId() %>">
                        <img src="<%= acc.getAvatar() %>" alt="Avatar" class="manager-avatar">
                        <div class="manager-info">
                            <span class="manager-fullname"><%= acc.getFullname() %></span>
                            <span class="manager-username">@<%= acc.getUsername() %></span>
                        </div>
                    </div>
                </label>
                <%
                        }
                    }
                %>
                <!-- Message for no search results, hidden by default -->
                <div id="noResultsMessage" class="no-results-message" style="display: none;">
                    No users found matching your search.
                </div>
            </div>
        </div>

        <div class="form-group">
            <button type="submit" class="submit-btn">Create Group</button>
        </div>

    </form>
    <%
        if(request.getAttribute("msg") != null){
    %>
    <p>Message: <%= request.getAttribute("msg")%></p>
    <%
        }
    %>
</div>

<!-- JavaScript for Image Preview and Manager Filter -->
<script>
    // --- Image Preview Logic ---
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

    // --- Manager Filter Logic ---
    document.getElementById('managerSearchInput').addEventListener('input', function() {
        // Get the search term and convert to lower case for case-insensitive matching
        const searchTerm = this.value.toLowerCase().trim();

        // Get all the manager items and the 'no results' message element
        const managerList = document.getElementById('managerListContainer');
        const managerItems = managerList.querySelectorAll('.manager-item-label');
        const noResultsMessage = document.getElementById('noResultsMessage');

        let visibleCount = 0;

        // Loop through all manager items to show/hide them
        managerItems.forEach(function(item) {
            // Find the name and username text within the current item
            const fullName = item.querySelector('.manager-fullname').textContent.toLowerCase();
            const username = item.querySelector('.manager-username').textContent.toLowerCase();

            // Check if the search term is part of the full name or username
            if (fullName.includes(searchTerm) || username.includes(searchTerm)) {
                item.style.display = 'block'; // Show the item
                visibleCount++;
            } else {
                item.style.display = 'none'; // Hide the item
            }
        });

        // Show or hide the 'no results' message based on whether any items are visible
        if (visibleCount === 0) {
            noResultsMessage.style.display = 'block';
        } else {
            noResultsMessage.style.display = 'none';
        }
    });
</script>
</body>
</html>