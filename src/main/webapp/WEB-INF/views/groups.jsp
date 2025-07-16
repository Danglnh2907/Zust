<%@ page import="dto.InteractGroupDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="model.Account" %>
<%@ page contentType="text/html;charset=UTF-8" %>


<%
    List<InteractGroupDTO> allGroups = (List<InteractGroupDTO>) request.getAttribute("allGroups");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>All Groups - Zust</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/group.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
          crossorigin="anonymous">
    <style>
        /* --- STYLES FOR THE MAIN CONTENT --- */
        .main-content { padding: 20px; }
        .main-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .main-header h1 { font-size: 1.8rem; font-weight: 700; }
        .search-box input { border: 1px solid #ddd; border-radius: 20px; padding: 8px 15px; width: 300px; }

        .all-groups-container { display: flex; flex-direction: column; gap: 15px; }
        .group-card-link { display: block; text-decoration: none; color: inherit; }
        .group-card {
            display: grid;
            grid-template-columns: auto 1fr auto;
            gap: 20px;
            align-items: center;
            background: white;
            padding: 15px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .group-card-link:hover .group-card {
            transform: translateY(-3px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }

        .group-card-image img { width: 100px; height: 100px; border-radius: 8px; object-fit: cover; }
        .group-card-info h3 { font-size: 1.1rem; font-weight: 600; margin-bottom: 5px; }
        .group-card-stats { display: flex; gap: 15px; font-size: 0.9rem; color: #666; margin-bottom: 10px; }
        .group-card-stats span { display: flex; align-items: center; gap: 5px; }
        .group-card-desc { font-size: 0.9rem; color: #555; line-height: 1.5; }

        .read-more-btn { background: none; border: none; padding: 0; font: inherit; color: #FF852F; text-decoration: underline; cursor: pointer; margin-left: 5px; }
        .hidden { display: none; }

        .group-card-action .btn {
            border: none;
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: 600;
            cursor: pointer;
            transition: background-color 0.2s;
        }
        .btn-join { background-color: #28a745; color: white; }
        .btn-leave { background-color: #dc3545; color: white; }
        .btn-cancel { background-color: #6c757d; color: white; }
        .no-data-message { background-color: white; border-radius: 5px; padding: 30px 20px; text-align: center; }

        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.6); align-items: center; justify-content: center; }
        .modal-content-wrapper { background: white; padding: 25px; border-radius: 8px; width: 90%; max-width: 500px; box-shadow: 0 5px 20px rgba(0,0,0,0.2); animation: zoomIn 0.3s ease; }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .modal-header h2 { margin: 0; font-size: 1.4rem; }
        .modal-close { font-size: 1.8rem; font-weight: bold; cursor: pointer; color: #aaa; background: none; border: none; }
        .modal-body textarea { width: 100%; height: 100px; padding: 10px; border-radius: 5px; border: 1px solid #ddd; font-size: 1rem; resize: vertical; margin-top: 10px; }
        .modal-footer { text-align: right; margin-top: 20px; }
        .modal-footer .btn-submit { background-color: #28a745; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: 600; }
        @keyframes zoomIn { from {transform: scale(0.8); opacity: 0;} to {transform: scale(1); opacity: 1;} }
    </style>
</head>

<body>
<div class="app-layout">
    <!-- Left Sidebar (Reused) -->
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

        <% List<InteractGroupDTO> joinedGroups = (List<InteractGroupDTO>) request.getAttribute("joinedGroups");%>
        <div class="groups-header">
            <h2>My Groups</h2>
            <span class="groups-count"><%= joinedGroups != null ? joinedGroups.size() : 0 %></span>
        </div>
        <div class="scrollable-group-list">
            <%
                if(joinedGroups != null && !joinedGroups.isEmpty()){
            %>
            <div class="group-list">
                <%
                    for(InteractGroupDTO group : joinedGroups){
                        String status = group.getStatus() != null ? group.getStatus().toLowerCase() : "unknown";
                %>
                <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>" class="group-link">
                    <div class="group-item">
                        <img src="${pageContext.request.contextPath}/static/images/<%= group.getCoverImage()%>" alt="Group Avatar">
                        <div class="group-item-info">
                            <span><%= group.getName()%></span>
                            <span class="members"><%= group.getMemberCount()%> members</span>
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
        <header class="main-header">
            <h1>Discover Groups</h1>
            <div class="search-box">
                <input type="text" id="groupSearchInput" placeholder=" Search for groups..." style="font-family: 'Inter', FontAwesome;">
            </div>
        </header>
        <div class="all-groups-container" id="allGroupsContainer">
            <% if (allGroups == null || allGroups.isEmpty()) { %>
            <div class="no-data-message">
                <h2>No Groups Available</h2>
                <p>There are no public groups to display at this time.</p>
            </div>
            <% } else {
                for (InteractGroupDTO group : allGroups) { %>
            <div class="group-card-wrapper" data-group-name="<%= group.getName().toLowerCase() %>">
                <a href="${pageContext.request.contextPath}/group?id=<%= group.getId() %>" class="group-card-link">
                    <div class="group-card">
                        <div class="group-card-image">
                            <img src="${pageContext.request.contextPath}/static/images/<%= group.getCoverImage() %>" alt="<%= group.getName() %> cover">
                        </div>
                        <div class="group-card-info">
                            <h3><%= group.getName() %></h3>
                            <div class="group-card-stats">
                                <span><i class="fas fa-users"></i> <%= group.getMemberCount() %> Members</span>
                                <span><i class="fas fa-pen-to-square"></i> <%= group.getPostCount() %> Posts</span>
                            </div>
                            <div class="group-card-desc">
                                <%
                                    String desc = group.getDescription();
                                    if (desc != null && desc.length() > 100) {
                                        String shortDesc = desc.substring(0, 100) + "...";
                                %>
                                <span class="short-desc"><%= shortDesc %></span>
                                <span class="full-desc hidden"><%= desc %></span>
                                <button class="read-more-btn">more</button>
                                <% } else { %>
                                <%= (desc != null) ? desc : "No description available." %>
                                <% } %>
                            </div>
                        </div>
                        <div class="group-card-action">
                            <%
                                InteractGroupDTO.InteractStatus interactStatus = group.getInteractStatus();
                                if (interactStatus == InteractGroupDTO.InteractStatus.UNJOINED) { %>
                            <%-- THIS IS NO LONGER A FORM. It's a button to open the modal. --%>
                            <button type="button" class="btn btn-join btn-open-modal" data-group-id="<%= group.getId() %>">Join</button>
                            <% } else if (interactStatus == InteractGroupDTO.InteractStatus.JOINED ||
                                    interactStatus == InteractGroupDTO.InteractStatus.MANAGER) { %>
                            <form method="POST">
                                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                <input type="hidden" name="action" value="leave">
                                <button type="submit" class="btn btn-leave">Leave</button>
                            </form>
                            <%} else if (interactStatus == InteractGroupDTO.InteractStatus.LEADER) { %>
                            <form method="POST" style="display:inline;">
                                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                <input type="hidden" name="action" value="disband">
                                <button type="submit" class="btn btn-leave">Disband Group</button>
                            </form>
                            <% } else if (interactStatus == InteractGroupDTO.InteractStatus.SENT) { %>
                            <form method="POST">
                                <input type="hidden" name="groupId" value="<%= group.getId() %>">
                                <input type="hidden" name="action" value="cancel_request">
                                <button type="submit" class="btn btn-cancel">Cancel Request</button>
                            </form>
                            <% } %>
                        </div>
                    </div>
                </a>
            </div>
            <%     }
            } %>
        </div>
    </main>
</div>

<div id="joinRequestModal" class="modal">
    <div class="modal-content-wrapper">
        <div class="modal-header">
            <h2>Send Join Request</h2>
            <button class="modal-close">×</button>
        </div>
        <form id="joinForm" action="${pageContext.request.contextPath}/group" method="POST">
            <div class="modal-body">
                <p>You can include an optional message to the group manager(s).</p>
                <textarea name="joinMessage" placeholder="E.g., Hi, I'm interested in joining because..."></textarea>

                <!-- Hidden fields to be populated by JavaScript -->
                <input type="hidden" name="action" value="join">
                <input type="hidden" id="modalGroupId" name="groupId" value="">
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn-submit">Send Request</button>
            </div>
        </form>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // --- Live Search for Groups ---
        const searchInput = document.getElementById('groupSearchInput');
        const groupContainer = document.getElementById('allGroupsContainer');
        const groupCards = groupContainer.querySelectorAll('.group-card-wrapper');

        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            groupCards.forEach(card => {
                const groupName = card.dataset.groupName;
                if (groupName.includes(searchTerm)) {
                    card.style.display = 'block';
                } else {
                    card.style.display = 'none';
                }
            });
        });

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

        const joinModal = document.getElementById('joinRequestModal');
        const joinForm = document.getElementById('joinForm');

        // Generic function to close any modal
        function closeModal(modal) {
            if (modal) {
                modal.style.display = "none";
            }
        }

        // --- Event listener for opening the Join modal ---
        document.querySelectorAll('.btn-open-modal').forEach(button => {
            button.addEventListener('click', function(event) {
                event.preventDefault();  // Prevent the card's link from firing
                event.stopPropagation(); // Stop the event from bubbling up

                const groupId = this.dataset.groupId;

                // Set the groupId in the modal's hidden form field
                joinForm.querySelector('#modalGroupId').value = groupId;

                // Display the modal
                joinModal.style.display = 'flex';
            });
        });

        // Add close functionality to the modal's close button
        if (joinModal) {
            joinModal.querySelector('.modal-close').addEventListener('click', () => {
                closeModal(joinModal);
            });
        }

        // Close modal by clicking background
        window.addEventListener('click', (event) => {
            if (event.target == joinModal) {
                closeModal(joinModal);
            }
        });

        // Stop form clicks from triggering the card link
        document.querySelectorAll('.group-card-action form, .group-card-action button').forEach(el => {
            el.addEventListener('click', function(event) {
                event.stopPropagation();
            });
        });
    });
</script>
</body>
</html>