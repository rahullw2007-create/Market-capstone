/* Rahul Mart Floating Chatbot Widget */

function toggleChatWidget() {
    const modal = document.getElementById('chat-modal');
    if (modal) {
        modal.classList.toggle('open');
        if (modal.classList.contains('open')) {
            document.getElementById('chat-input').focus();
        }
    }
}

async function sendChatMessage() {
    const input = document.getElementById('chat-input');
    const body = document.getElementById('chat-body');
    if (!input || !body) return;

    const message = input.value.trim();
    if (!message) return;

    // Render User Message
    const userDiv = document.createElement('div');
    userDiv.className = 'chat-msg user';
    userDiv.textContent = message;
    body.appendChild(userDiv);
    input.value = '';
    body.scrollTop = body.scrollHeight;

    // Render Bot Loading
    const botDiv = document.createElement('div');
    botDiv.className = 'chat-msg bot';
    botDiv.textContent = 'Thinking...';
    body.appendChild(botDiv);
    body.scrollTop = body.scrollHeight;

    try {
        const res = await apiCall('/chat', 'POST', { message });
        botDiv.textContent = res.data.reply;
    } catch (e) {
        botDiv.textContent = 'Sorry, RahulBot is currently offline. Please try again later.';
    }
    body.scrollTop = body.scrollHeight;
}

document.addEventListener('DOMContentLoaded', () => {
    const chatInput = document.getElementById('chat-input');
    if (chatInput) {
        chatInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                sendChatMessage();
            }
        });
    }
});
