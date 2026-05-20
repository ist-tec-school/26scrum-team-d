/**
 * 共通設定: Choices.js（複数選択セレクトボックス）のオプション設定
 */
const commonOptions = {
    searchEnabled: true,
    removeItemButton: true,
};

// 更新用ダイアログ内の担当者選択インスタンスを保持する変数
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

function checkTaskLength(input, warnId) {
    const warn = document.getElementById(warnId);
    if (!warn) return;

    if (input.value.length >= 255) {
        // 255文字以上のとき
        input.style.borderColor = "red";
        warn.classList.add('show');    // CSSの visibility: visible が適用される
    } else {
        // 255文字未満のとき（正常）
        input.style.borderColor = "";   // ★枠線を元の色（CSSの設定）に戻す
        warn.classList.remove('show'); // ★CSSの visibility: hidden に戻る
    }
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
    const startDate = row.getAttribute('data-start-date');

    // 2. フォームに値をセット
    document.getElementById('update_id').value = id;
    document.getElementById('update_task').value = task;
    document.getElementById('update_deadline').value = deadline;
    document.getElementById('update_description').value = description;
    document.getElementById('update_status').value = statusMap[statusText] ?? 0;
    document.getElementById('update_start_date').value = startDate;
    updateDateConstraints('update');


    const projectSelect = document.getElementById('update_project');
    if (projectSelect) projectSelect.value = projectId || '';

    // 3. Choices.js の担当者選択状態を更新
    if (updateChoice) {
        updateChoice.removeActiveItems();
        updateChoice.setChoiceByValue(userIds.map(id => id.toString()));
    }

    // 4. 文字数カウントをリセット表示
    updateCountLabel('update_description', 'update_count');

    // 5. ダイアログの表示
    dialog.style.left = "";
    dialog.style.display = 'flex'
}

/**
 * @param {string} prefix 'add' または 'update'
 */
function updateDateConstraints(prefix) {
    const startInput = document.getElementById(prefix === 'add' ? 'add_startDate' : 'update_start_date');
    const deadlineInput = document.getElementById(prefix === 'add' ? 'add_deadline' : 'update_deadline');

    if (!startInput || !deadlineInput) return;
    const today = new Date().toISOString().split('T')[0];

    if (startInput.value) {
        if (startInput.value > today) {
            deadlineInput.min = startInput.value;
        } else {
            deadlineInput.min = today;
        }
    } else {
        deadlineInput.min = today;
    }

    if (deadlineInput.value) {
        startInput.max = deadlineInput.value;
    }
}

/**
 * 更新ダイアログを閉じる
 */
function closeUpdateDialog() {
    document.getElementById('updateDialog').style.display = 'none';
}

/**
 * プロジェクト選択時の新規登録処理
 */

// モーダルを管理するための変数
// モーダル制御用の変数をグローバルに定義
let currentTargetId = '';
let currentSelectEl = null;

/**
 * プロジェクト選択が変更された時の処理
 */
function handleProjectChange(selectElement, hiddenInputId) {
    if (selectElement.value === "0") {
        // 次に値をセットすべきhiddenのIDを保存
        currentTargetId = hiddenInputId;
        currentSelectEl = selectElement;
        openProjectModal();
    } else {
        // 既存プロジェクト選択時は隠しフィールドをクリア
        const hiddenField = document.getElementById(hiddenInputId);
        if (hiddenField) hiddenField.value = "";
    }
}

/**
 * プロジェクトモーダルの「登録」ボタン
 */
function submitProjectModal() {
    // 1. 入力値を取得（ID: custom_newProjectName）
    const inputEl = document.getElementById('custom_newProjectName');
    const name = inputEl.value.trim();

    if (!name) {
        alert("プロジェクト名を入力してください。");
        return;
    }

    if (name.length > 50) {
        alert("プロジェクト名は50文字以内で入力してください。");
        return;
    }

    // 2. HTML側の hidden フィールドに値をセット
    // ここが add_newProjectName_hidden に該当します
    const hiddenField = document.getElementById(currentTargetId);
    if (hiddenField) {
        hiddenField.value = name;
    }

    // 3. セレクトボックスの見た目を更新
    if (currentSelectEl) {
        // 「+ 新規で登録」の文字を書き換えて、選択されていることを分かりやすくする
        const optionZero = currentSelectEl.querySelector('option[value="0"]');
        if (optionZero) {
            optionZero.textContent = "新規登録: " + name;
        }
        currentSelectEl.value = "0"; // 値を0に固定（Daoがこれを見て新規判定するため）
    }

    // 4. モーダルを閉じる
    closeProjectModal();
}

/**
 * モーダルを閉じる処理
 */
