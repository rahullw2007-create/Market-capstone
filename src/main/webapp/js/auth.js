/* Rahul Mart Auth JS */

function quickFill(email, password = 'Demo1234!') {
    const emailInput = document.getElementById('email');
    const passInput = document.getElementById('password');
    if (emailInput) emailInput.value = email;
    if (passInput) passInput.value = password;
}

async function handleLoginSubmit(event) {
    event.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        const res = await apiCall('/account/login', 'POST', { email, password });
        showToast('Login successful!', 'success');
        const user = res.data;

        setTimeout(() => {
            if (user.role === 'ADMINISTRATOR') {
                window.location.href = 'admin-dashboard.html';
            } else if (user.role === 'VENDOR') {
                window.location.href = 'vendor-dashboard.html';
            } else {
                window.location.href = 'index.html';
            }
        }, 600);
    } catch (e) {
        showToast(e.message || 'Invalid login credentials', 'error');
    }
}

async function handleRegisterSubmit(event) {
    event.preventDefault();
    const fullName = document.getElementById('fullName').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const role = document.getElementById('role').value;

    if (role === 'ADMINISTRATOR') {
        showToast('Administrator registration is prohibited', 'error');
        return;
    }

    try {
        const res = await apiCall('/account/register', 'POST', { fullName, email, password, role });
        showToast('Registration successful!', 'success');
        const user = res.data;

        setTimeout(() => {
            if (user.role === 'VENDOR') {
                window.location.href = 'vendor-dashboard.html';
            } else {
                window.location.href = 'index.html';
            }
        }, 600);
    } catch (e) {
        showToast(e.message || 'Registration failed', 'error');
    }
}
