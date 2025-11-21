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