function closeProjectModal() {
    document.getElementById('projectModal').style.display = 'none';
    // もし値を入れずに閉じたなら、選択をリセット
    const hiddenField = document.getElementById(currentTargetId);
    if (hiddenField && !hiddenField.value && currentSelectEl) {
        currentSelectEl.value = "";
    }
}

/**
 * 文字数カウント
 */
function updateProjectCountLabel() {
    const input = document.getElementById('custom_newProjectName');
    const countLabel = document.getElementById('project_count');
    const errorDiv = document.getElementById('project_error');
    if (!input || !countLabel) return;

    const len = input.value.length;
    countLabel.textContent = `${len} / 50`;

    if (len > 50) {
        countLabel.style.color = 'red';
        errorDiv.style.display = 'block';
    } else {
        countLabel.style.color = 'black';
        errorDiv.style.display = 'none';
    }
}

// 補助：openProjectModal
function openProjectModal() {
    const modal = document.getElementById('projectModal');
    if (modal) {
        modal.style.display = 'block';
        document.getElementById('custom_newProjectName').value = '';
        updateProjectCountLabel();
    }
}
/**
 * 部署・課の連動フィルタリング
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
 * Choices.js の開閉ヒットボックス設定
 */
function setupToggle(selector, instance) {
    const el = document.querySelector(selector);
    if (!el) return;
    const container = el.closest('.choices');
    if (container.querySelector('.toggle-hit-box')) return;

    const hitBox = document.createElement('div');
    hitBox.className = 'toggle-hit-box';
    container.appendChild(hitBox);
    hitBox.addEventListener('click', (e) => {
        e.stopPropagation();
        container.classList.contains('is-open') ? instance.hideDropdown() : instance.showDropdown();
    });
}

/**
 * メイン初期化処理
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

    // 2. フィルタ連動の初期設定
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
            updateCountLabel(item.inputId, item.countId);
        }
    });

    // 4. エラー時の自動スクロール処理
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

    // =================================================================
    // 💡 【追加】ユーザー情報モーダルの開閉制御イベント登録
    // =================================================================
    const profileTrigger = document.getElementById('userProfileTrigger');
    const profileModal = document.getElementById('userProfileModal');

    if (profileTrigger && profileModal) {
        profileTrigger.addEventListener('click', () => {
            profileModal.style.display = 'flex';
            hidePasswordChangeForm();
        });
    }

    if (profileModal) {
        profileModal.addEventListener('click', (e) => {
            if (e.target === profileModal) {
                closeUserProfileModal();
            }
        });
    }

    const pwChangeBtn = document.querySelector('.password-change-btn');
    if (pwChangeBtn) {
        pwChangeBtn.addEventListener('click', () => {
            showPasswordChangeForm();
        });
    }

    // --- ここからが折り畳み判定ロジック ---
    const refreshRowStatus = (row) => {
        const userCell = row.cells[3];
        const descCell = row.cells[5];
        const descText = descCell?.querySelector('.desc-text');
        if (descText) {
            const style = window.getComputedStyle(descText);
            const lineHeight = parseFloat(style.lineHeight);
            const maxVisibleHeight = lineHeight * 3;
            const isDescOverflow = descText.scrollHeight > maxVisibleHeight + (lineHeight / 2);

            if (isDescOverflow) {
                descCell.classList.add('has-overflow');
            } else {
                descCell.classList.remove('has-overflow');
                descCell.classList.remove('is-expanded');
            }
        }

        const users = userCell?.querySelectorAll('.user-item, div:not(.more-btn)');
        if (users && users.length >= 4) {
            userCell.classList.add('has-overflow');
        } else {
            userCell.classList.remove('has-overflow');
            userCell.classList.remove('is-expanded');
        }
    };
    const rows = document.querySelectorAll('.tasklist tbody tr');
    rows.forEach(row => {
        const userCell = row.cells[3];
        const descCell = row.cells[5];

        const toggleRow = (row) => {
            if (userCell.classList.contains('has-overflow') || descCell.classList.contains('has-overflow')) {
                userCell.classList.toggle('is-expanded');
                descCell.classList.toggle('is-expanded');
            }
        };

        if (userCell) userCell.addEventListener('click', toggleRow);
        if (descCell) descCell.addEventListener('click', toggleRow);

        setTimeout(() => refreshRowStatus(row), 200);
    });

    let resizeTimer;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
            rows.forEach(row => refreshRowStatus(row));
        }, 150);
    });

    window.addEventListener('load', () => {
        // --- ガントチャートからの遷移演出ロジック ---
        const urlParams = new URLSearchParams(window.location.search);
        const fromGantt = urlParams.get('fromGantt');
        const hash = window.location.hash;

        if (fromGantt === 'true' && hash) {
            // A. ガントチャートから来た場合：対象タスクを中央へ表示して強調
            const targetId = hash.substring(1);
            const targetElement = document.getElementById(targetId);

            if (targetElement) {
                setTimeout(() => {
                    // 中央へ一瞬で移動
                    targetElement.scrollIntoView({ behavior: 'auto', block: 'center' });

                    // 強調表示（パッと色をつけて、1秒後から1.5秒かけてふわっと消す）
                    targetElement.style.transition = "none";
                    targetElement.style.backgroundColor = "#fff9c4";

                    setTimeout(() => {
                        targetElement.style.transition = "background-color 1.5s ease";
                        targetElement.style.backgroundColor = "";
                    }, 1000);

                    // URLからfromGanttフラグを削除（リロード時に再発するのを防ぐ）
                    const newUrl = window.location.pathname + window.location.search.replace(/[&?]fromGantt=true/, '').replace(/^&/, '?') + window.location.hash;
                    window.history.replaceState(null, '', newUrl);
                }, 0);
            }
        } else {
            // B. 通常の読み込み時：既存のトップスクロール処理
            const scrollTarget = document.getElementById("task-list-top");
            if (scrollTarget) {
                scrollTarget.scrollIntoView({
                    behavior: "instant",
                    block: "start"
                });
            }
        }

        setTimeout(() => {
            const homeOverlay = document.getElementById("home-loading-overlay");
            if (homeOverlay) {
                homeOverlay.classList.add("fade-out");

                setTimeout(() => {
                    homeOverlay.remove();
                }, 200);
            }
        }, 250);
    });
});
/**
 * 削除確認モーダルを表示し、ユーザーの選択結果を返す
 * @returns {Promise<boolean>} 削除ならtrue、キャンセルならfalse
 */
