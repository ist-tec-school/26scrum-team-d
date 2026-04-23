/**
 * 更新ダイアログを表示する
 */
function showUpdateDialog(button) {
    // ボタンが属する行(tr)を取得
    const row = button.closest('tr');
    const dialog = document.getElementById('updateDialog');

    // 各セルのデータを取得（セルの位置に注意）
    const id = row.cells[0].innerText;                // ID (hidden)
    const projectId = row.cells[1].dataset.projectId;  // プロジェクトID
    const task = row.cells[2].innerText;               // タスク名
    const userIds = JSON.parse(row.cells[3].dataset.userIds || "[]");// 担当者ID
    const deadline = row.cells[4].innerText;           // 期限
    const description = row.cells[5].innerText;        // 説明

    // ダイアログの各入力欄に値をセット
    document.getElementById('update_id').value = id;
    document.getElementById('update_task').value = task;
    document.getElementById('update_deadline').value = deadline;
    document.getElementById('update_description').value = description;

    // プロジェクトのセレクトボックスをセット
    const projectSelect = document.getElementById('update_project');
    if (projectSelect) {
        projectSelect.value = projectId || '';
    }

    // Choices.js を使用している担当者セレクトボックスをセット
    if (updateChoice) {
        updateChoice.removeActiveItems();
        updateChoice.setChoiceByValue(userIds.map(id => id.toString()));
    }

    // 状態（テキストから数値へ変換）
    const statusMap = {'未着手': 0, '対応中': 1, '完了': 3};
    const statusText = row.cells[6].innerText.trim();
    document.getElementById('update_status').value = statusMap[statusText] ?? 0;

    dialog.style.left = ((window.innerWidth - 500) / 2) + 'px';
    // ダイアログを表示
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
 * @param selectElement - 選択されたselect要素
 * @param hiddenInputId - プロジェクト名を格納するhiddenのID
 */
function handleProjectChange(selectElement, hiddenInputId) {
    const selectedValue = selectElement.value;
    const hiddenInput = document.getElementById(hiddenInputId);

    if (selectedValue === 'new') {
        const newProjectName = prompt("新しいプロジェクト名を入力してください");

        if (newProjectName && newProjectName.trim() !== "") {
            hiddenInput.value = newProjectName;

            // 画面上の選択肢に一時的に追加
            const newOption = new Option(newProjectName, "new");
            selectElement.add(newOption, selectElement.options[2]);
            newOption.selected = true;
        } else {
            // キャンセル時は選択をリセット
            selectElement.value = "";
            hiddenInput.value = "";
        }
    } else {
        hiddenInput.value = "";
    }
}

let updateChoice = null;

document.addEventListener('DOMContentLoaded', function() {
    const commonOptions = {
        removeItemButton: true,
        searchEnabled: true,
        placeholder: true,
        placeholderValue: '担当者を選択...',
        itemSelectText: '',
        shouldSort: false,
    };

    // --- 登録フォーム ---
    const addChoice = new Choices('#add_task_user', commonOptions);
    setupToggle('#add_task_user', addChoice);

    // --- 更新フォーム（初期化のみ） ---
    const updateEl = document.getElementById('update_user');
    if (updateEl) {
        updateChoice = new Choices(updateEl, commonOptions);
        setupToggle('#update_user', updateChoice);
    }
});

/**
 * 更新ボタンが押された時に呼ばれる関数（既存の処理に組み込んでください）
 * @param {Array} selectedIds - DBから取得した担当者のID配列 e.g. [1, 5]
 */
function reflectSelectedUsers(selectedIds) {
    if (updateChoice) {
        // 1. 一旦現在の選択をクリア
        updateChoice.removeActiveItems();
        // 2. HTMLのoptionではなく、Choicesのメソッドで値をセット
        updateChoice.setChoiceByValue(selectedIds.map(String));
    }
}

/** 矢印エリアの開閉スイッチ */
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