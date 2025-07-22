<%@ page import="model.Account" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="model.Group" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    int totalUsers = (int)request.getAttribute("total_users");
    int totalGroups = (int)request.getAttribute("total_groups");
    int totalPendingReports = (int)request.getAttribute("total_pending_reports");
    int totalPostInLast24Hours = (int)request.getAttribute("total_post_in_last_24_hours");
    HashMap<Account, Integer> userRanking = (HashMap<Account, Integer>)request.getAttribute("user_ranking");
    HashMap<Group, Integer> groupRanking = (HashMap<Group, Integer>)request.getAttribute("group_ranking");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zust - Social Media Dashboard</title>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
            crossorigin="anonymous"></script>
    <!-- Font Awesome for Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        /* --- Global Styles & Variables --- */
        :root {
            --orange: #FF852F;
            --black: #1a1a1a;
            --white: #FFFFFF;
            --light-gray: #f0f2f5;
            --text-color: #333;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        body {
            background-color: var(--light-gray);
            display: flex;
        }

        /* --- Sidebar Styles --- */
        .sidebar {
            width: 260px;
            background-color: var(--black);
            color: var(--white);
            height: 100vh;
            padding: 25px;
            display: flex;
            flex-direction: column;
            position: fixed;
            left: 0;
            top: 0;
        }

        .sidebar .logo {
            font-size: 2.5rem;
            font-weight: bold;
            text-align: center;
            margin-bottom: 40px;
            color: var(--orange);
            letter-spacing: 2px;
        }

        .sidebar .nav-menu {
            list-style-type: none;
            flex-grow: 1;
        }

        .sidebar .nav-menu li a {
            display: flex;
            align-items: center;
            color: var(--white);
            text-decoration: none;
            padding: 15px 20px;
            margin-bottom: 10px;
            border-radius: 8px;
            transition: background-color 0.3s, color 0.3s;
        }

        .sidebar .nav-menu li a .icon {
            margin-right: 15px;
            font-size: 1.2rem;
            width: 20px;
            text-align: center;
        }

        .sidebar .nav-menu li a:hover {
            background-color: var(--orange);
        }

        /* Active link style */
        .sidebar .nav-menu li.active a {
            background-color: var(--orange);
            font-weight: 600;
        }

        /* --- Main Content Styles --- */
        .main-content {
            margin-left: 260px;
            padding: 40px;
            width: calc(100% - 260px);
            background-color: var(--white);
        }

        .main-content header h1 {
            color: var(--black);
            font-size: 2rem;
            margin-bottom: 10px;
        }

        .main-content header p {
            color: #777;
            margin-bottom: 30px;
        }

        /* --- Dashboard Cards --- */
        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 25px;
        }

        .card {
            background-color: #fdfdfd;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.05);
            border: 1px solid #e0e0e0;
        }

        .card .card-icon {
            font-size: 2.5rem;
            color: var(--orange);
            margin-bottom: 15px;
        }

        .card h3 {
            font-size: 1.2rem;
            color: #555;
            margin-bottom: 5px;
        }

        .card .card-value {
            font-size: 2.2rem;
            font-weight: 700;
            color: var(--black);
        }

        /* --- NEW: Ranking Container --- */
        .ranking-container {
            display: grid;
            grid-template-columns: 1fr 1fr; /* Two equal columns */
            gap: 25px; /* Space between the two ranking boards */
            margin-top: 40px; /* Space between cards and boards */
        }

        /* --- Ranking Board Styles --- */
        .ranking-board h3 {
            margin-bottom: 20px;
        }

        .ranking-board table {
            width: 100%;
            border-collapse: collapse;
        }

        .ranking-board th,
        .ranking-board td {
            padding: 15px 5px;
            text-align: left;
            border-bottom: 1px solid #f0f0f0;
        }

        .ranking-board tbody tr:last-child td {
            border-bottom: none;
        }

        .ranking-board th {
            color: #777;
            font-size: 0.8rem;
            font-weight: 600;
            text-transform: uppercase;
        }

        .ranking-board .rank {
            font-weight: 700;
            font-size: 1.1rem;
            width: 60px;
            text-align: center;
        }

        .ranking-board .user-info {
            display: flex;
            align-items: center;
            gap: 15px;
        }

        .ranking-board .avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            object-fit: cover;
        }

        .ranking-board .user-info a {
            text-decoration: none;
            color: var(--black);
            font-weight: 600;
            transition: color 0.3s;
        }

        .ranking-board .user-info a:hover {
            color: var(--orange);
        }

        /* --- NEW: Media Query for Column Spanning --- */
        @media (min-width: 800px) {
            .ranking-board {
                grid-column: span 2;
                /* Make ranking boards take up 2 columns */
            }
        }
    </style>