function confirmDelete() {
    const modal = document.getElementById('delete-modal');
    modal.style.display = 'flex'; // モーダルを表示

    return new Promise((resolve) => {
        // 削除ボタンの処理
        window.deleteModal = () => {
            // closeDeleteModal();
            modal.style.display = 'none';
            resolve(true); // 実行を許可
        };

        // キャンセルボタンの処理
        window.closeDeleteModal = () => {
            modal.style.display = 'none'; // モーダルを非表示
            resolve(false); // 実行をキャンセル
        };
    });
}

/**
 * 実際に実行するメインの処理
 */
// 修正ポイント: 引数 button を受け取る
async function handleDelete(button) {
    // 1. クリックされたボタンが含まれる <form> を取得しておく
    const form = button.closest('form');

    // 2. モーダルの確認を待つ
    const confirmed = await confirmDelete();

    // 3. ユーザーが「削除」を押した場合のみ、JavaScriptから送信を実行
    if (confirmed && form) {
        form.submit();
    }
}

/**
 * 💡 グローバル関数として「閉じる」処理を定義
 * HTML側の onclick="closeUserProfileModal()" から呼び出されます
 */
/**
 * 💡 ユーザー情報モーダル関連の制御関数
 */
function closeUserProfileModal() {
    const profileModal = document.getElementById('userProfileModal');
    if (profileModal) {
        profileModal.style.display = 'none';
    }
}

// パスワード入力フォームに切り替え
function showPasswordChangeForm() {
    document.getElementById('profileView').style.display = 'none';
    document.getElementById('passwordChangeForm').style.display = 'block';
}

// プロフィール表示に戻す
function hidePasswordChangeForm() {
    document.getElementById('profileView').style.display = 'block';
    document.getElementById('passwordChangeForm').style.display = 'none';
    // 入力クリア
    document.getElementById('currentPasswordInput').value = '';
    document.getElementById('newPasswordInput').value = '';
    const errorArea = document.getElementById('pwErrorArea');
    if (errorArea) errorArea.style.display = 'none';
}

// パスワード変更の実行（API通信）
function submitPasswordChange() {
    const currentPw = document.getElementById('currentPasswordInput').value;
    const newPw = document.getElementById('newPasswordInput').value;
    const errorArea = document.getElementById('pwErrorArea');

    if (!currentPw || !newPw) {
        errorArea.innerText = "パスワードを入力してください";
        errorArea.style.display = "block";
        return;
    }

    const params = new URLSearchParams();
    params.append('currentPassword', currentPw);
    params.append('newPassword', newPw);

    fetch('/api/user/update-password', {
        method: 'POST',
        body: params
    })
        .then(response => response.text())
        .then(data => {
            if (data === 'success') {
                fetch('/logout', { method: 'POST' }).finally(() => {
                    window.location.href = '/login?pwChanged=true';
                });
            } else {
                errorArea.style.display = "block";
                if (data === "error: current_password_incorrect") {
                    errorArea.innerText = "現在のパスワードが正しくありません";
                } else if (data === "error: password_too_short") {
                    errorArea.innerText = "新しいパスワードは8文字以上必要です";
                } else {
                    errorArea.innerText = "エラーが発生しました";
                }
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('システムエラーが発生しました');
        });
}