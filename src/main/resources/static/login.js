document.addEventListener('DOMContentLoaded', function() {
    const passwordInput = document.getElementById('password');
    const toggleButton = document.getElementById('togglePassword');

    if (toggleButton && passwordInput) {
        toggleButton.addEventListener('click', function() {
            // type属性を切り替える
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);

            // ボタンの文字を切り替える
            this.textContent = type === 'password' ? '表示' : '非表示';
        });
    }
});