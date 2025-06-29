<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zust - Connect, Share, Discover</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 50%, #000000 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            overflow-x: hidden;
        }
        body::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background:
                    radial-gradient(circle at 20% 80%, rgba(255, 165, 0, 0.1) 0%, transparent 50%),
                    radial-gradient(circle at 80% 20%, rgba(255, 140, 0, 0.15) 0%, transparent 50%),
                    radial-gradient(circle at 40% 40%, rgba(255, 69, 0, 0.08) 0%, transparent 50%);
            animation: float 20s ease-in-out infinite;
        }
        @keyframes float {
            0%, 100% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-20px) rotate(180deg); }
        }
        .container {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            border-radius: 24px;
            border: 1px solid rgba(255, 165, 0, 0.2);
            box-shadow:
                    0 32px 64px rgba(0, 0, 0, 0.3),
                    inset 0 1px 0 rgba(255, 165, 0, 0.1),
                    0 0 0 1px rgba(255, 255, 255, 0.1);
            padding: 48px;
            width: 100%;
            max-width: 480px;
            position: relative;
            z-index: 1;
            animation: slideIn 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
        }
        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(50px) scale(0.95);
            }
            to {
                opacity: 1;
                transform: translateY(0) scale(1);
            }
        }
        .logo {
            text-align: center;
            margin-bottom: 40px;
        }
        .logo h1 {
            background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 50%, #ffa726 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            font-size: 3.5rem;
            font-weight: 800;
            margin-bottom: 8px;
            letter-spacing: -2px;
        }
        .logo p {
            color: #666;
            font-size: 1.1rem;
            font-weight: 500;
        }
        .form-tabs {
            display: flex;
            margin-bottom: 32px;
            background: #f5f5f5;
            border-radius: 16px;
            padding: 6px;
            border: 1px solid #e0e0e0;
        }
        .tab-button {
            flex: 1;
            padding: 14px 20px;
            border: none;
            background: transparent;
            cursor: pointer;
            border-radius: 12px;
            font-weight: 600;
            font-size: 14px;
            color: #777;
            transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
            position: relative;
        }
        .tab-button.active {
            background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
            color: white;
            box-shadow:
                    0 8px 32px rgba(255, 107, 53, 0.4),
                    inset 0 1px 0 rgba(255, 255, 255, 0.2);
            transform: translateY(-1px);
        }
        .form-container {
            display: none;
            animation: fadeIn 0.5s ease-out;
        }
        .form-container.active {
            display: block;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .form-group {
            margin-bottom: 24px;
        }
        .form-row {
            display: flex;
            gap: 16px;
        }
        .form-row .form-group {
            flex: 1;
        }
        label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 600;
            font-size: 14px;
        }
        input, textarea, select {
            width: 100%;
            padding: 16px 20px;
            border: 2px solid #e0e0e0;
            border-radius: 12px;
            font-size: 16px;
            transition: all 0.3s ease;
            background: #fafafa;
            color: #333;
        }
        input::placeholder, textarea::placeholder {
            color: #999;
        }
        input:focus, textarea:focus, select:focus {
            outline: none;
            border-color: #ff6b35;
            background: white;
            box-shadow: 0 0 0 4px rgba(255, 107, 53, 0.1);
            transform: translateY(-2px);
        }
        .file-input-wrapper {
            position: relative;
            display: inline-block;
            width: 100%;
        }
        .file-input {
            position: absolute;
            opacity: 0;
            width: 100%;
            height: 100%;
            cursor: pointer;
        }
        .file-input-label {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            padding: 16px 20px;
            border: 2px dashed #ccc;
            border-radius: 12px;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s ease;
            background: #fafafa;
            color: #666;
            font-weight: 500;
        }
        .file-input-label:hover {
            border-color: #ff6b35;
            background: #fff5f2;
            color: #ff6b35;
            transform: translateY(-2px);
        }
        .btn {
            width: 100%;
            padding: 18px 24px;
            border: none;
            border-radius: 12px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
            margin-top: 16px;
            position: relative;
            overflow: hidden;
        }
        .btn-primary {
            background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
            color: white;
            box-shadow: 0 10px 30px rgba(255, 107, 53, 0.3);
        }
        .btn-primary:hover {
            transform: translateY(-3px);
            box-shadow: 0 20px 40px rgba(255, 107, 53, 0.4);
        }
        .btn-primary:active {
            transform: translateY(-1px);
        }
        .alert {
            padding: 16px 20px;
            border-radius: 12px;
            margin-bottom: 24px;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .alert-success {
            background: rgba(34, 197, 94, 0.1);
            color: #16a34a;
            border: 1px solid rgba(34, 197, 94, 0.2);
        }
        .alert-error {
            background: rgba(239, 68, 68, 0.1);
            color: #dc2626;
            border: 1px solid rgba(239, 68, 68, 0.2);
        }
        .email-instruction {
            background: #f8f9fa;
            border-radius: 12px;
            padding: 20px;
            margin-top: 20px;
            border: 1px solid #e9ecef;
        }
        .email-instruction h4 {
            color: #333;
            margin-bottom: 12px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .email-instruction p {
            color: #666;
            line-height: 1.5;
        }
        @media (max-width: 480px) {
            .container {
                margin: 20px;
                padding: 32px 24px;
            }
            .logo h1 {
                font-size: 2.8rem;
            }
            .form-row {
                flex-direction: column;
                gap: 0;
            }
        }
    </style>
</head>
<body>
<div class="container">
    <div class="logo">
        <h1>Zust</h1>
        <p>Connect, Share, Discover</p>
    </div>

    <!-- Navigation Tabs -->
    <div class="form-tabs">
        <button class="tab-button ${activeTab == 'login' || empty activeTab ? 'active' : ''}" onclick="showTab('login')">Login</button>
        <button class="tab-button ${activeTab == 'register' ? 'active' : ''}" onclick="showTab('register')">Register</button>
    </div>

    <!-- Login Form -->
    <div id="login" class="form-container ${activeTab == 'login' || empty activeTab ? 'active' : ''}">
        <form action="${pageContext.request.contextPath}/auth" method="post">
            <input type="hidden" name="action" value="login">
            <c:if test="${not empty successMessage}">
                <div class="alert alert-success">
                    <span>✅</span>
                    <span><c:out value="${successMessage}"/></span>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage && (activeTab == 'login' || empty activeTab)}">
                <div class="alert alert-error">
                    <span>❌</span>
                    <span><c:out value="${errorMessage}"/></span>
                </div>
            </c:if>

            <div class="form-group">
                <label for="loginUsername">Username or Email</label>
                <input type="text" id="loginUsername" name="username" placeholder="Enter your username or email" value="<c:out value='${username}'/>" required>
            </div>

            <div class="form-group">
                <label for="loginPassword">Password</label>
                <input type="password" id="loginPassword" name="password" placeholder="Enter your password" required>
            </div>

            <button type="submit" class="btn btn-primary">Sign In to Zust</button>
        </form>
    </div>

    <!-- Registration Form -->
    <div id="register" class="form-container ${activeTab == 'register' ? 'active' : ''}">
        <form action="${pageContext.request.contextPath}/auth" method="post" enctype="multipart/form-data">
            <input type="hidden" name="action" value="register">
            <c:if test="${not empty errorMessage && activeTab == 'register'}">
                <div class="alert alert-error">
                    <span>❌</span>
                    <span><c:out value="${errorMessage}"/></span>
                </div>
            </c:if>

            <div class="form-group">
                <label for="username">Username *</label>
                <input type="text" id="username" name="username" placeholder="Choose a unique username" value="<c:out value='${account.username}'/>" required>
            </div>

            <div class="form-group">
                <label for="password">Password *</label>
                <input type="password" id="password" name="password" placeholder="Create a strong password" required>
            </div>

            <div class="form-group">
                <label for="fullname">Full Name *</label>
                <input type="text" id="fullname" name="fullname" placeholder="Enter your full name" value="<c:out value='${account.fullname}'/>" required>
            </div>

            <div class="form-group">
                <label for="email">Email Address *</label>
                <input type="email" id="email" name="email" placeholder="Enter your email address" value="<c:out value='${account.email}'/>" required>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="phone">Phone Number</label>
                    <input type="tel" id="phone" name="phone" placeholder="Your phone number" value="<c:out value='${account.phone}'/>">
                </div>
                <div class="form-group">
                    <label for="gender">Gender</label>
                    <select id="gender" name="gender">
                        <option value="" <c:if test="${empty account.gender}">selected</c:if>>Select Gender</option>
                        <option value="true" <c:if test="${account.gender == true}">selected</c:if>>Male</option>
                        <option value="false" <c:if test="${account.gender == false}">selected</c:if>>Female</option>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label for="dob">Date of Birth</label>
                <input type="date" id="dob" name="dob" value="<c:out value='${account.dob}'/>">
            </div>

            <div class="form-group">
                <label for="avatar">Profile Picture</label>
                <div class="file-input-wrapper">
                    <input type="file" id="avatar" name="avatar" class="file-input" accept="image/*">
                    <label for="avatar" class="file-input-label">
                        <span>📷</span>
                        <span>Choose your profile picture</span>
                    </label>
                </div>
            </div>

            <div class="form-group">
                <label for="bio">Bio</label>
                <textarea id="bio" name="bio" rows="3" placeholder="Tell the Zust community about yourself..."><c:out value="${account.bio}"/></textarea>
            </div>

            <button type="submit" class="btn btn-primary">Join Zust Community</button>

            <div class="email-instruction">
                <h4>📧 Email Verification</h4>
                <p>After registration, we'll send you a verification email with a link. Click the link to activate your account and start connecting with friends on Zust!</p>
            </div>
        </form>
    </div>
</div>

<script>
    function showTab(tabName) {
        const containers = document.querySelectorAll('.form-container');
        containers.forEach(container => {
            container.classList.remove('active');
            container.style.display = 'none';
        });

        const tabs = document.querySelectorAll('.tab-button');
        tabs.forEach(tab => {
            tab.classList.remove('active');
        });

        document.getElementById(tabName).classList.add('active');
        document.getElementById(tabName).style.display = 'block';

        const tabButtons = document.querySelectorAll('.tab-button');
        tabButtons.forEach(tab => {
            if ((tabName === 'login' && tab.textContent === 'Login') ||
                (tabName === 'register' && tab.textContent === 'Register')) {
                tab.classList.add('active');
            }
        });
    }

    document.getElementById('avatar').addEventListener('change', function(e) {
        const label = document.querySelector('.file-input-label');
        if (e.target.files.length > 0) {
            label.innerHTML = `<span>✅</span><span>${e.target.files[0].name}</span>`;
        } else {
            label.innerHTML = `<span>📷</span><span>Choose your profile picture</span>`;
        }
    });

    const urlParams = new URLSearchParams(window.location.search);
    const successMessage = urlParams.get('successMessage');
    if (successMessage) {
        document.getElementById('login').classList.add('active');
        document.getElementById('login').style.display = 'block';
        document.querySelector('.tab-button[onclick="showTab(\'login\')]').classList.add('active');
        document.getElementById('register').classList.remove('active');
        document.getElementById('register').style.display = 'none';
    }
</script>
</body>
</html>
