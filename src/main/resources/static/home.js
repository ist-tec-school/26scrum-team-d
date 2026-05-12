/**
 * グローバル設定・変数
 */
const commonOptions = {
    searchEnabled: true,
    removeItemButton: true,
};
let updateChoice = null;

/**
 * 文字数カウントの更新ロジック (共通関数)
 */
function updateCountLabel(areaId, countId) {
    const area = document.getElementById(areaId);
    const countLabel = document.getElementById(countId);
    if (!area || !countLabel) return;

    if (area.value.length > 200) {
        area.value = area.value.substring(0, 200);
    }
    const len = area.value.length;
    countLabel.textContent = `${len} / 200`;
    countLabel.style.color = (len >= 200) ? 'red' : 'black';
}

/**
 * 更新ダイアログを表示する
 */
function showUpdateDialog(button) {
    const row = button.closest('tr');
    const dialog = document.getElementById('updateDialog');

    // 1. データの取得
    const id = row.cells[0].innerText;                // ID
    const projectId = row.cells[1].dataset.projectId;  // プロジェクトID
    const task = row.cells[2].innerText;               // タスク名
    const userIds = JSON.parse(row.cells[3].dataset.userIds || "[]"); // 担当者ID
    const deadline = row.cells[4].innerText;           // 期限
    const description = row.cells[5].innerText;        // 説明

    const statusMap = { '未着手': 0, '対応中': 1, '完了': 3 };
    const statusText = row.cells[6].innerText.trim();

    // 2. フォームへの値セット
    document.getElementById('update_id').value = id;
    document.getElementById('update_task').value = task;
    document.getElementById('update_deadline').value = deadline;
    document.getElementById('update_status').value = statusMap[statusText] ?? 0;

    const textArea = document.getElementById('update_description');
    textArea.value = description;

    const projectSelect = document.getElementById('update_project');
    if (projectSelect) {
        projectSelect.value = projectId || '';
    }

    // 3. Choices.js の担当者セット
    if (updateChoice) {
        updateChoice.removeActiveItems();
        updateChoice.setChoiceByValue(userIds.map(id => id.toString()));
    }

    // 4. 文字数カウントの初期表示
    updateCountLabel('update_description', 'update_count');

    // 5. ダイアログを表示
    dialog.style.left = ((window.innerWidth - 500) / 2) + 'px';
    dialog.style.display = 'block'; // 元のCSSに合わせて flex か block を選択してください
}

/**
 * 更新ダイアログを閉じる
 */
function closeUpdateDialog() {
    document.getElementById('updateDialog').style.display = 'none';
}

/**
 * プロジェクト選択が「新規」になった時の入力処理
 */
function handleProjectChange(selectElement, hiddenInputId) {
    const selectedValue = selectElement.value;
    const hiddenInput = document.getElementById(hiddenInputId);

    if (selectedValue === '0') {
        const newProjectName = prompt("新しいプロジェクト名を入力してください");

        if (newProjectName && newProjectName.trim() !== "") {
            const trimmedName = newProjectName.trim();
            hiddenInput.value = trimmedName;

            const oldTemp = selectElement.querySelector('.temp-option');
            if (oldTemp) oldTemp.remove();

            const newOption = new Option(newProjectName, "0");
            newOption.classList.add('temp-option');
            selectElement.add(newOption, selectElement.options[2]);
            newOption.selected = true;
        } else {
            selectElement.value = "";
            hiddenInput.value = "";
        }
    } else {
        hiddenInput.value = "";
        const oldTemp = selectElement.querySelector('.temp-option');
        if (oldTemp) oldTemp.remove();
    }
}

/**
 * 部署の選択状態に合わせて、課の選択肢をフィルタリング
 */
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

/**
 * Choices.js のドロップダウン開閉補助
 */
function setupToggle(selector, instance) {
    const el = document.querySelector(selector);
    if (!el) return;
    const container = el.closest('.choices');

    const hitBox = document.createElement('div');
    hitBox.className = 'toggle-hit-box';
    container.appendChild(hitBox);

    hitBox.addEventListener('click', function(e) {
        e.stopPropagation();
        if (container.classList.contains('is-open')) {
            instance.hideDropdown();
        } else {
            instance.showDropdown();
        }
    });
}

/**
 * 初期化処理
 */
document.addEventListener('DOMContentLoaded', function() {

    // 1. Choices.js 初期化
    const addChoice = new Choices('#add_task_user', commonOptions);
    setupToggle('#add_task_user', addChoice);

    const updateEl = document.getElementById('update_user');
    if (updateEl) {
        updateChoice = new Choices(updateEl, commonOptions);
        setupToggle('#update_user', updateChoice);
    }

    // 2. 部署・課の連動
    syncSectionFilter();
    const deptSelect = document.querySelector('select[name="deptId"]');
    if (deptSelect) {
        deptSelect.addEventListener('change', syncSectionFilter);
    }

    // 3. 文字数カウントのイベント登録
    const textAreas = [
        { inputId: 'add_description', countId: 'add_count' },
        { inputId: 'update_description', countId: 'update_count' }
    ];

    textAreas.forEach(item => {
        const area = document.getElementById(item.inputId);
        if (area) {
            area.addEventListener('input', () => updateCountLabel(item.inputId, item.countId));
        }
    });

    // 4. エラー時の自動再表示とスクロール
    const isUpdateError = document.getElementById('isUpdateError');
    if (isUpdateError && isUpdateError.value === 'true') {
        const dialog = document.getElementById('updateDialog');
        if (dialog) {
            dialog.style.display = 'block';

            // Choicesの同期（選択状態を戻す）
            if (updateChoice && updateEl) {
                const selectedValues = Array.from(updateEl.options)
                    .filter(opt => opt.selected)
                    .map(opt => opt.value);
                if (selectedValues.length > 0) {
                    updateChoice.setChoiceByValue(selectedValues);
                }
            }

            // スクロール処理
            setTimeout(() => {
                const target = document.getElementById('task-list-section');
                if (target) {
                    target.scrollIntoView({ behavior: 'auto', block: 'start' });
                }
            }, 100);
        }
    }
});