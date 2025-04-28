package org.abp.vpl_proforma.response.htmlformatter;

import org.abp.vpl_proforma.response.gradingstructure.CombineNode;
import org.abp.vpl_proforma.response.gradingstructure.TestNode;
import proforma.xml21.FeedbackType;
import proforma.xml21.SeparateTestFeedbackType;
import java.util.Base64;
import java.util.List;

/**
 * Concrete implementation of HTMLResponseGenerator for separate test feedback.
 * This class handles the generation of separate HTML reports for student and teacher views.
 * Contains all the HTML generation logic that was previously in the base class.
 */
public class SeparateTestFeedbackGenerator extends HTMLResponseGenerator {

    // --- Constants ---
    private static final String CSS_GRADING_NODE = "grading-node";
    private static final String CSS_TEST_REF = "test-ref";
    private static final String CSS_INDENT_PREFIX = "indent-";
    private static final String CSS_TITLE_CONTAINER = "title-container";
    private static final String CSS_TITLE_RESULT = "title-result";
    private static final String CSS_INNER_CONTENT = "inner-content";
    private static final String CSS_NULLIFY_REASON = "nullify-reason";
    private static final String CSS_FEEDBACK_CONTENT = "feedback-content";
    private static final String CSS_FEEDBACK_LIST = "feedback-list";
    private static final String CSS_COLLAPSIBLE = "collapsible";
    private static final String CSS_INNER_COLLAPSIBLE = "inner-collapsible";
    private static final String CSS_BOLD = "bold";
    private static final String CSS_CONTENT = "content";
    private static final String CSS_EXPAND_COLLAPSE_BUTTONS = "expand-collapse-buttons";
    private static final String CSS_EXPAND_COLLAPSE_BUTTON = "expand-collapse-button";

    private static final String HTML_BUTTON = "button";
    private static final String HTML_DIV = "div";
    private static final String HTML_P = "p";
    private static final String HTML_SPAN = "span";
    private static final String HTML_EM = "em";
    private static final String HTML_H4 = "h4";
    private static final String HTML_H2 = "h2";
    private static final String HTML_H1 = "h1";
    private static final String HTML_I = "i";

    private static final String FEEDBACK_TITLE_STUDENT = "Student Feedback";
    private static final String FEEDBACK_TITLE_TEACHER = "Teacher Feedback";
    private static final String FEEDBACK_TITLE_SUMMARIZED = "Summarized Feedback";
    private static final String FEEDBACK_TITLE_DETAILED = "Detailed Feedback";
    private static final String NO_FEEDBACK_MSG = "No feedback provided.";
    private static final String INTERNAL_FEEDBACK_PREFIX = "Internal: ";
    private static final String SCORE_CALC_PREFIX = "Score calculation: ";
    private static final String SCORE_CALC_SUFFIX = " of the following sub aspects";
    private static final String NULLIFIED_REASON_PREFIX = "Nullified. Reason for nullification:\n";
    private static final String NULLIFIED_SUFFIX = " [nullified]";
    private static final String DETAILS_BUTTON_TEXT = "Details";
    private static final String DETAILS_FEEDBACK_BUTTON_TEXT = "Details & Feedback";
    private static final String ROOT_NODE_TITLE = "Overall result";
    private static final String TEST_RESULT_CORRECT = "correct";
    private static final String TEST_RESULT_WRONG = "wrong";
    // --- End Constants ---

    private final SeparateTestFeedbackType separateTestFeedback;
    private final CombineNode gradingStructure;
    private final double scaleFactor;
    private final boolean hasInternalError;

    /**
     * Constructor for SeparateTestFeedbackGenerator.
     * 
     * @param separateTestFeedback 
     * @param gradingStructur
     * @param scaleFactor
     */
    public SeparateTestFeedbackGenerator(
            SeparateTestFeedbackType separateTestFeedback,
            CombineNode gradingStructure,
            double scaleFactor,
            boolean hasInternalError) {
        this.separateTestFeedback = separateTestFeedback;
        this.gradingStructure = gradingStructure;
        this.scaleFactor = scaleFactor;
        this.hasInternalError = hasInternalError;
    }

