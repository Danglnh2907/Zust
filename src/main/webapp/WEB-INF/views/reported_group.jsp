<%@ page import="model.InteractGroupDTO" %>
<%@ page import="model.RespPostDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="model.Account" %>
<%@ page import="model.ResGroupReportPostDTO" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.GroupCommentReportDTO" %>

<%
  InteractGroupDTO group = (InteractGroupDTO) request.getAttribute("group");
  List<ResGroupReportPostDTO> reportPostList = (List<ResGroupReportPostDTO>) request.getAttribute("reportPostList");
  List<GroupCommentReportDTO> reportCommentList = (List<GroupCommentReportDTO>) request.getAttribute("reportCommentList");
%>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%= group != null ? group.getName() : "Group" %> - Zust</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
        rel="stylesheet"
        integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
        crossorigin="anonymous">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/group.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/composer.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/comment.css">
  <style>
    .post-feed-section { margin-top: 20px; }
    .feed-header { font-size: 1.2rem; font-weight: 600; margin-bottom: 15px; }
    .no-data-message-post { background-color: white; border-radius: 8px; padding: 40px; text-align: center; color: #777; }
    .feed-container { display: flex; flex-direction: column; gap: 15px; }
    /* Enhanced styles for reports */
    .report-card { border: none; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    .report-header { display: flex; align-items: center; gap: 15px; }
    .report-avatar { width: 50px; height: 50px; border-radius: 50%; object-fit: cover; }
    .report-details { flex-grow: 1; }
    .report-reason { background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
    .reported-post { border-top: 1px solid #dee2e6; padding-top: 15px; }
    .report-actions { display: flex; gap: 10px; justify-content: flex-end; }
    .suspension-textarea { resize: vertical; min-height: 80px; }


    /* Modal layout giống feedback/join modal */
    #suspendModal {
      display: none;
      position: fixed;
      z-index: 1050;
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      overflow: auto;
      background-color: rgba(0, 0, 0, 0.5);
      justify-content: center;
      align-items: center;
    }

    #suspendModal .modal-content-wrapper {
      background-color: #fff;
      padding: 25px 30px;
      border-radius: 10px;
      max-width: 600px;
      width: 90%;
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
      animation: fadeIn 0.3s ease-in-out;
    }

    #suspendModal .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    #suspendModal .modal-header h2 {
      font-size: 20px;
      font-weight: bold;
      margin: 0;
    }

    #suspendModal .modal-close {
      font-size: 24px;
      background: none;
      border: none;
      cursor: pointer;
      color: #888;
      transition: color 0.2s;
    }

    #suspendModal .modal-close:hover {
      color: #333;
    }

    #suspendModal .modal-body {
      margin-bottom: 20px;
    }

    #suspendModal .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }

    #suspendModal textarea {
      resize: vertical;
      min-height: 100px;
    }
    .report-card {
      border: none;
      box-shadow: 0 4px 8px rgba(0,0,0,0.1);
    }
    .report-header {
      display: flex;
      align-items: center;
      gap: 15px;
    }
    .report-avatar {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      object-fit: cover;
    }
    .report-details {
      flex-grow: 1;
    }
    .report-reason {
      background-color: #f8f9fa;
      padding: 15px;
      border-radius: 8px;
      margin-bottom: 20px;
    }
    .reported-post {
      border-top: 1px solid #dee2e6;
      padding-top: 15px;
    }
    .report-actions {
      display: flex;
      gap: 10px;
      justify-content: flex-end;
    }

  </style>
</head>

<body>

<div class="modal fade" id="modal" data-bs-keyboard="false"
     tabindex="-1" aria-labelledby="modal-label" aria-hidden="true">
  <div class="modal-dialog modal-dialog-scrollable modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="modal-title-label"></h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"
                aria-label="Close"></button>
      </div>
      <div class="modal-body" id="modal-body">
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>

<div class="lightbox-overlay" id="lightbox">
  <button class="lightbox-close">×</button>
  <img class="lightbox-image" src="" alt="Full-screen image view">
</div>

