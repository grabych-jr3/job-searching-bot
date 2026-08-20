const API_BASE_URL = 'http://localhost:8081';

const signupForm = document.getElementById('signupForm');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const submitBtn = document.getElementById('submitBtn');
const errorAlert = document.getElementById('errorAlert');
const errorMessage = document.getElementById('errorMessage');
const successAlert = document.getElementById('successAlert');
const successMessage = document.getElementById('successMessage');
const togglePasswordBtn = document.getElementById('togglePassword');
const eyeIcon = document.getElementById('eyeIcon');

// Toggle password visibility
togglePasswordBtn.addEventListener('click', () => {
    const isPassword = passwordInput.type === 'password';
    passwordInput.type = isPassword ? 'text' : 'password';
    confirmPasswordInput.type = isPassword ? 'text' : 'password';

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

signupForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlerts();

    const email = emailInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    if (!email || !password || !confirmPassword) {
        showError('Please fill in all fields.');
        return;
    }

    if (password.length < 6) {
        showError('Password must be at least 6 characters long.');
        return;
    }

    if (password !== confirmPassword) {
        showError('Passwords do not match.');
        return;
    }

    submitBtn.disabled = true;
    submitBtn.classList.add('loading');

    try {
        // Step 1: Register user
        const registerResponse = await fetch(`${API_BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (!registerResponse.ok) {
            const errorText = await registerResponse.text();
            throw new Error(errorText || 'Registration failed. Email may already be in use.');
        }

        // Step 2: Automatically log in to set the HttpOnly cookie
        showSuccess('Account created! Logging you in...');
        
        try {
            const loginResponse = await fetch(`${API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password }),
                credentials: 'include'
            });

            if (loginResponse.ok) {
                setTimeout(() => {
                    window.location.href = '../homePage/home.html';
                }, 600);
                return;
            }
        } catch (autoLoginErr) {
            console.warn('Auto-login failed after registration, redirecting to login page', autoLoginErr);
        }

        // Fallback if auto-login didn't redirect
        setTimeout(() => {
            window.location.href = 'login.html?registered=true';
        }, 800);

    } catch (err) {
        console.error('Registration error:', err);
        showError(err.message || 'Unable to register. Please try again.');
    } finally {
        submitBtn.disabled = false;
        submitBtn.classList.remove('loading');
    }
});
