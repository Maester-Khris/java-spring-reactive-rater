var toptop = document.querySelector("#back-to-top");
window.onscroll = function () {
    if (document.body.scrollTop > 100 || document.documentElement.scrollTop > 100) {
        toptop.style.display = "block";
    } else {
        toptop.style.display = "none";
    }
};
toptop.addEventListener("click", function () {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
});