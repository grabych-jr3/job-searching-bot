const API_BASE_URL = 'http://localhost:8081';

const authActions = document.getElementById('authActions');
const guestActions = document.getElementById('guestActions');
const userEmailEl = document.getElementById('userEmail');
const logoutBtn = document.getElementById('logoutBtn');

// Check authentication status to show appropriate nav actions and CTAs
async function checkAuthStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();
            if (userEmailEl && data.email) {
                userEmailEl.textContent = data.email;
            }
            if (authActions) authActions.style.display = 'flex';
            if (guestActions) guestActions.style.display = 'none';
        } else {
            if (authActions) authActions.style.display = 'none';
            if (guestActions) guestActions.style.display = 'flex';
        }
    } catch (error) {
        // Backend not reachable or guest
        console.warn('Auth status check failed (guest mode):', error);
        if (authActions) authActions.style.display = 'none';
        if (guestActions) guestActions.style.display = 'flex';
    }
}

if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
        try {
            await fetch(`${API_BASE_URL}/api/auth/logout`, {
                method: 'POST',
                credentials: 'include'
            });
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            window.location.href = '../auth/login.html?logged_out=true';
        }
    });
}

checkAuthStatus();

