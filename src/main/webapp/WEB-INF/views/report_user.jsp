<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList" %>

<%--
  ================================================================================
  1. DEFINE DATA MODELS (POJOs)
  ================================================================================
--%>
<%!
    // A SimpleUser class enhanced with a status field
    public class SimpleUser {
        private String avatarUrl, fullName, username, status;
        public SimpleUser(String avatar, String fname, String uname, String status) {
            this.avatarUrl=avatar; this.fullName=fname; this.username=uname; this.status=status;
        }
        public String getAvatarUrl() { return avatarUrl; }
        public String getFullName() { return fullName; }
        public String getUsername() { return username; }
        public String getStatus() { return status; }
    }

    // The main UserReport object
    public class UserReport {
        private int reportId;
        private SimpleUser reporter;
        private SimpleUser reportedUser;
        private String reportMessage;
        private String reportDate;
        public UserReport(int id, SimpleUser r, SimpleUser ru, String msg, String date) {
            this.reportId=id; this.reporter=r; this.reportedUser=ru; this.reportMessage=msg; this.reportDate=date;
        }
        public int getReportId() { return reportId; }
        public SimpleUser getReporter() { return reporter; }
        public SimpleUser getReportedUser() { return reportedUser; }
        public String getReportMessage() { return reportMessage; }
        public String getReportDate() { return reportDate; }
    }
%>

<%--
  ================================================================================
  2. SIMULATE DATA RETRIEVAL
  ================================================================================
