<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- LOGIN/REGISTER PAGE -->

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
            background: radial-gradient(circle at 20% 80%, rgba(255, 165, 0, 0.1) 0%, transparent 50%),
            radial-gradient(circle at 80% 20%, rgba(255, 140, 0, 0.15) 0%, transparent 50%),
            radial-gradient(circle at 40% 40%, rgba(255, 69, 0, 0.08) 0%, transparent 50%);
        }

        .container {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            border-radius: 24px;
            border: 1px solid rgba(255, 165, 0, 0.2);
            box-shadow: 0 32px 64px rgba(0, 0, 0, 0.3),
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
            box-shadow: 0 8px 32px rgba(255, 107, 53, 0.4),
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
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
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
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .btn-primary {
            background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 50%, #ffa726 100%);
            color: white;
            box-shadow: 0 10px 30px rgba(255, 107, 53, 0.3),
            inset 0 1px 0 rgba(255, 255, 255, 0.2);
            border: 1px solid rgba(255, 107, 53, 0.3);
            position: relative;
        }

        .btn-primary::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
            transition: left 0.5s;
        }

        .btn-primary:hover::before {
            left: 100%;
        }

        .btn-primary:hover {
            transform: translateY(-3px);
            box-shadow: 0 20px 40px rgba(255, 107, 53, 0.4),
            inset 0 1px 0 rgba(255, 255, 255, 0.3);
            background: linear-gradient(135deg, #ff8c42 0%, #ffa726 50%, #ffb74d 100%);
        }

        .btn-primary:active {
            transform: translateY(-1px);
            box-shadow: 0 5px 15px rgba(255, 107, 53, 0.5);
        }

        .btn-google {
            background: white;
            color: #333;
            border: 2px solid #e0e0e0;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            margin-bottom: 16px;
            position: relative;
            overflow: hidden;
        }

        .btn-google::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(66, 133, 244, 0.1), transparent);
            transition: left 0.5s;
        }

        .btn-google:hover::before {
            left: 100%;
        }

        .btn-google:hover {
            background: #f8f9fa;
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
            border-color: #4285f4;
        }

        .btn-icon {
            font-size: 18px;
        }

        .google-icon {
            width: 20px;
            height: 20px;
        }

        .divider {
            display: flex;
            align-items: center;
            margin: 24px 0;
            color: #666;
        }

        .divider::before,
        .divider::after {
            content: '';
            flex: 1;
            height: 1px;
            background: #e0e0e0;
        }

        .divider span {
            padding: 0 16px;
            font-size: 14px;
            font-weight: 500;
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

        /* Forgot Password Link */
        .forgot-password-link {
            text-align: center;
            margin-top: 16px;
        }

        .forgot-password-link a {
            color: #ff6b35;
            text-decoration: none;
            font-weight: 500;
            font-size: 14px;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }

        .forgot-password-link a:hover {
            color: #ff8c42;
            text-decoration: underline;
            transform: translateY(-1px);
        }

        /* Back link styling */
        .back-link {
            text-align: center;
            margin-top: 20px;
        }

        .back-link a {
            color: #666;
            text-decoration: none;
            font-size: 14px;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            transition: all 0.3s ease;
            padding: 8px 16px;
            border-radius: 8px;
        }

        .back-link a:hover {
            color: #ff6b35;
            background: rgba(255, 107, 53, 0.1);
            transform: translateY(-1px);
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

            .tab-button {
                padding: 12px 16px;
                font-size: 13px;
            }
        }

        /* Loading state for buttons */
        .btn.loading {
            pointer-events: none;
            opacity: 0.7;
        }

        .btn.loading::after {
            content: '';
            position: absolute;
            width: 16px;
            height: 16px;
            margin: auto;
            border: 2px solid transparent;
            border-top-color: currentColor;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            0% {
                transform: rotate(0deg);
            }
            100% {
                transform: rotate(360deg);
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
        <button class="tab-button ${activeTab == 'login' || empty activeTab ? 'active' : ''}"
                onclick="showTab('login')">Login
        </button>
        <button class="tab-button ${activeTab == 'register' ? 'active' : ''}" onclick="showTab('register')">
            Register
        </button>
        <button class="tab-button ${activeTab == 'forgot' ? 'active' : ''}" onclick="showTab('forgot')">
            Reset
        </button>
    </div>

    <!-- Login Form -->
    <div id="login" class="form-container ${activeTab == 'login' || empty activeTab ? 'active' : ''}">
        <c:if test="${not empty successMessage && (activeTab == 'login' || empty activeTab)}">
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
        <c:if test="${not empty param.error && (activeTab == 'login' || empty activeTab)}">
            <div class="alert alert-error">
                <span>❌</span>
                <span><c:out value="${param.error}"/></span>
            </div>
        </c:if>

        <!-- Google Login Button -->
        <a href="${pageContext.request.contextPath}/auth/google?state=login" class="btn btn-google">
            <svg class="google-icon" viewBox="0 0 24 24">
                <path fill="#4285F4"
                      d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"></path>
                <path fill="#34A853"
                      d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"></path>
                <path fill="#FBBC05"
                      d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"></path>
                <path fill="#EA4335"
                      d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"></path>
            </svg>
<%--            <span class="btn-icon">🔐</span>--%>
            Continue with Google
        </a>

        <div class="divider">
            <span>or</span>
        </div>

        <form action="${pageContext.request.contextPath}/auth" method="post" onsubmit="showLoading(this)">
            <input type="hidden" name="action" value="login">

            <div class="form-group">
                <label for="loginUsername">Username or Email</label>
                <input type="text" id="loginUsername" name="username" placeholder="Enter your username or email"
                       value="<c:out value='${username}'/>" required>
            </div>

            <div class="form-group">
                <label for="loginPassword">Password</label>
                <input type="password" id="loginPassword" name="password" placeholder="Enter your password"
                       required>
            </div>

            <div id="example-container" data-theme="light"></div>

            <!-- Forgot Password Link -->
            <div class="forgot-password-link">
                <a href="#" onclick="showTab('forgot'); return false;">
                    🔑 Forgot your password?
                </a>
            </div>

            <button id="login-button" type="submit" class="btn btn-primary" disabled>
                <span class="btn-icon">🔐</span>
                Sign In to Zust
            </button>
        </form>


    </div>

    <!-- Registration Form -->
    <div id="register" class="form-container ${activeTab == 'register' ? 'active' : ''}">
        <c:if test="${not empty errorMessage && activeTab == 'register'}">
            <div class="alert alert-error">
                <span>❌</span>
                <span><c:out value="${errorMessage}"/></span>
            </div>
        </c:if>
        <c:if test="${not empty param.error && activeTab == 'register'}">
            <div class="alert alert-error">
                <span>❌</span>
                <span><c:out value="${param.error}"/></span>
            </div>
        </c:if>

        <!-- Google Register Button -->
        <a href="${pageContext.request.contextPath}/auth/google?state=register" class="btn btn-google">
            <svg class="google-icon" viewBox="0 0 24 24">
                <path fill="#4285F4"
                      d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"></path>
                <path fill="#34A853"
                      d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"></path>
                <path fill="#FBBC05"
                      d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"></path>
                <path fill="#EA4335"
                      d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"></path>
            </svg>
<%--            <span class="btn-icon">🚀</span>--%>
            Continue with Google
        </a>

        <div class="divider">
            <span>or</span>
        </div>

        <form action="${pageContext.request.contextPath}/auth" method="post" enctype="multipart/form-data"
              onsubmit="showLoading(this)">
            <input type="hidden" name="action" value="register">

            <div class="form-group">
                <label for="username">Username *</label>
                <input type="text" id="username" name="username" placeholder="Choose a unique username"
                       value="<c:out value='${account.username}'/>" required>
            </div>

            <div class="form-group">
                <label for="password">Password *</label>
                <input type="password" id="password" name="password" placeholder="Create a strong password"
                       required>
            </div>

            <div class="form-group">
                <label for="fullname">Full Name *</label>
                <input type="text" id="fullname" name="fullname" placeholder="Enter your full name"
                       value="<c:out value='${account.fullname}'/>" required>
            </div>

            <div class="form-group">
                <label for="email">Email Address *</label>
                <input type="email" id="email" name="email" placeholder="Enter your email address"
                       value="<c:out value='${account.email}'/>" required>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="phone">Phone Number</label>
                    <input type="tel" id="phone" name="phone" placeholder="Enter your phone number"
                           value="<c:out value='${account.phone}'/>">
                </div>
                <div class="form-group">
                    <label for="gender">Gender</label>
                    <select id="gender" name="gender">
                        <option value="">Select Gender</option>
                        <option value="true" ${account.gender == true ? 'selected' : ''}>Male</option>
                        <option value="false" ${account.gender == false ? 'selected' : ''}>Female</option>
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
                        <span>Choose profile picture</span>
                    </label>
                </div>
            </div>

            <div class="form-group">
                <label for="bio">Bio</label>
                <textarea id="bio" name="bio" placeholder="Tell us about yourself..." rows="3"><c:out
                        value='${account.bio}'/></textarea>
            </div>

            <button type="submit" class="btn btn-primary">
                <span class="btn-icon">🚀</span>
                Create Your Zust Account
            </button>
        </form>

        <div class="email-instruction">
            <h4>📧 Email Verification Required</h4>
            <p>After registration, you'll receive a verification email. Please check your inbox and click the
                verification link to activate your account.</p>
        </div>
    </div>

    <!-- Forgot Password Form -->
    <div id="forgot" class="form-container ${activeTab == 'forgot' ? 'active' : ''}">
        <c:if test="${not empty successMessage && activeTab == 'forgot'}">
            <div class="alert alert-success">
                <span>✅</span>
                <span><c:out value="${successMessage}"/></span>
            </div>
        </c:if>
        <c:if test="${not empty errorMessage && activeTab == 'forgot'}">
            <div class="alert alert-error">
                <span>❌</span>
                <span><c:out value="${errorMessage}"/></span>
            </div>
        </c:if>

        <div style="text-align: center; margin-bottom: 24px;">
            <h3 style="color: #333; margin-bottom: 8px;">🔑 Reset Your Password</h3>
            <p style="color: #666; font-size: 14px;">Enter your email address and we'll send you a link to reset your password.</p>
        </div>

        <form action="${pageContext.request.contextPath}/auth" method="post" onsubmit="showLoading(this)">
            <input type="hidden" name="action" value="forgot-password">

            <div class="form-group">
                <label for="forgotEmail">Email Address</label>
                <input type="email" id="forgotEmail" name="email" placeholder="Enter your email address"
                       value="<c:out value='${email}'/>" required>
            </div>

            <button type="submit" class="btn btn-primary">
                <span class="btn-icon">📧</span>
                Send Reset Link
            </button>
        </form>

        <div class="email-instruction">
            <h4>📬 What happens next?</h4>
            <p>If an account with this email exists, you'll receive a password reset link within a few minutes. Check your spam folder if you don't see it in your inbox.</p>
        </div>

        <div class="back-link">
            <a href="#" onclick="showTab('login'); return false;">
                ← Back to Login
            </a>
        </div>
    </div>
</div>

<!-- Cloudflare turnstile setting -->
<script src="https://challenges.cloudflare.com/turnstile/v0/api.js?onload=onloadTurnstileCallback"
        defer></script>
<script>
    let login = document.getElementById("login-button");

    window.onloadTurnstileCallback = function () {
        turnstile.render("#example-container", {
            sitekey: "0x4AAAAAABmlszOBwLdGR_n1",
            callback: function (token) {
                console.log(`Challenge Success ` + token);
                setTimeout(() => {
                    login.removeAttribute("disabled");
                })
            },
        });
    };
</script>
<script>
    function showTab(tabName) {
        // Hide all containers
        const containers = document.querySelectorAll('.form-container');
        containers.forEach(container => {
            container.classList.remove('active');
        });

        // Remove active class from all buttons
        const buttons = document.querySelectorAll('.tab-button');
        buttons.forEach(button => {
            button.classList.remove('active');
        });

        // Show selected container and activate button
        document.getElementById(tabName).classList.add('active');

        // Find and activate the corresponding tab button
        const targetButton = Array.from(buttons).find(btn =>
            btn.textContent.trim().toLowerCase().includes(tabName.toLowerCase()) ||
            (tabName === 'forgot' && btn.textContent.trim().toLowerCase() === 'reset')
        );
        if (targetButton) {
            targetButton.classList.add('active');
        }
    }

    // File input label update
    document.getElementById('avatar').addEventListener('change', function (e) {
        const label = document.querySelector('.file-input-label span:last-child');
        if (e.target.files.length > 0) {
            label.textContent = e.target.files[0].name;
        } else {
            label.textContent = 'Choose profile picture';
        }
    });

    // Loading state for form submission
    function showLoading(form) {
        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.classList.add('loading');

        // Prevent multiple submissions
        setTimeout(() => {
            submitBtn.disabled = true;
        }, 100);
    }

    // Enhanced button interactions
    document.querySelectorAll('.btn-primary').forEach(btn => {
        btn.addEventListener('mouseenter', function () {
            this.style.transform = 'translateY(-3px) scale(1.02)';
        });

        btn.addEventListener('mouseleave', function () {
            if (!this.classList.contains('loading')) {
                this.style.transform = 'translateY(0) scale(1)';
            }
        });
    });

    // Ripple effect for buttons
    document.querySelectorAll('.btn').forEach(btn => {
        btn.addEventListener('click', function (e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;

            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255, 255, 255, 0.3);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s ease-out;
                pointer-events: none;
            `;

            this.appendChild(ripple);

            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });

    // Add ripple animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes ripple {
            to {
                transform: scale(2);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);
</script>
</body>
</html>