<div class="app-layout">
  <aside class="left-sidebar">
    <div class="logo">Zust</div>
    <nav class="sidebar-nav">
      <ul>
        <li><a href="${pageContext.request.contextPath}/" class="active">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
               viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
            <polyline points="9 22 9 12 15 12 15 22"></polyline>
          </svg>
          <span>Home</span>
        </a></li>
        <%Account currentUser = (Account) request.getSession().getAttribute("users");%>
        <li><a href="${pageContext.request.contextPath}/profile?userId=<%=currentUser.getId()%>">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
               viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
          <span>My Profile</span>
        </a></li>
        <li><a href="${pageContext.request.contextPath}/createGroup">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
               viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
            <line x1="12" y1="18" x2="12" y2="12"></line>
            <line x1="9" y1="15" x2="15" y2="15"></line>
          </svg>
          <span>Create Group</span>
        </a></li>
      </ul>
    </nav>
    <div class="sidebar-divider"></div>

    <% List<InteractGroupDTO> myGroups = (List<InteractGroupDTO>) request.getAttribute("joinedGroups");%>
    <div class="groups-header">
      <h2>My Groups</h2>
      <span class="groups-count"><%= myGroups != null ? myGroups.size() : 0 %></span>
    </div>
    <div class="scrollable-group-list">
      <%
        if(myGroups != null && !myGroups.isEmpty()){
      %>
      <div class="group-list">
        <%
          for(InteractGroupDTO currentGroup : myGroups){
            String status = currentGroup.getStatus() != null ? currentGroup.getStatus().toLowerCase() : "unknown";
        %>
        <a href="${pageContext.request.contextPath}/group?id=<%= currentGroup.getId() %>" class="group-link">
          <div class="group-item">
            <img src="${pageContext.request.contextPath}/static/images/<%= currentGroup.getCoverImage()%>" alt="Group Avatar">
            <div class="group-item-info">
              <span><%= currentGroup.getName()%></span>
              <span class="members"><%= currentGroup.getMemberCount()%> members</span>
              <span class="status-badge status-<%= status %>"><%= status %></span>
            </div>
          </div>
        </a>
        <%
          }
        %>
      </div>
      <%
      } else {
      %>
      <div class="no-data-message">
        <div class="icon"><i class="fas fa-search"></i></div>
        <h2>No Groups Found</h2>
      </div>
      <%
        }
      %>
    </div>
    <button class="see-all-btn" onclick="location.href='group'">See All</button>
  </aside>

  <main class="main-content">
    <% if (group == null) { %>
    <div class="no-data-message-post" style="margin-top: 20px;"><h2>Group Not Found</h2><p>The requested group does not exist or it was deleted.</p></div>
    <% } else {
      InteractGroupDTO.InteractStatus interactStatus = group.getInteractStatus();
    %>
    <div class="group-header">
      <div class="group-cover-image clickable-cover"
           style="background-image: url('${pageContext.request.contextPath}/static/images/<%= group.getCoverImage() %>');"
           data-image-url="${pageContext.request.contextPath}/static/images/<%= group.getCoverImage() %>">
      </div>
      <div class="group-info-bar">
        <div class="group-title-stats">
          <h1><%= group.getName() %></h1>
          <div class="group-stats">
            <span><i class="fas fa-users"></i> <%= group.getMemberCount() %> Members</span>
            <span><i class="fas fa-eye"></i> <%= group.getStatus() %></span>
          </div>
        </div>
        <div class="group-actions">
          <% if (interactStatus == InteractGroupDTO.InteractStatus.UNJOINED) { %>
          <button type="button" class="btn btn-join" id="openJoinModal" data-group-id="<%= group.getId() %>">Join Group</button>
          <% } else if (interactStatus == InteractGroupDTO.InteractStatus.JOINED || interactStatus == InteractGroupDTO.InteractStatus.MANAGER) { %>
          <form method="POST" style="display:inline;">
            <input type="hidden" name="groupId" value="<%= group.getId() %>">
            <input type="hidden" name="action" value="leave">
            <button type="submit" class="btn btn-leave">Leave Group</button>
          </form>
<%--          <% } else if (interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>--%>
<%--          <form method="POST" style="display:inline;">--%>
<%--            <input type="hidden" name="groupId" value="<%= group.getId() %>">--%>
<%--            <input type="hidden" name="action" value="disband">--%>
<%--            <button type="submit" class="btn btn-leave">Disband Group</button>--%>
<%--          </form>--%>
          <% } else if (interactStatus == InteractGroupDTO.InteractStatus.SENT) { %>
          <form method="POST" style="display:inline;">
            <input type="hidden" name="groupId" value="<%= group.getId() %>">
            <input type="hidden" name="action" value="cancel_request">
            <button type="submit" class="btn btn-cancel">Cancel Request</button>
          </form>
          <% } %>

          <%-- Existing Admin/Feedback Buttons --%>
          <% if (interactStatus == InteractGroupDTO.InteractStatus.MANAGER || interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>
          <a href="${pageContext.request.contextPath}/groupProfile?groupId=<%= group.getId() %>" class="btn btn-edit">Edit Profile</a>
          <% } else if (interactStatus == InteractGroupDTO.InteractStatus.JOINED) {%>
          <button type="button" class="btn btn-feedback" id="openFeedbackModal">Send Feedback</button>
          <% } %>
        </div>
      </div>

      <div class="group-description">
        <% String desc = group.getDescription();
          if (desc != null && desc.length() > 200) { %>
        <span class="short-desc"><%= desc.substring(0, 200) %>...</span>
        <span class="full-desc hidden"><%= desc %></span>
        <button class="read-more-btn">Read More</button>
        <% } else { %>
        <%= desc != null ? desc : "No description provided." %>
        <% } %>
      </div>
    </div>

    <div class="group-tabs">
      <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>" class="tab-item">Discussion</a>
      <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=members" class="tab-item">Members</a>
      <% if (interactStatus == InteractGroupDTO.InteractStatus.MANAGER || interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>
      <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=requests" class="tab-item">Joining Request</a>
      <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=pending" class="tab-item">Pending Post</a>
      <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=feedback" class="tab-item">View Feedback</a>
      <% }
        if (interactStatus == InteractGroupDTO.InteractStatus.LEADER || interactStatus == InteractGroupDTO.InteractStatus.MANAGER) { %>
      <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=report" class="tab-item active">Report Content</a>
      <% } %>
    </div>


    <!-- Main Report Comment Section -->
    <div class="post-feed-section">
      <h2 class="feed-header">Reported Comments</h2>
      <div class="feed">
        <% if (reportCommentList == null || reportCommentList.isEmpty()) { %>
        <div class="no-data-message-post">
          <h3>No Reported Comments</h3>
          <p>No comment reports are available for review.</p>
        </div>
        <% } else {
          for (GroupCommentReportDTO report : reportCommentList) { %>
        <div class="card report-card">
          <div class="card-body">
            <div class="report-header">
              <img src="${pageContext.request.contextPath}/static/images/<%= report.getReporter().getAvatar() %>" alt="Reporter Avatar" class="report-avatar">
              <div class="report-details">
                <h5 class="card-title mb-1">Reported by: <%= report.getReporter().getUsername() %></h5>
                <small class="text-muted">Date: <%= report.getReportDate() %></small>
              </div>
            </div>
            <div class="report-reason mt-3">
              <p class="card-text"><strong>Report Reason:</strong> <%= report.getReportMessage() %></p>
            </div>
            <div class="reported-post mt-3">
              <h6 class="card-subtitle mb-2 text-muted">Reported Comment:</h6>
              <p><%= report.getCommentContent() %></p>
              <% if (report.getCommentImage() != null && !report.getCommentImage().isEmpty()) { %>
              <img src="${pageContext.request.contextPath}/static/images/<%= report.getCommentImage() %>" class="img-fluid mt-2">
              <% } %>

            </div>
            <div class="report-actions mt-4">
              <!-- APPROVE -->
              <form method="POST" action="${pageContext.request.contextPath}/group" class="d-inline-block">
                <input type="hidden" name="action" value="accept_comment">
                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                <input type="hidden" name="reportId" value="<%= report.getReportId() %>">
                <input type="hidden" name="reporterId" value="<%= report.getReportAccountId() %>">
                <input type="hidden" name="reportedId" value="<%= report.getReportedAccountId() %>">
                <input type="hidden" name="commentId" value="<%= report.getCommentId() %>">
                <input type="hidden" name="suspensionMessage" value="Your comment has been removed due to violation.">
                <button type="submit" class="btn btn-danger"><i class="fas fa-check me-1"></i> Approve</button>
              </form>

              <!-- DISMISS -->
              <form method="POST" action="${pageContext.request.contextPath}/group" class="ms-2 d-inline-block">
                <input type="hidden" name="action" value="dismiss_comment">
                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                <input type="hidden" name="reportId" value="<%= report.getReportId() %>">
                <button type="submit" class="btn btn-secondary"><i class="fas fa-times me-1"></i> Dismiss</button>
              </form>
            </div>
          </div>
        </div>
        <% } } %>
      </div>
    </div>



    <!-- Main Report Posts Section -->
    <div class="post-feed-section">
      <h2 class="feed-header">Reported Content</h2>
      <div class="feed">
        <% if (reportPostList == null || reportPostList.isEmpty()) { %>
        <div class="no-data-message-post">
          <h3>No Reports Available</h3>
          <p>There are no reported contents in this group at the moment.</p>
        </div>
        <% } else {
          for (ResGroupReportPostDTO report : reportPostList) { %>
        <div class="card report-card">
          <div class="card-body">
            <div class="report-header">
              <img src="${pageContext.request.contextPath}/static/images/<%= report.getAccount().getAvatar() %>" alt="Reporter Avatar" class="report-avatar">
              <div class="report-details">
                <h5 class="card-title mb-1">Reported by: <%= report.getAccount().getUsername() %></h5>
                <small class="text-muted">Date: <%= report.getReportCreateDate() %></small>
              </div>
            </div>
            <div class="report-reason mt-3">
              <p class="card-text"><strong>Report Reason:</strong> <%= report.getReportContent() %></p>
            </div>
            <div class="reported-post mt-3">
              <h6 class="card-subtitle mb-2 text-muted">Reported Post:</h6>
              <%= report.getPost() %>
            </div>
            <div class="report-actions mt-4">
              <!-- Nút mở modal Takedown Post -->
              <button type="button" class="btn btn-danger btn-takedown"
                      data-report-id="<%= report.getReportId() %>"
                      data-reporter-id="<%= report.getAccount().getId() %>"
                      data-reported-id="<%= report.getPost().getUsername() != null ? report.getPost().getUsername() : "0" %>"
                      data-reported-post-id="<%= report.getPost().getPostId() %>">
                <i class="fas fa-check me-1"></i> Approve
              </button>

              <!-- Nút từ chối báo cáo -->
              <form method="POST" action="${pageContext.request.contextPath}/group" class="ms-2">
                <input type="hidden" name="action" value="dismiss">
                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                <input type="hidden" name="reportId" value="<%= report.getReportId() %>">
                <button type="submit" class="btn btn-secondary"><i class="fas fa-times me-1"></i> Disapprove</button>
              </form>
            </div>

          </div>
        </div>
        <% }
        } %>
      </div>
    </div>
    <% } %>
  </main>
