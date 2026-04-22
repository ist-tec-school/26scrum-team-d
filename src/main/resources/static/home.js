function showUpdateDialog(button) {
    let row = button.parentElement.parentElement;

    document.getElementById('update_id').value = row.cells[0].innerText;
    document.getElementById('update_task').value = row.cells[1].innerText;
    document.getElementById('update_user').value = row.cells[2].dataset.userId || '';
    document.getElementById('update_deadline').value = row.cells[3].innerText;
    document.getElementById('update_description').value = row.cells[4].innerText;

    const statusMap = {'未着手': 0, '対応中': 1, 'レビュー中': 2, '完了': 3};
    const text = row.cells[5].innerText.trim();
    document.getElementById('update_status').value = statusMap[text] ?? 0;

    let dialog = document.getElementById('updateDialog');
    dialog.style.left = ((window.innerWidth - 500) / 2) + 'px';
    dialog.style.display = 'block';
}


function closeUpdateDialog() {
    document.getElementById('updateDialog').style.display = 'none';
}

function handleProjectChange(selectElement) {
    const selectedValue = selectElement.value;
    const hiddenInput = document.getElementById('new-project-name-hidden');

    if (selectedValue === 'new') {
        const newProjectName = prompt("新しいプロジェクト名を入力してください");

        if (newProjectName && newProjectName.trim() !== "") {
            hiddenInput.value = newProjectName;

            // リストの3番目（新規登録の下）に項目を追加して選択
            const newOption = new Option(newProjectName, "new_added");
            selectElement.add(newOption, selectElement.options[2]);
            newOption.selected = true;
        } else {
            selectElement.value = "";
            hiddenInput.value = "";
        }
    } else {
        hiddenInput.value = "";
    }
}


