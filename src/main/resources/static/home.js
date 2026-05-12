/**
 * 共通設定: Choices.js（複数選択セレクトボックス）
 */
const commonOptions = {
    searchEnabled: true,
    removeItemButton: true,
};

let updateChoice = null;

/**
 * 更新ダイアログ表示
 */
function showUpdateDialog(button) {
    const row = button.closest('tr');
    const dialog = document.getElementById('updateDialog');
    const id = row.cells[0].innerText;
    const projectId = row.cells[1].dataset.projectId;
    const task = row.cells[2].innerText;
    const userIds = JSON.parse(row.cells[3].dataset.userIds || "[]");
    const deadline = row.cells[4].innerText;
    const description = row.cells[5].innerText;

    document.getElementById('update_id').value = id;
    document.getElementById('update_task').value = task;
    document.getElementById('update_deadline').value = deadline;
    document.getElementById('update_description').value = description;

    const projectSelect = document.getElementById('update_project');
    if (projectSelect) projectSelect.value = projectId || '';

    if (updateChoice) {
        updateChoice.removeActiveItems();
        updateChoice.setChoiceByValue(userIds.map(id => id.toString()));
    }

    const statusMap = {'未着手': 0, '対応中': 1, '完了': 3};
    const statusText = row.cells[6].innerText.trim();
    document.getElementById('update_status').value = statusMap[statusText] ?? 0;

    dialog.style.left = ((window.innerWidth - 500) / 2) + 'px';
    dialog.style.display = 'block';
}

function closeUpdateDialog() {
    document.getElementById('updateDialog').style.display = 'none';
}

function handleProjectChange(selectElement, hiddenInputId) {
    const selectedValue = selectElement.value;
    const hiddenInput = document.getElementById(hiddenInputId);
    if (selectedValue === '0') {
        const newProjectName = prompt("新しいプロジェクト名を入力してください");
        if (newProjectName && newProjectName.trim() !== "") {
            hiddenInput.value = newProjectName.trim();
            const oldTemp = selectElement.querySelector('.temp-option');
            if (oldTemp) oldTemp.remove();
            const newOption = new Option(newProjectName.trim(), "0");
            selectElement.add(newOption, selectElement.options[2]);
            newOption.selected = true;
        } else {
            selectElement.value = "";
        }
    }
}

function syncSectionFilter() {
    const deptSelect = document.querySelector('select[name="deptId"]');
    const sectionSelect = document.querySelector('select[name="sectionId"]');
    if (!deptSelect || !sectionSelect) return;
    const selectedDeptId = deptSelect.value;
    const options = sectionSelect.options;
    for (let i = 0; i < options.length; i++) {
        const opt = options[i];
        const parentDeptId = opt.getAttribute('data-dept');
        if (opt.value === 'all' || selectedDeptId === 'all' || parentDeptId === selectedDeptId) {
            opt.style.display = 'block';
            opt.disabled = false;
        } else {
            opt.style.display = 'none';
            opt.disabled = true;
        }
    }
}

function setupToggle(selector, instance) {
    const el = document.querySelector(selector);
    if (!el) return;
    const container = el.closest('.choices');
    const hitBox = document.createElement('div');
    hitBox.className = 'toggle-hit-box';
    container.appendChild(hitBox);
    hitBox.addEventListener('click', (e) => {
        e.stopPropagation();
        container.classList.contains('is-open') ? instance.hideDropdown() : instance.showDropdown();
    });
}

/**
 * 全ての初期化を一つのDOMContentLoadedにまとめる
 */
document.addEventListener('DOMContentLoaded', () => {

    // 1. Choices.js 初期化
    const addChoice = new Choices('#add_task_user', commonOptions);
    setupToggle('#add_task_user', addChoice);

    const updateEl = document.getElementById('update_user');
    if (updateEl) {
        updateChoice = new Choices(updateEl, commonOptions);
        setupToggle('#update_user', updateChoice);
    }

    // 2. フィルタ連動
    syncSectionFilter();
    const deptSelect = document.querySelector('select[name="deptId"]');
    if (deptSelect) deptSelect.addEventListener('change', syncSectionFilter);

    // 3. エラー時の処理
    const isUpdateError = document.getElementById('isUpdateError');
    if (isUpdateError && isUpdateError.value === 'true') {
        const dialog = document.getElementById('updateDialog');
        if (dialog) {
            dialog.style.display = 'block';
            setTimeout(() => {
                const target = document.getElementById('task-list-section');
                if (target) target.scrollIntoView({ behavior: 'auto', block: 'start' });
            }, 100);
        }
    }

    // 4. 【本題】説明列の判定（ここを一つに統合）
    const descCells = document.querySelectorAll('.tasklist td:nth-child(6)');

    // CSSの適用を待つために少し遅延させる
    setTimeout(() => {
        descCells.forEach(cell => {
            // 判定ロジック
            const descText = cell.querySelector('.desc-text');
            if (!descText) return;

            if (descText.scrollWidth > descText.clientWidth + 1) {
                cell.classList.add('has-overflow');
            } else {
                cell.classList.remove('has-overflow');
            }

            // クリックイベント設定
            cell.addEventListener('click', () => {
                if (cell.classList.contains('has-overflow') || cell.classList.contains('is-expanded')) {
                    cell.classList.toggle('is-expanded');
                }
            });
        });
    }, 200); // 100msだと不安なので200msに調整
});