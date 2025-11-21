// Форматирование даты (только дата)
function formatDate(dateString) {
    if (!dateString) return 'не указано';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU');
    } catch (e) {
        return 'неверный формат';
    }
}

// Форматирование даты и времени
function formatDateTime(dateString) {
    if (!dateString) return 'не указано';
    try {
        const date = new Date(dateString);
        return date.toLocaleString('ru-RU');
    } catch (e) {
        return 'неверный формат';
    }
}

// Форматирование даты для input[type="date"]
function formatDateForInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Расчет дней больничного
function calculateSickLeaveDays(startDate, endDate) {
    if (!startDate || !endDate) return 0;
    try {
        const start = new Date(startDate);
        const end = new Date(endDate);
        const diffTime = Math.abs(end - start);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
        return diffDays;
    } catch (e) {
        return 0;
    }
}

// Показать/скрыть поля дат больничного
function toggleSickLeaveDates() {
    const sickLeaveCheck = document.getElementById('sickLeaveCheck');
    const sickLeaveDates = document.getElementById('sickLeaveDates');

    if (sickLeaveCheck && sickLeaveDates) {
        if (sickLeaveCheck.checked) {
            sickLeaveDates.style.display = 'block';

            // Установить даты по умолчанию
            const today = new Date();
            const endDate = new Date();
            endDate.setDate(today.getDate() + 7);

            document.getElementById('sickLeaveStart').value = formatDateForInput(today);
            document.getElementById('sickLeaveEnd').value = formatDateForInput(endDate);
        } else {
            sickLeaveDates.style.display = 'none';
        }
    }
}






function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    setTheme(savedTheme);
}

function toggleTheme() {
    console.log('Toggle theme clicked!');
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
}

function setTheme(theme) {
    console.log('Setting theme to:', theme);
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);

    const themeIcon = document.getElementById('themeIcon');
    const themeText = document.getElementById('themeText');

    if (themeIcon && themeText) {
        if (theme === 'dark') {
            themeIcon.className = 'fas fa-sun';
            themeText.textContent = 'Светлая тема';
        } else {
            themeIcon.className = 'fas fa-moon';
            themeText.textContent = 'Темная тема';
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const user = checkAuth();
    if (!user) return;

    // Ограничения по ролям
    if (user.role === 'NURSE') {
        // Медсестры могут только просматривать
        document.getElementById('addPatientBtn').style.display = 'none';
        document.getElementById('deletePatientBtn').style.display = 'none';
    }

    // Остальная инициализация...
    initTheme();
    initializeEventListeners();
    loadPatients();
    initializeAutocomplete();
});




// Глобальные переменные
let selectedPatientId = null;
let allSymptoms = [];
let allPatients = [];
let patientVisits = [];






// Автодополнение симптомов
function initializeAutocomplete() {
    const symptomInput = document.getElementById('newSymptomDesc');

    symptomInput.addEventListener('input', async function(e) {
        const query = e.target.value.trim();
        const suggestionsDiv = document.getElementById('suggestions');

        if (query.length < 2) {
            suggestionsDiv.innerHTML = '';
            return;
        }

        try {
            const response = await fetch(`/api/medical-symptoms/search?query=${encodeURIComponent(query)}`);
            const suggestions = await response.json();

            if (suggestions.length > 0) {
                suggestionsDiv.innerHTML = suggestions.map(symptom =>
                    `<div class="suggestion-item" onclick="selectSuggestion('${symptom.replace(/'/g, "\\'")}')">
                        ${symptom}
                    </div>`
                ).join('');
            } else {
                suggestionsDiv.innerHTML = '<div class="text-muted p-2">Симптомы не найдены</div>';
            }
        } catch (error) {
            console.error('Ошибка поиска симптомов:', error);
        }
    });
}

// Выбор подсказки
function selectSuggestion(symptom) {
    document.getElementById('newSymptomDesc').value = symptom;
    document.getElementById('suggestions').innerHTML = '';
}

// Поиск пациентов
document.getElementById('patientSearch').addEventListener('input', function(e) {
    const query = e.target.value.toLowerCase();
    filterPatients(query);
});