</div>

<!-- Approve model-->
<div class="modal" id="suspendModal">
  <div class="modal-content-wrapper">
    <div class="modal-header">
      <h2>Confirm Approve</h2>
      <button class="modal-close">×</button>
    </div>
    <form id="suspendForm" method="POST" action="${pageContext.request.contextPath}/group">
      <div class="modal-body">
        <p>Are you sure you want to Approve this post?</p>
        <label for="suspensionMessage" class="form-label">Suspension Message:</label>
        <textarea id="suspensionMessage" name="suspensionMessage" class="form-control suspension-textarea mb-2" placeholder="Enter suspension message..." required></textarea>

        <!-- Hidden inputs -->
        <input type="hidden" name="action" value="accept">
        <input type="hidden" id="hiddenReportId" name="reportId">
        <input type="hidden" id="hiddenReporterId" name="reporterId">
        <input type="hidden" id="hiddenReportedId" name="reportedId">
        <input type="hidden" id="hiddenReportedPostId" name="reportedPostId">
        <input type="hidden" name="groupId" value="<%= group.getId() %>">
      </div>
      <div class="modal-footer">
        <button type="submit" class="btn btn-danger"><i class="fas fa-check me-1"></i> Approve</button>
      </div>
    </form>
  </div>
</div>


<!-- Feedback Modal -->
<div id="feedbackModal" class="modal">
  <div class="modal-content-wrapper">
    <div class="modal-header">
      <h2>Send Feedback to Managers</h2>
      <button class="modal-close">×</button>
    </div>
    <form id="feedbackForm" method="POST">
      <div class="modal-body">
        <p>Your feedback will be sent privately to the group's leadership.</p>
        <textarea name="feedbackContent" required placeholder="Type your feedback here..."></textarea>
        <input type="hidden" name="action" value="send_feedback">
        <input type="hidden" name="groupId" value="<%= group != null ? group.getId() : "0" %>">
      </div>
      <div class="modal-footer">
        <button type="submit" class="btn-submit">Send</button>
      </div>
    </form>
  </div>
