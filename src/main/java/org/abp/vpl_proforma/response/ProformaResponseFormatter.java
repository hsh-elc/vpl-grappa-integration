package org.abp.vpl_proforma.response;

import proforma.xml21.GradesBaseRefChildType;
import proforma.xml21.GradesCombineRefChildType;
import proforma.xml21.GradesNodeType;
import proforma.xml21.GradesTestRefChildType;
import proforma.xml21.GradingHintsType;
import proforma.xml21.TestsType;
import proforma.xml21.ResponseType;
import proforma.xml21.ResultType;
import proforma.xml21.SeparateTestFeedbackType;
import proforma.xml21.SubtestResponseType;
import proforma.xml21.SubtestsResponseType;
import proforma.xml21.TestResponseType;
import proforma.xml21.TestResultType;
import proforma.xml21.TestType;
import proforma.xml21.TestsResponseType;

import org.abp.vpl_proforma.response.gradingstructure.CombineNode;
import org.abp.vpl_proforma.response.gradingstructure.GradingNode;
import org.abp.vpl_proforma.response.gradingstructure.TestNode;
import org.abp.vpl_proforma.response.html.HTMLResponseGenerator;
import org.abp.vpl_proforma.utility.GradingHintsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProformaResponseFormatter {

    private ResponseType responsePojo;
    private GradingHintsType taskGradingHintsElem;
    private TestsType taskTestElem;
    private HTMLResponseGenerator htmlGenerator;

    public ProformaResponseFormatter(ResponseType responsePojo, GradingHintsType taskGradingHintsElem, TestsType taskTestElem) {
        this.responsePojo = responsePojo;
        this.taskGradingHintsElem = taskGradingHintsElem;
        this.taskTestElem = taskTestElem;
        this.htmlGenerator = new HTMLResponseGenerator();
    }

    public void processResult(double maxScoreLMS) {
        if (responsePojo.getMergedTestFeedback() != null) {
            double grade = responsePojo.getMergedTestFeedback().getOverallResult().getScore().doubleValue();
            String studentFeedback = this.responsePojo.getMergedTestFeedback().getStudentFeedback();
            String teacherFeedback = this.responsePojo.getMergedTestFeedback().getTeacherFeedback();
            
            htmlGenerator.outputMergedTestFeedback(studentFeedback, teacherFeedback, grade);
        } else {
            SeparateTestFeedbackType separateTestFeedback = this.responsePojo.getSeparateTestFeedback();
            createSeparateTestFeedbackResponseHTML(separateTestFeedback, maxScoreLMS);
        }
    }

    private void createSeparateTestFeedbackResponseHTML(SeparateTestFeedbackType separateTestFeedback, double maxScoreLMS) {        
        // Process scores and grading structure
        Map<String, Map<String, Double>> scores = processAllTestScores(separateTestFeedback.getTestsResponse());
        CombineNode gradingStructure = processGradingNode(taskGradingHintsElem.getRoot(), 
            separateTestFeedback.getTestsResponse(), scores, 1.0, 
            new GradingHintsHelper(taskGradingHintsElem, taskTestElem), 0);


        debugPrintGradingStructure(gradingStructure, "");
    }

    /**
     * Processes all test scores from the response and stores them in a nested map structure.
     * The outer map uses the test ID as key, and the inner map uses subtest ID (or empty string if no subtests)
     * as key with the corresponding scores as values.
     *
     * @param testsResponse The tests response element containing test results
     * @return A nested map containing all test and subtest scores
     */
    private Map<String, Map<String, Double>> processAllTestScores(TestsResponseType testsResponse) {
        Map<String, Map<String, Double>> scoresMap = new HashMap<>();
        
        // Get all test responses
        List<TestResponseType> testResponses = testsResponse.getTestResponse();
        
        for (TestResponseType testResponse : testResponses) {
            String testId = testResponse.getId();
            
            // Initialize inner map for this test
            scoresMap.putIfAbsent(testId, new HashMap<>());
            
            // Check if there are subtests
            SubtestsResponseType subtestsResponse = testResponse.getSubtestsResponse();
            
            if (subtestsResponse == null || subtestsResponse.getSubtestResponse().isEmpty()) {
                // No subtests, store the score with empty subtest id
                TestResultType result = testResponse.getTestResult();
                if (result != null) {
                    ResultType testResult = result.getResult();
                    if (testResult != null) {
                        if (testResult.isIsInternalError()) {
                            throw new RuntimeException("One of the test results returned is invalid (Internal Error).");
                        }
                        scoresMap.get(testId).put("", testResult.getScore().doubleValue());
                    }
                }
            } else {
                // Process subtests
                for (SubtestResponseType subTestResponse : subtestsResponse.getSubtestResponse()) {
                    String subTestId = subTestResponse.getId();
                    TestResultType subResult = subTestResponse.getTestResult();
                    
                    if (subResult != null) {
                        ResultType subTestResult = subResult.getResult();
                        if (subTestResult != null) {
                            if (subTestResult.isIsInternalError()) {
                                throw new RuntimeException("One of the subtest results returned is invalid (Internal Error).");
                            }
                            scoresMap.get(testId).put(subTestId, subTestResult.getScore().doubleValue());
                        }
                    }
                }
            }
        }
        
        return scoresMap;
    }


    /**
     * Process all nodes in the grading-hints element to include all the information needed to generate an evaluation
     * report in data classes created specifically to store all the information together.
     * The evaluation report isn't generated directly because nullifying conditions must be evaluated first.
     *
     * @param node The grading node to process
     * @param testsResponse The tests response containing test results
     * @param scoresMap Map containing test scores
     * @param weight Weight to apply to the node's score
     * @param gradingHintsHelper Helper for grading hints calculations
     * @param indentLevel Current indent level for visual hierarchy
     * @return Processed CombineNode containing the grading structure
     */
    private CombineNode processGradingNode(GradesNodeType node, TestsResponseType testsResponse, 
                                        Map<String, Map<String, Double>> scoresMap,
                                        double weight, GradingHintsHelper gradingHintsHelper, 
                                        int indentLevel) {
        String refId = node.getId() != null ? node.getId() : "root";
        String title = node.getTitle() != null ? node.getTitle() : refId;
        String description = node.getDescription() != null ? node.getDescription() : "";
        String internalDescription = node.getInternalDescription() != null ? node.getInternalDescription() : "";
        String function = node.getFunction() != null ? node.getFunction() : "min";
        double rawScore = 0.0;
        double maxScore = gradingHintsHelper.calculateMaxScore(node) * weight;

        if ("root".equals(refId)) {
            title = "Overall result";
        }

        List<Double> childScores = new ArrayList<>();
        List<GradingNode> processedChildren = new ArrayList<>();

        // Process test-ref and combine-ref elements
        List<GradesBaseRefChildType> refs = node.getTestRefOrCombineRef();
        if (refs != null) {
            for (GradesBaseRefChildType ref : refs) {
                if (ref instanceof GradesTestRefChildType testRef) {
                    processTestRef(testRef, testsResponse, scoresMap, indentLevel, 
                                childScores, processedChildren);
                } else if (ref instanceof GradesCombineRefChildType combineRef) {
                    processCombineRef(combineRef, testsResponse, scoresMap, 
                                    gradingHintsHelper, indentLevel,
                                    childScores, processedChildren);
                }
            }
        }

        // Calculate combined score based on function
        rawScore = switch (function) {
            case "sum" -> childScores.stream().mapToDouble(Double::doubleValue).sum();
            case "min" -> childScores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            case "max" -> childScores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            default -> 0.0;
        };

        double actualScore = rawScore * weight;
        return new CombineNode(refId, title, description, internalDescription,
                            weight, rawScore, maxScore, actualScore,
                            indentLevel, function, processedChildren);
    }

    private void processTestRef(GradesTestRefChildType testRef, TestsResponseType testsResponse,
                            Map<String, Map<String, Double>> scoresMap, int indentLevel,
                            List<Double> childScores, List<GradingNode> processedChildren) {
        String refId = testRef.getRef();
        String subRefId = testRef.getSubRef() != null ? testRef.getSubRef() : "";
        
        // Get or generate title and description
        String title = testRef.getTitle() != null ? testRef.getTitle() : "";
        String description = testRef.getDescription() != null ? testRef.getDescription() : "";
        String internalDescription = testRef.getInternalDescription() != null ? 
                                    testRef.getInternalDescription() : "";

        // If title or description is empty, try to get from test element
        if (title.isEmpty() || description.isEmpty()) {
            TestType test = findTestById(refId);
            if (test != null) {
                if (title.isEmpty()) {
                    title = test.getTitle() != null ? test.getTitle() : refId;
                    if (!subRefId.isEmpty()) {
                        title += " (" + subRefId + ")";
                    }
                }
                if (description.isEmpty()) {
                    description = test.getDescription() != null ? test.getDescription() : "";
                    if (!subRefId.isEmpty()) {
                        description += " (" + subRefId + ")";
                    }
                }
            }
        }

        // Set default title if still empty
        if (title.isEmpty()) {
            title = !subRefId.isEmpty() ? refId + " (" + subRefId + ")" : refId;
        }

        double weight = testRef.getWeight() != null ? testRef.getWeight() : 1.0;
        double rawScore = scoresMap.getOrDefault(refId, new HashMap<>())
                                .getOrDefault(subRefId, 0.0);
        double actualScore = rawScore * weight;

        List<String> studentFeedback = new ArrayList<>();
        List<String> teacherFeedback = new ArrayList<>();
        processTestFeedback(studentFeedback, teacherFeedback, testsResponse, refId, subRefId);

        childScores.add(actualScore);
        processedChildren.add(new TestNode(refId, title, description, internalDescription,
                                        weight, rawScore, weight, actualScore,
                                        indentLevel + 1, subRefId, 
                                        studentFeedback, teacherFeedback));
    }

    private void processCombineRef(GradesCombineRefChildType combineRef, TestsResponseType testsResponse,
                                Map<String, Map<String, Double>> scoresMap,
                                GradingHintsHelper gradingHintsHelper, int indentLevel,
                                List<Double> childScores, List<GradingNode> processedChildren) {
        String refId = combineRef.getRef();
        GradesNodeType combineNode = gradingHintsHelper.getCombineNode(refId);
        
        if (combineNode != null) {
            double weight = combineRef.getWeight() != null ? combineRef.getWeight() : 1.0;
            CombineNode processedCombineNode = processGradingNode(combineNode, testsResponse,
                                                                scoresMap, weight,
                                                                gradingHintsHelper,
                                                                indentLevel + 1);
            childScores.add(processedCombineNode.getActualScore());
            processedChildren.add(processedCombineNode);
        }
    }

    private TestType findTestById(String testId) {
        if (taskTestElem != null && taskTestElem.getTest() != null) {
            return taskTestElem.getTest().stream()
                    .filter(test -> testId.equals(test.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    
    /**
     * Processes and extracts student and teacher feedback from the test response element.
     * The feedback is added to the provided lists.
     *
     * @param studentFeedback List to store student feedback
     * @param teacherFeedback List to store teacher feedback
     * @param testsResponse The tests response containing all test results
     * @param refId The ID of the test to find
     * @param subRefId The ID of the subtest to find (empty string if no subtest)
     */
    private void processTestFeedback(List<String> studentFeedback, List<String> teacherFeedback,
                                TestsResponseType testsResponse, String refId, String subRefId) {
        // Find the test response with matching refId
        TestResponseType testResponse = testsResponse.getTestResponse().stream()
                .filter(test -> refId.equals(test.getId()))
                .findFirst()
                .orElse(null);

        if (testResponse == null) {
            return;
        }

        if (subRefId.isEmpty()) {
            // Process main test feedback
            processTestResultFeedback(studentFeedback, teacherFeedback, 
                                    testResponse.getTestResult());
        } else {
            // Process subtest feedback
            SubtestsResponseType subtestsResponse = testResponse.getSubtestsResponse();
            if (subtestsResponse != null) {
                subtestsResponse.getSubtestResponse().stream()
                        .filter(subtest -> subRefId.equals(subtest.getId()))
                        .findFirst()
                        .ifPresent(subtest -> processTestResultFeedback(
                                studentFeedback, teacherFeedback, 
                                subtest.getTestResult()));
            }
        }
    }


    /**
     * Helper method to process feedback from a test result.
     * Extracts both student and teacher feedback and adds them to the respective lists.
     *
     * @param studentFeedback List to store student feedback
     * @param teacherFeedback List to store teacher feedback
     * @param testResult The test result containing the feedback
     */
    private void processTestResultFeedback(List<String> studentFeedback, 
                                        List<String> teacherFeedback,
                                        TestResultType testResult) {
        if (testResult == null || testResult.getFeedbackList() == null) {
            return;
        }

        // Process student feedback
        testResult.getFeedbackList().getStudentFeedback().stream()
                .map(feedback -> feedback.getContent().getValue())
                .forEach(studentFeedback::add);

        // Process teacher feedback
        testResult.getFeedbackList().getTeacherFeedback().stream()
                .map(feedback -> feedback.getContent().getValue())
                .forEach(teacherFeedback::add);
    }


    // Debugging methods

    private void debugPrintGradingStructure(CombineNode node, String prefix) {
        System.out.println("\n=== DEBUG: Grading Structure ===");
        printNode(node, "");
        System.out.println("==============================\n");
    }
    
    private void printNode(GradingNode node, String indent) {
        // Basic node information
        System.out.println(indent + "Node Type: " + node.getType());
        System.out.println(indent + "ID: " + node.getRefId());
        System.out.println(indent + "Title: " + node.getTitle());
        System.out.println(indent + "Indent Level: " + node.getIndentLevel());
        System.out.println(indent + "Scores:");
        System.out.println(indent + "  - Raw Score: " + node.getRawScore());
        System.out.println(indent + "  - Weight: " + node.getWeight());
        System.out.println(indent + "  - Max Score: " + node.getMaxScore());
        System.out.println(indent + "  - Actual Score: " + node.getActualScore());
    
        if (node instanceof TestNode testNode) {
            System.out.println(indent + "Test Node Specific:");
            System.out.println(indent + "  - SubRef ID: " + testNode.getSubRefId());
            System.out.println(indent + "  - Student Feedback Count: " + testNode.getStudentFeedback().size());
            System.out.println(indent + "  - Teacher Feedback Count: " + testNode.getTeacherFeedback().size());
            
            // if (!testNode.getStudentFeedback().isEmpty()) {
            //     System.out.println(indent + "  - Student Feedback:");
            //     testNode.getStudentFeedback().forEach(fb -> 
            //         System.out.println(indent + "    * " + fb));
            // }
            
            // if (!testNode.getTeacherFeedback().isEmpty()) {
            //     System.out.println(indent + "  - Teacher Feedback:");
            //     testNode.getTeacherFeedback().forEach(fb -> 
            //         System.out.println(indent + "    * " + fb));
            // }
        } else if (node instanceof CombineNode combineNode) {
            System.out.println(indent + "Combine Node Specific:");
            System.out.println(indent + "  - Function: " + combineNode.getFunction());
            System.out.println(indent + "  - Children Count: " + combineNode.getChildren().size());
            System.out.println(indent + "  - Nullification Checked: " + combineNode.isNullificationChecked());
            
            if (!combineNode.getChildren().isEmpty()) {
                System.out.println(indent + "  - Children:");
                combineNode.getChildren().forEach(child -> {
                    System.out.println(indent + "    ----");
                    printNode(child, indent + "    ");
                });
            }
        }
        System.out.println(indent + "------------------------");
    }
}