    @Override
    public void generateReport() {
        // Generate student view
        String studentHtml = buildReportHtml(false);
        
        // Generate teacher view
        String teacherHtml = buildReportHtml(true);
        
        // Calculate final grade
        double finalGrade = Math.round(gradingStructure.getActualScore() * scaleFactor * 100.0) / 100.0;
        
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
        
        if (!hasInternalError) {
            System.out.println(OUTPUT_GRADE_PREFIX + grade);
        }
    }

    /**
     * Builds the complete HTML report string for either the student or teacher view.
     * @param includeTeacherFeedback If true, includes teacher-specific feedback and internal descriptions.
     * @return The complete HTML document as a String.
     */
    private String buildReportHtml(boolean includeTeacherFeedback) {
        StringBuilder sb = new StringBuilder();

        initializeHtmlDocument(sb);
        addHeadSection(sb);
        addBodySection(sb);

        // Add Summarized Feedback Section
        addCollapsibleFeedbackSection(sb, FEEDBACK_TITLE_SUMMARIZED);
        addStudentFeedbackList(sb, separateTestFeedback);
        if (includeTeacherFeedback) {
            addTeacherFeedbackList(sb, separateTestFeedback);
        }
        sb.append("</div>\n"); // Close summarized content div

        // Add Detailed Feedback Section
        addCollapsibleFeedbackSection(sb, FEEDBACK_TITLE_DETAILED);
        processGradingNodeRecursive(gradingStructure, scaleFactor, sb, includeTeacherFeedback); // Start recursive processing
        sb.append("</div>\n"); // Close detailed content div

        finalizeHtmlDocument(sb);
        return sb.toString();
    }

    /**
     * Recursive method to process a CombineNode and its children.
     */
    private void processGradingNodeRecursive(CombineNode node, double scaleFactor, StringBuilder sb, boolean includeTeacherFeedback) {
        String title = node.getTitle();
        double actualScore = node.isNullified() ? 0 : Math.round(node.getActualScore() * scaleFactor * 100.0) / 100.0;
        double maxScore = Math.round(node.getMaxScore() * scaleFactor * 100.0) / 100.0;

        appendCombineNodeStart(sb, node, title, maxScore, actualScore);

        sb.append("    <" + HTML_DIV + " class=\"" + CSS_CONTENT + " " + CSS_INNER_CONTENT + "\">\n");

        if (node.isNullified()) {
            appendNullificationInfo(sb, node.getNullifyReason());
        }

        appendDescription(sb, node.getDescription(), includeTeacherFeedback ? node.getInternalDescription() : null);

        appendScoreCalculationInfo(sb, node.getFunction());

        sb.append("    </" + HTML_DIV + ">\n"); // Close inner-content div

        for (var child : node.getChildren()) {
            if (child instanceof TestNode testChild) {
                processTestNode(testChild, scaleFactor, sb, includeTeacherFeedback);
            } else if (child instanceof CombineNode combineChild) {
                processGradingNodeRecursive(combineChild, scaleFactor, sb, includeTeacherFeedback);
            }
        }

        sb.append("</" + HTML_DIV + ">\n"); // Close grading-node div
    }

