/**
 * グローバル設定・変数
 */
const commonOptions = {
    searchEnabled: true,
    removeItemButton: true,
};
let updateChoice = null;

/**
 * 文字数カウントの更新ロジック
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

    // 1. 各列からデータを取得
    const id = row.cells[0].innerText;
    const projectId = row.cells[1].dataset.projectId;
    const task = row.cells[2].innerText;
    const userIds = JSON.parse(row.cells[3].dataset.userIds || "[]");
    const deadline = row.cells[4].innerText;
    const description = row.cells[5].innerText;
    const statusMap = { '未着手': 0, '対応中': 1, '完了': 3 };
    const statusText = row.cells[6].innerText.trim();

    // 2. フォームにセット
    document.getElementById('update_id').value = id;
    document.getElementById('update_task').value = task;
    document.getElementById('update_deadline').value = deadline;
    document.getElementById('update_description').value = description;
    document.getElementById('update_status').value = statusMap[statusText] ?? 0;

    const projectSelect = document.getElementById('update_project');
    if (projectSelect) projectSelect.value = projectId || '';

    // 3. Choices.js の担当者セット
    if (updateChoice) {
        updateChoice.removeActiveItems();
        updateChoice.setChoiceByValue(userIds.map(id => id.toString()));
    }

    // 4. 文字数カウントの初期表示
    updateCountLabel('update_description', 'update_count');

    // 5. ダイアログの位置調整と表示
    dialog.style.left = ((window.innerWidth - 500) / 2) + 'px';
    dialog.style.display = 'block';
}

/**
 * 更新ダイアログを閉じる
 */
function closeUpdateDialog() {
    document.getElementById('updateDialog').style.display = 'none';
}

/**
 * プロジェクト選択時の新規登録プロンプト
 */
function handleProjectChange(selectElement, hiddenInputId) {
    const selectedValue = selectElement.value;
    const hiddenInput = document.getElementById(hiddenInputId);

    if (selectedValue === '0') {
        const newProjectName = prompt("新しいプロジェクト名を入力してください");
        if (newProjectName && newProjectName.trim() !== "") {
            const trimmedName = newProjectName.trim();
            hiddenInput.value = trimmedName;

            // 既存の仮選択肢があれば削除
            const oldTemp = selectElement.querySelector('.temp-option');
            if (oldTemp) oldTemp.remove();

            // 新しい仮選択肢を追加
            const newOption = new Option(trimmedName, "0");
            newOption.classList.add('temp-option');
            selectElement.add(newOption, selectElement.options[2]);
            newOption.selected = true;
        } else {
            selectElement.value = "";
        }
    }
}

/**
 * 部署・課の連動フィルタ
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
 * Choices.js のドロップダウン開閉ボタン（ヒットボックス）の設定
 */
function setupToggle(selector, instance) {
    const el = document.querySelector(selector);
    if (!el) return;
    const container = el.closest('.choices');
    if (container.querySelector('.toggle-hit-box')) return; // 重複作成防止

    const hitBox = document.createElement('div');
    hitBox.className = 'toggle-hit-box';
    container.appendChild(hitBox);
    hitBox.addEventListener('click', (e) => {
        e.stopPropagation();
        container.classList.contains('is-open') ? instance.hideDropdown() : instance.showDropdown();
    });
}

/**
 * 初期化処理
 */
document.addEventListener('DOMContentLoaded', () => {

    // 1. Choices.js 初期化
    const addEl = document.getElementById('add_task_user');
    if (addEl) {
        const addChoice = new Choices(addEl, commonOptions);
        setupToggle('#add_task_user', addChoice);
    }

    const updateEl = document.getElementById('update_user');
    if (updateEl) {
        updateChoice = new Choices(updateEl, commonOptions);
        setupToggle('#update_user', updateChoice);
    }

    // 2. フィルタ連動
    syncSectionFilter();
    const deptSelect = document.querySelector('select[name="deptId"]');
    if (deptSelect) deptSelect.addEventListener('change', syncSectionFilter);

    // 3. 文字数カウントのイベント登録
    const textAreas = [
        { inputId: 'add_description', countId: 'add_count' },
        { inputId: 'update_description', countId: 'update_count' }
    ];
    textAreas.forEach(item => {
        const area = document.getElementById(item.inputId);
        if (area) {
            area.addEventListener('input', () => updateCountLabel(item.inputId, item.countId));
            // 初期表示用
            updateCountLabel(item.inputId, item.countId);
        }
    });

    // 4. エラー時の自動再表示とスクロール
    const isUpdateError = document.getElementById('isUpdateError');
    if (isUpdateError && isUpdateError.value === 'true') {
        const dialog = document.getElementById('updateDialog');
        if (dialog) {
            dialog.style.display = 'block';

            // バリデーションエラー時に選択されていた値をChoicesに復元
            if (updateChoice && updateEl) {
                const selectedValues = Array.from(updateEl.options)
                    .filter(opt => opt.selected)
                    .map(opt => opt.value);
                if (selectedValues.length > 0) {
                    updateChoice.setChoiceByValue(selectedValues);
                }
            }

            setTimeout(() => {
                const target = document.getElementById('task-list-section');
                if (target) target.scrollIntoView({ behavior: 'auto', block: 'start' });
            }, 100);
        }
    }

    // 5. 説明列の省略判定と開閉
    const descCells = document.querySelectorAll('.tasklist td:nth-child(6)');
    setTimeout(() => {
        descCells.forEach(cell => {
            const descText = cell.querySelector('.desc-text');
            if (!descText) return;

            // はみ出しているか判定
            if (descText.scrollWidth > descText.clientWidth + 1) {
                cell.classList.add('has-overflow');
            }

            // クリックイベント
            cell.addEventListener('click', () => {
                if (cell.classList.contains('has-overflow') || cell.classList.contains('is-expanded')) {
                    cell.classList.toggle('is-expanded');
                }
            });
        });
    }, 300); // 描画完了を待つため少し長めに設定
});