</div>
<div id="joinRequestModal" class="modal">
  <div class="modal-content-wrapper">
    <div class="modal-header">
      <h2>Send Join Request</h2>
      <button class="modal-close">×</button>
    </div>
    <form id="joinForm" method="POST">
      <div class="modal-body">
        <p>You can include an optional message to the group manager(s).</p>
        <textarea name="joinMessage" placeholder="E.g., Hi, I'm interested in joining because..."></textarea>
        <input type="hidden" name="action" value="join">
        <input type="hidden" id="modalGroupId" name="groupId" value="">
      </div>
      <div class="modal-footer">
        <button type="submit" class="btn-submit join-btn">Send Request</button>
      </div>
    </form>
  </div>
</div>
<div id="imageModal" class="modal">
  <button class="modal-close">×</button>
  <img class="modal-content-image" id="modalImage" src="" alt="Group Cover Image">
</div>

<script>
  // Close suspend modal when clicking on the close button (×)
  document.querySelector('#suspendModal .modal-close')?.addEventListener('click', function () {
    document.getElementById('suspendModal').style.display = 'none';
  });

  // Optional: Close modal if user clicks outside content area
  window.addEventListener('click', function(event) {
    const modal = document.getElementById('suspendModal');
    if (event.target === modal) {
      modal.style.display = 'none';
    }
  });

  document.querySelectorAll('.btn-takedown').forEach(button => {
    button.addEventListener('click', function(event) {
      event.preventDefault();

      suspendForm.querySelector('#hiddenReportId').value = this.dataset.reportId;
      suspendForm.querySelector('#hiddenReporterId').value = this.dataset.reporterId;
      suspendForm.querySelector('#hiddenReportedId').value = this.dataset.reportedId;
      suspendForm.querySelector('#hiddenReportedPostId').value = this.dataset.reportedPostId;

      suspendModal.style.display = 'flex';
    });
  });

  document.addEventListener('DOMContentLoaded', function() {
    // --- "Read More" for Group Description ---
    const descContainer = document.querySelector('.group-description');
    if (descContainer) {
      const readMoreBtn = descContainer.querySelector('.read-more-btn');
      if (readMoreBtn) {
        readMoreBtn.addEventListener('click', function() {
          const shortText = descContainer.querySelector('.short-desc');
          const fullText = descContainer.querySelector('.full-desc');
          shortText.classList.toggle('hidden');
          fullText.classList.toggle('hidden');
          this.textContent = fullText.classList.contains('hidden') ? 'Read More' : 'Read Less';
        });
      }
    }

    const feedbackModal = document.getElementById('feedbackModal');
    const joinModal = document.getElementById('joinRequestModal');
    const allModals = document.querySelectorAll('#feedbackModal, #joinRequestModal, #imageModal');

    // Generic close function
    function closeModal(modal) {
      if (modal) modal.style.display = 'none';
    }

    // Attach close handlers
    allModals.forEach(modal => {
      const closeBtn = modal.querySelector('.modal-close');
      if(closeBtn) {
        closeBtn.addEventListener('click', () => closeModal(modal));
      }
    });
    window.addEventListener('click', (event) => {
      allModals.forEach(modal => {
        if (event.target == modal) closeModal(modal);
      });
    });

    // --- Feedback Modal Trigger ---
    const openFeedbackBtn = document.getElementById('openFeedbackModal');
    if (openFeedbackBtn) {
      openFeedbackBtn.addEventListener('click', () => {
        if (feedbackModal) feedbackModal.style.display = 'flex';
      });
    }

    // --- NEW: Join Modal Trigger ---
    const openJoinBtn = document.getElementById('openJoinModal');
    if(openJoinBtn) {
      openJoinBtn.addEventListener('click', (event) => {
        const groupId = event.currentTarget.dataset.groupId;
        if (joinModal) {
          joinModal.querySelector('#modalGroupId').value = groupId;
          joinModal.style.display = 'flex';
        }
      });
    }

    // --- "Read More" for Descriptions ---
    document.querySelectorAll('.read-more-btn').forEach(button => {
      button.addEventListener('click', function(event) {
        event.preventDefault();  // Prevent the card's link from firing
        event.stopPropagation(); // Stop the event from bubbling up

        const parent = this.closest('.group-card-desc');
        const shortText = parent.querySelector('.short-desc');
        const fullText = parent.querySelector('.full-desc');

        shortText.classList.toggle('hidden');
        fullText.classList.toggle('hidden');
        this.textContent = fullText.classList.contains('hidden') ? 'more' : 'less';
      });
    });

    const imageModal = document.getElementById('imageModal');
    const modalImage = document.getElementById('modalImage');
    const coverImageElement = document.querySelector('.clickable-cover');

    if (coverImageElement) {
      coverImageElement.addEventListener('click', function() {
        const imageUrl = this.dataset.imageUrl;
        modalImage.src = imageUrl;
        imageModal.style.display = 'flex';
      });
    }
  });
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
        crossorigin="anonymous"></script>
<script>
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
</script>
</body>
</html>