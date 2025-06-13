<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Group Request</title>

    <!-- Internal CSS -->
    <style>
        /* General Body Styles */
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: #f0f2f5; /* Common social media background color */
            margin: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }

        /* The main container for the form, styled like a card */
        .request-form-container {
            background-color: #ffffff;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 600px;
            padding: 24px;
            box-sizing: border-box;
        }

        /* Form Header */
        .form-header h1 {
            font-size: 24px;
            color: #1c1e21; /* Dark grey text color */
            margin: 0 0 20px 0;
            text-align: center;
            border-bottom: 1px solid #dddfe2;
            padding-bottom: 16px;
        }

        /* Styles for each form group (label + input) */
        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            font-weight: 600;
            color: #606770;
            margin-bottom: 8px;
        }

        /* Style for text inputs and textarea */
        .form-control {
            width: 100%;
            padding: 12px;
            border: 1px solid #dddfe2;
            border-radius: 6px;
            font-size: 16px;
            box-sizing: border-box; /* Important for 100% width + padding */
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        .form-control:focus {
            outline: none;
            border-color: #1877f2; /* Facebook blue for focus */
            box-shadow: 0 0 0 2px rgba(24, 119, 242, 0.2);
        }

        /* Specific style for the textarea */
        textarea.form-control {
            resize: vertical; /* Allow vertical resizing */
            min-height: 150px;
        }

        /* Style for the submit button */
        .submit-btn {
            width: 100%;
            padding: 12px 20px;
            background-color: #1877f2; /* A vibrant blue, common in social media */
            color: #ffffff;
            border: none;
            border-radius: 6px;
            font-size: 18px;
            font-weight: bold;
            cursor: pointer;
            transition: background-color 0.2s;
        }

        .submit-btn:hover {
            background-color: #166fe5; /* A slightly darker blue on hover */
        }

        /* A simple field to show who the request is sent to */
        .static-info {
            background-color: #f5f6f7;
            padding: 12px;
            border-radius: 6px;
            color: #606770;
            font-size: 16px;
        }

    </style>
</head>
<body>

<div class="request-form-container">
    <div class="form-header">
        <h1>New Group Request</h1>
    </div>

    <!-- The form submits to /createGroupRequest servlet -->
    <form action="sendCreateGroupRequest" method="POST">


        <input type="hidden" value="1" name="accountId">
        <div class="form-group">
            <label>To</label>
            <div class="static-info">System Administrators</div>
        </div>

        <div class="form-group">
            <label for="requestMessage">Reason for Request & Purpose</label>
            <textarea id="requestMessage" name="content" class="form-control"
                      placeholder="Please describe why this group is needed, who it is for, and what topics will be discussed."
                      required></textarea>
        </div>

        <div class="form-group">
            <button type="submit" class="submit-btn">Send Request</button>
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

</body>
</html>