// Фильтрация пациентов
function filterPatients(query = '') {
    let filtered = allPatients;

    // Только поиск по ФИО
    if (query) {
        filtered = filtered.filter(patient =>
            patient.firstName.toLowerCase().includes(query) ||
            patient.lastName.toLowerCase().includes(query)
        );
    }

    renderPatientsList(filtered);
}

// Загрузка визитов пациента
async function loadVisits() {
    if (!selectedPatientId) return;

    try {
        console.log('🔄 Загружаем визиты для пациента:', selectedPatientId);

        const response = await fetch(`/api/patients/${selectedPatientId}/visits`);
        console.log('📡 Ответ сервера:', response.status);

        if (!response.ok) return;

        const visits = await response.json();
        console.log('✅ Получены визиты:', visits);

        patientVisits = visits;
        renderVisitsList(visits);

    } catch (error) {
        console.error('❌ Ошибка загрузки визитов:', error);
    }
}

// Отображение списка визитов

// Отображение списка визитов
function renderVisitsList(visits) {
    const list = document.getElementById('visitsList');
    if (!list) return;

    if (!visits || visits.length === 0) {
        list.innerHTML = `
            <div class="text-center text-muted py-4">
                <i class="fas fa-calendar-times fa-2x mb-3" style="color: #e3f2fd;"></i>
                <p>Визитов нет</p>
            </div>
        `;
        return;
    }

    list.innerHTML = visits.map(visit => {
        const hasClosedSickLeave = !visit.sickLeaveIssued && visit.sickLeaveClosedDate;

        return `
        <div class="visit-item">
            <div class="d-flex justify-content-between align-items-start mb-3">
                <div class="d-flex align-items-center">
                    <i class="fas fa-calendar-alt text-primary me-3 fa-lg"></i>
                    <div>
                        <strong class="h5 mb-1">${formatDateTime(visit.visitDate)}</strong>
                        <div class="d-flex flex-wrap gap-2 mt-1">
                            <span class="badge badge-medical badge-primary">
                                <i class="fas fa-stethoscope me-1"></i>${visit.visitType || 'не указан'}
                            </span>
                            <span class="badge badge-medical ${visit.status === 'завершен' ? 'badge-success' : 'badge-warning'}">
                                <i class="fas ${visit.status === 'завершен' ? 'fa-check-circle' : 'fa-spinner'} me-1"></i>
                                ${visit.status || 'не указан'}
                            </span>
                            ${visit.sickLeaveIssued ?
            '<span class="badge badge-medical badge-danger">' +
            '<i class="fas fa-file-medical me-1"></i>Больничный открыт</span>' :
            hasClosedSickLeave ?
                '<span class="badge badge-medical badge-secondary">' +
                '<i class="fas fa-file-contract me-1"></i>Больничный закрыт</span>' :
                ''
        }
                        </div>
                    </div>
                </div>
                <div class="d-flex gap-2">
                    ${visit.sickLeaveIssued && visit.status !== 'завершен' ? `
                        <button class="btn btn-sm btn-danger btn-medical" onclick="closeSickLeave(${visit.id})">
                            <i class="fas fa-file-contract me-1"></i>Закрыть больничный
                        </button>
                    ` : ''}
                    ${visit.status !== 'завершен' ? `
                        <button class="btn btn-sm btn-outline-primary btn-medical" onclick="editVisit(${visit.id})">
                            <i class="fas fa-edit me-1"></i>Редактировать
                        </button>
                    ` : ''}
                </div>
            </div>
            
            ${visit.diagnosis ? `
                <div class="alert alert-info border-0 mb-3">
                    <i class="fas fa-diagnoses me-2"></i>
                    <strong>Диагноз:</strong> ${visit.diagnosis}
                </div>
            ` : ''}
            
            ${visit.sickLeaveStart && visit.sickLeaveIssued ? `
                <div class="medical-info-card p-3 mb-3">
                    <div class="d-flex align-items-center mb-2">
                        <i class="fas fa-calendar-week text-warning me-2"></i>
                        <strong>Период больничного:</strong>
                    </div>
                    <div class="ms-4">
                        📅 ${formatDate(visit.sickLeaveStart)} - ${formatDate(visit.sickLeaveEnd)}
                        <small class="text-muted ms-2">(${calculateSickLeaveDays(visit.sickLeaveStart, visit.sickLeaveEnd)} дней)</small>
                    </div>
                </div>
            ` : ''}
            
            ${hasClosedSickLeave ? `
                <div class="alert alert-success border-0">
                    <div class="d-flex align-items-center">
                        <i class="fas fa-check-circle me-2 fa-lg"></i>
                        <div>
                            <strong>Больничный закрыт</strong>
                            <div class="text-muted small">
                                <i class="fas fa-clock me-1"></i>${formatDateTime(visit.sickLeaveClosedDate)}
                            </div>
                        </div>
                    </div>
                </div>
            ` : ''}
            
            ${visit.notes ? `
                <div class="mt-3 p-3 bg-light rounded">
                    <i class="fas fa-sticky-note me-2"></i>
                    <strong>Примечания:</strong> ${visit.notes}
                </div>
            ` : ''}
        </div>
        `;
    }).join('');
}
// Начало нового визита
function startNewVisit() {
    document.getElementById('newVisitForm').style.display = 'block';
}

