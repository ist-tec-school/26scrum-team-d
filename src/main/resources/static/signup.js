
function handleDeptChange(deptSelect) {
    const selectedId = deptSelect.value;
    const sectionSelect = document.getElementById('section');
    const newDeptInput = document.getElementById('newDepartmentName');

    if (selectedId === "0") {
        newDeptInput.style.display = "block";
        newDeptInput.required = true;
        prepareSectionForNewDept();
    } else {
        newDeptInput.style.display = "none";
        newDeptInput.required = false;
        newDeptInput.value = "";
        filterSections(selectedId);
    }
}

function filterSections(deptId) {
    const sectionSelect = document.getElementById('section');
    const newSectionInput = document.getElementById('newSectionName');

    sectionSelect.innerHTML = '<option value="">課を選択してください</option>';
    newSectionInput.style.display = "none";
    newSectionInput.required = false;

    if (!deptId) {
        sectionSelect.disabled = true;
        return;
    }

    const filtered = allSections.filter(sec => String(sec.DEPT_ID)===deptId);

    filtered.forEach(sec => {
        const opt = document.createElement('option');
        opt.value = sec.SECTION_ID;
        opt.textContent = sec.SECTION_NAME;
        sectionSelect.appendChild(opt);
    });

    const newOpt = document.createElement('option');
    newOpt.value = "0";
    newOpt.textContent = "+ 課を新規で登録";
    sectionSelect.appendChild(newOpt);

    sectionSelect.disabled = false;
}

function handleSectionChange(sectionSelect) {
    const newInput = document.getElementById('newSectionName');
    if (sectionSelect.value === "0") {
        newInput.style.display = "block";
        newInput.required = true;
    } else {
        newInput.style.display = "none";
        newInput.required = false;
        newInput.value = "";
    }
}

function prepareSectionForNewDept() {
    const sectionSelect = document.getElementById('section');
    sectionSelect.innerHTML = '<option value="0">+ 課を新規で登録</option>';
    sectionSelect.value = "0";
    sectionSelect.disabled = false;
    handleSectionChange(sectionSelect);
}

// パスワード表示切り替え
document.addEventListener('DOMContentLoaded', function() {
    const passwordInput = document.getElementById('password');
    const toggleButton = document.getElementById('togglePassword');

    if (toggleButton && passwordInput) {
        toggleButton.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.textContent = type === 'password' ? '表示' : '非表示';
        });
    }
});

document.addEventListener('DOMContentLoaded', function() {
    // --- 要素の取得 ---
    const newDeptInput = document.getElementById('newDepartmentName');
    const hiddenKanaInput = document.getElementById('newDepartmentKana');
    const warningElement = document.getElementById('deptWarning');
    const submitBtn = document.querySelector('button[type="submit"]');

    /**
     * 重複チェックを実行する共通関数
     * @param {string} value - チェック対象の文字列（読み or 漢字）
     */
    function performCheck(value) {
        if (!value || value.length === 0) {
            if (warningElement) warningElement.style.display = 'none';
            if (newDeptInput) newDeptInput.style.borderColor = '';
            if (submitBtn) submitBtn.disabled = false;
            return;
        }

        fetch(`/api/check-dept?name=${encodeURIComponent(value)}`)
            .then(response => response.json())
            .then(data => {
                if (data.isDuplicate) {
                    if (warningElement) warningElement.style.display = 'block';
                    if (newDeptInput) newDeptInput.style.borderColor = 'red';
                    if (submitBtn) submitBtn.disabled = true;
                } else {
                    if (warningElement) warningElement.style.display = 'none';
                    if (newDeptInput) newDeptInput.style.borderColor = '';
                    if (submitBtn) submitBtn.disabled = false;
                }
            })
            .catch(error => console.error('Error:', error));
    }

    if (newDeptInput) {
        // 1. ひらがな入力中（IME変換前）の読みをキャプチャ
        newDeptInput.addEventListener('compositionupdate', (e) => {
            const currentKana = e.data;
            if (hiddenKanaInput) {
                hiddenKanaInput.value = currentKana; // 隠しフィールドに「読み」を保存
            }
            performCheck(currentKana); // 読みでチェック
        });

        // 2. 確定時、または直接入力（英数字など）
        newDeptInput.addEventListener('input', (e) => {
            // IME変換中でないときだけ実行
            if (!e.isComposing) {
                const finalValue = e.target.value;
                if (hiddenKanaInput) {
                    hiddenKanaInput.value = finalValue;
                }
                performCheck(finalValue);
            }
        });
    }

});