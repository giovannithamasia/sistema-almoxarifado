/**
 * JavaScript - Tela de Login
 * Validações front-end e interações
 * 
 * FE-02: Validação de campos vazios antes do submit
 * Toggle: Mostrar/ocultar senha
 */

document.addEventListener('DOMContentLoaded', function() {
    const togglePassword = document.getElementById('togglePassword');
    const senhaInput = document.getElementById('senha');
    const loginForm = document.getElementById('loginForm');
    const loginInput = document.getElementById('login');
    const submitBtn = document.getElementById('submitBtn');

    // Toggle: Alternar entre password e text
    togglePassword.addEventListener('click', function() {
        const type = senhaInput.getAttribute('type') === 'password' ? 'text' : 'password';
        senhaInput.setAttribute('type', type);

        // Atualiza cor do ícone (CSS: .active muda fill para #f59e0b)
        togglePassword.classList.toggle('active');

        // Atualiza title para acessibilidade
        const title = type === 'password' ? 'Mostrar senha' : 'Ocultar senha';
        togglePassword.setAttribute('title', title);
    });

    // FE-02: Validação de campos vazios antes do submit
    loginForm.addEventListener('submit', function(e) {
        let hasError = false;

        // Remove estados de erro anteriores
        loginInput.classList.remove('error');
        senhaInput.classList.remove('error');

        // Verifica se campos estão vazios
        if (!loginInput.value.trim()) {
            loginInput.classList.add('error');
            hasError = true;
        }

        if (!senhaInput.value.trim()) {
            senhaInput.classList.add('error');
            hasError = true;
        }

        // Se houver erro, impede submit e mostra mensagem
        if (hasError) {
            e.preventDefault();

            // Cria ou atualiza div de erro
            let errorDiv = document.querySelector('.error-message');
            if (!errorDiv) {
                errorDiv = document.createElement('div');
                errorDiv.className = 'error-message';
                errorDiv.innerHTML = `
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M8 0C3.6 0 0 3.6 0 8C0 12.4 3.6 16 8 16C12.4 16 16 12.4 16 8C16 3.6 12.4 0 8 0ZM7 4H9V9H7V4ZM7 11H9V13H7V11Z" fill="#f87171"/>
                    </svg>
                    <span>Usuário e senha são obrigatórios.</span>
                `;
                loginForm.insertBefore(errorDiv, loginForm.firstChild);
            } else {
                const errorSpan = errorDiv.querySelector('span');
                errorSpan.textContent = 'Usuário e senha são obrigatórios.';
            }
        }
    });

    // Remove estado de erro quando usuário começa a digitar
    loginInput.addEventListener('input', function() {
        if (this.value.trim()) {
            this.classList.remove('error');
        }
    });

    senhaInput.addEventListener('input', function() {
        if (this.value.trim()) {
            this.classList.remove('error');
        }
    });
});
