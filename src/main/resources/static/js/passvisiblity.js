const togglePasswordVisibilityBtns = document.querySelectorAll('.togglePassword');
    console.log(togglePasswordVisibilityBtns);
    togglePasswordVisibilityBtns.forEach(passVisiblityBtn => {
    passVisiblityBtn.addEventListener("click", (event) => {
        const password = passVisiblityBtn.previousElementSibling;
        const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
        password.setAttribute('type', type);
        event.target.classList.toggle('ph-eye-slash');
    });
});