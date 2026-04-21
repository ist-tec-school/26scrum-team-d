function showUpdateDialog(button) {
    let row = button.parentElement.parentElement;

    document.getElementById('update_id').value = row.cells[0].innerText;
    document.getElementById('update_task').value = row.cells[1].innerText;
    document.getElementById('update_user').value = row.cells[2].innerText;
    document.getElementById('update_description').value = row.cells[3].innerText;
    document.getElementById('update_deadline').value = row.cells[4].innerText;

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