/**
 * Fetches notifications from the server and updates the UI.
 */
function loadNotification() {
    fetch("/zust/notification")
        .then(res => {
            if (res.ok) {
                return res.json();
            }
            throw new Error(`Server returned status: ${res.status}`);
        })
        .then(data => {
            generateNotifications(data);
        })
        .catch(error => {
            console.error('Failed to load notifications:', error);
            const notificationList = document.getElementById('notification-list');
            if (notificationList) {
                notificationList.innerHTML = '<div class="notification-item"><div class="notification-content"><div class="notification-body">Failed to load notifications.</div></div></div>';
            }
        });
}

/**
 * Generates the HTML for notifications and inserts it into the DOM.
 * @param {Array<Object>} notifications - An array of notification objects from the server.
 */
function generateNotifications(notifications) {
    const notificationList = document.getElementById('notification-list');
    const notificationBadge = document.getElementById('notification-badge');
    const markAllReadBtn = document.getElementById('mark-all-read-btn');

    if (!notificationList || !notificationBadge) {
        console.error('Notification elements not found in the DOM.');
        return;
    }

    notificationList.innerHTML = ''; // Clear previous notifications

    if (!notifications || notifications.length === 0) {
        const noNotificationMessage = `
            <div class="notification-item">
                <div class="notification-content">
                    <div class="notification-body">You have no new notifications.</div>
                </div>
            </div>
        `;
        notificationList.innerHTML = noNotificationMessage;
        notificationBadge.classList.add('hidden');
        markAllReadBtn.style.display = 'none';
        return;
    }

    let unreadCount = 0;
    notifications.forEach(notification => {
        const isUnread = notification.status !== 'read';
        if (isUnread) {
            unreadCount++;
        }

        const item = document.createElement('div');
        item.className = 'notification-item';
        item.dataset.notificationId = notification.id;
        if (isUnread) {
            item.classList.add('unread');
        }

        const avatarSrc = notification.senderAvatar.startsWith('http')
            ? notification.senderAvatar
            : `/zust/static/images/${notification.senderAvatar}`;

        const timestamp = timeAgo(notification.createDate);

        item.innerHTML = `
            <img src="${avatarSrc}" class="avatar" alt="User Avatar">
            <div class="notification-content">
                ${notification.title ? `<div class="notification-title">${notification.title}</div>` : ''}
                <div class="notification-body"><b>${notification.sender}</b> ${notification.content}</div>
                <div class="notification-timestamp">${timestamp}</div>
            </div>
        `;

        item.addEventListener('click', () => {
            handleNotificationClick(notification, isUnread);
        });

        notificationList.appendChild(item);
    });

    if (unreadCount > 0) {
        notificationBadge.textContent = unreadCount;
        notificationBadge.classList.remove('hidden');
        markAllReadBtn.style.display = 'block';
    } else {
        notificationBadge.classList.add('hidden');
        markAllReadBtn.style.display = 'none';
    }
}

/**
 * Handles clicking on a notification.
 * Marks it as read if unread, and then redirects if a link is present.
 * @param {Object} notification - The notification object.
 * @param {boolean} isUnread - Whether the notification was unread.
 */
function handleNotificationClick(notification, isUnread) {
    const userId = getUserIdFromProfileLink();
    if (!userId) {
        console.error("Could not find user ID to mark notification as read.");
        if (notification.link) window.location.href = notification.link;
        return;
    }

    if (isUnread) {
        markNotificationAsRead(notification.id, userId).finally(() => {
            if (notification.link) {
                window.location.href = notification.link;
            }
        });
    } else {
        if (notification.link) {
            window.location.href = notification.link;
        }
    }
}

/**
 * Marks a single notification as read.
 * @param {number} notificationId - The ID of the notification.
 * @param {number} userId - The ID of the current user.
 * @returns {Promise<void>}
 */
async function markNotificationAsRead(notificationId, userId) {
    try {
        const response = await fetch(`/zust/notification?action=mark&id=${notificationId}&userID=${userId}`, {
            method: "POST"
        });
        if (!response.ok) {
            throw new Error(`Server responded with status ${response.status}`);
        }
        console.log(`Notification ${notificationId} marked as read.`);
        // Visually update the item without a full reload
        const item = document.querySelector(`.notification-item[data-notification-id='${notificationId}']`);
        if (item) {
            item.classList.remove('unread');
        }
        // Update the badge count
        const badge = document.getElementById('notification-badge');
        let count = parseInt(badge.textContent) - 1;
        badge.textContent = count;
        badge.classList.toggle('hidden', count <= 0);
    } catch (error) {
        console.error("Failed to mark notification as read:", error);
    }
}

/**
 * Marks all notifications as read for the current user.
 * @param {number} userId - The ID of the current user.
 */
async function markAllNotificationsAsRead(userId) {
    try {
        const response = await fetch(`/zust/notification?action=mark&userID=${userId}`, {
            method: "POST"
        });
        if (!response.ok) {
            throw new Error(`Server responded with status ${response.status}`);
        }
        console.log("All notifications marked as read.");
        // Reload notifications to show the updated state
        loadNotification();
    } catch (error) {
        console.error("Failed to mark all notifications as read:", error);
    }
}

/**
 * Extracts the user ID from the 'My Profile' link.
 * @returns {string|null} The user ID or null if not found.
 */
function getUserIdFromProfileLink() {
    try {
        const profileLink = document.querySelector('a[href*="/profile?userId="]');
        const url = new URL(profileLink.href);
        return url.searchParams.get("userId");
    } catch (e) {
        console.error("Could not extract user ID from profile link.", e);
        return null;
    }
}

/**
 * Converts an ISO 8601 date string to a user-friendly "time ago" format.
 * @param {string} dateString - The ISO date string.
 * @returns {string} - The formatted time ago string.
 */
function timeAgo(dateString) {
    const now = new Date();
    const past = new Date(dateString);
    const seconds = Math.floor((now - past) / 1000);

    let interval = seconds / 31536000;
    if (interval > 1) return Math.floor(interval) + "y ago";

    interval = seconds / 2592000;
    if (interval > 1) return Math.floor(interval) + "mo ago";

    interval = seconds / 86400;
    if (interval > 1) return Math.floor(interval) + "d ago";

    interval = seconds / 3600;
    if (interval > 1) return Math.floor(interval) + "h ago";

    interval = seconds / 60;
    if (interval > 1) return Math.floor(interval) + "m ago";

    return Math.floor(seconds) + "s ago";
}

// --- Initialize Notification Logic ---
document.addEventListener('DOMContentLoaded', () => {
    const notificationBtn = document.getElementById('notification-btn');
    const markAllReadBtn = document.getElementById('mark-all-read-btn');

    // Load notifications when the page is ready
    loadNotification();

    if (notificationBtn) {
        notificationBtn.addEventListener('click', (e) => {
            // This just toggles the dropdown, doesn't need to reload.
            const dropdown = document.getElementById('notification-dropdown');
            dropdown.classList.toggle('active');
            e.stopPropagation();
        });
    }

    if (markAllReadBtn) {
        markAllReadBtn.addEventListener('click', () => {
            const userId = getUserIdFromProfileLink();
            if (userId) {
                markAllNotificationsAsRead(userId);
            }
        });
    }

    // Close dropdown if clicking outside
    window.addEventListener('click', (e) => {
        const dropdown = document.getElementById('notification-dropdown');
        if (dropdown.classList.contains('active') && !e.target.closest('.notification-container')) {
            dropdown.classList.remove('active');
        }
    });

    // Short polling: reload notifications every 30 seconds
    setInterval(loadNotification, 30000);
});