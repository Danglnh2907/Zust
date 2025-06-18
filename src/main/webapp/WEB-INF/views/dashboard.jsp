<%--
  Created by IntelliJ IDEA.
  User: hoqua
  Date: 6/18/2025
  Time: 8:55 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zust - Social Media Dashboard</title>
    <!-- Font Awesome for Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        /* --- Global Styles & Variables --- */
        :root {
            --orange: #FF852F;
            --black: #1a1a1a; /* Using a slightly softer black */
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
            width: 20px; /* Aligns text even if icons have different widths */
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
            margin-left: 260px; /* Same as sidebar width */
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
        .dashboard-cards {
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

    </style>
</head>
<body>

<!-- ======================= Sidebar ======================= -->
<aside class="sidebar">
    <div class="logo">Zust</div>

    <ul class="nav-menu">
        <li class="active">
            <a href="#">
                <span class="icon"><i class="fas fa-chart-pie"></i></span>
                <span>Statistic</span>
            </a>
        </li>
        <li>
            <a href="#">
                <span class="icon"><i class="fas fa-users"></i></span>
                <span>User</span>
            </a>
        </li>
        <!-- Added Notification Item -->
        <li>
            <a href="#">
                <span class="icon"><i class="fas fa-bell"></i></span>
                <span>Notification</span>
            </a>
        </li>
        <li>
            <a href="#">
                <span class="icon"><i class="fas fa-plus-square"></i></span>
                <span>Create Group Request</span>
            </a>
        </li>
        <li>
            <a href="#">
                <span class="icon"><i class="fas fa-user-friends"></i></span>
                <span>Group</span>
            </a>
        </li>
        <li>
            <a href="#">
                <span class="icon"><i class="fas fa-flag"></i></span>
                <span>Report</span>
            </a>
        </li>
    </ul>
</aside>

<!-- ======================= Main Content ======================= -->
<main class="main-content">
    <header>
        <h1>Dashboard Overview</h1>
        <p>Welcome back, Admin! Here's a summary of your social media activity.</p>
    </header>

    <div class="dashboard-cards">
        <div class="card">
            <div class="card-icon">
                <i class="fas fa-users"></i>
            </div>
            <h3>Total Users</h3>
            <div class="card-value">12,458</div>
        </div>

        <div class="card">
            <div class="card-icon">
                <i class="fas fa-user-friends"></i>
            </div>
            <h3>Active Groups</h3>
            <div class="card-value">672</div>
        </div>

        <div class="card">
            <div class="card-icon">
                <i class="fas fa-flag"></i>
            </div>
            <h3>Pending Reports</h3>
            <div class="card-value">34</div>
        </div>

        <!-- Updated Fourth Card -->
        <div class="card">
            <div class="card-icon">
                <i class="fas fa-pen-to-square"></i>
            </div>
            <h3>Number of posts (24h)</h3>
            <div class="card-value">2,189</div>
        </div>
    </div>
</main>

</body>
</html>
