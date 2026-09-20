/**
 * Shared Authentication State & Navigation Manager
 * Handles JWT cookie sessions, navbar authentication widgets, and protected route guards.
 */

(function () {
    const API_BASE_URL = 'http://localhost:8081';

    // Global state
    const AuthState = {
        API_BASE_URL,
        user: null,
        isLoaded: false,

        /**
         * Resolves relative path to auth/home pages regardless of which subdirectory the current page is in.
         */
        resolvePath(target) {
            const path = window.location.pathname.replace(/\\/g, '/');
            const isInSubdir = path.includes('/homePage/') ||
                               path.includes('/historyPage/') ||
                               path.includes('/applicationsPage/') ||
                               path.includes('/authPage/');

            switch (target) {
                case 'login':
                    return isInSubdir ? '../authPage/login.html' : 'authPage/login.html';
                case 'signup':
                    return isInSubdir ? '../authPage/signup.html' : 'authPage/signup.html';
                case 'home':
                    return isInSubdir ? '../homePage/home.html' : 'homePage/home.html';
                case 'analyzer':
                    return isInSubdir ? '../homePage/analyzer.html' : 'homePage/analyzer.html';
                case 'history':
                    return isInSubdir ? '../historyPage/history.html' : 'historyPage/history.html';
                case 'applications':
                    return isInSubdir ? '../applicationsPage/applications.html' : 'applicationsPage/applications.html';
                default:
                    return target;
            }
        },

        /**
         * Checks current session with backend GET /api/auth/me
         */
        async checkAuth() {
            try {
                const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
                    method: 'GET',
                    credentials: 'include'
                });

                if (response.ok) {
                    const data = await response.json();
                    this.user = { email: data.email };
                } else {
                    this.user = null;
                }
            } catch (err) {
                console.warn('Auth check skipped / backend unreachable:', err);
                this.user = null;
            } finally {
                this.isLoaded = true;
                this.updateNavUI();
            }
            return this.user;
        },

        /**
         * Performs user login
         */
        async login(email, password) {
            const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                let errorMsg = 'Invalid email or password';
                let fieldErrors = {};
                try {
                    const data = await response.json();
                    errorMsg = data.message || errorMsg;
                    fieldErrors = data.fieldErrors || {};
                } catch {
                    const text = await response.text();
                    if (text && text.length < 120) errorMsg = text;
                }
                const error = new Error(errorMsg);
                error.status = response.status;
                error.fieldErrors = fieldErrors;
                throw error;
            }

            // Successfully authenticated, refresh user object
            await this.checkAuth();
            return this.user;
        },

        /**
         * Performs user registration
         */
        async register(email, password) {
            const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                let errorMsg = 'Registration failed';
                let fieldErrors = {};
                try {
                    const data = await response.json();
                    errorMsg = data.message || errorMsg;
                    fieldErrors = data.fieldErrors || {};
                } catch {
                    const text = await response.text();
                    if (text && text.length < 120) errorMsg = text;
                }
                const error = new Error(errorMsg);
                error.status = response.status;
                error.fieldErrors = fieldErrors;
                throw error;
            }

            return true;
        },

        /**
         * Performs logout
         */
        async logout() {
            try {
                await fetch(`${API_BASE_URL}/api/auth/logout`, {
                    method: 'POST',
                    credentials: 'include'
                });
            } catch (err) {
                console.error('Logout error:', err);
            }

            this.user = null;
            // Redirect to home or reload
            window.location.href = this.resolvePath('home');
        },

        /**
         * Dynamically updates the navbar to show user profile or login/signup buttons
         */
        updateNavUI() {
            // Check if there is a custom container or target the nav-actions
            let authContainer = document.getElementById('navAuthContainer');
            const navActions = document.querySelector('.nav-actions');

            if (!authContainer && navActions) {
                authContainer = document.createElement('div');
                authContainer.id = 'navAuthContainer';
                authContainer.className = 'nav-auth-container';
                navActions.prepend(authContainer);
            }

            if (!authContainer) return;

            if (this.user && this.user.email) {
                const safeEmail = this.escapeHtml(this.user.email);
                authContainer.innerHTML = `
                    <div class="user-nav-profile">
                        <span class="user-email-pill" title="Logged in as ${safeEmail}">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                            <span class="user-email-text">${safeEmail}</span>
                        </span>
                        <button type="button" class="nav-logout-btn" id="navLogoutBtn" title="Log out from your account">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                                <polyline points="16 17 21 12 16 7"></polyline>
                                <line x1="21" y1="12" x2="9" y2="12"></line>
                            </svg>
                            <span>Sign Out</span>
                        </button>
                    </div>
                `;

                const logoutBtn = document.getElementById('navLogoutBtn');
                if (logoutBtn) {
                    logoutBtn.addEventListener('click', () => this.logout());
                }
            } else {
                const loginUrl = this.resolvePath('login');
                const signupUrl = this.resolvePath('signup');

                authContainer.innerHTML = `
                    <div class="auth-nav-group">
                        <a href="${loginUrl}" class="auth-nav-link">Sign In</a>
                        <a href="${signupUrl}" class="auth-nav-signup-btn">
                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                            <span>Sign Up</span>
                        </a>
                    </div>
                `;
            }
        },

        /**
         * Guard for protected pages (e.g., analyzer, history, applications)
         */
        async enforceAuth(pageName = 'this page') {
            await this.checkAuth();
            if (!this.user) {
                this.showAuthModal(pageName);
                return false;
            }
            return true;
        },

        /**
         * Renders the access required modal
         */
        showAuthModal(pageName = 'this feature') {
            const existingModal = document.getElementById('authGuardModal');
            if (existingModal) return;

            const currentUrl = encodeURIComponent(window.location.href);
            const loginUrl = `${this.resolvePath('login')}?redirect=${currentUrl}`;
            const signupUrl = `${this.resolvePath('signup')}?redirect=${currentUrl}`;
            const homeUrl = this.resolvePath('home');

            const overlay = document.createElement('div');
            overlay.id = 'authGuardModal';
            overlay.className = 'auth-guard-overlay';
            overlay.innerHTML = `
                <div class="auth-guard-modal" role="dialog" aria-modal="true">
                    <div class="auth-guard-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                        </svg>
                    </div>
                    <h2 class="auth-guard-title">Sign In Required</h2>
                    <p class="auth-guard-desc">
                        Authentication is required to access ${pageName}. Please sign in to your account or create a new account to continue.
                    </p>
                    <div class="auth-guard-actions">
                        <a href="${loginUrl}" class="auth-guard-btn-primary">
                            <span>Sign In to Continue</span>
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="5" y1="12" x2="19" y2="12"></line>
                                <polyline points="12 5 19 12 12 19"></polyline>
                            </svg>
                        </a>
                        <a href="${signupUrl}" class="auth-guard-btn-secondary">
                            Don't have an account? Sign Up
                        </a>
                        <a href="${homeUrl}" class="auth-guard-btn-secondary" style="border: none; margin-top: 4px;">
                            Back to Home
                        </a>
                    </div>
                </div>
            `;

            document.body.appendChild(overlay);
        },

        escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text || '';
            return div.innerHTML;
        }
    };

    // Expose globally
    window.AuthState = AuthState;

    // Automatically check auth and initialize UI when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => AuthState.checkAuth());
    } else {
        AuthState.checkAuth();
    }
})();
