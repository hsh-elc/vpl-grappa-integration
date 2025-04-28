package org.abp.vpl_proforma.response.htmlformatter;

/**
 * Concrete implementation of HTMLResponseGenerator for merged test feedback.
 * This class handles the output of pre-merged student and teacher feedback.
 */
public class MergedTestFeedbackGenerator extends HTMLResponseGenerator {

    private final String studentFeedback;
    private final String teacherFeedback;
    private final double grade;

    /**
     * Constructor for MergedTestFeedbackGenerator.
     * 
     * @param studentFeedback 
     * @param teacherFeedback 
     * @param grade
     */
    public MergedTestFeedbackGenerator(
            String studentFeedback,
            String teacherFeedback,
            double grade) {
        this.studentFeedback = studentFeedback;
        this.teacherFeedback = teacherFeedback;
        this.grade = grade;
    }
 
    @Override
    public void generateReport() {
        outputReport(studentFeedback, teacherFeedback, grade);
    }
} 