</head>
<body>

<!-- ======================= Sidebar ======================= -->
<aside class="sidebar">
    <div class="logo">Zust</div>
    <ul class="nav-menu">
        <li  class="active"><a href="dashboard"><span class="icon"><i class="fas fa-chart-pie"></i></span><span>Statistic</span></a></li>
        <li><a href="accountDashboard"><span class="icon"><i class="fas fa-users"></i></span><span>User</span></a></li>
        <li><a href="groupRequest"><span class="icon"><i class="fas fa-plus-square"></i></span><span>Group Request</span></a></li>
        <li><a href="groupDashboard"><span class="icon"><i class="fas fa-user-friends"></i></span><span>Group</span></a></li>
        <li><a href="reportPost"><span class="icon"><i class="fas fa-flag"></i></span><span>Report</span></a></li>
        <li><a href="logout"><span class="icon"><i class="fas fa-sign-out-alt"></i></span><span>Logout</span></a></li>
    </ul>
</aside>

<!-- ======================= Main Content ======================= -->
<main class="main-content">
    <header>
        <h1>Dashboard Overview</h1>
        <p>Welcome back, admin</p>
    </header>

    <div class="dashboard-grid">
        <div class="card">
            <div class="card-icon"><i class="fas fa-users"></i></div>
            <h3>Total Users</h3>
            <div class="card-value">
                <%= totalUsers %>
            </div>
        </div>

        <div class="card">
            <div class="card-icon"><i class="fas fa-user-friends"></i></div>
            <h3>Active Groups</h3>
            <div class="card-value">
                <%= totalGroups %>
            </div>
        </div>

        <div class="card">
            <div class="card-icon"><i class="fas fa-flag"></i></div>
            <h3>Pending Reports</h3>
            <div class="card-value">
                <%= totalPendingReports %>
            </div>
        </div>

        <div class="card">
            <div class="card-icon"><i class="fas fa-pen-to-square"></i></div>
            <h3>Number of posts (24h)</h3>
            <div class="card-value">
                <%= totalPostInLast24Hours %>
            </div>
        </div>

            <!-- Users Ranking Board -->
            <div class="card ranking-board">
                <h3>Top Users Ranking</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>User</th>
                            <th>Point</th>
                        </tr>
                    </thead>
                    <tbody>
                            <%
                                int index = 1;
                                for (Map.Entry<Account, Integer> entry : userRanking.entrySet()) {
                                    Account acc = entry.getKey();
                                    int point = entry.getValue();
                            %>
                                <tr>
                                    <td class="rank"><%= index %></td>
                                    <td class="user-info">
                                        <img class="avatar" src="${pageContext.request.contextPath}/static/images/<%=acc.getAvatar()%>"
                                             alt="User Avatar">
                                        <p>
                                            <%= acc.getUsername() %>
                                        </p>
                                    </td>
                                    <td><%=point%></td>
                                </tr>
                            <%
                                    index++;
                                }
                            %>
                    </tbody>
                </table>
            </div>

            <!-- Groups Ranking Board -->
            <div class="card ranking-board">
                <h3>Top Groups Ranking</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>Group</th>
                            <th>Point</th>
                        </tr>
                    </thead>
                    <tbody>
                            <%
                                index = 1;
                                for (Map.Entry<Group, Integer> entry : groupRanking.entrySet()) {
                                    Group grp = entry.getKey();
                                    int point = entry.getValue();
                            %>
                                <tr>
                                    <td class="rank"><%= index %></td>
                                    <td class="user-info">
                                        <img class="avatar" src="${pageContext.request.contextPath}/static/images/<%=grp.getGroupCoverImage()%>"
                                             alt="Group Avatar">
                                        <p>
                                            <%= grp.getGroupName() %>
                                        </p>
                                    </td>
                                    <td><%=point%></td>
                                </tr>
                            <%
                                    index++;
                                }
                            %>
                    </tbody>
                </table>
            </div>
        </div>
</main>

</body>
</html>