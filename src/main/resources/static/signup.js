
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