// Отмена создания визита
function cancelNewVisit() {
    document.getElementById('newVisitForm').style.display = 'none';
    // Очистка формы
    document.getElementById('visitType').value = 'первичный';
    document.getElementById('sickLeaveCheck').checked = false;
}

// Создание визита
async function createVisit() {
    if (!selectedPatientId) {
        showAlert('Сначала выберите пациента', 'warning');
        return;
    }

    const visitData = {
        visitType: document.getElementById('visitType').value,
        status: 'в процессе',
        sickLeaveIssued: document.getElementById('sickLeaveCheck').checked,
        notes: document.getElementById('visitNotes').value.trim(),
        diagnosis: document.getElementById('initialDiagnosis').value.trim()
    };

    // Добавляем даты больничного если выдан
    if (visitData.sickLeaveIssued) {
        const startDate = document.getElementById('sickLeaveStart').value;
        const endDate = document.getElementById('sickLeaveEnd').value;

        console.log('📅 Даты больничного:', { startDate, endDate });

        if (!startDate || !endDate) {
            showAlert('Заполните даты больничного листа', 'warning');
            return;
        }

        visitData.sickLeaveStart = startDate + 'T00:00:00';
        visitData.sickLeaveEnd = endDate + 'T23:59:59';
    }

    console.log('📤 Отправляемые данные:', visitData);

    try {
        const response = await fetch(`/api/patients/${selectedPatientId}/visits`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(visitData)
        });

        console.log('📡 Ответ сервера:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }

        const result = await response.json();
        console.log('✅ Визит создан:', result);

        showAlert('Визит успешно создан', 'success');
        cancelNewVisit();
        await loadVisits();

    } catch (error) {
        console.error('❌ Ошибка:', error);
        showAlert('Ошибка создания визита: ' + error.message, 'danger');
    }
}

// Редактирование визита
function editVisit(visitId) {
    const visit = patientVisits.find(v => v.id === visitId);
    if (!visit) return;

    const newDiagnosis = prompt('Введите диагноз:', visit.diagnosis || '');
    if (newDiagnosis === null) return;

    const newStatus = prompt('Введите статус (завершен/в процессе):', visit.status || 'в процессе');
    if (newStatus === null) return;

    const newSickLeave = confirm('Больничный лист открыт?');

    updateVisit(visitId, {
        diagnosis: newDiagnosis,
        status: newStatus,
        sickLeaveIssued: newSickLeave
    });
}

