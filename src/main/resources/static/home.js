function showUpdateDialog(button) {
    // 1. ボタンが押された行全体(tr)を取得します
    let row = button.parentElement.parentElement;

    // 2. [0番目] 隠れているIDをセット
    document.getElementById('update_id').value = row.cells[0].innerText;

    // 3. [1番目] プロジェクトIDをセット（セルのdata-project-id属性から取得）
    if(document.getElementById('update_project')) {
        document.getElementById('update_project').value = row.cells[1].dataset.projectId || '';
    }

    // 4. [2番目] タスク名をセット（以前は1番目でしたが2番目にズレました）
    document.getElementById('update_task').value = row.cells[2].innerText;

    // 5. [3番目] 担当者IDをセット（以前は2番目でしたが3番目にズレました）
    document.getElementById('update_user').value = row.cells[3].dataset.userId || '';

    // 6. [4番目] 期限をセット（ここが「説明」に入り込んでいた原因！）
    document.getElementById('update_deadline').value = row.cells[4].innerText;

    // 7. [5番目] 説明をセット
    document.getElementById('update_description').value = row.cells[5].innerText;

    // 8. [6番目] 状態（テキスト→数値変換）をセット
    const statusMap = {'未着手': 0, '対応中': 1, 'レビュー中': 2, '完了': 3};
    const text = row.cells[6].innerText.trim();
    document.getElementById('update_status').value = statusMap[text] ?? 0;

    // 9. ダイアログを表示
    let dialog = document.getElementById('updateDialog');
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
 * プロジェクト選択が変更された時の処理（新規登録・更新共通）
 * @param selectElement - 選択されたselect要素自体
 * @param hiddenInputId - プロジェクト名を格納する隠し入力欄のID
 */
function handleProjectChange(selectElement, hiddenInputId) {
    const selectedValue = selectElement.value;
    const hiddenInput = document.getElementById(hiddenInputId);

    if (selectedValue === 'new') {
        const newProjectName = prompt("新しいプロジェクト名を入力してください");

        if (newProjectName && newProjectName.trim() !== "") {
            hiddenInput.value = newProjectName;

            // ユーザーに見せるために、一時的にセレクトボックスに選択肢を追加する
            const newOption = new Option(newProjectName, "new");
            selectElement.add(newOption, selectElement.options[2]);
            newOption.selected = true;
        } else {
            // キャンセルされた場合は選択を空に戻す
            selectElement.value = "";
            hiddenInput.value = "";
        }
    } else {
        // 既存のプロジェクトを選んだ場合は隠しフィールドを空にする
        hiddenInput.value = "";
    }
}