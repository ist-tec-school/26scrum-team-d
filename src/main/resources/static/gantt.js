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

function changeTimeScale(scale) {
    document.getElementById('timeScaleInput').value = scale;
    document.getElementById('mainFilterForm').submit();
}

document.addEventListener("DOMContentLoaded", function() {
    const activeBtn = document.querySelector(".time-scale .btn.active");
    let timeScale = "day";
    if (activeBtn) {
        if (activeBtn.textContent.trim() === "週") timeScale = "week";
        if (activeBtn.textContent.trim() === "月") timeScale = "month";
    }

    const wrapper = document.querySelector(".gantt-wrapper");

    const stickyCols = document.querySelectorAll("thead th.sticky-col");
    let stickyWidth = 0;
    stickyCols.forEach(col => stickyWidth += col.offsetWidth);

    const availableWidth = (wrapper ? wrapper.clientWidth : 1400) - stickyWidth - 20;

    let cellWidth = 40; // デフォルト値
    if (timeScale === "day") {
        cellWidth = Math.max(35, Math.floor(availableWidth / 15));
    } else if (timeScale === "week") {
        cellWidth = Math.max(60, Math.floor(availableWidth / 10));
    } else if (timeScale === "month") {
        cellWidth = Math.max(100, Math.floor(availableWidth / 5));
    }

    const timelineHeaders = document.querySelectorAll("th.timeline-header");
    timelineHeaders.forEach(th => {
        // セルの横幅を設定
        th.style.minWidth = `${cellWidth}px`;
        th.style.maxWidth = `${cellWidth}px`;
        th.style.width = `${cellWidth}px`;

        const dateStr = th.getAttribute("data-date");
        if (!dateStr) return;

        if (timeScale === "month") {
            th.textContent = dateStr.replace("-", "/");
        } else {
            const date = new Date(dateStr);
            if (!isNaN(date.getTime())) {
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const dayNum = String(date.getDate()).padStart(2, '0');
                th.textContent = `${month}/${dayNum}`;

                const dayOfWeek = date.getDay();
                if (dayOfWeek === 6) th.classList.add("weekend-sat");
                if (dayOfWeek === 0) th.classList.add("weekend-sun");
            }
        }
    });
    const chartCells = document.querySelectorAll("td.chart-cell");
    chartCells.forEach(td => {
        td.style.minWidth = `${cellWidth}px`;
        td.style.maxWidth = `${cellWidth}px`;
        td.style.width = `${cellWidth}px`;

        const dateStr = td.getAttribute("data-date");
        if (!dateStr || timeScale === "month") return;

        const date = new Date(dateStr);
        if (!isNaN(date.getTime())) {
            const dayOfWeek = date.getDay();
            if (dayOfWeek === 6) td.classList.add("weekend-sat");
            if (dayOfWeek === 0) td.classList.add("weekend-sun");
        }
    });
    const displayDates = document.querySelectorAll("[data-display-date]");
    displayDates.forEach(el => {
        const rawDate = el.getAttribute("data-display-date");
        if (rawDate && rawDate.includes("-")) {
            el.textContent = rawDate.replace(/-/g, "/");
        }
    });
});