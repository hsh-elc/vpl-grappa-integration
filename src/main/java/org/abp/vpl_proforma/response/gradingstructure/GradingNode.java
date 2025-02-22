package org.abp.vpl_proforma.response.gradingstructure;


/**
 * Abstract base class representing a node in the grading structure
 */
public abstract class GradingNode {
    private final String refId;
    private final NodeType type;
    private final String title;
    private final String description;
    private final String internalDescription;
    
    private final double weight;
    private final double rawScore;
    private final double maxScore;
    private double actualScore;
    
    private final int indentLevel;
    
    private boolean nullified;
    private String nullifyReason;

    protected GradingNode(String refId, NodeType type, String title, String description,
                         String internalDescription, double weight, double rawScore,
                         double maxScore, double actualScore, int indentLevel) {
        this.refId = refId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.internalDescription = internalDescription;
        this.weight = weight;
        this.rawScore = rawScore;
        this.maxScore = maxScore;
        this.actualScore = actualScore;
        this.indentLevel = indentLevel;
        this.nullified = false;
        this.nullifyReason = "";
    }

    // Getters
    public String getRefId() { return refId; }
    public NodeType getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInternalDescription() { return internalDescription; }
    public double getWeight() { return weight; }
    public double getRawScore() { return rawScore; }
    public double getMaxScore() { return maxScore; }
    public double getActualScore() { return actualScore; }
    public int getIndentLevel() { return indentLevel; }
    public boolean isNullified() { return nullified; }
    public String getNullifyReason() { return nullifyReason; }

    // Setters for mutable properties
    public void setActualScore(double actualScore) { this.actualScore = actualScore; }
    public void setNullified(boolean nullified) { this.nullified = nullified; }
    public void setNullifyReason(String nullifyReason) { this.nullifyReason = nullifyReason; }
}