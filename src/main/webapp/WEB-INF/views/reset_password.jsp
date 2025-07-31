<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Password - Zust</title>
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

        .form-group {
            margin-bottom: 24px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 600;
            font-size: 14px;
        }

        input {
            width: 100%;
            padding: 16px 20px;
            border: 2px solid #e0e0e0;
            border-radius: 12px;
            font-size: 16px;
            transition: all 0.3s ease;
            background: #fafafa;
            color: #333;
        }

        input::placeholder {
            color: #999;
        }

        input:focus {
            outline: none;
            border-color: #ff6b35;
            background: white;
            box-shadow: 0 0 0 4px rgba(255, 107, 53, 0.1);
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

        .password-requirements {
            background: #f8f9fa;
            border-radius: 12px;
            padding: 16px;
            margin-top: 16px;
            border: 1px solid #e9ecef;
        }

        .password-requirements h4 {
            color: #333;
            margin-bottom: 8px;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .password-requirements ul {
            color: #666;
            font-size: 12px;
            line-height: 1.4;
            margin: 0;
            padding-left: 20px;
        }

        .password-requirements li {
            margin-bottom: 4px;
        }

        @media (max-width: 480px) {
            .container {
                margin: 20px;
                padding: 32px 24px;
            }

            .logo h1 {
                font-size: 2.8rem;
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
        <p>Reset Your Password</p>
    </div>

    <div style="text-align: center; margin-bottom: 24px;">
        <h3 style="color: #333; margin-bottom: 8px;">🔐 Create New Password</h3>
        <p style="color: #666; font-size: 14px;">Enter your new password below to complete the reset process.</p>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error">
            <span>❌</span>
            <span><c:out value="${errorMessage}"/></span>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/auth" method="post" onsubmit="showLoading(this)">
        <input type="hidden" name="action" value="reset-password">
        <input type="hidden" name="token" value="<c:out value='${token}'/>">

        <div class="form-group">
            <label for="newPassword">New Password</label>
            <input type="password" id="newPassword" name="newPassword" placeholder="Enter your new password" required>
        </div>

        <div class="form-group">
            <label for="confirmPassword">Confirm Password</label>
            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm your new password" required>
        </div>

        <div class="password-requirements">
            <h4>🔒 Password Requirements</h4>
            <ul>
                <li>At least 6 characters long</li>
                <li>Use a combination of letters, numbers, and symbols for better security</li>
                <li>Make sure both passwords match</li>
            </ul>
        </div>

        <button type="submit" class="btn btn-primary">
            <span class="btn-icon">🔐</span>
            Reset Password
        </button>
    </form>

    <div class="back-link">
        <a href="${pageContext.request.contextPath}/auth">
            ← Back to Login
        </a>
    </div>
</div>

<script>
    // Loading state for form submission
    function showLoading(form) {
        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.classList.add('loading');

        // Prevent multiple submissions
        setTimeout(() => {
            submitBtn.disabled = true;
        }, 100);
    }

    // Password confirmation validation
    document.getElementById('confirmPassword').addEventListener('input', function() {
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = this.value;
        const submitBtn = document.querySelector('button[type="submit"]');

        if (newPassword !== confirmPassword) {
            this.style.borderColor = '#dc2626';
            submitBtn.disabled = true;
        } else {
            this.style.borderColor = '#16a34a';
            submitBtn.disabled = false;
        }
    });

    // Enhanced button interactions
    document.querySelectorAll('.btn-primary').forEach(btn => {
        btn.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px) scale(1.02)';
        });

        btn.addEventListener('mouseleave', function() {
            if (!this.classList.contains('loading')) {
                this.style.transform = 'translateY(0) scale(1)';
            }
        });
    });

    // Ripple effect for buttons
    document.querySelectorAll('.btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
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