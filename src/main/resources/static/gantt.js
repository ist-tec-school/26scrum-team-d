document.addEventListener("DOMContentLoaded", function() {
    const cells = document.querySelectorAll("[data-date]");

    cells.forEach(cell => {
        const dateStr = cell.getAttribute("data-date");
        if (!dateStr) return;

        const date = new Date(dateStr);
        const day = date.getDay();

        if (day === 6) {
            cell.classList.add("weekend-sat");
        } else if (day === 0) {
            cell.classList.add("weekend-sun");
        }
    });
});