<%@ page import="dto.InteractGroupDTO" %>
<%@ page import="dto.MemberDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="model.Account" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%

    InteractGroupDTO group = (InteractGroupDTO) request.getAttribute("group");

    List<MemberDTO> managerList = (List<MemberDTO>) request.getAttribute("managers");
    List<MemberDTO> memberList = (List<MemberDTO>) request.getAttribute("members");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Members of <%= group.getName() %> - Zust</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
          crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/group.css">
    <style>
        /* --- STYLES FOR MEMBER LIST PAGE --- */
        .members-header { display: flex; justify-content: space-between; align-items: center; background: white; padding: 15px 20px; border-radius: 8px; margin-top: 20px; }
        .members-header h2 { font-size: 1.5rem; margin: 0; }
        .search-box input { border: 1px solid #ddd; border-radius: 20px; padding: 8px 15px; width: 300px; }

        .member-list-section { margin-top: 20px; }
        .member-list-section > h3 { font-size: 1.2rem; font-weight: 600; margin-bottom: 15px; color: #333; }

        .member-cards-container { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 15px; }

        .member-card-link { display: block; text-decoration: none; color: inherit; }
        .member-card {
            display: flex;
            align-items: center;
            gap: 15px;
            background: white;
            padding: 15px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .member-card-link:hover .member-card {
            transform: translateY(-3px);
            box-shadow: 0 4px 10px rgba(0,0,0,0.08);
        }

        .member-card-avatar img { width: 50px; height: 50px; border-radius: 50%; object-fit: cover; }
        .member-card-info { flex-grow: 1; }
        .member-card-info .name { font-weight: 600; }
        .member-card-info .date { font-size: 0.85rem; color: #777; }

        .member-card-action .btn { border: none; padding: 6px 14px; border-radius: 20px; font-weight: 600; cursor: pointer; }
        .btn-add-friend { background-color: #28a745; color: white; }
        .btn-unfriend { background-color: #dc3545; color: white; }

        .hidden { display: none; }
        .no-data-message-member { background: white; padding: 30px; border-radius: 8px; text-align: center; color: #777; }

        /* Modal Styles */
        /*.modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.6); align-items: center; justify-content: center; }*/
        /*.modal-content-wrapper { background: white; padding: 25px; border-radius: 8px; width: 90%; max-width: 450px; }*/
        /*.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }*/
        /*.modal-header h2 { margin: 0; font-size: 1.3rem; }*/
        /*.modal-close { font-size: 1.8rem; font-weight: bold; cursor: pointer; background: none; border: none; }*/
        /*.modal-body textarea { width: 100%; height: 90px; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }*/
        /*.modal-footer { text-align: right; margin-top: 20px; }*/
        /*.modal-footer .btn-submit { background-color: #28a745; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }*/
    </style>
</head>

<body>
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
        <div class="no-data-message" style="margin-top: 20px;"><h2>Group Not Found</h2><p>The requested group does not exist or you do not have permission to view it.</p></div>
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
                    <form style="display:inline;">
                        <input type="hidden" name="groupId" value="<%= group.getId() %>">
                        <input type="hidden" name="action" value="leave">
                        <button type="submit" class="btn btn-leave">Leave Group</button>
                    </form>
                    <% } else if (interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>
                    <form method="POST" style="display:inline;">
                        <input type="hidden" name="groupId" value="<%= group.getId() %>">
                        <input type="hidden" name="action" value="leave">
                        <button type="submit" class="btn btn-leave">Disband Group</button>
                    </form>
                    <% } else if (interactStatus == InteractGroupDTO.InteractStatus.SENT) { %>
                    <form method="POST" style="display:inline;">
                        <input type="hidden" name="groupId" value="<%= group.getId() %>">
                        <input type="hidden" name="action" value="cancel_request">
                        <button type="submit" class="btn btn-cancel">Cancel Request</button>
                    </form>
                    <% } %>

                    <%-- Existing Admin/Feedback Buttons --%>
                    <% if (interactStatus == InteractGroupDTO.InteractStatus.MANAGER || interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>
                    <a href="${pageContext.request.contextPath}/groupProfile?id=<%= group.getId() %>" class="btn btn-edit">Edit Profile</a>
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
            <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=members" class="tab-item active">Members</a>
            <% if (group.getInteractStatus() == InteractGroupDTO.InteractStatus.MANAGER || group.getInteractStatus() == InteractGroupDTO.InteractStatus.LEADER) { %>
            <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=requests" class="tab-item">Joining Request</a>
            <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=pending" class="tab-item">Pending Post</a>
            <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=feedback" class="tab-item">View Feedback</a>
            <% }
            if (interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>
            <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>&tag=assign" class="tab-item">Assign Manager</a>
            <% } %>
        </div>

        <div class="members-header">
            <h2>Group Members</h2>
            <div class="search-box">
                <input type="text" id="memberSearchInput" placeholder=" Find a member..." style="font-family: 'Inter', FontAwesome;">
            </div>
        </div>

        <!-- Administration List -->
        <div class="member-list-section">
            <h3><i class="fas fa-crown"></i> Managers</h3>
            <div class="member-cards-container">
                <% if (managerList == null || managerList.isEmpty()) { %>
                <p>No managers found.</p>
                <% } else {
                    for (MemberDTO member : managerList) {
                        if (member.getInteractStatus() != MemberDTO.InteractStatus.BLOCK) { %>
                <div class="member-card-wrapper" data-member-name="<%= member.getName().toLowerCase() %>">
                    <a href="${pageContext.request.contextPath}/profile?id=<%= member.getId() %>" class="member-card-link">
                        <div class="member-card">
                            <div class="member-card-avatar"><img src="${pageContext.request.contextPath}/static/images/<%= member.getAvatar() %>" alt="Avatar"></div>
                            <div class="member-card-info">
                                <div class="name"><%= member.getName() %></div>
                                <div class="date">Managed: <%= member.getDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) %></div>
                            </div>
                            <div class="member-card-action">
                                <% if (member.getInteractStatus() == MemberDTO.InteractStatus.FRIEND) { %>
                                <form action="${pageContext.request.contextPath}/friendAction" method="POST"><input type="hidden" name="friendId" value="<%= member.getId() %>"><button type="submit" name="action" value="unfriend" class="btn btn-unfriend">Unfriend</button></form>
                                <% } else if (member.getInteractStatus() == MemberDTO.InteractStatus.NORMAL) { %>
                                <button type="button" class="btn btn-add-friend btn-open-friend-modal" data-friend-id="<%= member.getId() %>" data-friend-name="<%= member.getName() %>">Add Friend</button>
                                <% } %>
                            </div>
                        </div>
                    </a>
                </div>
                <%     }
                }
                } %>
            </div>
        </div>

        <!-- Members List -->
        <div class="member-list-section">
            <h3><i class="fas fa-users"></i> Members</h3>
            <div class="member-cards-container">
                <% if (memberList == null || memberList.isEmpty()) { %>
                <div class="no-data-message-member" style="grid-column: 1 / -1;">This group has no other members.</div>
                <% } else {
                    for (MemberDTO member : memberList) {
                        if (member.getInteractStatus() != MemberDTO.InteractStatus.BLOCK) { %>
                <div class="member-card-wrapper" data-member-name="<%= member.getName().toLowerCase() %>">
                    <%-- The entire card structure is repeated here --%>
                    <a href="${pageContext.request.contextPath}/profile?id=<%= member.getId() %>" class="member-card-link">
                        <div class="member-card">
                            <div class="member-card-avatar"><img src="${pageContext.request.contextPath}/static/images/<%= member.getAvatar() %>" alt="Avatar"></div>
                            <div class="member-card-info">
                                <div class="name"><%= member.getName() %></div>
                                <div class="date">Joined: <%= member.getDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) %></div>
                            </div>
                            <div class="member-card-action">
                                <% if (member.getInteractStatus() == MemberDTO.InteractStatus.FRIEND) { %>
                                <form action="${pageContext.request.contextPath}/friendAction" method="POST"><input type="hidden" name="friendId" value="<%= member.getId() %>"><button type="submit" name="action" value="unfriend" class="btn btn-unfriend">Unfriend</button></form>
                                <% } else if (member.getInteractStatus() == MemberDTO.InteractStatus.NORMAL) { %>
                                <button type="button" class="btn btn-add-friend btn-open-friend-modal" data-friend-id="<%= member.getId() %>" data-friend-name="<%= member.getName() %>">Add Friend</button>
                                <% } %>
                            </div>
                        </div>
                    </a>
                </div>
                <%     }
                }
                } %>
            </div>
        </div>
        <% } %>
    </main>
</div>

<!-- Add Friend Modal -->
<div id="addFriendModal" class="modal">
    <div class="modal-content-wrapper">
        <div class="modal-header">
            <h2 id="addFriendModalTitle">Send Friend Request</h2>
            <button class="modal-close">×</button>
        </div>
        <form id="addFriendForm" action="${pageContext.request.contextPath}/friendAction" method="POST">
            <div class="modal-body">
                <p>You can include an optional message with your request.</p>
                <textarea name="message" placeholder="E.g., Hi, we're in the same group!"></textarea>
                <input type="hidden" name="action" value="add_friend">
                <input type="hidden" id="modalFriendId" name="friendId" value="">
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn-submit">Send Request</button>
            </div>
        </form>
    </div>
</div>

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
    document.addEventListener('DOMContentLoaded', function() {

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
        // --- Live Search for Members ---
        const searchInput = document.getElementById('memberSearchInput');
        const memberCards = document.querySelectorAll('.member-card-wrapper');
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            memberCards.forEach(card => {
                card.style.display = card.dataset.memberName.includes(searchTerm) ? 'block' : 'none';
            });
        });

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

        // --- "Add Friend" Modal Logic ---
        const addFriendModal = document.getElementById('addFriendModal');
        const modalTitle = document.getElementById('addFriendModalTitle');
        const modalFriendIdInput = document.getElementById('modalFriendId');

        document.querySelectorAll('.btn-open-friend-modal').forEach(button => {
            button.addEventListener('click', function(event) {
                event.preventDefault();
                event.stopPropagation();

                const friendId = this.dataset.friendId;
                const friendName = this.dataset.friendName;

                modalFriendIdInput.value = friendId;
                modalTitle.textContent = 'Send Friend Request to ' + friendName;
                addFriendModal.style.display = 'flex';
            });
        });

        // --- Generic Modal Close Logic ---
        addFriendModal.querySelector('.modal-close').addEventListener('click', () => {
            addFriendModal.style.display = 'none';
        });
        window.addEventListener('click', (event) => {
            if (event.target == addFriendModal) {
                addFriendModal.style.display = 'none';
            }
        });

        // --- Prevent actions from triggering card link ---
        document.querySelectorAll('.member-card-action form, .member-card-action button').forEach(el => {
            el.addEventListener('click', e => e.stopPropagation());
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
</body>
</html>