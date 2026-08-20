const API_BASE_URL = 'http://localhost:8081';

const loginForm = document.getElementById('loginForm');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const submitBtn = document.getElementById('submitBtn');
const errorAlert = document.getElementById('errorAlert');
const errorMessage = document.getElementById('errorMessage');
const successAlert = document.getElementById('successAlert');
const successMessage = document.getElementById('successMessage');
const togglePasswordBtn = document.getElementById('togglePassword');
const eyeIcon = document.getElementById('eyeIcon');

// Check URL params for messages (e.g. from signup or session expired)
window.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('registered') === 'true') {
        showSuccess('Account created successfully! Please sign in.');
    } else if (urlParams.get('expired') === 'true') {
        showError('Your session has expired. Please sign in again.');
    } else if (urlParams.get('logged_out') === 'true') {
        showSuccess('You have been logged out.');
    }
});

// Toggle password visibility
togglePasswordBtn.addEventListener('click', () => {
    const isPassword = passwordInput.type === 'password';
    passwordInput.type = isPassword ? 'text' : 'password';

    if (isPassword) {
        // Eye-off icon
        eyeIcon.innerHTML = `
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
            <line x1="1" y1="1" x2="23" y2="23"></line>
        `;
    } else {
        // Eye icon
        eyeIcon.innerHTML = `
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
        `;
    }
});

function showError(msg) {
    errorMessage.textContent = msg;
    errorAlert.classList.add('visible');
    successAlert.classList.remove('visible');
}

function showSuccess(msg) {
    successMessage.textContent = msg;
    successAlert.classList.add('visible');
    errorAlert.classList.remove('visible');
}

function hideAlerts() {
    errorAlert.classList.remove('visible');
    successAlert.classList.remove('visible');
}

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlerts();

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    if (!email || !password) {
        showError('Please enter both email and password.');
        return;
    }

    submitBtn.disabled = true;
    submitBtn.classList.add('loading');

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password }),
            // Critical: tells browser to receive and store the HttpOnly cookie
            credentials: 'include'
        });

        if (response.ok) {
            showSuccess('Login successful! Redirecting...');
            setTimeout(() => {
                window.location.href = '../homePage/home.html';
            }, 600);
        } else if (response.status === 401 || response.status === 400 || response.status === 403) {
            showError('Invalid email or password.');
        } else {
            const errorText = await response.text();
            showError(errorText || 'Authentication failed. Please check your credentials.');
        }
    } catch (err) {
        console.error('Login error:', err);
        showError('Unable to connect to the authentication server. Ensure backend is running.');
    } finally {
        submitBtn.disabled = false;
        submitBtn.classList.remove('loading');
    }
});
