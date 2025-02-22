package org.abp.vpl_proforma.response.gradingstructure;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a combine node in the grading structure that can have child nodes
 */
public class CombineNode extends GradingNode {
    private final String function;
    private final List<GradingNode> children;
    private boolean nullificationChecked;

    public CombineNode(String refId, String title, String description,
                      String internalDescription, double weight, double rawScore,
                      double maxScore, double actualScore, int indentLevel,
                      String function, List<GradingNode> children) {
        super(refId, NodeType.COMBINE, title, description, internalDescription,
              weight, rawScore, maxScore, actualScore, indentLevel);
        this.function = function;
        this.children = new ArrayList<>(children);
        this.nullificationChecked = false;
    }

    // Getters
    public String getFunction() { return function; }
    public List<GradingNode> getChildren() { return new ArrayList<>(children); }
    public boolean isNullificationChecked() { return nullificationChecked; }

    // Setter for mutable property
    public void setNullificationChecked(boolean nullificationChecked) {
        this.nullificationChecked = nullificationChecked;
    }

    // Method to add a child node
    public void addChild(GradingNode child) {
        children.add(child);
    }
}