    /**
     * Processes a TestNode.
     */
    private void processTestNode(TestNode testNode, double scaleFactor, StringBuilder sb, boolean includeTeacherFeedback) {
        double actualScore = testNode.isNullified() ? 0 : testNode.getActualScore();
        double maxScore = Math.round(testNode.getMaxScore() * scaleFactor * 100.0) / 100.0;
        double scaledActualScore = Math.round(actualScore * scaleFactor * 100.0) / 100.0;

        appendTestNodeStart(sb, testNode, maxScore, testNode.getRawScore(), scaledActualScore);

        sb.append("    <" + HTML_DIV + " class=\"" + CSS_CONTENT + " " + CSS_INNER_CONTENT + "\">\n");

        if (testNode.isNullified() && testNode.getRawScore() != 0) {
             appendNullificationInfo(sb, testNode.getNullifyReason());
        }

        appendDescription(sb, testNode.getDescription(), includeTeacherFeedback ? testNode.getInternalDescription() : null);

        // Group Student Feedback
        List<String> studentFeedbackList = testNode.getStudentFeedback();
        if (studentFeedbackList != null && !studentFeedbackList.isEmpty()) {
            sb.append("    <" + HTML_H4 + ">" + escapeHtml(FEEDBACK_TITLE_STUDENT) + "</" + HTML_H4 + ">\n");
            sb.append("  <" + HTML_DIV + " class=\"" + CSS_FEEDBACK_CONTENT + "\">");
            for (String feedback : studentFeedbackList) {
                sb.append(feedback).append("<br>\n");
            }
            sb.append("  </" + HTML_DIV + ">\n"); 
        }

        // Group Teacher Feedback
        if (includeTeacherFeedback) {
            List<String> teacherFeedbackList = testNode.getTeacherFeedback();
            if (teacherFeedbackList != null && !teacherFeedbackList.isEmpty()) {
                sb.append("    <" + HTML_H4 + ">" + escapeHtml(FEEDBACK_TITLE_TEACHER) + "</" + HTML_H4 + ">\n");
                sb.append("  <" + HTML_DIV + " class=\"" + CSS_FEEDBACK_CONTENT + "\">");
                for (String feedback : teacherFeedbackList) {
                    sb.append(feedback).append("<br>\n");
                }
                 sb.append("  </" + HTML_DIV + ">\n");
            }
        }

        sb.append("    </" + HTML_DIV + ">\n"); // Close inner-content div

        sb.append("</" + HTML_DIV + ">\n"); // Close test-ref div
    }

    /** Appends the starting divs and title container for a CombineNode. */
    private void appendCombineNodeStart(StringBuilder sb, CombineNode node, String title, double maxScore, double actualScore) {
        sb.append("<" + HTML_DIV + " class=\"" + CSS_GRADING_NODE + " " + CSS_INDENT_PREFIX + node.getIndentLevel() + "\">\n");
        sb.append("  <" + HTML_DIV + " class=\"" + CSS_TITLE_CONTAINER + "\" style=\"padding-bottom: 5px;\">\n"); // Inline style kept for now
        String titleText = node.getRefId().equals("root") ? ROOT_NODE_TITLE : escapeHtml(title);
        sb.append("    <" + HTML_P + ">" + String.format("%s [max. %.2f]",
                titleText, maxScore) + "</" + HTML_P + ">\n");
        sb.append("    <" + HTML_SPAN + " class=\"" + CSS_TITLE_RESULT + "\">" + String.format("[actual score. %.2f]", actualScore) + "</" + HTML_SPAN + ">\n");
        sb.append("    <" + HTML_BUTTON + " class=\"" + CSS_COLLAPSIBLE + " " + CSS_INNER_COLLAPSIBLE + "\">" + DETAILS_BUTTON_TEXT + "</" + HTML_BUTTON + ">\n");
        sb.append("  </" + HTML_DIV + ">\n"); // Close title-container div
    }

    /** Appends the starting divs and title container for a TestNode. */
    private void appendTestNodeStart(StringBuilder sb, TestNode testNode, double maxScore, double rawScore, double actualScore) {
        int indentLevel = testNode.getIndentLevel();
        sb.append("<" + HTML_DIV + " class=\"" + CSS_TEST_REF + " " + CSS_INDENT_PREFIX + indentLevel + "\">\n");
        String titleClass = rawScore == 0 ? CSS_TITLE_CONTAINER + " " + CSS_BOLD : CSS_TITLE_CONTAINER;
        sb.append("  <" + HTML_DIV + " class=\"" + titleClass + "\">\n");
        sb.append("    <" + HTML_P + ">" + escapeHtml(testNode.getTitle()) + String.format(" [max. %.2f]", maxScore) + "</" + HTML_P + ">\n");

        // Build result text
        StringBuilder resultText = new StringBuilder();
        resultText.append(String.format("[raw test score. %.2f]", rawScore))
                  .append(String.format(" [actual score. %.2f]", actualScore))
                  .append(" - ").append(rawScore != 0 ? TEST_RESULT_CORRECT : TEST_RESULT_WRONG);
        if (rawScore != 0 && testNode.isNullified()) {
            resultText.append(NULLIFIED_SUFFIX);
        }
        sb.append("    <" + HTML_SPAN + " class=\"" + CSS_TITLE_RESULT + "\">" + resultText.toString() + "</" + HTML_SPAN + ">\n");
        sb.append("    <" + HTML_BUTTON + " class=\"" + CSS_COLLAPSIBLE + " " + CSS_INNER_COLLAPSIBLE + "\">" + DETAILS_FEEDBACK_BUTTON_TEXT + "</" + HTML_BUTTON + ">\n");
        sb.append("  </" + HTML_DIV + ">\n"); // Close title-container div
    }

