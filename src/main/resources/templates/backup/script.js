// let updatebtns = document.querySelectorAll(".btn-update");
// let updateform = document.querySelector("#skill-rater");
// let table = new DataTable('#skillsTable');
// document.getElementById('skillsTable').addEventListener('click', function (e) {
//     const button = e.target.closest('.btn-update');
//     if (button) {
//         e.preventDefault();
//         toggleUpdateBnState(button, true);
//         const formData = new FormData(updateform);
//         formData.append('skilluuid', button.previousElementSibling.getAttribute("skill-uuid"));
//         if (!button.previousElementSibling.value) {
//             formData.append('rating', 0);
//         } else {
//             formData.append('rating', button.previousElementSibling.value);
//         }
//         const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute('content');
//         const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute('content');
//         fetch('/skill-rate-update', {
//             method: 'POST',
//             body: formData,
//             headers: {
//                 [csrfHeader]: csrfToken
//             }
//         })
//             .then(res => { return res.json(); })
//             .then(data => {
//                 console.log(data);
//                 upateview(data.skill)
//                 toggleUpdateBnState(button, false);
//                 button.previousElementSibling.value = 0;
//             });
//     }
// });