--%>
<%
    List<UserReport> reportList = new ArrayList<>();

    // --- Create some sample users ---
    SimpleUser userA = new SimpleUser("https://i.pravatar.cc/150?img=33", "Mia Foster", "mia_f", "active");
    SimpleUser userB = new SimpleUser("https://i.pravatar.cc/150?img=45", "Noah Patel", "noah_p", "active");
    SimpleUser userC = new SimpleUser("https://i.pravatar.cc/150?img=50", "Liam Chen", "liam_c", "suspended");

    // --- Create sample reports ---
    reportList.add(new UserReport(701, userA, userB, "This user is sending me threatening private messages. Their behavior is unacceptable and they are creating a hostile environment. I have blocked them but I believe their account requires administrative review for repeated policy violations.", "2023-11-03"));
    reportList.add(new UserReport(702, userB, userC, "This user's profile picture and bio are inappropriate and violate the community guidelines.", "2023-11-02"));

    String currentPage = "report";
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Reports - Zust</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        /* All CSS is identical to the post/comment report pages for consistency */
        :root {
            --orange: #FF852F; --black: #1a1a1a; --white: #FFFFFF; --light-gray: #f0f2f5;
            --green: #28a745; --red: #dc3545; --gray: #6c757d; --yellow: #ffc107;
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
        .report-card { display: grid; grid-template-columns: 1fr 1.5fr 1fr; gap: 25px; background: var(--white); padding: 20px; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .report-section { border-right: 1px solid #f0f0f0; padding-right: 25px; }
        .report-card .report-section:last-child { border-right: none; padding-right: 0; }
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
        .btn-suspend { background-color: var(--yellow); color: white; }
        .btn-view {background-color: var(--orange); color: white; }
        .report-section .report-actions { margin-top: 10px; display: flex; gap: 10px; }

        .no-data-message { background-color: var(--white); border-radius: 10px; padding: 60px 40px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .no-data-message .icon { font-size: 3.5rem; color: var(--orange); margin-bottom: 20px; }
        .no-data-message h2 { font-size: 1.5rem; color: var(--black); margin-bottom: 10px; }
        .no-data-message p { color: #777; font-size: 1rem; }

        .status-badge { padding: 5px 12px; border-radius: 20px; font-size: 0.8em; font-weight: bold; text-transform: capitalize; display: inline-block; margin-top: 10px; }
        .status-active { background-color: #e4f8eb; color: var(--green); }
        .status-suspended { background-color: #fff8e1; color: #f59e0b; }

        .read-more-btn { background: none; border: none; padding: 0; font: inherit; color: var(--orange); text-decoration: underline; cursor: pointer; margin-left: 5px; font-size: 0.9em; }
        .hidden { display: none; }
        .image-modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.85); align-items: center; justify-content: center; }
        .modal-content { max-width: 80%; max-height: 80%; border-radius: 5px; animation: zoomIn 0.3s ease; }
        .modal-close { position: absolute; top: 20px; right: 40px; color: #f1f1f1; font-size: 40px; font-weight: bold; cursor: pointer; }
        @keyframes zoomIn { from {transform: scale(0.5);} to {transform: scale(1);} }
        .clickable-image:hover { transform: scale(1.1); }
    </style>
</head>
<body>

<!-- Sidebar -->
<aside class="sidebar">
    <div class="logo">Zust</div>
    <ul class="nav-menu">
        <li><a href="dashboard"><span class="icon"><i class="fas fa-chart-pie"></i></span><span>Statistic</span></a></li>
        <li><a href="accountDashboard"><span class="icon"><i class="fas fa-users"></i></span><span>User</span></a></li>
        <li><a href="groupRequest"><span class="icon"><i class="fas fa-plus-square"></i></span><span>Group Request</span></a></li>
        <li><a href="groupDashboard"><span class="icon"><i class="fas fa-user-friends"></i></span><span>Group</span></a></li>
        <li class="active"><a href="reportUser"><span class="icon"><i class="fas fa-flag"></i></span><span>Report</span></a></li>
    </ul>
</aside>

<!-- Main Content -->
<main class="main-content">
    <div class="page-header">
        <h1>Report Dashboard</h1>
    </div>

    <div class="page-tabs">
        <a href="reportPost" class="tab-item">Post</a>
        <a href="reportComment" class="tab-item">Comment</a>
        <a href="reportUser" class="tab-item active">User</a>
    </div>

    <% if (reportList == null || reportList.isEmpty()) { %>
    <div class="no-data-message">
        <div class="icon"><i class="fas fa-search"></i></div>
        <h2>No Groups Found</h2>
        <p>There are no groups to display at this time.</p>
    </div>
    <% } else { %>

    <div class="report-list">
        <% for (UserReport report : reportList) { %>
        <div class="report-card">
            <!-- Column 1: Reporter Info -->
            <div class="report-section reporter-info">
                <h3><i class="fas fa-user-shield"></i> Reported By</h3>
                <% SimpleUser reporter = report.getReporter();
                    if (reporter != null) { %>
                <div class="user-block">
                    <img src="<%= reporter.getAvatarUrl()%>" alt="Avatar" class="avatar clickable-image" data-caption="<%= reporter.getFullName() %>">
                    <div>
                        <div class="user-name"><%= reporter.getFullName() %></div>
                        <div class="user-username">@<%= reporter.getUsername() %></div>
                    </div>
                </div>
                <% } %>
            </div>

            <!-- Column 2: Report Details -->
            <div class="report-section report-details">
                <h3><i class="fas fa-envelope-open-text"></i> Report Reason</h3>
                <div class="report-date">Date: <%= report.getReportDate() %></div>
                <div class="report-message">
                    <% String message = report.getReportMessage();
                        if (message != null && message.length() > 100) {
                            String shortMsg = message.substring(0, 100) + "..."; %>
                    <span class="short-text"><%= shortMsg %></span>
                    <span class="full-text hidden"><%= message %></span>
                    <button class="read-more-btn">Read More</button>
                    <% } else { %><%= message != null ? message : "N/A" %><% } %>
                </div>
                <div class="report-actions">
                    <button class="btn btn-suspend">Suspend User</button>
                    <button class="btn btn-dismiss">Dismiss Report</button>
                </div>
            </div>

            <!-- Column 3: Reported User -->
            <div class="report-section reported-user">
                <h3><i class="fas fa-user"></i> Reported User</h3>
                <% SimpleUser reportedUser = report.getReportedUser();
                    if (reportedUser != null) { %>
                <div class="user-block">
                    <img src="<%= reportedUser.getAvatarUrl() != null ? reportedUser.getAvatarUrl() : "https://via.placeholder.com/45/EEEEEE/AAAAAA?text=?" %>" alt="Reported User Avatar" class="avatar clickable-image" data-caption="<%= reportedUser.getFullName() %>">
                    <div>
                        <div class="user-name"><%= reportedUser.getFullName() %></div>
                        <div class="user-username">@<%= reportedUser.getUsername() %></div>
                    </div>
                </div>
                <span class="status-badge status-<%= reportedUser.getStatus() %>"><%= reportedUser.getStatus() %></span>
                <div class="report-actions">
                    <button class="btn btn-view">View Detail</button>
                </div>
                <% } else { %>
                <p>Reported user data is unavailable.</p>
                <% } %>
            </div>
        </div>
        <% } %>
    </div>
    <% } %>
</main>

<!-- Image Modal -->
<div id="imageModal" class="image-modal">
    <span class="modal-close">×</span>
    <img class="modal-content" id="modalImage">
    <div id="modal-caption" style="color: #ccc; text-align: center; padding: 15px 0;"></div>
</div>

<!-- JavaScript for Interactivity -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        // --- Image Modal Logic ---
        const modal = document.getElementById("imageModal");
        const modalImg = document.getElementById("modalImage");
        const captionText = document.getElementById("modal-caption");
        document.querySelectorAll('.clickable-image').forEach(image => {
            image.addEventListener('click', function() {
                modal.style.display = "flex";
                modalImg.src = this.src;
                captionText.textContent = this.dataset.caption;
            });
        });
        function closeModal() { modal.style.display = "none"; }
        modal.querySelector('.modal-close').addEventListener('click', closeModal);
        window.addEventListener('click', e => { if (e.target == modal) closeModal(); });

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