/**
 * Authentication Forms Controller
 * Handles Sign In and Sign Up workflows, validation, and error feedback.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Elements
    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');
    const authAlert = document.getElementById('authAlert');
    const authAlertText = document.getElementById('authAlertText');
    const submitBtn = document.getElementById('submitBtn');

    // Input elements
    const emailInput = document.getElementById('emailInput');
    const passwordInput = document.getElementById('passwordInput');
    const confirmPasswordInput = document.getElementById('confirmPasswordInput');

    const emailGroup = document.getElementById('emailGroup');
    const passwordGroup = document.getElementById('passwordGroup');
    const confirmPasswordGroup = document.getElementById('confirmPasswordGroup');

    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    const confirmPasswordError = document.getElementById('confirmPasswordError');

    // Toggle password visibility buttons
    const togglePasswordBtn = document.getElementById('togglePasswordBtn');
    const toggleConfirmPasswordBtn = document.getElementById('toggleConfirmPasswordBtn');

    // Read redirect URL parameter
    const urlParams = new URLSearchParams(window.location.search);
    const redirectUrl = urlParams.get('redirect');

    // Forward redirect param to alternate auth link
    const signupLink = document.getElementById('signupLink');
    const loginLink = document.getElementById('loginLink');
    if (redirectUrl) {
        if (signupLink) signupLink.href = `signup.html?redirect=${encodeURIComponent(redirectUrl)}`;
        if (loginLink) loginLink.href = `login.html?redirect=${encodeURIComponent(redirectUrl)}`;
    }

    // Check if redirected after registration
    if (urlParams.get('registered') === 'true') {
        const prefillEmail = urlParams.get('email');
        if (prefillEmail && emailInput) {
            emailInput.value = prefillEmail;
        }
        showAlert('Account created successfully! You can now sign in.', 'success');
    }

    // Toggle password helper
    function setupPasswordToggle(btn, input) {
        if (!btn || !input) return;
        btn.addEventListener('click', () => {
            const isPassword = input.getAttribute('type') === 'password';
            input.setAttribute('type', isPassword ? 'text' : 'password');
            btn.innerHTML = isPassword
                ? `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                     <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                     <line x1="1" y1="1" x2="23" y2="23"></line>
                   </svg>`
                : `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                     <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                     <circle cx="12" cy="12" r="3"></circle>
                   </svg>`;
        });
    }

    setupPasswordToggle(togglePasswordBtn, passwordInput);
    setupPasswordToggle(toggleConfirmPasswordBtn, confirmPasswordInput);

    // Alert helper
    function showAlert(message, type = 'error') {
        if (!authAlert || !authAlertText) return;
        authAlertText.textContent = message;
        authAlert.className = `auth-alert show ${type}`;
    }

    function hideAlert() {
        if (!authAlert) return;
        authAlert.className = 'auth-alert';
        authAlertText.textContent = '';
    }

    // Form input validation helpers
    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(email).trim());
    }

    function clearErrors() {
        hideAlert();
        [emailGroup, passwordGroup, confirmPasswordGroup].forEach(group => {
            if (group) {
                group.classList.remove('has-error');
                const wrapper = group.querySelector('.input-wrapper');
                if (wrapper) wrapper.classList.remove('has-error');
            }
        });
    }

    function setFieldError(group, errorEl, message) {
        if (!group) return;
        group.classList.add('has-error');
        const wrapper = group.querySelector('.input-wrapper');
        if (wrapper) wrapper.classList.add('has-error');
        if (errorEl && message) {
            errorEl.textContent = message;
        }
    }

    // Real-time error removal on input typing
    if (emailInput) {
        emailInput.addEventListener('input', () => {
            emailGroup?.classList.remove('has-error');
            emailGroup?.querySelector('.input-wrapper')?.classList.remove('has-error');
        });
    }

    if (passwordInput) {
        passwordInput.addEventListener('input', () => {
            passwordGroup?.classList.remove('has-error');
            passwordGroup?.querySelector('.input-wrapper')?.classList.remove('has-error');
        });
    }

    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', () => {
            confirmPasswordGroup?.classList.remove('has-error');
            confirmPasswordGroup?.querySelector('.input-wrapper')?.classList.remove('has-error');
        });
    }

    // Success navigation target
    function getDestinationUrl() {
        if (redirectUrl) {
            try {
                // Prevent open redirects
                const url = new URL(redirectUrl, window.location.origin);
                if (url.origin === window.location.origin) {
                    return url.href;
                }
            } catch {
                return redirectUrl;
            }
        }
        return '../homePage/home.html';
    }

    // ----------------------------------------------------
    // SIGN IN HANDLER
    // ----------------------------------------------------
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearErrors();

            const email = (emailInput?.value || '').trim();
            const password = passwordInput?.value || '';

            let hasError = false;

            if (!email) {
                setFieldError(emailGroup, emailError, 'Email address is required.');
                hasError = true;
            } else if (!isValidEmail(email)) {
                setFieldError(emailGroup, emailError, 'Please enter a valid email address.');
                hasError = true;
            }

            if (!password) {
                setFieldError(passwordGroup, passwordError, 'Password is required.');
                hasError = true;
            } else if (password.length < 6 || password.length > 20) {
                setFieldError(passwordGroup, passwordError, 'Password must be between 6 and 20 characters.');
                hasError = true;
            }

            if (hasError) return;

            submitBtn.disabled = true;
            submitBtn.classList.add('is-loading');

            try {
                await window.AuthState.login(email, password);
                showAlert('Signed in successfully! Redirecting...', 'success');
                setTimeout(() => {
                    window.location.href = getDestinationUrl();
                }, 400);
            } catch (err) {
                console.error('Login error:', err);
                submitBtn.disabled = false;
                submitBtn.classList.remove('is-loading');

                if (err.fieldErrors && Object.keys(err.fieldErrors).length > 0) {
                    if (err.fieldErrors.email) setFieldError(emailGroup, emailError, err.fieldErrors.email);
                    if (err.fieldErrors.password) setFieldError(passwordGroup, passwordError, err.fieldErrors.password);
                    showAlert('Please correct the highlighted fields.', 'error');
                } else if (err.status === 401) {
                    showAlert('Invalid email or password. Please check your credentials.', 'error');
                } else {
                    showAlert(err.message || 'Login failed. Please try again later.', 'error');
                }
            }
        });
    }

    // ----------------------------------------------------
    // SIGN UP HANDLER
    // ----------------------------------------------------
    if (signupForm) {
        signupForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearErrors();

            const email = (emailInput?.value || '').trim();
            const password = passwordInput?.value || '';
            const confirmPassword = confirmPasswordInput?.value || '';

            let hasError = false;

            if (!email) {
                setFieldError(emailGroup, emailError, 'Email address is required.');
                hasError = true;
            } else if (!isValidEmail(email)) {
                setFieldError(emailGroup, emailError, 'Please enter a valid email address.');
                hasError = true;
            }

            if (!password) {
                setFieldError(passwordGroup, passwordError, 'Password is required.');
                hasError = true;
            } else if (password.length < 6 || password.length > 20) {
                setFieldError(passwordGroup, passwordError, 'Password must be between 6 and 20 characters.');
                hasError = true;
            }

            if (!confirmPassword) {
                setFieldError(confirmPasswordGroup, confirmPasswordError, 'Please repeat your password.');
                hasError = true;
            } else if (password !== confirmPassword) {
                setFieldError(confirmPasswordGroup, confirmPasswordError, 'Passwords do not match.');
                hasError = true;
            }

            if (hasError) return;

            submitBtn.disabled = true;
            submitBtn.classList.add('is-loading');

            try {
                await window.AuthState.register(email, password);

                // Auto-login after successful registration
                try {
                    await window.AuthState.login(email, password);
                    showAlert('Account created and signed in! Redirecting...', 'success');
                    setTimeout(() => {
                        window.location.href = getDestinationUrl();
                    }, 500);
                } catch {
                    // Fallback redirect to login
                    const dest = `login.html?registered=true&email=${encodeURIComponent(email)}${redirectUrl ? `&redirect=${encodeURIComponent(redirectUrl)}` : ''}`;
                    window.location.href = dest;
                }
            } catch (err) {
                console.error('Registration error:', err);
                submitBtn.disabled = false;
                submitBtn.classList.remove('is-loading');

                if (err.fieldErrors && Object.keys(err.fieldErrors).length > 0) {
                    if (err.fieldErrors.email) setFieldError(emailGroup, emailError, err.fieldErrors.email);
                    if (err.fieldErrors.password) setFieldError(passwordGroup, passwordError, err.fieldErrors.password);
                    showAlert('Please correct the highlighted fields.', 'error');
                } else if (err.status === 409 || (err.message && err.message.toLowerCase().includes('already registered'))) {
                    setFieldError(emailGroup, emailError, 'This email is already registered.');
                    showAlert('This email address is already registered. Please sign in or use another email.', 'error');
                } else {
                    showAlert(err.message || 'Registration failed. Please try again.', 'error');
                }
            }
        });
    }
});
