document.addEventListener('DOMContentLoaded', () => {
    const reportForm = document.getElementById('report-form');
    const submitBtn = document.getElementById('submit-report-btn');
    const urlInput = document.getElementById('report-url');
    const checkboxes = reportForm.querySelectorAll('input[name="report_reason"]');

    function validateForm() {
        const isUrlFilled = urlInput.value.trim() !== '';
        const isReasonChecked = Array.from(checkboxes).some(cb => cb.checked);

        submitBtn.disabled = !(isUrlFilled && isReasonChecked);
    }

    urlInput.addEventListener('input', validateForm);
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', validateForm);
    });

    reportForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const reportUrl = urlInput.value;
        const reasons = Array.from(checkboxes)
            .filter(cb => cb.checked)
            .map(cb => cb.value);
        const additionalDetails = document.getElementById('report-details-textarea').value;

        console.log("--- Report Submitted ---");
        console.log("URL/Username:", reportUrl);
        console.log("Reasons:", reasons);
        console.log("Details:", additionalDetails);

        alert('Thank you, your report has been submitted. Our team will review it shortly.');
        window.location.href = "/zust/";
    });

    validateForm();
});