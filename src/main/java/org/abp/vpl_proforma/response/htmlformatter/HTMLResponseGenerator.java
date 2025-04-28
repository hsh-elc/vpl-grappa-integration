package org.abp.vpl_proforma.response.htmlformatter;

import java.util.Base64;

/**
 * Abstract base class for HTML response generators.
 * Contains only the minimal shared functionality for outputting reports.
 */
public abstract class HTMLResponseGenerator {

    // --- Constants for output formatting ---
    protected static final String STUDENT_OUTPUT_HEADER_START = "<|--";
    protected static final String STUDENT_OUTPUT_HEADER_END = "--|>";
    protected static final String OUTPUT_BASE64_PREFIX = "data:text/html;base64,";
    protected static final String OUTPUT_GRADE_PREFIX = "Grade :=>>";

    /**
     * Abstract method to be implemented by subclasses to generate the appropriate HTML report.
     */
    public abstract void generateReport();

    /**
     * Handles the common logic for outputting the final report(s) to System.out.
     */
    protected void outputReport(String studentHtml, String teacherHtml, double grade) {
        outputStandardHeader(); // Print initial instructions

        System.out.println("""
            ///////////////////////////////
            /// Student feedback //////////
            ///////////////////////////////
        """);
        System.out.println(STUDENT_OUTPUT_HEADER_START);
        System.out.println(OUTPUT_BASE64_PREFIX + Base64.getEncoder().encodeToString(studentHtml.getBytes()));
        System.out.println(STUDENT_OUTPUT_HEADER_END);

        System.out.println("""
            ///////////////////////////////
            /// Teacher feedback //////////
            ///////////////////////////////
        """);
        System.out.println(OUTPUT_BASE64_PREFIX + Base64.getEncoder().encodeToString(teacherHtml.getBytes()));
        
        System.out.println(OUTPUT_GRADE_PREFIX + grade);
    }

    protected void outputStandardHeader() {
        System.out.println(STUDENT_OUTPUT_HEADER_START);
        System.out.println("""
        ━━━━ EVALUATION REPORT ━━━━
        
        -To access the grading results, please follow the following steps:
        1. Triple-click the text provided below to select it fully.
        2. Right-click the selected text and choose "Copy," or simply press `Ctrl+C` on your keyboard to copy the text.
        3. Navigate to your web browser and focus on the URL bar at the top. You can do this by clicking on the URL bar or pressing `Ctrl+L` on your keyboard.
        4. Right-click inside the URL bar and select "Paste," or press `Ctrl+V` on your keyboard to paste the previously copied text.
        5. Press `Enter` to navigate to the link and view your grading results.

        -Text to copy:""");
        System.out.println(STUDENT_OUTPUT_HEADER_END);
    }
}
