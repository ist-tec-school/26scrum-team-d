
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

            // 目のアイコンを切り替える
            this.querySelector('.material-symbols-outlined').textContent = (type === 'password') ? 'visibility' : 'visibility_off';
        });
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const newDeptInput = document.getElementById('newDepartmentName');
    const hiddenKanaInput = document.getElementById('newDepartmentKana');

    if (newDeptInput) {
        newDeptInput.addEventListener('input', (e) => {
            const val = e.target.value;
            if (hiddenKanaInput) {
                hiddenKanaInput.value = val;
            }
            performCheck(val);
        });
    }

    function performCheck(value) {
        const warningElement = document.getElementById('deptWarning');
        const submitBtn = document.querySelector('button[type="submit"]');

        if (!value) {
            if (warningElement) warningElement.style.display = 'none';
            return;
        }

        const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch(`/api/check-dept?name=${encodeURIComponent(value)}`, {
            method: 'GET',
            headers: {
                [header]: token
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.isDuplicate) {
                    warningElement.style.display = 'block';
                    newDeptInput.style.borderColor = 'red';
                    if (submitBtn) submitBtn.disabled = true;
                } else {
                    warningElement.style.display = 'none';
                    newDeptInput.style.borderColor = '';
                    if (submitBtn) submitBtn.disabled = false;
                }
            });
    }
});