    /** Appends nullification reason if present. */
    private void appendNullificationInfo(StringBuilder sb, String reason) {
         if (reason != null && !reason.isEmpty()) {
            sb.append("  <" + HTML_DIV + " class=\"" + CSS_NULLIFY_REASON + "\">")
              .append(NULLIFIED_REASON_PREFIX)
              .append(escapeHtml(reason))
              .append("</" + HTML_DIV + ">\n");
         }
    }

    /** Appends description and optional internal description. */
    private void appendDescription(StringBuilder sb, String description, String internalDescription) {
        if (description != null && !description.isEmpty()) {
            sb.append("  <" + HTML_P + ">" + escapeHtml(description) + "</" + HTML_P + ">\n");
        }
        // Internal description is appended only if it exists (controlled by includeTeacherFeedback flag earlier)
        if (internalDescription != null && !internalDescription.isEmpty()) {
             sb.append("  <" + HTML_P + "><" + HTML_I + ">" + INTERNAL_FEEDBACK_PREFIX + escapeHtml(internalDescription) + "</" + HTML_I + "></" + HTML_P + ">\n");
        }
    }

    /** Appends score calculation info. */
    private void appendScoreCalculationInfo(StringBuilder sb, String function) {
        if (function != null && !function.isEmpty()) {
            String calculationText = SCORE_CALC_PREFIX + escapeHtml(function) + SCORE_CALC_SUFFIX;
            sb.append("  <" + HTML_P + "><" + HTML_EM + ">" + calculationText + "</" + HTML_EM + "></" + HTML_P + ">\n");
        }
    }

    private void initializeHtmlDocument(StringBuilder sb) {
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
    }

    private void addHeadSection(StringBuilder sb) {
        sb.append("<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <title>Evaluation Report</title>\n");
        sb.append("  <style>\n")
          .append(getCommonStyles())
          .append("\n  </style>\n");
        sb.append("  <script>\n")
           .append(getCommonScript())
           .append("\n  </script>\n");
        sb.append("</head>\n");
    }

    private void addBodySection(StringBuilder sb) {
        sb.append("<body>\n");
    }

    private void addCollapsibleFeedbackSection(StringBuilder sb, String title) {
        sb.append("<" + HTML_BUTTON + " class=\"" + CSS_COLLAPSIBLE + "\">\n")
          .append("  <" + HTML_H1 + ">" + escapeHtml(title) + "</" + HTML_H1 + ">\n")
          .append("</" + HTML_BUTTON + ">\n");
        // Content div is opened here, closed after content is added
        sb.append("<" + HTML_DIV + " class=\"content\">\n");
        
        // Add expand/collapse buttons for detailed feedback section
        if (title.equals(FEEDBACK_TITLE_DETAILED)) {
            sb.append("  <" + HTML_DIV + " class=\"" + CSS_EXPAND_COLLAPSE_BUTTONS + "\">\n")
              .append("    <" + HTML_BUTTON + " class=\"" + CSS_EXPAND_COLLAPSE_BUTTON + "\" id=\"expand-all\">Expand All</" + HTML_BUTTON + ">\n")
              .append("    <" + HTML_BUTTON + " class=\"" + CSS_EXPAND_COLLAPSE_BUTTON + "\" id=\"collapse-all\">Collapse All</" + HTML_BUTTON + ">\n")
              .append("  </" + HTML_DIV + ">\n");
        }
    }

