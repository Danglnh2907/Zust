# Zust - FPT University social media platform

Zust is a lightweight social media platform built with Java (JSP/Servlets) and SQL Server. It allows users to register, post content, interact with others via likes, comments, and hashtags, and manage personal or group-based feeds.
The platform also provide a admin dashboard for managing the system, with a robust report system.

## Features

- User authentication with OAuth2 and session management
- Create, edit, delete, and repost posts
- Like posts, comment, and tag hashtags
- Group management (invite, kick, promote to manager)
- Upload and serve images securely
- Admin managing system with modern dashboard
- Cloudflare Turnstile support for spam protection
- Responsive and modern UI using HTML, CSS, JS

## Tech Stack

| Layer        | Technology                    |
|--------------|-------------------------------|
| Backend      | Java Servlet, JSP, JDBC       |
| Frontend     | HTML5, CSS3, JavaScript (ES6) |
| Database     | SQL Server                    |
| Build Tool   | Apache Tomcat 11              |
| Security     | Cloudflare Turnstile          |

## Project Structure

```
Zust/
├── .idea/
├── .sonarlint/
├── diagram/
├── src/
│   └── main/
│       ├── java/
│       │   ├── controller/
│       │   ├── dao/
│       │   ├── model/
│       │   └── util/
│       ├── resources/
│       │   ├── images/
│       │   ├── templates/
│       │   ├── database.properties
│       │   ├── logback.xml
│       │   └── save.env
│       └── webapp/
│           ├── css/
│           ├── js/
│           └── WEB-INF/
│               ├── views/
│               └── web.xml
```

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/Danglnh2907/Zust.git
   ```

2. **Configure your environment**
    - Place `save.env` file in root with DB credentials
    - Example:
      ```
      GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID
      GOOGLE_CLIENT_SECRET=YOUR_GOOGLE_CLIENT_SECRET
      GOOGLE_REDIRECT_URI=http://localhost:8080/zust/auth/google/callback
      SMTP_USERNAME=YOUR_EMAIL
      SMTP_PASSWORD=YOUR_APP_PASSWORD
      SMTP_HOST=smtp.gmail.com
      SMTP_PORT=587
      APP_BASE_URL=http://localhost:8080/
      FILE_STORAGE_PATH=YOUR_LOCAL_RESOURCES_PATH
      CLOUDFLARE_SITE_KEY=YOUR_CLOUDFLARE_SITE_KEY
      CLOUDFLARE_SECRET_KEY=YOUR_CLOUDFLARE_SECRET_KEY
      ```

3. **Set up the database**
    - Create database.properties file
      ```
      DB_URL=jdbc:sqlserver://localhost:1433;databaseName=social_media;encrypt=true;trustServerCertificate=true
      DB_USERNAME=YOUR_USERNAME
      DB_PASSWORD=YOUR_PASSWORD
      DB_DRIVER=com.microsoft.sqlserver.jdbc.SQLServerDriver  
      ```
    - Use `dbscript.sql` to create the schema
    - Ensure your SQL Server is running

4. **Deploy the app**
    - Import the project into IntelliJ IDEA Ultimate
    - Build and run on Apache Tomcat 11
    - App runs at: `http://localhost:8080/zust`

5. **Enable Turnstile (optional)**
    - Create a [Cloudflare Turnstile](https://dash.cloudflare.com) site key
    - Add it to your JSP pages or form templates
    - Verify using backend call

---

### Made with ☕ by [Zust team]
