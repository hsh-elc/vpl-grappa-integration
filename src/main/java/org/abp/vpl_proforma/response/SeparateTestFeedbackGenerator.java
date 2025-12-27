package org.abp.vpl_proforma.response;

import proforma.util21.Proforma21HtmlFeedbackGenerator;

import proforma.xml21.SeparateTestFeedbackType;
import proforma.xml21.TaskType;
import proforma.xml21.ResponseFilesType;

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
        super.outputReport(studentHtml, teacherHtml, finalGrade);
    }

    @Override
    protected void outputGrade(double grade) {
        if (!htmlGenerator.getHasInternalError()) {
            super.outputGrade(grade);
        } else {
            super.outputGrade(0.0);
        }
    }
} 