    private void addStudentFeedbackList(StringBuilder sb, SeparateTestFeedbackType separateTestFeedback) {
        addFeedbackListSection(sb, FEEDBACK_TITLE_STUDENT,
            separateTestFeedback.getSubmissionFeedbackList().getStudentFeedback());
    }

    private void addTeacherFeedbackList(StringBuilder sb, SeparateTestFeedbackType separateTestFeedback) {
        addFeedbackListSection(sb, FEEDBACK_TITLE_TEACHER,
            separateTestFeedback.getSubmissionFeedbackList().getTeacherFeedback());
    }

    private void addFeedbackListSection(StringBuilder sb, String title, List<FeedbackType> feedbackList) {
        sb.append("  <" + HTML_H2 + ">" + escapeHtml(title) + "</" + HTML_H2 + ">\n");
        sb.append("  <" + HTML_DIV + " class=\"" + CSS_FEEDBACK_LIST + "\">\n");
        if (feedbackList != null && !feedbackList.isEmpty()) {
            for (FeedbackType feedback : feedbackList) {
                // Assuming feedback content is safe HTML/text
                 sb.append(feedback.getContent().getValue()).append("\n");
            }
        } else {
            sb.append("    <" + HTML_P + "><" + HTML_I + ">" + NO_FEEDBACK_MSG + "</" + HTML_I + "></" + HTML_P + ">\n");
        }
        sb.append("  </" + HTML_DIV + ">\n");
    }

    // Style and Script
    private String getCommonStyles() {
        return """
            h1, h2 { color: navy; margin-bottom: 0; }
            h3 { font-style: italic; }
            .bold { font-weight: bold; }
            .feedback-list, .feedback-content { margin-bottom: 5px}
            pre { white-space: pre-wrap; word-wrap: break-word; background-color: #f8f8f8; border: 1px solid #ddd; padding: 5px; margin-top: 5px; }
            .grading-node { padding: 5px; margin-bottom: 0; border-bottom: 1px solid #ddd; }
            .test-ref { padding: 5px; margin-bottom: 0; border-bottom: 1px solid #ddd; }
            .indent-0 { margin-left: 0px; background-color: #b0b0b0; }
            .indent-1 { margin-left: 20px; background-color: #c0c0c0; }
            .indent-2 { margin-left: 40px; background-color: #d0d0d0; }
            .indent-3 { margin-left: 60px; background-color: #e0e0e0; }
            .indent-4 { margin-left: 80px; background-color: #f0f0f0; }
            .indent-5 { margin-left: 100px; background-color: #f9f9f9; }
            .collapsible { cursor: pointer; padding: 10px; width: 100%; border: none; text-align: left; outline: none; font-size: 15px; background-color: #eee; margin-top: 5px; }
            .active, .collapsible:hover { background-color: #ccc; color: black; }
            .content { padding: 0; overflow: hidden; transition: max-height 0.2s ease-out; background-color: inherit; }
            .content:not(.visible) { max-height: 0; padding-top: 0; padding-bottom: 0; border: none; margin: 0; }
            .content.visible { max-height: none; } 
            .section { margin-bottom: 20px; border: 1px solid #aaa; padding: 10px; }
            .inner-collapsible { font-size: 12px; padding: 5px 10px; background-color: #f0f0f0; margin-top: 3px; width: auto; display: inline-block; border: 1px solid #ccc; }
            .inner-collapsible.active, .inner-collapsible:hover { background-color: #ccc; color: black; }
            .title-container { display: flex; justify-content: space-between; align-items: center; gap: 10px; flex-wrap: wrap; }
            .title-container p, .title-container h3 { margin: 0; flex-grow: 1; }
            .title-result { margin-left: auto; padding-left: 10px; white-space: normal; font-style: italic; font-size: 1.0em; }
            .inner-collapsible { margin-top: 0; flex-shrink: 0; }
            .inner-content { padding: 5px 10px; border-top: 1px dashed #ccc; margin-left: 10px; background-color: inherit; }
            body { max-width: 1300px; margin: 20px auto; border: 1px solid #ccc; box-shadow: 2px 2px 5px rgba(0,0,0,0.1); padding: 10px; }
            .nullify-reason { color: red; font-style: italic; font-weight: bold; margin-top: 10px; }
            .expand-collapse-buttons { display: flex; gap: 10px; margin: 10px 0; padding: 10px; justify-content: flex-end; }
            .expand-collapse-button { padding: 5px 10px; background-color:navy; color: white; border: none; border-radius: 4px; cursor: pointer; }
            .expand-collapse-button:hover { background-color: rgb(65, 105, 225); } /* RoyalBlue - lighter than navy */
            """;
    }
    
