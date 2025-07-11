document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('changePasswordForm');
    const currentPasswordInput = document.getElementById('current-password');
    const newPasswordInput = document.getElementById('new-password');
    const confirmPasswordInput = document.getElementById('confirm-password');
    const messageContainer = document.getElementById('message-container');

    form.addEventListener('submit', (event) => {
        // Clear previous client-side messages
        messageContainer.innerHTML = '';
        messageContainer.className = '';

        // Get the values from the input fields
        const currentPassword = currentPasswordInput.value;
        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        // Check if the new passwords match
        if (newPassword !== confirmPassword) {
            event.preventDefault();
            messageContainer.innerHTML = 'Error: New passwords do not match.';
            messageContainer.className = 'message-error';
            return;
        }

        // Check for password strength (at least 8 chars, 1 uppercase, 1 lowercase, 1 number)
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{8,}$/;
        if (!passwordRegex.test(newPassword)) {
            event.preventDefault();
            messageContainer.innerHTML = 'Error: New password does not meet the requirements.';
            messageContainer.className = 'message-error';
            return;
        }
    });
});