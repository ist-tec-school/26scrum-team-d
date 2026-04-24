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

    if (selectedValue === '0') {
        const newProjectName = prompt("新しいプロジェクト名を入力してください");

        if (newProjectName && newProjectName.trim() !== "") {
            hiddenInput.value = newProjectName;

            // 画面上の選択肢に一時的に追加
            const newOption = new Option(newProjectName, "0");
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
// --- ページ読み込み時の初期化 ---
let updateChoice = null;

    document.addEventListener('DOMContentLoaded', function() {
        // 1. 登録フォーム初期化
        new Choices('#add_task_user', {
            searchEnabled: true,
            removeItemButton: true,
        });

        // 2. 更新フォーム初期化
        const updateEl = document.getElementById('update_user');
        if (updateEl) {
            updateChoice = new Choices(updateEl, {
                searchEnabled: true,
                removeItemButton: true,
            });
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
            }
        }
    });