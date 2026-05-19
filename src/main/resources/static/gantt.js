
window.addEventListener("load", function() {
    const activeBtn = document.querySelector(".time-scale .btn.active");
    let timeScale = "day";
    if (activeBtn) {
        const scaleAttr = activeBtn.getAttribute("data-scale");
        if (scaleAttr) {
            timeScale = scaleAttr;
        } else {
            if (activeBtn.textContent.trim() === "週") timeScale = "week";
            if (activeBtn.textContent.trim() === "月") timeScale = "month";
        }
    }

    const wrapper = document.querySelector(".gantt-wrapper");
    const stickyCols = document.querySelectorAll("thead th.sticky-col");
    let stickyWidth = 0;
    stickyCols.forEach(col => stickyWidth += col.offsetWidth);
    const availableWidth = (wrapper ? wrapper.clientWidth : 1400) - 590 - 15;

    let cellWidth = 40;
    if (timeScale === "day") {
        cellWidth = Math.floor(availableWidth / 15);
    } else if (timeScale === "week") {
        cellWidth = Math.floor(availableWidth / 10);
    } else if (timeScale === "month") {
        cellWidth = Math.floor(availableWidth / 5);
    }

    const timelineHeaders = document.querySelectorAll("th.timeline-header");
    timelineHeaders.forEach(th => {
        th.style.minWidth = `${cellWidth}px`;
        th.style.maxWidth = `${cellWidth}px`;
        th.style.width = `${cellWidth}px`;

        const dateStr = th.getAttribute("data-date");
        if (!dateStr) return;

        if (timeScale === "month") {
            const parts = dateStr.split("-");
            if (parts.length >= 2) {
                th.textContent = `${parseInt(parts[1], 10)}月`;
            }
        } else if (timeScale === "week") {
            const startDate = new Date(dateStr);
            if (!isNaN(startDate.getTime())) {
                const endDate = new Date(startDate);
                endDate.setDate(startDate.getDate() + 6);

                const sMonth = startDate.getMonth() + 1;
                const sDate = startDate.getDate();
                const eMonth = endDate.getMonth() + 1;
                const eDate = endDate.getDate();
                th.textContent = `${sMonth}/${sDate}~${eMonth}/${eDate}`;
            }
        } else {
            const date = new Date(dateStr);
            if (!isNaN(date.getTime())) {
                th.textContent = `${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')}`;
                const day = date.getDay();
                if (day === 6) th.classList.add("weekend-sat");
                if (day === 0) th.classList.add("weekend-sun");
            }
        }
    });

    const chartCells = document.querySelectorAll("td.chart-cell");
    chartCells.forEach(td => {
        td.style.minWidth = `${cellWidth}px`;
        td.style.maxWidth = `${cellWidth}px`;
        td.style.width = `${cellWidth}px`;

        const cellDateStr = td.getAttribute("data-date");
        const startDateStr = td.getAttribute("data-start");
        const deadlineStr = td.getAttribute("data-deadline");
        const doneStr = td.getAttribute("data-done");

        if (!cellDateStr) return;

        if (timeScale !== "month") {
            const cellDate = new Date(cellDateStr);
            if (!isNaN(cellDate.getTime())) {
                const day = cellDate.getDay();
                if (day === 6) td.classList.add("weekend-sat");
                if (day === 0) td.classList.add("weekend-sun");
            }
        }

        if (startDateStr && deadlineStr) {
            let isInPeriod = false;
            let isDeadlineDay = false;

            if (timeScale === "month") {
                const cellMonth = cellDateStr.substring(0, 7);
                const startMonth = startDateStr.substring(0, 7);
                const deadlineMonth = deadlineStr.substring(0, 7);
                isInPeriod = (cellMonth >= startMonth && cellMonth <= deadlineMonth);
                isDeadlineDay = (cellMonth === deadlineMonth);
            } else {
                isInPeriod = (cellDateStr >= startDateStr && cellDateStr <= deadlineStr);
                isDeadlineDay = (cellDateStr === deadlineStr);
            }

            if (isInPeriod) {
                const bar = document.createElement("div");
                bar.classList.add("bar-actual");
                if (doneStr === "0") bar.classList.add("bar-todo");
                else if (doneStr === "1") bar.classList.add("bar-working");
                else if (doneStr === "3") bar.classList.add("bar-done");
                td.appendChild(bar);

                if (isDeadlineDay) {
                    const txtSpan = document.createElement("span");
                    txtSpan.classList.add("chart-status-text");
                    if (doneStr === "0") txtSpan.textContent = "未着手";
                    else if (doneStr === "1") txtSpan.textContent = "対応中";
                    else if (doneStr === "3") txtSpan.textContent = "完了";
                    td.appendChild(txtSpan);
                }
            }
        }
    });

    const displayDates = document.querySelectorAll("[data-display-date]");
    displayDates.forEach(el => {
        const rawDate = el.getAttribute("data-display-date");
        if (rawDate && rawDate.includes("-")) {
            el.textContent = rawDate.replace(/-/g, "/");
        }
    });

    const btnGroup = document.getElementById("timeScaleBtnGroup");
    const scaleInput = document.getElementById("timeScaleInput");
    const filterForm = document.getElementById("mainFilterForm");

    if (btnGroup && scaleInput && filterForm) {
        const buttons = btnGroup.querySelectorAll(".btn");
        buttons.forEach(button => {
            button.addEventListener("click", function() {
                const selectedScale = this.getAttribute("data-scale");
                if (selectedScale) {
                    scaleInput.value = selectedScale;
                    filterForm.submit();
                }
            });
        });
    }
});

