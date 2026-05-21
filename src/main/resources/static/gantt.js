
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
    const availableWidth = (wrapper ? wrapper.clientWidth : 1400) - 590 -1;

    let cellWidth = 40;
    if (timeScale === "day") {
        cellWidth = Math.max(35, Math.floor(availableWidth / 15));
    } else if (timeScale === "week") {
        cellWidth = Math.max(65, Math.floor(availableWidth / 10));
    } else if (timeScale === "month") {
        cellWidth = Math.max(120, Math.floor(availableWidth / 5));
    }

    const timelineHeaders = document.querySelectorAll("th.timeline-header");

    const tableEl = document.querySelector(".gantt-table");
    if (tableEl && timelineHeaders.length>0) {
        tableEl.style.width = `${590 + (timelineHeaders.length * cellWidth)}px`;
    }
    const todayObj = new Date();
    todayObj.setHours(0, 0, 0, 0);
    const todayTime = todayObj.getTime();

    const startDateInputTop = document.querySelector('input[name="startDate"]');
    const baseDateStr = (startDateInputTop && startDateInputTop.value) ? startDateInputTop.value : `${todayObj.getFullYear()}-${String(todayObj.getMonth() + 1).padStart(2, '0')}-${String(todayObj.getDate()).padStart(2, '0')}`;

    const baseDateObj = new Date(baseDateStr);
    baseDateObj.setHours(0, 0, 0, 0);
    const baseDateTime = baseDateObj.getTime();

    let targetHeaderEl = null; // スクロール先となるターゲット要素

    timelineHeaders.forEach(th => {
        th.style.minWidth = `${cellWidth}px`;
        th.style.maxWidth = `${cellWidth}px`;
        th.style.width = `${cellWidth}px`;

        const dateStr = th.getAttribute("data-date");
        if (!dateStr) return;

        const dateSpan = th.querySelector("span");

        if (timeScale === "month") {
            const parts = dateStr.split("-");
            if (parts.length >= 2) {
                if (dateSpan) dateSpan.textContent = `${parseInt(parts[1], 10)}月`;
                else th.textContent = `${parseInt(parts[1], 10)}月`;
            }
            const targetYearMonth = baseDateStr.substring(0, 7);
            if (dateStr.substring(0, 7) === targetYearMonth) {
                targetHeaderEl = th;
            }
            const realTodayYearMonth = `${todayObj.getFullYear()}-${String(todayObj.getMonth() + 1).padStart(2, '0')}`;
            if (dateStr.substring(0, 7) === realTodayYearMonth) {
                th.classList.add('today-header');
                if (!th.querySelector('.today-label')) {
                    const lbl = document.createElement('div');
                    lbl.classList.add('today-label');
                    lbl.textContent = '当月';
                    th.appendChild(lbl);
                }
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

                if (dateSpan) dateSpan.textContent = `${sMonth}/${sDate}~${eMonth}/${eDate}`;
                else th.textContent = `${sMonth}/${sDate}~${eMonth}/${eDate}`;

                startDate.setHours(0, 0, 0, 0);
                endDate.setHours(23, 59, 59, 999);

                if (baseDateTime >= startDate.getTime() && baseDateTime <= endDate.getTime()) {
                    targetHeaderEl = th;
                }
                if (todayTime >= startDate.getTime() && todayTime <= endDate.getTime()) {
                    th.classList.add('today-header');
                    if (!th.querySelector('.today-label')) {
                        const lbl = document.createElement('div');
                        lbl.classList.add('today-label');
                        lbl.textContent = '当週';
                        th.appendChild(lbl);
                    }
                }
            }
        } else {
            const date = new Date(dateStr);
            if (!isNaN(date.getTime())) {
                if (dateSpan) dateSpan.textContent = `${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')}`;
                else th.textContent = `${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')}`;

                const day = date.getDay();
                if (day === 6) th.classList.add("weekend-sat");
                if (day === 0) th.classList.add("weekend-sun");

                // 💡 日表示の判定：基準日と完全に一致するセルをスクロール対象にする
                date.setHours(0, 0, 0, 0);
                if (date.getTime() === baseDateTime) {
                    th.classList.add('today-header');
                    targetHeaderEl = th;
                }
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
            } else if (timeScale === "week") {
                const cellDate = new Date(cellDateStr);
                if (!isNaN(cellDate.getTime())) {
                    const end = new Date(cellDate);
                    end.setDate(cellDate.getDate() + 6);
                    const weekEndStr = end.toISOString().split('T')[0];
                    isInPeriod = (startDateStr <= weekEndStr && deadlineStr >= cellDateStr);
                    isDeadlineDay = (deadlineStr >= cellDateStr && deadlineStr <= weekEndStr);
                }
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
                if (td.parentElement.classList.contains("row-delayed")) bar.classList.add("bar-delayed");
                else if (td.parentElement.classList.contains("row-urgent")) bar.classList.add("bar-urgent");
                td.appendChild(bar);

                if (isDeadlineDay) {
                    const txtSpan = document.createElement("span");
                    txtSpan.classList.add("chart-status-text");
                    if (doneStr === "0") txtSpan.textContent = "未着手";
                    else if (doneStr === "1") txtSpan.textContent = "対応中";
                    else if (doneStr === "3") txtSpan.textContent = "完了";

                    bar.appendChild(txtSpan);
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

    const edateCells = document.querySelectorAll("td.col-edate");
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    edateCells.forEach(td => {
        const container = td.querySelector(".edate-container");
        if (!container) return;
        const oldBadge = container.querySelector(".days-badge");
        if (oldBadge) oldBadge.remove();

        const deadlineStr = td.getAttribute("data-deadline");
        const doneStr = td.getAttribute("data-done");

        if (!deadlineStr || doneStr === "3") return;

        const deadlineDate = new Date(deadlineStr);
        deadlineDate.setHours(0, 0, 0, 0);

        const diffTime = deadlineDate.getTime() - today.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        const badge = document.createElement("div");
        badge.classList.add("days-badge");

        if (diffDays < 0) {
            badge.innerHTML = `＋<span class="badge-num">${Math.abs(diffDays)}</span>日`;
            badge.classList.add("badge-delay");
            container.appendChild(badge);
        } else if (diffDays <= 3) {
            badge.innerHTML = `残<span class="badge-num">${diffDays}</span>日`;
            badge.classList.add("badge-near");
            container.appendChild(badge);
        }
    });
    // 本日の日付までスクロールする処理
    setTimeout(() => {
        const wrapperEl = document.querySelector('.gantt-wrapper');
        const lastStickyCol = document.querySelector('thead th.sticky-col-5');

        if (targetHeaderEl && wrapperEl) {
            const currentStickyWidth = lastStickyCol ? (lastStickyCol.offsetLeft + lastStickyCol.offsetWidth) : 590;
            // 指定した開始日（または今日）が固定列のすぐ右隣にジャストで表示されます
            wrapperEl.scrollLeft = targetHeaderEl.offsetLeft - currentStickyWidth;
        }
    }, 100);

    // 担当者の折りたたみ状態を更新する関数
    const refreshUserRowStatus = (row) => {
        const userCell = row.querySelector('.user-cell');
        if (!userCell) return;

        const users = userCell.querySelectorAll('div:not(.more-btn)');
        if (users && users.length >= 3) {
            userCell.classList.add('has-overflow');
        } else {
            userCell.classList.remove('has-overflow');
            userCell.classList.remove('is-expanded');
        }
    };
// 担当者セルのクリックイベント
    const userRows = document.querySelectorAll('tr'); // テーブルの全行を取得
    userRows.forEach(row => {
        const userCell = row.querySelector('.user-cell');
        if (!userCell) return;

        userCell.addEventListener('click', () => {
            if (userCell.classList.contains('has-overflow')) {
                userCell.classList.toggle('is-expanded');
            }
        });

        // 初期読み込み時に3人以上いるか判定をかける
        setTimeout(() => refreshUserRowStatus(row), 200);
    });
    const overlay = document.getElementById("gantt-loading-overlay");
    if (overlay) {
        overlay.classList.add("fade-out");
        setTimeout(() => {
            overlay.remove();
        }, 200);
    }
});
// 💡 リサイズ時の表示開始日のズレを防ぐ
window.addEventListener("resize", () => {
    const wrapper = document.querySelector(".gantt-wrapper");
    const tableEl = document.querySelector(".gantt-table");
    const timelineHeaders = document.querySelectorAll("th.timeline-header");
    const chartCells = document.querySelectorAll("td.chart-cell");
    const lastStickyCol = document.querySelector('thead th.sticky-col-5');
    const targetHeaderEl = document.querySelector('.timeline-header.today-header'); // 基準日セルを取得

    if (!wrapper || !tableEl || timelineHeaders.length === 0) return;

    // 1. 横幅の再計算
    const activeBtn = document.querySelector(".time-scale .btn.active");
    let timeScale = "day";
    if (activeBtn) {
        const scaleAttr = activeBtn.getAttribute("data-scale");
        if (scaleAttr) timeScale = scaleAttr;
    }

    const availableWidth = wrapper.clientWidth - 590 - 1;
    let cellWidth = 40;
    if (timeScale === "day") {
        cellWidth = Math.max(35, Math.floor(availableWidth / 15));
    } else if (timeScale === "week") {
        cellWidth = Math.max(65, Math.floor(availableWidth / 10));
    } else if (timeScale === "month") {
        cellWidth = Math.max(120, Math.floor(availableWidth / 5));
    }

    // 2. テーブルと各セルの幅をリサイズに追従させる
    tableEl.style.width = `${590 + (timelineHeaders.length * cellWidth)}px`;
    timelineHeaders.forEach(th => {
        th.style.minWidth = `${cellWidth}px`; th.style.maxWidth = `${cellWidth}px`; th.style.width = `${cellWidth}px`;
    });
    chartCells.forEach(td => {
        td.style.minWidth = `${cellWidth}px`; td.style.maxWidth = `${cellWidth}px`; td.style.width = `${cellWidth}px`;
    });

    // 3. 💡 タイマーを使わず、新しい幅に合わせて「即時」スクロール位置をジャストに追従させる
    if (targetHeaderEl) {
        const currentStickyWidth = lastStickyCol ? (lastStickyCol.offsetLeft + lastStickyCol.offsetWidth) : 590;
        wrapper.scrollLeft = targetHeaderEl.offsetLeft - currentStickyWidth;
    }
});