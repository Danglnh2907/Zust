<%@ page import="dto.RespPostDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="dto.RespPostDTO" %>
<%@ page import="model.Account" %>
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
        <title>Post upload</title>
        <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post_upload.css">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
              crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
                crossorigin="anonymous"></script>
    </head>
    <body>
        <%
            //Get user from session
            Account account = (Account) request.getAttribute("account");
            if (account == null) {
                request.getRequestDispatcher("/auth").forward(request, response);
                return;
            }

            String avatar = account.getAvatar();
        %>
        <div class="composer-container">
            <div class="composer-body">
                <img class="composer-avatar" src="${pageContext.request.contextPath}/static/images/<%= avatar %>"
                     alt="Your Avatar">
                <div class="editor-wrapper">
                    <div class="composer-privacy">
                        <div class="privacy-selector-wrapper" id="privacy-selector">
                        <span id="privacy-icon-display">
                            <!-- Populated by JS -->
                        </span>
                            <span class="privacy-display-text" id="privacy-display-text">Public</span>
                            <div class="privacy-dropdown-menu" id="privacy-dropdown-menu">
                                <button class="privacy-dropdown-option selected" data-value="public">
                                    <svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                                         stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="12" cy="12" r="10"></circle>
                                        <line x1="2" y1="12" x2="22" y2="12"></line>
                                        <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
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
                    <button
                            class="ql-underline"></button>
                    <button class="ql-list" value="ordered"></button>
                    <button class="ql-list" value="bullet"></button>
                    <button class="ql-link"></button>
                    <button class="ql-image"></button>
                </div>
                <button class="post-button" id="post-button" disabled>Post</button>
            </div>
        </div>

        <div class="live-feed-container" id="posts-feed"></div>

        <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/post_upload.js"></script>
    </body>
</html>