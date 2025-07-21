<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="model.*" %>
<%@ page import="model.RespPostDTO" %>

<%
  Map<String, List<?>> searchResults = (Map<String, List<?>>) request.getAttribute("searchResults");
  String keyword = (String) request.getAttribute("keyword");

  boolean hasResults = false;

  if (searchResults != null) {
    List<?> users = searchResults.get("users");
    List<?> postsContent = searchResults.get("posts_content");
    List<?> postsHashtag = searchResults.get("posts_hashtag");
    List<?> groups = searchResults.get("groups");

    hasResults = (users != null && !users.isEmpty()) ||
            (postsContent != null && !postsContent.isEmpty()) ||
            (postsHashtag != null && !postsHashtag.isEmpty()) ||
            (groups != null && !groups.isEmpty());
%>

<% if (!hasResults) { %>
<div style="padding: 30px 20px; text-align: center; color: #65676b;">
  <i class="fas fa-search" style="font-size: 24px; margin-bottom: 8px; display: block; opacity: 0.5;"></i>
  <div>No results found for "<%= keyword != null ? keyword.replaceAll("<", "&lt;").replaceAll(">", "&gt;") : "" %>"</div>
</div>
<% } else { %>

<!-- Users Section -->
<% if (users != null && !users.isEmpty()) { %>
<div class="search-section-header">
  <i class="fas fa-user" style="margin-right: 8px; color: #1877f2;"></i>
  People (<%= users.size() %>)
</div>
<%
  for (int i = 0; i < Math.min(users.size(), 3); i++) {
    Account user = (Account) users.get(i);
    String avatar = user.getAvatar();
    String fullname = user.getFullname() != null ? user.getFullname() : "Unknown";
    String username = user.getUsername() != null ? user.getUsername() : "unknown";
%>
<div class="search-result-item" onclick="goToProfile(<%= user.getId() %>)">
  <div style="width: 40px; height: 40px; border-radius: 50%; background: #e4e6ea; display: flex; align-items: center; justify-content: center; margin-right: 12px; color: #65676b;">
    <% if (avatar != null && !avatar.trim().isEmpty()) { %>
    <img src="${pageContext.request.contextPath}/static/images/<%=avatar%>" style="width: 100%; height: 100%; border-radius: 50%; object-fit: cover;" alt="<%= fullname.replaceAll("\"", "&quot;") %>">
    <% } else { %>
    <i class="fas fa-user"></i>
    <% } %>
  </div>
  <div style="flex-grow: 1;">
    <div style="font-weight: 600; color: #1c1e21; font-size: 14px;"><%= fullname.replaceAll("<", "&lt;").replaceAll(">", "&gt;") %></div>
    <div style="color: #65676b; font-size: 13px;">@<%= username.replaceAll("<", "&lt;").replaceAll(">", "&gt;") %></div>
  </div>
</div>
<% } %>
<% } %>

<!-- Posts Content Section -->
<% if (postsContent != null && !postsContent.isEmpty()) { %>
<div class="search-section-header">
  <i class="fas fa-file-alt" style="margin-right: 8px; color: #1877f2;"></i>
  Posts (<%= postsContent.size() %>)
</div>
<%
  for (int i = 0; i < Math.min(postsContent.size(), 2); i++) {
    RespPostDTO post = (RespPostDTO) postsContent.get(i);
    String content = post.getPostContent() != null ? post.getPostContent() : "[No content available]";
    // Strip HTML tags
    content = content.replaceAll("<[^>]*>", "").trim();
    if (content.length() > 100) {
      content = content.substring(0, 100) + "...";
    }
    String authorName = post.getUsername() != null ? post.getUsername() : "Unknown Author";
%>
<div class="search-result-item" onclick="goToPost(<%= post.getPostId() %>)">
  <div style="width: 40px; height: 40px; border-radius: 50%; background: #e4e6ea; display: flex; align-items: center; justify-content: center; margin-right: 12px; color: #65676b;">
    <i class="fas fa-file-alt"></i>
  </div>
  <div style="flex-grow: 1;">
    <div style="color: #65676b; font-size: 13px;">by <%= authorName.replaceAll("<", "&lt;").replaceAll(">", "&gt;") %></div>
    <div style="color: #1c1e21; font-size: 13px; line-height: 1.3;"><%= content.replaceAll("<", "&lt;").replaceAll(">", "&gt;") %></div>
  </div>
</div>
<% } %>
<% } %>

<!-- Posts Hashtag Section -->
<% if (postsHashtag != null && !postsHashtag.isEmpty()) { %>
<div class="search-section-header">
  <i class="fas fa-hashtag" style="margin-right: 8px; color: #1877f2;"></i>
  Tagged Posts (<%= postsHashtag.size() %>)
</div>
<%
  for (int i = 0; i < Math.min(postsHashtag.size(), 2); i++) {
    RespPostDTO post = (RespPostDTO) postsHashtag.get(i);
    String content = post.getPostContent() != null ? post.getPostContent() : "[No content available]";
    content = content.replaceAll("<[^>]*>", "").trim();
    if (content.length() > 100) {
      content = content.substring(0, 100) + "...";
    }
    String authorName = post.getUsername() != null ? post.getUsername() : "Unknown Author";
%>
<div class="search-result-item" onclick="goToPost(<%= post.getPostId() %>)">
  <div style="width: 40px; height: 40px; border-radius: 50%; background: #e4e6ea; display: flex; align-items: center; justify-content: center; margin-right: 12px; color: #65676b;">
    <i class="fas fa-hashtag"></i>
  </div>
  <div style="flex-grow: 1;">
    <div style="color: #65676b; font-size: 13px;">by <%= authorName.replaceAll("<", "&lt;").replaceAll(">", "&gt;") %></div>
    <div style="color: #1c1e21; font-size: 13px; line-height: 1.3;"><%= content.replaceAll("<", "&lt;").replaceAll(">", "&gt;") %></div>
  </div>
  <span style="background: #1877f2; color: white; padding: 2px 6px; border-radius: 10px; font-size: 10px; margin-left: auto;">
                    <i class="fas fa-hashtag"></i>
                </span>
</div>
<% } %>
<% } %>

<!-- Groups Section -->
<% if (groups != null && !groups.isEmpty()) { %>
<div class="search-section-header">
  <i class="fas fa-users" style="margin-right: 8px; color: #1877f2;"></i>
  Groups (<%= groups.size() %>)
</div>
<%
  for (int i = 0; i < Math.min(groups.size(), 2); i++) {
    Group group = (Group) groups.get(i);
    String groupName = group.getGroupName() != null ? group.getGroupName() : "Unknown Group";
    String description = group.getGroupDescription();
    if (description != null && description.length() > 60) {
      description = description.substring(0, 60) + "...";
    }
%>
<div class="search-result-item" onclick="goToGroup(<%= group.getId() %>)">
  <div style="width: 40px; height: 40px; border-radius: 8px; background: #e4e6ea; display: flex; align-items: center; justify-content: center; margin-right: 12px; color: #65676b;">
    <i class="fas fa-users"></i>
  </div>
  <div style="flex-grow: 1;">
    <div style="font-weight: 600; color: #1c1e21; font-size: 14px;"><%= groupName.replaceAll("<", "&lt;").replaceAll(">", "&gt;") %></div>
    <% if (description != null && !description.trim().isEmpty()) { %>
    <div style="color: #65676b; font-size: 13px;"><%= description.replaceAll("<", "&lt;").replaceAll(">", "&gt;") %></div>
    <% } %>
  </div>
</div>
<% } %>
<% } %>

<!-- View All Footer -->
<div style="padding: 12px 16px; text-align: center; border-top: 1px solid #e4e6ea; background: #f8f9fa;">
  <a href="${pageContext.request.contextPath}/search?keyword=<%= java.net.URLEncoder.encode(keyword != null ? keyword : "", "UTF-8") %>"
     style="color: #1877f2; text-decoration: none; font-weight: 500; font-size: 14px;">
    View all results for "<%= keyword != null ? keyword.replaceAll("<", "&lt;").replaceAll(">", "&gt;") : "" %>"
  </a>
</div>

<% } %>

<%
  }
%>
