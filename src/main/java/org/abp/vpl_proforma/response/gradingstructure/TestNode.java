package org.abp.vpl_proforma.response.gradingstructure;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a test node in the grading structure
 */
public class TestNode extends GradingNode {
    private final String subRefId;
    private final List<String> studentFeedback;
    private final List<String> teacherFeedback;

    public TestNode(String refId, String title, String description,
                   String internalDescription, double weight, double rawScore,
                   double maxScore, double actualScore, int indentLevel,
                   String subRefId, List<String> studentFeedback,
                   List<String> teacherFeedback) {
        super(refId, NodeType.TEST, title, description, internalDescription,
              weight, rawScore, maxScore, actualScore, indentLevel);
        this.subRefId = subRefId;
        this.studentFeedback = new ArrayList<>(studentFeedback);
        this.teacherFeedback = new ArrayList<>(teacherFeedback);
    }

    // Getters
    public String getSubRefId() { return subRefId; }
    public List<String> getStudentFeedback() { return new ArrayList<>(studentFeedback); }
    public List<String> getTeacherFeedback() { return new ArrayList<>(teacherFeedback); }
}