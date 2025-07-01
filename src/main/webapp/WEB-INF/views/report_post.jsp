<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList, java.time.LocalDate" %>
<%@ page import="dto.ReportPostDTO" %>
<%@ page import="model.Account" %>
<%@ page import="dto.RespPostDTO" %>


<%
    List<ReportPostDTO> reportList = (List<ReportPostDTO>) request.getAttribute("reportPostList");
    String currentPage = "report";
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Report Dashboard - Zust</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<%--    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"--%>
<%--          rel="stylesheet"--%>
<%--          integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"--%>
<%--          crossorigin="anonymous">--%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        :root {
            --orange: #FF852F; --black: #1a1a1a; --white: #FFFFFF; --light-gray: #f0f2f5;
            --green: #28a745; --red: #dc3545; --gray: #6c757d; --blue: #007bff;
        }
        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        body { background-color: var(--light-gray); display: flex; }
        .sidebar { width: 260px; background-color: var(--black); color: var(--white); height: 100vh; padding: 25px; display: flex; flex-direction: column; position: fixed; left: 0; top: 0; }
        .sidebar .logo { font-size: 2.5rem; font-weight: bold; text-align: center; margin-bottom: 40px; color: var(--orange); letter-spacing: 2px; }
        .sidebar .nav-menu { list-style-type: none; flex-grow: 1; }
        .sidebar .nav-menu li a { display: flex; align-items: center; color: var(--white); text-decoration: none; padding: 15px 20px; margin-bottom: 10px; border-radius: 8px; transition: background-color 0.3s, color 0.3s; }
        .sidebar .nav-menu li a .icon { margin-right: 15px; font-size: 1.2rem; width: 20px; text-align: center; }
        .sidebar .nav-menu li a:hover { background-color: var(--orange); }
        .sidebar .nav-menu li.active a { background-color: var(--orange); font-weight: 600; }
        .main-content { margin-left: 260px; padding: 40px; width: calc(100% - 260px); }

        .page-header h1 { color: var(--black); font-size: 2rem; margin-bottom: 15px; }
        .page-tabs { display: flex; border-bottom: 2px solid #e0e0e0; margin-bottom: 30px; }
        .tab-item { padding: 10px 20px; cursor: pointer; font-weight: 600; color: #777; border-bottom: 3px solid transparent; transform: translateY(2px); text-decoration: none; }
        .tab-item.active { color: var(--orange); border-color: var(--orange); }
        .tab-item:not(.active):hover { color: var(--black); }

        .report-list { display: flex; flex-direction: column; gap: 25px; }
        .report-card { display: grid; grid-template-columns: 1fr 1.5fr 1.5fr; gap: 25px; background: var(--white); padding: 20px; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }

        .report-section h3 { font-size: 1rem; color: #333; margin-bottom: 15px; border-bottom: 1px solid #eee; padding-bottom: 10px; }

        .user-block { display: flex; align-items: center; gap: 12px; }
        .user-block .avatar { width: 45px; height: 45px; border-radius: 50%; object-fit: cover; cursor: pointer; }
        .user-block .user-name { font-weight: 600; }
        .user-block .user-username { font-size: 0.9em; color: #777; }

        .report-details .report-date { font-size: 0.8em; color: #999; margin-bottom: 10px; }
        .report-details .report-message { font-size: 0.95em; color: #555; line-height: 1.5; }
        .report-details .report-actions { margin-top: 20px; display: flex; gap: 10px; }
        .report-actions .btn { border: none; padding: 8px 15px; border-radius: 5px; cursor: pointer; font-weight: 600; }
        .btn-dismiss { background-color: var(--gray); color: white; }
        .btn-takedown { background-color: var(--red); color: white; }

        .reported-post .post-image { width: 100%; height: 150px; object-fit: cover; border-radius: 8px; margin-bottom: 15px; cursor: pointer; }
        .reported-post .post-text { font-size: 0.95em; color: #555; line-height: 1.5; background: #f9f9f9; padding: 10px; border-radius: 5px; }

        .read-more-btn { background: none; border: none; padding: 0; font: inherit; color: var(--orange); text-decoration: underline; cursor: pointer; margin-left: 5px; font-size: 0.9em; }
        .hidden { display: none; }

        .no-data-message { background-color: var(--white); border-radius: 10px; padding: 60px 40px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .no-data-message .icon { font-size: 3.5rem; color: var(--orange); margin-bottom: 20px; }
        .no-data-message h2 { font-size: 1.5rem; color: var(--black); margin-bottom: 10px; }
        .no-data-message p { color: #777; font-size: 1rem; }

        /* Modal Styles */
        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.7); align-items: center; justify-content: center; }
        .modal-content-wrapper { background: white; padding: 30px; border-radius: 8px; width: 90%; max-width: 500px; box-shadow: 0 5px 20px rgba(0,0,0,0.2); animation: zoomIn 0.3s ease; }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .modal-header h2 { margin: 0; }
        .modal-close { font-size: 2rem; font-weight: bold; cursor: pointer; color: #aaa; }
        .modal-close:hover { color: #333; }
        .modal-body textarea { width: 100%; height: 120px; padding: 10px; border-radius: 5px; border: 1px solid #ddd; font-size: 1rem; resize: vertical; }
        .modal-footer { text-align: right; margin-top: 20px; }
        .modal-footer .btn-submit { background-color: var(--red); color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: 600; }
        .image-modal .modal-content-wrapper { background: none; box-shadow: none; width: auto; max-width: 80%; padding: 0; } /* Specific for image modal */

        .image-modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.85); align-items: center; justify-content: center; }
        .modal-content { max-width: 80%; max-height: 80%; border-radius: 5px; animation: zoomIn 0.3s ease; }
        /*.modal-close { position: absolute; top: 20px; right: 40px; color: #f1f1f1; font-size: 40px; font-weight: bold; cursor: pointer; }*/
        @keyframes zoomIn { from {transform: scale(0.5);} to {transform: scale(1);} }
    </style>
</head>
<body>

<!-- Sidebar -->
<aside class="sidebar">
    <div class="logo">Zust</div>
    <ul class="nav-menu">
        <li><a href="dashboard"><span class="icon"><i class="fas fa-chart-pie"></i></span><span>Statistic</span></a></li>
        <li><a href="accountDashboard"><span class="icon"><i class="fas fa-users"></i></span><span>User</span></a></li>
        <li><a href="notification"><span class="icon"><i class="fas fa-bell"></i></span><span>Notification</span></a></li>
        <li><a href="groupRequest"><span class="icon"><i class="fas fa-plus-square"></i></span><span>Group Request</span></a></li>
        <li><a href="groupDashboard"><span class="icon"><i class="fas fa-user-friends"></i></span><span>Group</span></a></li>
        <li class="active"><a href="reportPost"><span class="icon"><i class="fas fa-flag"></i></span><span>Report</span></a></li>
    </ul>
</aside>

<!-- Main Content -->
<main class="main-content">
    <div class="page-header">
        <h1>Report Dashboard</h1>
    </div>

    <div class="page-tabs">
        <a href="reportPost" class="tab-item active">Post</a>
        <a href="reportComment" class="tab-item">Comment</a>
        <a href="reportUser" class="tab-item">User</a>
    </div>

    <% if (reportList == null || reportList.isEmpty()) { %>
    <div class="no-data-message">
        <div class="icon"><i class="fas fa-search"></i></div>
        <h2>No Report Found</h2>
        <p>Currently, there are no pending report.</p>
    </div>
    <% } else { %>

    <div class="report-list">
        <% for (ReportPostDTO report : reportList) { %>
        <div class="report-card">
            <!-- Column 1: Reporter Info -->
            <div class="report-section reporter-info">
                <h3><i class="fas fa-user-shield"></i> Reported By</h3>
                <% Account reporter = report.getAccount();
                    if (reporter != null) { %>
                <div class="user-block">
                    <img src="<%= reporter.getAvatar() != null ? reporter.getAvatar() : "https://via.placeholder.com/45/EEEEEE/AAAAAA?text=?" %>" alt="Avatar" class="avatar clickable-image" data-caption="<%= reporter.getFullname() %>">
                    <div>
                        <div class="user-name"><%= reporter.getFullname() %></div>
                        <div class="user-username">@<%= reporter.getUsername() %></div>
                    </div>
                </div>
                <% } else { %>
                <p>Unknown Reporter</p>
                <% } %>
            </div>

            <!-- Column 2: Report Details -->
            <div class="report-section report-details">
                <h3><i class="fas fa-envelope-open-text"></i> Report Reason</h3>
                <div class="report-date">Date: <%= report.getReportCreateDate() %></div>
                <div class="report-message">
                    <%
                        String message = report.getReportContent();
                        if (message != null && message.length() > 100) {
                            String shortMsg = message.substring(0, 100) + "...";
                    %>
                    <span class="short-text"><%= shortMsg %></span>
                    <span class="full-text hidden"><%= message %></span>
                    <button class="read-more-btn">Read More</button>
                    <% } else { %>
                    <%= message != null ? message : "No message provided." %>
                    <% } %>
                </div>
                <div class="report-actions">
<%--                    <button class="btn btn-takedown">Take Down Post</button>--%>
<%--                    <button class="btn btn-dismiss">Dismiss Report</button>--%>
                    <button class="btn btn-takedown"
                            data-report-id="<%= report.getReportId() %>"
                            data-reporter-id="<%= reporter != null ? reporter.getId() : "0" %>"
                            data-reported-id="<%= report.getPost().getUsername() != null ? report.getPost().getUsername() : "0" %>"
                            data-reported-post-id="<%= report.getPost().getPostId()%>">
                        Suspend User
                    </button>
                    <form action="reportPost" method="POST" style="display: inline;">
                        <input type="hidden" name="action" value="dismiss">
                        <input type="hidden" name="reportId" value="<%= report.getReportId() %>">
                        <button type="submit" class="btn btn-dismiss">Dismiss Report</button>
                    </form>
                </div>
            </div>

            <!-- Column 3: Reported Post -->
            <div class="report-section reported-post feed">
<%--                <h3><i class="fas fa-file-alt"></i> Reported Content</h3>--%>
                <% RespPostDTO post = report.getPost();
                    if (post != null) {
                        out.println(post);
                        %>
<%--                        SimpleUser author = post.getAuthor();--%>
<%--                        if (author != null) { %>--%>
<%--                <div class="user-block" style="margin-bottom: 15px;">--%>
<%--                    <img src="<%= author.getAvatarUrl() != null ? author.getAvatarUrl() : "https://via.placeholder.com/45/EEEEEE/AAAAAA?text=?" %>" alt="Author Avatar" class="avatar clickable-image" data-caption="<%= author.getFullName() %>">--%>
<%--                    <div>--%>
<%--                        <div class="user-name"><%= author.getFullName() %></div>--%>
<%--                        <div class="user-username">@<%= author.getUsername() %></div>--%>
<%--                    </div>--%>
<%--                </div>--%>
<%--                <% } %>--%>
<%--                <% if (post.getPostImageUrl() != null) { %>--%>
<%--                <img src="<%= post.getPostImageUrl() %>" alt="Post Image" class="post-image clickable-image" data-caption="Reported Post Image">--%>
<%--                <% } %>--%>
<%--                <div class="post-text">--%>
<%--                    <%--%>
<%--                        String postText = post.getPostText();--%>
<%--                        if (postText != null && postText.length() > 120) {--%>
<%--                            String shortText = postText.substring(0, 120) + "...";--%>
<%--                    %>--%>
<%--                    <span class="short-text"><%= shortText %></span>--%>
<%--                    <span class="full-text hidden"><%= postText %></span>--%>
<%--                    <button class="read-more-btn">Read More</button>--%>
<%--                    <% } else { %>--%>
<%--                    <%= postText != null ? postText : "<i>Post has no text.</i>" %>--%>
<%--                    <% } %>--%>
<%--                </div>--%>

                <% } else { %>
                <p>Reported post data is unavailable.</p>
                <% } %>
            </div>
        </div>
        <% } %>
    </div>
    <% } %>
</main>

<!-- Image Modal -->
<div id="imageModal" class="modal image-modal">
    <span class="modal-close">×</span>
    <img class="modal-content" id="modalImage">
    <div id="modal-caption" style="color: #ccc; text-align: center; padding: 15px 0;"></div>
</div>

<!-- NEW: Suspend User Modal -->
<div id="suspendModal" class="modal">
    <div class="modal-content-wrapper">
        <div class="modal-header">
            <h2>Take-down Post</h2>
            <span class="modal-close">×</span>
        </div>
        <form id="suspendForm" action="reportPost" method="POST">
            <div class="modal-body">
                <p>Enter a message for the reported user. This will be sent as a notification.</p>
                <textarea name="suspensionMessage" required placeholder="E.g., Your post has been deleted for violating community guidelines..."></textarea>

                <!-- Hidden fields to be populated by JavaScript -->
                <input type="hidden" name="action" value="accept">
                <input type="hidden" id="hiddenReportId" name="reportId">
                <input type="hidden" id="hiddenReporterId" name="reporterId">
                <input type="hidden" id="hiddenReportedId" name="reportedUsename">
                <input type="hidden" id="hiddenReportedPostId" name="reportedPostId">
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn-submit">Confirm Suspension</button>
            </div>
        </form>
    </div>
</div>

<!-- JavaScript for Interactivity -->

<script>
    document.addEventListener('DOMContentLoaded', function() {

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


        const imageModal = document.getElementById("imageModal");
        const suspendModal = document.getElementById("suspendModal");
        const allModals = document.querySelectorAll('.modal');

        const suspendForm = document.getElementById('suspendForm');
        document.querySelectorAll('.btn-takedown').forEach(button => {
            button.addEventListener('click', function(event) {
                event.preventDefault(); // Stop default button action

                // Get data from the clicked button
                const reportId = this.dataset.reportId;
                const reporterId = this.dataset.reporterId;
                const reportedId = this.dataset.reportedId;
                const reportedPostId = this.dataset.reportedPostId;

                // Populate the hidden fields in the modal form
                suspendForm.querySelector('#hiddenReportId').value = reportId;
                suspendForm.querySelector('#hiddenReporterId').value = reporterId;
                suspendForm.querySelector('#hiddenReportedId').value = reportedId;
                suspendForm.querySelector('#hiddenReportedPostId').value = reportedPostId;

                // Show the modal
                suspendModal.style.display = 'flex';
            });
        });

        // --- Image Modal Logic ---
        const modalImg = document.getElementById("modalImage");
        const captionText = document.getElementById("modal-caption");
        document.querySelectorAll('.clickable-image').forEach(image => {
            image.addEventListener('click', function() {
                imageModal.style.display = "flex";
                modalImg.src = this.src;
                captionText.textContent = this.dataset.caption;
            });
        });

        // function closeModal() { modal.style.display = "none"; }
        // modal.querySelector('.modal-close').addEventListener('click', closeModal);
        // window.addEventListener('click', e => { if (e.target == modal) closeModal(); });

        // Generic function to close any modal
        function closeModal(modal) {
            if (modal) {
                modal.style.display = "none";
            }
        }

        // Setup close buttons for all modals
        document.querySelectorAll('.modal-close').forEach(btn => {
            btn.addEventListener('click', () => closeModal(btn.closest('.modal')));
        });

        // Close modal by clicking background
        window.addEventListener('click', e => {
            allModals.forEach(modal => {
                if (e.target == modal) {
                    closeModal(modal);
                }
            });
        });

        // --- "Read More" Logic ---
        document.querySelectorAll('.read-more-btn').forEach(button => {
            button.addEventListener('click', function(event) {
                event.stopPropagation();
                const parent = this.parentElement;
                const shortText = parent.querySelector('.short-text');
                const fullText = parent.querySelector('.full-text');

                shortText.classList.toggle('hidden');
                fullText.classList.toggle('hidden');
                this.textContent = fullText.classList.contains('hidden') ? 'Read More' : 'Read Less';
            });
        });
    });
</script>
</body>
</html>