// Обновление визита
async function updateVisit(visitId, visitData) {
    try {
        const response = await fetch(`/api/visits/${visitId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(visitData)
        });

        if (!response.ok) throw new Error('Ошибка обновления визита');

        showAlert('Визит успешно обновлен', 'success');
        await loadVisits();

    } catch (error) {
        showAlert('Ошибка обновления визита: ' + error.message, 'danger');
    }
}

// Закрытие больничного
async function closeSickLeave(visitId) {
    if (!confirm('Закрыть больничный лист и завершить визит?')) return;

    try {
        const response = await fetch(`/api/visits/${visitId}/close-sick-leave`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) throw new Error('Ошибка закрытия больничного');

        showAlert('Больничный закрыт, визит завершен', 'success');
        await loadVisits();

    } catch (error) {
        showAlert('Ошибка закрытия больничного: ' + error.message, 'danger');
    }
}

// Инициализация всех обработчиков событий
function initializeEventListeners() {
    document.getElementById('addPatientBtn').addEventListener('click', addPatient);
    document.getElementById('addSymptomBtn').addEventListener('click', addSymptom);
    document.getElementById('deletePatientBtn').addEventListener('click', deletePatient);
    document.getElementById('generateReportBtn').addEventListener('click', generateReport);
    document.getElementById('downloadPdfBtn').addEventListener('click', downloadPdf);
    // Фильтр пациентов
    document.getElementById('visitFilter').addEventListener('change', function() {
        filterPatients(document.getElementById('patientSearch').value);
    });
}

// Загрузка списка пациентов
async function loadPatients() {
    try {
        console.log('🔄 Начинаем загрузку пациентов...');
        showLoading(true);

        const currentUser = JSON.parse(localStorage.getItem('currentUser'));
        console.log('👤 Текущий пользователь:', currentUser);

        const response = await fetch('/api/patients');
        console.log('📡 Ответ сервера:', response.status, response.statusText);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const patients = await response.json();
        console.log('✅ Пациенты загружены:', patients);

        allPatients = patients;
        renderPatientsList(allPatients);

        if (patients.length === 0) {
            console.log('ℹ️ База пациентов пуста');
            showAlert('База пациентов пуста. Добавьте первого пациента.', 'info');
        }

    } catch (error) {
        console.error('❌ Ошибка загрузки пациентов:', error);
        // Показываем тестовые данные при ошибке
        console.log('🔄 Показываем тестовые данные...');
        allPatients = [
            {
                id: 1,
                firstName: "Тестовый",
                lastName: "Пациент",
                age: 30,
                address: "Москва",
                phone: "+79990000000",
                doctor: null
            }
        ];
        renderPatientsList(allPatients);
        showAlert('Ошибка загрузки данных. Показаны тестовые данные.', 'warning');
    } finally {
        showLoading(false);
    }
}

// Рендер списка пациентов - ИСПРАВЛЕННАЯ ВЕРСИЯ (без дублирования)
function renderPatientsList(patients) {
    const list = document.getElementById('patientsList');
    list.innerHTML = '';

    patients.forEach(patient => {
        const li = document.createElement('li');
        li.className = 'list-group-item list-group-item-action patient-card';
        li.innerHTML = `
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <strong>${patient.firstName} ${patient.lastName}</strong>
                    <br>
                    <small class="text-muted">${patient.age} лет, ${patient.phone || 'тел. не указан'}</small>
                    ${patient.doctor ? `<br><small class="text-info">Врач: ${patient.doctor.fullName}</small>` : ''}
                </div>
                <button class="btn btn-sm btn-outline-primary" onclick="selectPatient(${patient.id})">
                    Открыть
                </button>
            </div>
        `;
        list.appendChild(li);
    });
}
patients.forEach(patient => {
    const li = document.createElement('li');
    li.className = 'list-group-item list-group-item-action patient-card';
    li.innerHTML = `
        <div class="d-flex justify-content-between align-items-center">
            <div>
                <strong>${patient.firstName} ${patient.lastName}</strong>
                <br>
                <small class="text-muted">${patient.age} лет, ${patient.phone || 'тел. не указан'}</small>
            </div>
            <button class="btn btn-sm btn-outline-primary" onclick="selectPatient(${patient.id})">
                Открыть
            </button>
        </div>
    `;
    list.appendChild(li);
});

// ОБНОВЛЕННАЯ ФУНКЦИЯ ДОБАВЛЕНИЯ ПАЦИЕНТА
async function addPatient() {
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));

    const patientData = {
        firstName: document.getElementById('firstName').value.trim(),
        lastName: document.getElementById('lastName').value.trim(),
        age: parseInt(document.getElementById('age').value) || 0,
        address: document.getElementById('address').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        doctor: currentUser // ДОБАВИТЬ ТЕКУЩЕГО ВРАЧА
    };

    // Валидация
    if (!patientData.firstName || !patientData.lastName || patientData.age <= 0) {
        showAlert('Заполните обязательные поля: Имя, Фамилия, Возраст', 'warning');
        return;
    }

    try {
        console.log('Отправляемые данные:', patientData);

        const response = await fetch('/api/patients', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(patientData)
        });

        console.log('Статус ответа:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Текст ошибки:', errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        console.log('Успешный ответ:', result);

        showAlert('Пациент успешно добавлен!', 'success');
        clearPatientForm();
        await loadPatients();

        // Автоматический выбор нового пациента
        selectPatient(result.id);

    } catch (error) {
        console.error('Полная ошибка:', error);
        showAlert('Ошибка добавления пациента: ' + error.message, 'danger');
    }
}

// Выбор пациента
// Улучшенная функция выбора пациента
async function selectPatient(patientId) {
    try {
        console.log('Выбран пациент ID:', patientId);
        showLoading(true);
        selectedPatientId = patientId;

        // Находим пациента в списке
        const patient = allPatients.find(p => p.id === patientId);
        console.log('Найден пациент:', patient);

        if (!patient) {
            throw new Error('Пациент не найден в списке');
        }

        // Обновление UI - ВАЖНО: всегда показываем панель пациента
        document.getElementById('noSelection').style.display = 'none';
        document.getElementById('patientPanel').style.display = 'block';

        // Обновляем информацию о пациенте
        document.getElementById('patientName').textContent = `${patient.firstName} ${patient.lastName}`;
        document.getElementById('infoAge').textContent = patient.age || 'Не указан';
        document.getElementById('infoAddress').textContent = patient.address || 'Не указан';
        document.getElementById('infoPhone').textContent = patient.phone || 'Не указан';

        console.log('UI обновлен, загружаем симптомы и визиты...');

        // Загрузка симптомов и визитов пациента
        await loadSymptoms();
        await loadVisits();

        // Активируем первую вкладку (Информация)
        const firstTab = document.querySelector('#patientTabs .nav-link');
        if (firstTab) {
            firstTab.click();
        } else {
            // Если вкладки не найдены, показываем информацию
            showTab('infoTab');
        }

    } catch (error) {
        console.error('Ошибка выбора пациента:', error);
        showAlert('Ошибка выбора пациента: ' + error.message, 'danger');
    } finally {
        showLoading(false);
    }
}

// Загрузка симптомов пациента
async function loadSymptoms() {
    if (!selectedPatientId) return;

    try {
        const res = await fetch(`/api/patients/${selectedPatientId}/symptoms`);
        if (!res.ok) throw new Error('Ошибка загрузки симптомов');

        allSymptoms = await res.json();
        renderSymptomsList(allSymptoms);
        loadTreatmentsAndAnalysis();
    } catch (error) {
        showAlert('Ошибка загрузки симптомов: ' + error.message, 'danger');
    }
}

// Рендер списка симптомов
function renderSymptomsList(symptoms) {
    const list = document.getElementById('symptomsList');
    list.innerHTML = '';

    if (symptoms.length === 0) {
        list.innerHTML = '<div class="text-muted">Симптомы не добавлены</div>';
        return;
    }

    symptoms.forEach(symptom => {
        const div = document.createElement('div');
        div.className = 'symptom-item d-flex justify-content-between align-items-center';
        div.innerHTML = `
            <div>
                <strong>${symptom.description}</strong>
                ${symptom.treatment ? `<br><small class="text-success">Лечение: ${symptom.treatment}</small>` : ''}
            </div>
        `;
        list.appendChild(div);
    });
}

// Добавление нового симптома
async function addSymptom() {
    if (!selectedPatientId) {
        showAlert('Сначала выберите пациента', 'warning');
        return;
    }

    const description = document.getElementById('newSymptomDesc').value.trim();
    if (!description) {
        showAlert('Введите описание симптома', 'warning');
        return;
    }

    try {
        const response = await fetch(`/api/patients/${selectedPatientId}/symptoms`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ description: description })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }

        document.getElementById('newSymptomDesc').value = '';
        document.getElementById('suggestions').innerHTML = '';
        showAlert('Симптом успешно добавлен', 'success');
        await loadSymptoms();

    } catch (error) {
        showAlert('Ошибка добавления симптома: ' + error.message, 'danger');
    }
}

// Загрузка лечения и анализов
async function loadTreatmentsAndAnalysis() {
    if (!selectedPatientId) return;

    try {
        // Загрузка лечения
        const treatmentsRes = await fetch(`/api/patients/${selectedPatientId}/treatments`);
        if (treatmentsRes.ok) {
            const treatments = await treatmentsRes.json();
            renderTreatmentsList(treatments);
        }

        // Загрузка анализов
        const testsRes = await fetch(`/api/patients/${selectedPatientId}/tests`);
        if (testsRes.ok) {
            const tests = await testsRes.json();
            renderAnalysisList(tests);
        }

    } catch (error) {
        console.error('Ошибка загрузки лечения/анализов:', error);
    }
}

// Рендер списка лечения
function renderTreatmentsList(treatments) {
    const list = document.getElementById('treatmentsList');
    list.innerHTML = '';

    if (!treatments || treatments.length === 0) {
        list.innerHTML = '<div class="text-muted">Лечение не назначено</div>';
        return;
    }

    treatments.forEach(treatment => {
        const div = document.createElement('div');
        div.className = 'symptom-item';
        div.textContent = treatment;
        list.appendChild(div);
    });
}

// Рендер списка анализов
function renderAnalysisList(analyses) {
    const list = document.getElementById('analysisList');
    list.innerHTML = '';

    if (!analyses || analyses.length === 0) {
        list.innerHTML = '<div class="text-muted">Анализы не назначены</div>';
        return;
    }

    analyses.forEach(analysis => {
        const div = document.createElement('div');
        div.className = 'analysis-item';
        div.textContent = analysis;
        list.appendChild(div);
    });
}

// Удаление пациента
async function deletePatient() {
    if (!selectedPatientId || !confirm('Вы уверены, что хотите удалить этого пациента?')) {
        return;
    }

    try {
        const response = await fetch(`/api/patients/${selectedPatientId}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Ошибка удаления пациента');

        showAlert('Пациент успешно удален', 'success');

        // Сброс UI
        selectedPatientId = null;
        document.getElementById('patientPanel').style.display = 'none';
        document.getElementById('noSelection').style.display = 'block';

        await loadPatients();

    } catch (error) {
        showAlert('Ошибка удаления пациента: ' + error.message, 'danger');
    }
}

// Генерация отчета
async function generateReport() {
    if (!selectedPatientId) return;

    try {
        const response = await fetch(`/api/patients/${selectedPatientId}/report`);
        if (!response.ok) throw new Error('Ошибка генерации отчета');

        const report = await response.text();
        document.getElementById('reportContent').textContent = report;
        showAlert('Отчет сгенерирован', 'success');
    } catch (error) {
        showAlert('Ошибка генерации отчета: ' + error.message, 'danger');
    }
}

// Скачивание PDF
function downloadPdf() {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    const reportText = document.getElementById('reportContent').textContent;

    if (!reportText || reportText === '') {
        showAlert('Сначала сгенерируйте отчет', 'warning');
        return;
    }

    // Простой PDF - можно улучшить форматирование
    doc.text(reportText, 10, 10);
    doc.save('medical-report.pdf');
    showAlert('PDF успешно скачан', 'success');
}

// Вспомогательные функции
function clearPatientForm() {
    document.getElementById('firstName').value = '';
    document.getElementById('lastName').value = '';
    document.getElementById('age').value = '';
    document.getElementById('address').value = '';
    document.getElementById('phone').value = '';
}

function showAlert(message, type) {
    // Удаляем существующие алерты
    const existingAlert = document.querySelector('.alert-message');
    if (existingAlert) {
        existingAlert.remove();
    }

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-message`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    document.body.appendChild(alertDiv);

    // Автоматическое скрытие через 5 секунд
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
}

function showLoading(show) {
    const buttons = document.querySelectorAll('button');
    buttons.forEach(button => {
        if (show) {
            button.disabled = true;
            button.classList.add('loading');
        } else {
            button.disabled = false;
            button.classList.remove('loading');
        }
    });
}

// Новые вспомогательные функции для дат

function formatDateTime(dateString) {
    if (!dateString) return 'не указано';
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU');
}

function getStatusBadgeClass(status) {
    switch (status) {
        case 'завершен': return 'bg-success';
        case 'в процессе': return 'bg-warning';
        default: return 'bg-secondary';
    }
}





// Показать/скрыть поля дат больничного
function toggleSickLeaveDates() {
    const sickLeaveCheck = document.getElementById('sickLeaveCheck');
    const sickLeaveDates = document.getElementById('sickLeaveDates');

    if (sickLeaveCheck.checked) {
        sickLeaveDates.style.display = 'block';

        // Установить даты по умолчанию
        const today = new Date();
        const endDate = new Date();
        endDate.setDate(today.getDate() + 7); // +7 дней по умолчанию

        document.getElementById('sickLeaveStart').value = formatDateForInput(today);
        document.getElementById('sickLeaveEnd').value = formatDateForInput(endDate);
    } else {
        sickLeaveDates.style.display = 'none';
    }
}

// Форматирование даты для input[type="date"]
function formatDateForInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}
function calculateSickLeaveDays(startDate, endDate) {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
}
// Расчет даты выхода на работу (следующий день после окончания больничного)
function getBackToWorkDate(sickLeaveEnd) {
    if (!sickLeaveEnd) return 'не указано';

    const endDate = new Date(sickLeaveEnd);
    const backToWorkDate = new Date(endDate);
    backToWorkDate.setDate(endDate.getDate() + 1);

    // Пропускаем выходные (простая реализация)
    const dayOfWeek = backToWorkDate.getDay();
    if (dayOfWeek === 6) { // Суббота
        backToWorkDate.setDate(backToWorkDate.getDate() + 2);
    } else if (dayOfWeek === 0) { // Воскресенье
        backToWorkDate.setDate(backToWorkDate.getDate() + 1);
    }

    return backToWorkDate.toLocaleDateString('ru-RU');
}


// Расчет дней больничного
function calculateSickLeaveDays(startDate, endDate) {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
}


// Функция для получения цвета роли
function getRoleColor(role) {
    switch(role) {
        case 'ADMIN': return 'danger';
        case 'DOCTOR': return 'primary';
        case 'NURSE': return 'success';
        default: return 'secondary';
    }
}

// Проверка авторизации
function checkAuth() {
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (!currentUser) {
        window.location.href = '/login.html';
        return null;
    }

    // Показать информацию о пользователе с цветом роли
    const roleColor = getRoleColor(currentUser.role);
    document.getElementById('userInfo').innerHTML =
        `${currentUser.fullName} <span class="badge bg-${roleColor}">${currentUser.role}</span>`;

    return currentUser;
}

// Выход
document.getElementById('logoutBtn').addEventListener('click', function() {
    localStorage.removeItem('currentUser');
    window.location.href = '/login.html';
});
// Проверка авторизации
async function checkAuth() {
    try {
        // Сначала проверяем сессию на сервере
        const response = await fetch('/api/auth/current-user', {
            credentials: 'include'
        });

        if (response.ok) {
            const userData = await response.json();
            localStorage.setItem('currentUser', JSON.stringify(userData));
            return userData;
        } else {
            // Если сессии нет, проверяем локальное хранилище
            const savedUser = localStorage.getItem('currentUser');
            if (!savedUser) {
                window.location.href = '/login.html';
                return null;
            }
            return JSON.parse(savedUser);
        }
    } catch (error) {
        console.error('Ошибка проверки авторизации:', error);
        const savedUser = localStorage.getItem('currentUser');
        if (!savedUser) {
            window.location.href = '/login.html';
            return null;
        }
        return JSON.parse(savedUser);
    }
}