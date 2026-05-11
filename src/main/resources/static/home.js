/**
 * 更新ダイアログを表示する
 */
// --- 追加した箇所 ---
const commonOptions = {
    searchEnabled: true,
    removeItemButton: true,
};
let updateChoice = null;

function showUpdateDialog(button) {

    const row = button.closest('tr');
    const dialog = document.getElementById('updateDialog');


    const id = row.cells[0].innerText;                // ID (hidden)
    const projectId = row.cells[1].dataset.projectId;  // プロジェクトID
    const task = row.cells[2].innerText;               // タスク名
    const userIds = JSON.parse(row.cells[3].dataset.userIds || "[]"); // 担当者ID
    const deadline = row.cells[4].innerText;           // 期限
    const description = row.cells[5].innerText;        // 説明


    document.getElementById('update_id').value = id;
    document.getElementById('update_task').value = task;
    document.getElementById('update_deadline').value = deadline;
    document.getElementById('update_description').value = description;


    const projectSelect = document.getElementById('update_project');
    if (projectSelect) {
        projectSelect.value = projectId || '';
    }


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

            // 以前追加した一時的な選択肢があれば削除
            const oldTemp = selectElement.querySelector('.temp-option');
            if (oldTemp) oldTemp.remove();

            // 新しい選択肢を作成して「新規で登録」の下に追加
            const newOption = new Option(newProjectName, "0");
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
 * 部署の選択状態に合わせて、課の選択肢をフィルタリングする
 */
function syncSectionFilter() {
    // 部署と課のセレクトボックスを取得（name属性で特定）
    const deptSelect = document.querySelector('select[name="deptId"]');
    const sectionSelect = document.querySelector('select[name="sectionId"]');

    if (!deptSelect || !sectionSelect) return;

    const selectedDeptId = deptSelect.value;
    const options = sectionSelect.options;

    // 各「課」の選択肢をチェック
    for (let i = 0; i < options.length; i++) {
        const opt = options[i];
        const parentDeptId = opt.getAttribute('data-dept');

        // 条件： 「すべて」である、または親部署IDが一致する
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

    hitBox.addEventListener('click', function(e) {
        e.stopPropagation();
        if (container.classList.contains('is-open')) {
            instance.hideDropdown();
        } else {
            instance.showDropdown();
        }
    });
}

document.addEventListener('DOMContentLoaded', function() {

    // 1. 登録フォームの Choices.js 初期化
    const addChoice = new Choices('#add_task_user', commonOptions);
    setupToggle('#add_task_user', addChoice);

    // 2. 更新フォームの Choices.js 初期化
    const updateEl = document.getElementById('update_user');
    if (updateEl) {
        updateChoice = new Choices(updateEl, commonOptions);
        setupToggle('#update_user', updateChoice);
    }

    // 3. 部署・課の連動初期化
    syncSectionFilter();
    const deptSelect = document.querySelector('select[name="deptId"]');
    if (deptSelect) {
        deptSelect.addEventListener('change', syncSectionFilter);
    }

// 3. エラー時の自動再表示とスクロール
        const isUpdateError = document.getElementById('isUpdateError');
        if (isUpdateError && isUpdateError.value === 'true') {
            const dialog = document.getElementById('updateDialog');
            if (dialog) {
                dialog.style.display = 'block';

                // Choicesの同期（選択状態を戻す）
                if (updateChoice && updateEl) {
                    // セレクトボックスに元々ある選択済みの値を取得してセット
                    const selectedValues = Array.from(updateEl.options)
                        .filter(opt => opt.selected)
                        .map(opt => opt.value);
                    if (selectedValues.length > 0) {
                        updateChoice.setChoiceByValue(selectedValues);
                    }
                }

                // --- スクロール処理 (独立) ---
                setTimeout(() => {
                    const target = document.getElementById('task-list-section');
                    if (target) {
                        console.log("Scrolling to task list section...");
                        target.scrollIntoView({ behavior: 'auto', block: 'start' });
                    }
                }, 100); // 描画を待つため少し長めに設定
/**
 * 担当者選択用の Choices.js インスタンスに値を反映
 */
function reflectSelectedUsers(selectedIds) {
    if (updateChoice) {
        updateChoice.removeActiveItems();
        updateChoice.setChoiceByValue(selectedIds.map(String));
    }
}

                /** 矢印エリアの開閉スイッチ */

            }
        }
    });