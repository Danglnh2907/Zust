<%@ page import="dto.RespPostDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.Duration" %>
<%--
  Created by IntelliJ IDEA.
  User: Asus
  Date: 6/7/2025
  Time: 9:54 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>View posts</title>
        <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
        <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css">
        <script src="${pageContext.request.contextPath}/js/post.js"></script>
    </head>
    <body>
        <div class="posts">
            <%
                //Get posts from request
                ArrayList<RespPostDTO> posts = (ArrayList<RespPostDTO>) request.getAttribute("posts");

                /*Template for post*/
                String postHeaderTemplate = """
                            <div class="post-header">
                                <img class="post-avatar" src="https://i.pravatar.cc/150?u=a042581f4e29026704d" alt="User Avatar">
                                <div class="post-user-info">
                                    <span class="post-user-name">%s</span>
                                </div>
                                <span class="post-timestamp">%s</span>

                                <div class="post-options">
                                    <button class="options-btn" aria-label="More options">...</button>
                                    <div class="options-menu">
                                        <a href="#">Edit</a>
                                        <a href="#" class="delete">Delete</a>
                                        <a href="#">Report</a>
                                    </div>
                                </div>
                            </div>\
                        """;
                String imageTemplate = """
                            <div class="post-media">
                                <div class="carousel-track">
                                    %s
                                </div>
                                <button class="carousel-btn prev" aria-label="Previous slide">❮</button>
                                <button class="carousel-btn next" aria-label="Next slide">❯</button>
                            </div>\
                        """;
                String contentTemplate = "<div class=\"post-content\">%s</div>";
                String actionTemplate = """
                            <div class="post-actions">
                                <!-- Like Button -->
                                <div class="action-btn-group like-btn-group">
                                    <button class="action-btn like-btn" aria-label="Like">
                                        <svg class="icon icon-heart-outline" viewBox="0 0 24 24">
                                            <g>
                                                <path
                                                    d="M12 21.638h-.014C9.403 21.59 1.95 14.856 1.95 8.478c0-3.064 2.525-5.754 5.403-5.754 2.29 0 3.83 1.58 4.646 2.73.814-1.148 2.354-2.73 4.645-2.73 2.88 0 5.404 2.69 5.404 5.755 0 6.376-7.454 13.11-10.037 13.157H12zM7.354 4.225c-2.08 0-3.903 1.988-3.903 4.255 0 5.74 6.036 11.5 8.55 11.623 2.514-.123 8.55-5.882 8.55-11.623 0-2.267-1.823-4.255-3.904-4.255-2.526 0-3.94 2.936-3.952 2.96-.23.562-1.156.562-1.387 0-.014-.023-1.425-2.96-3.95-2.96z">
                                                </path>
                                            </g>
                                        </svg>
                                        <svg class="icon icon-heart-filled" viewBox="0 0 24 24">
                                            <g>
                                                <path
                                                    d="M12 21.638h-.014C9.403 21.59 1.95 14.856 1.95 8.478c0-3.064 2.525-5.754 5.403-5.754 2.29 0 3.83 1.58 4.646 2.73.814-1.148 2.354-2.73 4.645-2.73 2.88 0 5.404 2.69 5.404 5.755 0 6.376-7.454 13.11-10.037 13.157H12z">
                                                </path>
                                            </g>
                                        </svg>
                                    </button>
                                    <span class="count like-count">%d</span>
                                </div>

                                <!-- Comment Button -->
                                <div class="action-btn-group comment-btn-group">
                                    <button class="action-btn" aria-label="Comment">
                                        <svg class="icon" viewBox="0 0 24 24">
                                            <g>
                                                <path
                                                    d="M14.046 2.242l-4.148-.01h-.002c-4.374 0-7.8 3.427-7.8 7.802 0 4.098 3.186 7.206 7.465 7.37v3.828c0 .108.044.286.12.403.142.225.384.347.632.347.138 0 .277-.038.402-.118.264-.168 6.473-4.14 8.088-5.506 1.902-1.61 3.04-3.97 3.043-6.312v-.017c-.006-4.367-3.43-7.787-7.8-7.788zm-2.45 2h4.135c3.287 0 5.954 2.667 5.954 5.948v.017c-.003 1.94-1.026 3.87-2.614 5.284l-3.34 2.836.002-2.64c0-.552-.447-1-1-1h-4.51c-3.287 0-5.954-2.667-5.954-5.948s2.667-5.948 5.954-5.948z">
                                                </path>
                                            </g>
                                        </svg>
                                    </button>
                                    <span class="count comment-count">%d</span>
                                </div>

                                <!-- Repost Button -->
                                <div class="action-btn-group repost-btn-group">
                                    <button class="action-btn" aria-label="Repost">
                                        <svg class="icon" viewBox="0 0 24 24">
                                            <g>
                                                <path
                                                    d="M23.77 15.67c-.292-.293-.767-.293-1.06 0l-2.22 2.22V7.65c0-2.068-1.683-3.75-3.75-3.75h-5.85c-.414 0-.75.336-.75.75s.336.75.75.75h5.85c1.24 0 2.25 1.01 2.25 2.25v10.24l-2.22-2.22c-.293-.293-.768-.293-1.06 0s-.294.768 0 1.06l3.5 3.5c.145.147.337.22.53.22s.383-.072.53-.22l3.5-3.5c.294-.292.294-.767 0-1.06zm-10.66 3.28H7.26c-1.24 0-2.25-1.01-2.25-2.25V6.46l2.22 2.22c.293.293.768.293 1.06 0s.294-.768 0-1.06l-3.5-3.5c-.293-.294-.768-.294-1.06 0l-3.5 3.5c-.294.292-.294.767 0 1.06s.767.293 1.06 0l2.22-2.22V16.7c0 2.068 1.683 3.75 3.75 3.75h5.85c.414 0 .75-.336.75-.75s-.336-.75-.75-.75z">
                                                </path>
                                            </g>
                                        </svg>
                                    </button>
                                    <span class="count repost-count">%d</span>
                                </div>


                            </div>\
                        """;

                //Loop through each post
                for (RespPostDTO post : posts) {
                    //Get header
                    Duration timeDiff = Duration.between(post.getLastModified(), LocalDateTime.now());
                    String lastTimeUpdate;
                    if (timeDiff.toSeconds() < 60) {
                        lastTimeUpdate = "Just now";
                    } else if (timeDiff.toMinutes() < 60) {
                        lastTimeUpdate = timeDiff.toMinutes() + " minutes";
                    } else if (timeDiff.toHours() < 24) {
                        lastTimeUpdate = timeDiff.toHours() + " hours";
                    } else {
                        lastTimeUpdate = timeDiff.toDays() + " days";
                    }
                    String header = String.format(postHeaderTemplate, post.getUsername(), lastTimeUpdate);

                    //Get image carousel
                    String carousel = "";
                    if (!post.getImages().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (String image : post.getImages()) {
                            sb.append(String.format("<img class=\"carousel-slide\" src=\"%s\">",
                                    "http://localhost:9090/zust/static/images/" + image));
                        }
                        carousel = String.format(imageTemplate, sb);
                    }


                    //Get post content
                    String content = String.format(contentTemplate, post.getPostContent());

                    //Get likes, comment and repost count
                    String action = String.format(actionTemplate, post.getLikeCount(), post.getCommentCount(), post.getRepostCount());

                    //Get the full post and print out
                    out.println(String.format("<div class=\"post\">%s\n%s\n%s\n%s</div>", header, content, carousel, action));
                }
            %>
        </div>
  </body>
</html>
