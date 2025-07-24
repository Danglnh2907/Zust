<%@ page import="model.Account" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<!-- REPORT FORM -->

<html>
    <head>
        <title>Title</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/report.css">
    </head>
    <body>
        <%
            Account account = (Account) request.getAttribute("account");
            String type = (String) request.getAttribute("type");
            int id = (int) request.getAttribute("id");
            String reportLink = (String) request.getAttribute("reportLink");
        %>
        <div class="report-form-card">
                <h1>Report Content</h1>
                <form id="report-form">
                    <div class="form-group">
                        <label for="report-url">Link to Content or Username to Report</label>
                        <input type="text" id="report-url" readonly disabled
                               value="${pageContext.request.contextPath}<%=reportLink%>">
                        <p>Please provide a specific link or username for our moderators to review.</p>
                    </div>

                    <div class="form-group">
                        <label>Reason for Report</label>
                        <div class="report-options-list">
                            <label class="report-option">
                                <input type="checkbox" name="report_reason" value="hate_speech"><span
                                    class="custom-checkbox"></span>
                                <span class="option-text">Hate Speech</span>
                            </label>
                            <label class="report-option">
                                <input type="checkbox" name="report_reason" value="spam"><span
                                    class="custom-checkbox"></span>
                                <span class="option-text">Spam or Misleading</span>
                            </label>
                            <label class="report-option">
                                <input type="checkbox" name="report_reason" value="harassment"><span
                                    class="custom-checkbox"></span>
                                <span class="option-text">Harassment</span>
                            </label>
                            <label class="report-option">
                                <input type="checkbox" name="report_reason" value="sexual_content"><span
                                    class="custom-checkbox"></span>
                                <span class="option-text">Nudity or Sexual Content</span>
                            </label>
                            <label class="report-option">
                                <input type="checkbox" name="report_reason" value="impersonation"><span
                                    class="custom-checkbox"></span>
                                <span class="option-text">Impersonation</span>
                            </label>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="report-details-textarea">Other reasons (Optional)</label>
                        <textarea id="report-details-textarea"
                                  placeholder="Provide any other relevant information..."></textarea>
                    </div>

                    <div class="form-footer">
                        <button type="submit" class="submit-report-btn" id="submit-report-btn">Submit Report
                        </button>
                    </div>
                </form>
            </div>

        <script>
            document.addEventListener('DOMContentLoaded', () => {
                const reportForm = document.getElementById('report-form');
                const submitBtn = document.getElementById('submit-report-btn');
                const checkboxes = reportForm.querySelectorAll('input[name="report_reason"]');
                const detail = document.getElementById("report-details-textarea");

                function validateForm() {
                    const isReasonChecked = Array.from(checkboxes).some(cb => cb.checked);
                    //submitBtn.disabled = !(detail.trim() !== "" || isReasonChecked);
                    submitBtn.disabled = detail.value === "" && !isReasonChecked;
                }

                //Live validation
                checkboxes.forEach(cb => cb.addEventListener('change', validateForm));
                detail.addEventListener('input', validateForm);

                reportForm.addEventListener('submit', (e) => {
                    e.preventDefault();
                    const reasons = Array.from(checkboxes)
                        .filter(cb => cb.checked)
                        .map(cb => cb.value);
                    const additionalDetails = document.getElementById('report-details-textarea').value;

                    console.log("--- Report Submitted ---");
                    console.log("Reasons:", reasons);
                    console.log("Details:", additionalDetails);

                    //Create data
                    const data = new URLSearchParams();
                    data.append("id", "<%=id%>");
                    data.append("type", "<%=type%>");
                    data.append("content", reasons.join("#") + "#" + additionalDetails);
                    fetch("/zust/report", {
                        method: "POST",
                        body: data
                    })
                        .then(resp => {
                            if (resp.status === 201) {
                                alert("Report success");
                                console.log("Report success " + resp.status);
                                    window.location.href = "/zust/"
                            }
                        })
                        .catch(error => {
                            console.log(error);
                            alert(error);
                        })
                });

                validateForm();
            });
        </script>
    </body>
</html>
