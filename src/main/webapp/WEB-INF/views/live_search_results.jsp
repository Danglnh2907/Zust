<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page import="model.Account, model.Post, model.Group" %>
<%@ page import="java.util.List" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="keyword" value="${keyword}" />

<c:choose>
  <c:when test="${empty liveResults or (empty liveResults.users and empty liveResults.posts_content and empty liveResults.posts_hashtag and empty liveResults.groups)}">
    <div class="no-results">
      <i class="fas fa-search" style="margin-right: 8px; color: #65676b;"></i>
      No results found for "<c:out value="${keyword}"/>"
    </div>
  </c:when>
  <c:otherwise>
    <!-- Users Section -->
    <c:if test="${not empty liveResults.users}">
      <div class="search-category">
        <div class="search-category-header">
          <span><i class="fas fa-user" style="margin-right: 6px;"></i>People</span>
          <a href="#" class="view-all-link" data-category="users">See all</a>
        </div>
        <c:forEach var="user" items="${liveResults.users}" varStatus="status">
          <div class="search-item" data-type="user" data-id="${user.id}">
            <div class="search-item-avatar">
              <c:choose>
                <c:when test="${not empty user.avatar}">
                  <img src="${user.avatar}" alt="${user.fullname}">
                </c:when>
                <c:otherwise>
                  <i class="fas fa-user"></i>
                </c:otherwise>
              </c:choose>
            </div>
            <div class="search-item-content">
              <div class="search-item-title">
                <c:out value="${user.fullname}"/>
              </div>
              <div class="search-item-subtitle">
                @<c:out value="${user.username}"/>
                <c:if test="${not empty user.bio and fn:length(user.bio) > 0}">
                  · <c:choose>
                  <c:when test="${fn:length(user.bio) > 30}">
                    <c:out value="${fn:substring(user.bio, 0, 30)}"/>...
                  </c:when>
                  <c:otherwise>
                    <c:out value="${user.bio}"/>
                  </c:otherwise>
                </c:choose>
                </c:if>
              </div>
            </div>
            <div class="search-item-type">Person</div>
          </div>
        </c:forEach>
      </div>
    </c:if>

    <!-- Posts by Content Section -->
    <c:if test="${not empty liveResults.posts_content}">
      <div class="search-category">
        <div class="search-category-header">
          <span><i class="fas fa-file-text" style="margin-right: 6px;"></i>Posts</span>
          <a href="#" class="view-all-link" data-category="posts_content">See all</a>
        </div>
        <c:forEach var="post" items="${liveResults.posts_content}" varStatus="status">
          <div class="search-item" data-type="post" data-id="${post.id}">
            <div class="search-item-avatar">
              <c:choose>
                <c:when test="${not empty post.account and not empty post.account.avatar}">
                  <img src="${post.account.avatar}" alt="${post.account.fullname}">
                </c:when>
                <c:otherwise>
                  <i class="fas fa-file-alt"></i>
                </c:otherwise>
              </c:choose>
            </div>
            <div class="search-item-content">
              <div class="search-item-title">
                <c:choose>
                  <c:when test="${post.account != null}">
                    <c:out value="${post.account.fullname}"/>
                  </c:when>
                  <c:otherwise>
                    Post #${post.id}
                  </c:otherwise>
                </c:choose>
              </div>
              <div class="search-item-subtitle">
                <c:choose>
                  <c:when test="${not empty post.postContent and fn:length(post.postContent) > 60}">
                    <c:out value="${fn:substring(post.postContent, 0, 60)}"/>...
                  </c:when>
                  <c:when test="${not empty post.postContent}">
                    <c:out value="${post.postContent}"/>
                  </c:when>
                  <c:otherwise>
                    Post content
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
            <div class="search-item-type">Post</div>
          </div>
        </c:forEach>
      </div>
    </c:if>

    <!-- Posts by Hashtag Section -->
    <c:if test="${not empty liveResults.posts_hashtag}">
      <div class="search-category">
        <div class="search-category-header">
          <span><i class="fas fa-hashtag" style="margin-right: 6px;"></i>Hashtags</span>
          <a href="#" class="view-all-link" data-category="posts_hashtag">See all</a>
        </div>
        <c:forEach var="post" items="${liveResults.posts_hashtag}" varStatus="status">
          <div class="search-item" data-type="post" data-id="${post.id}">
            <div class="search-item-avatar">
              <c:choose>
                <c:when test="${not empty post.account and not empty post.account.avatar}">
                  <img src="${post.account.avatar}" alt="${post.account.fullname}">
                </c:when>
                <c:otherwise>
                  <i class="fas fa-hashtag"></i>
                </c:otherwise>
              </c:choose>
            </div>
            <div class="search-item-content">
              <div class="search-item-title">
                <c:choose>
                  <c:when test="${post.account != null}">
                    <c:out value="${post.account.fullname}"/>
                  </c:when>
                  <c:otherwise>
                    Tagged Post #${post.id}
                  </c:otherwise>
                </c:choose>
              </div>
              <div class="search-item-subtitle">
                <c:choose>
                  <c:when test="${not empty post.postContent and fn:length(post.postContent) > 60}">
                    <c:out value="${fn:substring(post.postContent, 0, 60)}"/>...
                  </c:when>
                  <c:when test="${not empty post.postContent}">
                    <c:out value="${post.postContent}"/>
                  </c:when>
                  <c:otherwise>
                    Tagged post content
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
            <div class="search-item-type">#Tag</div>
          </div>
        </c:forEach>
      </div>
    </c:if>

    <!-- Groups Section -->
    <c:if test="${not empty liveResults.groups}">
      <div class="search-category">
        <div class="search-category-header">
          <span><i class="fas fa-users" style="margin-right: 6px;"></i>Groups</span>
          <a href="#" class="view-all-link" data-category="groups">See all</a>
        </div>
        <c:forEach var="group" items="${liveResults.groups}" varStatus="status">
          <div class="search-item" data-type="group" data-id="${group.id}">
            <div class="search-item-avatar" style="border-radius: 8px;">
              <c:choose>
                <c:when test="${not empty group.groupCoverImage}">
                  <img src="${group.groupCoverImage}" alt="${group.groupName}" style="border-radius: 8px;">
                </c:when>
                <c:otherwise>
                  <i class="fas fa-users"></i>
                </c:otherwise>
              </c:choose>
            </div>
            <div class="search-item-content">
              <div class="search-item-title">
                <c:out value="${group.groupName}"/>
              </div>
              <div class="search-item-subtitle">
                <c:choose>
                  <c:when test="${not empty group.groupDescription and fn:length(group.groupDescription) > 50}">
                    <c:out value="${fn:substring(group.groupDescription, 0, 50)}"/>...
                  </c:when>
                  <c:when test="${not empty group.groupDescription}">
                    <c:out value="${group.groupDescription}"/>
                  </c:when>
                  <c:otherwise>
                    Group
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
            <div class="search-item-type">Group</div>
          </div>
        </c:forEach>
      </div>
    </c:if>
  </c:otherwise>
</c:choose>
