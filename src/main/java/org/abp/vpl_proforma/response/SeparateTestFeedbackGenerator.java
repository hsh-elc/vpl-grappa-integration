package org.abp.vpl_proforma.response;

import proforma.util21.Proforma21HtmlFeedbackGenerator;

import proforma.xml21.SeparateTestFeedbackType;
import proforma.xml21.TaskType;
import proforma.xml21.ResponseFilesType;

import java.util.Base64;

/**
 * This class uses the implementation in Proforma21HtmlFeedbackGenerator for the generation
 * of separate HTML reports for student and teacher views.
 */
public class SeparateTestFeedbackGenerator extends HTMLResponseGenerator {
    
    private final Proforma21HtmlFeedbackGenerator htmlGenerator;

    public SeparateTestFeedbackGenerator(SeparateTestFeedbackType separateTestFeedback, TaskType task, ResponseFilesType responseFiles, double maxScoreLMS) {
        this.htmlGenerator = new Proforma21HtmlFeedbackGenerator(separateTestFeedback, task, responseFiles, maxScoreLMS);
    }

    @Override
    public void generateReport() {
        // Generate student view
        String studentHtml = this.htmlGenerator.buildFeedbackHtml(false, true, true);
        
        // Generate teacher view
        String teacherHtml = this.htmlGenerator.buildFeedbackHtml(true, true, true);
        
        // Calculate final grade
        double finalGrade = Math.round(this.htmlGenerator.getScore().doubleValue() * this.htmlGenerator.getScaleFactor());
        
        // Output the reports
        outputReport(studentHtml, teacherHtml, finalGrade);
    }

    @Override
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
        
        if (!htmlGenerator.getHasInternalError()) {
            System.out.println(OUTPUT_GRADE_PREFIX + grade);
        }
    }
} 