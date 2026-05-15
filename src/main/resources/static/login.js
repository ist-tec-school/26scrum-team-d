document.addEventListener('DOMContentLoaded', function() {
    const passwordInput = document.getElementById('password');
    const toggleButton = document.getElementById('togglePassword');

    if (toggleButton && passwordInput) {
        toggleButton.addEventListener('click', function() {
            // type属性を切り替える
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);

            // 目のアイコンを切り替える
            this.querySelector('.material-symbols-outlined').textContent = (type === 'password') ? 'visibility' : 'visibility_off';
        });
    }
});