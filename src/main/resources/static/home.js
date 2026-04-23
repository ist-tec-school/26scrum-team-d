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
// home.js の handleProjectChange を以下に差し替えてください
function handleProjectChange(selectElement, hiddenInputId) {
    const selectedValue = selectElement.value;
    const hiddenInput = document.getElementById(hiddenInputId);

    if (selectedValue === 'new') {
        const newProjectName = prompt("新しいプロジェクト名を入力してください");

        if (newProjectName && newProjectName.trim() !== "") {
            hiddenInput.value = newProjectName.trim();
            // 「新規で登録」の option のテキストを一時的に変更して、何を入力したかわかるようにする（任意）
            // selectElement.options[selectElement.selectedIndex].text = "+ 新規: " + newProjectName;
        } else {
            // キャンセル時は選択をリセット
            selectElement.value = "";
            hiddenInput.value = "";
        }
    } else {
        hiddenInput.value = "";
    }
}

// --- ページ読み込み時の初期化 ---
let updateChoice;

document.addEventListener('DOMContentLoaded', function() {
    // 登録フォームの担当者（Choices.js）
    new Choices('#add_task_user', {
        searchEnabled: true,
        searchPlaceholderValue: '名前で検索...',
        itemSelectText: '',
        shouldSort: false,
        searchFloor: 0,
        renderChoiceLimit: -1,
        removeItemButton:true,
    });

    // 更新フォームの担当者（Choices.js）
    const updateEl = document.getElementById('update_user');
    if (updateEl) {
        updateChoice = new Choices(updateEl, {
            searchEnabled: true,
            itemSelectText: '',
            shouldSort: false,
            removeItemButton: true,
        });
    }
});