    private String getCommonScript() {
        return """
            document.addEventListener('DOMContentLoaded', function() {
              var coll = document.getElementsByClassName('collapsible');
              for (var i = 0; i < coll.length; i++) {
                // Determine the target content element based on button type
                var button = coll[i];
                var content;
                if (button.classList.contains('inner-collapsible')) {
                  // For inner buttons, content is the next sibling of the button's parent container
                  content = button.parentElement.nextElementSibling;
                } else {
                  // For top-level buttons, content is the direct next sibling
                  content = button.nextElementSibling;
                }

                // Set initial state based on presence of 'visible' class
                if (content && !content.classList.contains('visible')) {
                   content.style.maxHeight = '0px';
                } else if (content) {
                   content.style.maxHeight = 'none'; // Allows content to determine height naturally
                }

                button.addEventListener('click', function() {
                  this.classList.toggle('active');
                  var currentContent;
                  // Find the correct content element again on click
                  if (this.classList.contains('inner-collapsible')) {
                    currentContent = this.parentElement.nextElementSibling;
                  } else {
                    currentContent = this.nextElementSibling;
                  }

                  if (!currentContent) return; // Exit if no content element found

                  if (currentContent.style.maxHeight && currentContent.style.maxHeight !== '0px'){
                    // Close it: Set max-height to 0
                    currentContent.style.maxHeight = '0px';
                  } else {
                    // Open it: Set max-height to its scroll height for animation
                    currentContent.style.maxHeight = currentContent.scrollHeight + 'px';
                    // Optional: After animation, set to 'none' to allow dynamic content resizing
                    setTimeout(() => { if (currentContent && currentContent.style.maxHeight !== '0px') currentContent.style.maxHeight = 'none'; }, 200); // Match transition duration
                  }
                });
              }
              
              // Add event listeners for Expand All and Collapse All buttons
              var expandAllBtn = document.getElementById('expand-all');
              var collapseAllBtn = document.getElementById('collapse-all');
              
              if (expandAllBtn) {
                expandAllBtn.addEventListener('click', function() {
                  var innerCollapsibles = document.getElementsByClassName('inner-collapsible');
                  for (var i = 0; i < innerCollapsibles.length; i++) {
                    var button = innerCollapsibles[i];
                    var content = button.parentElement.nextElementSibling;
                    
                    if (content && content.style.maxHeight === '0px') {
                      button.classList.add('active');
                      content.style.maxHeight = content.scrollHeight + 'px';
                      setTimeout(() => { if (content && content.style.maxHeight !== '0px') content.style.maxHeight = 'none'; }, 200);
                    }
                  }
                });
              }
              
              if (collapseAllBtn) {
                collapseAllBtn.addEventListener('click', function() {
                  var innerCollapsibles = document.getElementsByClassName('inner-collapsible');
                  for (var i = 0; i < innerCollapsibles.length; i++) {
                    var button = innerCollapsibles[i];
                    var content = button.parentElement.nextElementSibling;
                    
                    if (content && content.style.maxHeight !== '0px') {
                      button.classList.remove('active');
                      content.style.maxHeight = '0px';
                    }
                  }
                });
              }
            });
            """;
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        // Basic escaping, consider a library for more robust escaping if needed later
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private void finalizeHtmlDocument(StringBuilder sb) {
        sb.append("</body>\n");
        sb.append("</html>\n");
    }
} 