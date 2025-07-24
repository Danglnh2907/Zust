<%@ page import="model.Account" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<!-- POST UPLOAD PAGE -->

<html>
    <head>
        <title>Post upload</title>
        <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
              crossorigin="anonymous">

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/composer.css">
    </head>
    <body id="body">
        <%
            //Get user from session
            Account account = (Account) request.getAttribute("account");
            if (account == null) {
                request.getRequestDispatcher("/auth").forward(request, response);
                return;
            }

            String avatar = account.getAvatar();
            Integer groupId = (Integer) request.getAttribute("groupId");
            if (groupId == null) {
                groupId = -1;
            }
        %>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
                crossorigin="anonymous"></script>
        <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/composer.js"></script>
        <script>
            document.addEventListener("DOMContentLoaded", () => {
                const groupIdFromGet = <%= groupId %>;
                document.getElementById("body").innerHTML = generateComposerHTML("/zust/static/images/<%= avatar %>",
                    "Create post", groupIdFromGet);
                // Initialize components
                const quill = initializeQuill("", []); //Empty

                let disablePrivacySelector = false;
                if (groupIdFromGet !== -1) {
                    disablePrivacySelector = true;
                }
                const privacySelector = initializePrivacySelector('privacy-selector', "public", disablePrivacySelector); //Set default
                setupSaveButton(quill, 'submit');
                // Set up save button
                const saveButton = document.getElementById('submit');
                const currentGroupId = document.getElementById('groupIdHidden').value;
                saveButton.addEventListener('click', () => {
                    submitPost(quill, () => privacySelector.getPrivacy(), 'create', null, currentGroupId);
                });
            })
        </script>
    </body>
</html>