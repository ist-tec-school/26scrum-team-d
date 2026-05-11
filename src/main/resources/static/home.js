/**
 * 更新ダイアログを表示する
 */
function showUpdateDialog(button) {
    // 1. ボタンが属する行(tr)を取得
    const row = button.closest('tr');
    const dialog = document.getElementById('updateDialog');

    // 2. 各セルのデータ属性やテキストからデータを取得
    const id = row.cells[0].innerText;                // ID (hidden)
    const projectId = row.cells[1].dataset.projectId;  // プロジェクトID
    const task = row.cells[2].innerText;               // タスク名
    const userIds = JSON.parse(row.cells[3].dataset.userIds || "[]"); // 担当者ID
    const deadline = row.cells[4].innerText;           // 期限
    const description = row.cells[5].innerText;        // 説明

    // 3. ダイアログの各入力欄に値をセット
    document.getElementById('update_id').value = id;
    document.getElementById('update_task').value = task;
    document.getElementById('update_deadline').value = deadline;
    document.getElementById('update_description').value = description;

    // 4. プロジェクトのセレクトボックスをセット
    const projectSelect = document.getElementById('update_project');
    if (projectSelect) {
        projectSelect.value = projectId || '';
    }

    // 5. Choices.js を使用している担当者セレクトボックスをセット
    if (updateChoice) {
        updateChoice.removeActiveItems();
        updateChoice.setChoiceByValue(userIds.map(id => id.toString()));
    }

    // 6. 状態（テキストから数値へ変換）
    const statusMap = {'未着手': 0, '対応中': 1, '完了': 3};
    const statusText = row.cells[6].innerText.trim();
    document.getElementById('update_status').value = statusMap[statusText] ?? 0;

    // 7. ダイアログの位置調整と表示
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

    if (selectedValue === 'new') {
        const newProjectName = prompt("新しいプロジェクト名を入力してください");

        if (newProjectName && newProjectName.trim() !== "") {
            const trimmedName = newProjectName.trim();
            hiddenInput.value = trimmedName;

            // 以前追加した一時的な選択肢があれば削除
            const oldTemp = selectElement.querySelector('.temp-option');
            if (oldTemp) oldTemp.remove();

            // 新しい選択肢を作成して「新規で登録」の下に追加
            const newOption = document.createElement('option');
            newOption.value = "new";
            newOption.text = "新規: " + trimmedName;
            newOption.className = 'temp-option';

            selectElement.add(newOption, selectElement.options[1]);
            selectElement.value = "new";
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

let updateChoice = null;

/**
 * メインの初期化処理
 */
document.addEventListener('DOMContentLoaded', function() {
    const commonOptions = {
        removeItemButton: true,
        searchEnabled: true,
        placeholder: true,
        placeholderValue: '担当者を選択...',
        itemSelectText: '',
        shouldSort: false,
    };

    // --- 登録フォームの Choices.js 初期化 ---
    const addChoice = new Choices('#add_task_user', commonOptions);
    setupToggle('#add_task_user', addChoice);

    // --- 更新フォームの Choices.js 初期化 ---
    const updateEl = document.getElementById('update_user');
    if (updateEl) {
        updateChoice = new Choices(updateEl, commonOptions);
        setupToggle('#update_user', updateChoice);
    }

    // --- 部署・課の連動初期化 ---
    syncSectionFilter();
    const deptSelect = document.querySelector('select[name="deptId"]');
    if (deptSelect) {
        deptSelect.addEventListener('change', syncSectionFilter);
    }
});

/**
 * 担当者選択用の Choices.js インスタンスに値を反映
 */
function reflectSelectedUsers(selectedIds) {
    if (updateChoice) {
        updateChoice.removeActiveItems();
        updateChoice.setChoiceByValue(selectedIds.map(String));
    }
}

/**
 * Choices.js の右端クリックでドロップダウンを開閉するための設定
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
document.addEventListener('DOMContentLoaded', () => {
    // 登録用と更新用、それぞれ個別に設定
    const textAreas = [
        { inputId: 'add_description', countId: 'add_count' },
        { inputId: 'update_description', countId: 'update_count' }
    ];

    textAreas.forEach(item => {
        const area = document.getElementById(item.inputId);
        const countLabel = document.getElementById(item.countId);

        if (area && countLabel) {
            // 入力されるたびに実行
            area.addEventListener('input', () => {
                const currentLength = area.value.length;
                countLabel.textContent = `${currentLength} / 200`;

                if (currentLength >= 199) {
                    // 200文字に到達
                    countLabel.style.color = 'red';
                    area.style.border = '2px solid red';
                } else {
                    // 200文字未満
                    countLabel.style.color = 'black';
                    area.style.border = '1px solid #ccc'; // 元の枠線色
                }
            });
        }
    });
});