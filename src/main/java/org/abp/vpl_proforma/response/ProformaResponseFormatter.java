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
import proforma.xml21.GradesNullifyConditionType;
import proforma.xml21.GradesNullifyComparisonOperandType;
import proforma.xml21.GradesNullifyCombineRefType;
import proforma.xml21.GradesNullifyTestRefType;
import proforma.xml21.GradesNullifyLiteralType;
import proforma.xml21.GradesNullifyConditionsType;
import proforma.xml21.GradesNullifyBaseType;

import org.abp.vpl_proforma.response.gradingstructure.CombineNode;
import org.abp.vpl_proforma.response.gradingstructure.GradingNode;
import org.abp.vpl_proforma.response.gradingstructure.TestNode;
import org.abp.vpl_proforma.response.htmlformatter.HTMLResponseGenerator;
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
    private GradingHintsHelper gradingHintsHelper;
    public ProformaResponseFormatter(ResponseType responsePojo, GradingHintsType taskGradingHintsElem, TestsType taskTestElem) {
        this.responsePojo = responsePojo;
        this.taskGradingHintsElem = taskGradingHintsElem;
        this.taskTestElem = taskTestElem;
        this.htmlGenerator = new HTMLResponseGenerator(null, null, 1.0);
        this.gradingHintsHelper = new GradingHintsHelper(taskGradingHintsElem, taskTestElem);
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
            separateTestFeedback.getTestsResponse(), scores, 1.0, 0);

        // Process nullification conditions
        processNullifyConditions(taskGradingHintsElem.getRoot(), gradingStructure);

        // Calculate scale factor to adjust weights in the final output.
        double scaleFactor = 1;
        if (!gradingHintsHelper.isEmpty()) {
            double maxScoreGradingHints = gradingHintsHelper.calculateMaxScore();
            if (Math.abs(maxScoreGradingHints - maxScoreLMS) > 1e-5) {
                scaleFactor = maxScoreLMS / maxScoreGradingHints;
            }
        }
        
        // Output HTML documents
        htmlGenerator = new HTMLResponseGenerator(separateTestFeedback, gradingStructure, scaleFactor);
        htmlGenerator.generateSeparateTestFeedbackDocuments();

        // Output debugging information
        // debugPrintGradingStructure(gradingStructure, "");
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
     * @param indentLevel Current indent level for visual hierarchy
     * @return Processed CombineNode containing the grading structure
     */
    private CombineNode processGradingNode(GradesNodeType node, TestsResponseType testsResponse, 
                                        Map<String, Map<String, Double>> scoresMap,
                                        double weight, int indentLevel) {
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
                                    indentLevel,
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
        Map<String, Double> testScores = scoresMap.get(refId);
        if (testScores == null || !testScores.containsKey(subRefId)) {
            throw new IllegalStateException(
                String.format("Missing score data for test reference: id='%s', subRef='%s'. Check grading hints and test response consistency.", refId, subRefId.isEmpty() ? "<none>" : subRefId)
            );
        }
        double rawScore = testScores.get(subRefId);
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
                                int indentLevel,
                                List<Double> childScores, List<GradingNode> processedChildren) {
        String refId = combineRef.getRef();
        GradesNodeType combineNode = gradingHintsHelper.getCombineNode(refId);
        
        if (combineNode != null) {
            double weight = combineRef.getWeight() != null ? combineRef.getWeight() : 1.0;
            CombineNode processedCombineNode = processGradingNode(combineNode, testsResponse,
                                                                scoresMap, weight,
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

    /**
     * Process nullification conditions for a node and its children recursively.
     * 
     * @param node The GradingHints XML node to process nullification conditions for
     * @param rootProcessed The root of the processed grading structure
     */
    private void processNullifyConditions(GradesNodeType node, CombineNode rootProcessed) {
        String refId = node.getId() != null ? node.getId() : "root";
        CombineNode processedNode = (CombineNode) findNodeInProcessedStructure(rootProcessed, refId);
        
        // Check if node was already processed
        if (processedNode.isNullificationChecked()) {
            return;
        }
        
        // Process test-ref elements
        List<GradesBaseRefChildType> refs = node.getTestRefOrCombineRef();
        if (refs != null) {
            for (GradesBaseRefChildType ref : refs) {
                if (ref instanceof GradesTestRefChildType testRef) {
                    String testRefId = testRef.getRef();
                    String subRefId = testRef.getSubRef() != null ? testRef.getSubRef() : "";

                    GradingNode processedTestNode = findNodeInProcessedStructure(rootProcessed, testRefId, subRefId);
                    if (processedTestNode != null && processedTestNode.getRawScore() != 0) {
                        boolean result = false;
                        
                        if (testRef.getNullifyConditions() != null) {
                            result = processCompositeNullifyConditions(testRef.getNullifyConditions(), rootProcessed);
                        } else if (testRef.getNullifyCondition() != null) {
                            result = processSingleNullifyCondition(testRef.getNullifyCondition(), rootProcessed);
                        }
                        
                        if (result) {
                            processedTestNode.setNullified(true);
                            String nullifyTitle = "";
                            String nullifyDescription = "";
                            
                            if (testRef.getNullifyConditions() != null) {
                                nullifyTitle = testRef.getNullifyConditions().getTitle() != null ? 
                                    testRef.getNullifyConditions().getTitle() : "";
                                nullifyDescription = testRef.getNullifyConditions().getDescription() != null ? 
                                    testRef.getNullifyConditions().getDescription() : "";
                            } else if (testRef.getNullifyCondition() != null) {
                                nullifyTitle = testRef.getNullifyCondition().getTitle() != null ? 
                                    testRef.getNullifyCondition().getTitle() : "";
                                nullifyDescription = testRef.getNullifyCondition().getDescription() != null ? 
                                    testRef.getNullifyCondition().getDescription() : "";
                            }
                            
                            String nullifyReason = nullifyTitle;
                            if (!nullifyDescription.isEmpty()) {
                                nullifyReason += ":<br>" + nullifyDescription;
                            }
                            processedTestNode.setNullifyReason(nullifyReason);
                        }
                    }
                } else if (ref instanceof GradesCombineRefChildType combineRef) {
                    String combineRefId = combineRef.getRef();
                    GradingNode processedCombineNode = findNodeInProcessedStructure(rootProcessed, combineRefId);
                    
                    if (processedCombineNode != null && processedCombineNode.getRawScore() != 0) {
                        boolean result = false;
                        
                        if (combineRef.getNullifyConditions() != null) {
                            result = processCompositeNullifyConditions(combineRef.getNullifyConditions(), rootProcessed);
                        } else if (combineRef.getNullifyCondition() != null) {
                            result = processSingleNullifyCondition(combineRef.getNullifyCondition(), rootProcessed);
                        }
                        
                        if (result) {
                            processedCombineNode.setNullified(true);
                            String nullifyTitle = "";
                            String nullifyDescription = "";
                            
                            if (combineRef.getNullifyConditions() != null) {
                                nullifyTitle = combineRef.getNullifyConditions().getTitle() != null ? 
                                    combineRef.getNullifyConditions().getTitle() : "";
                                nullifyDescription = combineRef.getNullifyConditions().getDescription() != null ? 
                                    combineRef.getNullifyConditions().getDescription() : "";
                            } else if (combineRef.getNullifyCondition() != null) {
                                nullifyTitle = combineRef.getNullifyCondition().getTitle() != null ? 
                                    combineRef.getNullifyCondition().getTitle() : "";
                                nullifyDescription = combineRef.getNullifyCondition().getDescription() != null ? 
                                    combineRef.getNullifyCondition().getDescription() : "";
                            }
                            
                            String nullifyReason = nullifyTitle;
                            if (!nullifyDescription.isEmpty()) {
                                nullifyReason += ":<br>" + nullifyDescription;
                            }
                            processedCombineNode.setNullifyReason(nullifyReason);
                        }
                    }
                    
                    // Recursively process the combine node
                    GradesNodeType combineNode = gradingHintsHelper.getCombineNode(combineRefId);
                    if (combineNode != null) {
                        processNullifyConditions(combineNode, rootProcessed);
                    }
                }
            }
        }
        
        updateScoreOfCombineNode(processedNode);
        processedNode.setNullificationChecked(true);
    }

    /**
     * Process a single nullify condition.
     * 
     * @param nullifyCondition The nullify condition to process
     * @param rootProcessed The root of the processed grading structure
     * @return true if the condition is met, false otherwise
     */
    private boolean processSingleNullifyCondition(GradesNullifyConditionType nullifyCondition, CombineNode rootProcessed) {
        String compareOp = nullifyCondition.getCompareOp();
        double operand1 = 0, operand2 = 0;
        boolean operand1Set = false, operand2Set = false;

        List<GradesNullifyComparisonOperandType> operands = nullifyCondition.getNullifyCombineRefOrNullifyTestRefOrNullifyLiteral();
        for (GradesNullifyComparisonOperandType operand : operands) {
            if (operand instanceof GradesNullifyCombineRefType nullifyCombineRef) {
                String refId = nullifyCombineRef.getRef();
                GradingNode gradingNode = findNodeInProcessedStructure(rootProcessed, refId);
                
                if (gradingNode instanceof CombineNode combineNode && !combineNode.isNullificationChecked()) {
                    GradesNodeType xmlCombineNode = gradingHintsHelper.getCombineNode(refId);
                    if (xmlCombineNode != null) {
                        processNullifyConditions(xmlCombineNode, rootProcessed);
                    }
                }
                
                if (gradingNode != null) {
                    if (!operand1Set) {
                        operand1 = gradingNode.getRawScore();
                        operand1Set = true;
                    } else {
                        operand2 = gradingNode.getRawScore();
                        operand2Set = true;
                    }
                }
            } else if (operand instanceof GradesNullifyTestRefType nullifyTestRef) {
                String refId = nullifyTestRef.getRef();
                String subRefId = nullifyTestRef.getSubRef() != null ? nullifyTestRef.getSubRef() : "";
                GradingNode gradingNode = findNodeInProcessedStructure(rootProcessed, refId, subRefId);
                
                if (gradingNode != null) {
                    if (!operand1Set) {
                        operand1 = gradingNode.getRawScore();
                        operand1Set = true;
                    } else {
                        operand2 = gradingNode.getRawScore();
                        operand2Set = true;
                    }
                }
            } else if (operand instanceof GradesNullifyLiteralType nullifyLiteral) {
                double value = nullifyLiteral.getValue().doubleValue();
                if (!operand1Set) {
                    operand1 = value;
                    operand1Set = true;
                } else {
                    operand2 = value;
                    operand2Set = true;
                }
            }
        }

        if (!(operand1Set && operand2Set)) {
            throw new RuntimeException("Invalid nullify condition: " + nullifyCondition + 
                ". Both operands must be set for comparison. Operand1 set: " + operand1Set + 
                ", Operand2 set: " + operand2Set + 
                ". Please check the grading hints configuration and ensure all referenced nodes exist.");
        }

        return compareOperands(operand1, operand2, compareOp);
    }

    /**
     * Compare two operands using the specified comparison operator.
     * 
     * @param operand1 First operand
     * @param operand2 Second operand
     * @param compareOp Comparison operator
     * @return Result of the comparison
     */
    private boolean compareOperands(double operand1, double operand2, String compareOp) {
        return switch (compareOp) {
            case "eq" -> operand1 == operand2;
            case "ne" -> operand1 != operand2;
            case "gt" -> operand1 > operand2;
            case "ge" -> operand1 >= operand2;
            case "lt" -> operand1 < operand2;
            case "le" -> operand1 <= operand2;
            // Make invalid operator an error
            default -> throw new IllegalArgumentException("Unsupported comparison operator found in nullify condition: '" + compareOp + "'");
        };
    }

    /**
     * Process composite nullify conditions.
     * 
     * @param nullifyConditions The composite nullify conditions to process
     * @param rootProcessed The root of the processed grading structure
     * @return true if the conditions are met, false otherwise
     */
    private boolean processCompositeNullifyConditions(GradesNullifyConditionsType nullifyConditions, CombineNode rootProcessed) {
        String composeOp = nullifyConditions.getComposeOp();
        List<Boolean> results = new ArrayList<>();

        List<GradesNullifyBaseType> conditions = nullifyConditions.getNullifyConditionsOrNullifyCondition();
        for (GradesNullifyBaseType condition : conditions) {
            boolean result = false;
            if (condition instanceof GradesNullifyConditionsType nestedConditions) {
                result = processCompositeNullifyConditions(nestedConditions, rootProcessed);
            } else if (condition instanceof GradesNullifyConditionType singleCondition) {
                result = processSingleNullifyCondition(singleCondition, rootProcessed);
            }
            results.add(result);
        }

        return switch (composeOp) {
            case "and" -> results.stream().allMatch(result -> result);
            case "or" -> results.stream().anyMatch(result -> result);
            // Make invalid operator an error
            default -> throw new IllegalArgumentException("Unsupported composition operator found in nullify conditions: '" + composeOp + "'");
        };
    }

    /**
     * Find a node in the processed grading structure by its ID.
     * 
     * @param currentNode Current node being examined
     * @param refId Reference ID to find
     * @return The found node or null if not found
     */
    private GradingNode findNodeInProcessedStructure(GradingNode currentNode, String refId) {
        return findNodeInProcessedStructure(currentNode, refId, "");
    }

    /**
     * Find a node in the processed grading structure by its ID and optional subtest ID.
     * 
     * @param currentNode Current node being examined
     * @param refId Reference ID to find
     * @param subRefId Subtest reference ID
     * @return The found node or null if not found
     */
    private GradingNode findNodeInProcessedStructure(GradingNode currentNode, String refId, String subRefId) {
        if (currentNode.getRefId().equals(refId)) {
            if (currentNode instanceof TestNode testNode) {
                if (!subRefId.isEmpty() && testNode.getSubRefId().equals(subRefId)) {
                    return testNode; // Match found with specific subRefId
                } else if (subRefId.isEmpty()) {
                    return testNode; // Match found without needing to check subRefId
                }
            } else if (currentNode instanceof CombineNode) {
                return currentNode;
            }
        }

        // If the current node is a CombineNode, iterate through its children
        if (currentNode instanceof CombineNode combineNode) {
            for (GradingNode child : combineNode.getChildren()) {
                GradingNode foundNode = findNodeInProcessedStructure(child, refId, subRefId);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }

    /**
     * Update the score of a combine node based on its non-nullified children's scores.
     * 
     * @param combineNode The combine node to update
     */
    private void updateScoreOfCombineNode(CombineNode combineNode) {
        double sum = 0.0;
        List<Double> childrenScores = new ArrayList<>();
        
        for (GradingNode child : combineNode.getChildren()) {
            if (!child.isNullified()) {
                childrenScores.add(child.getActualScore());
            }
        }
        
        if (!childrenScores.isEmpty()) {
            sum = switch (combineNode.getFunction()) {
                case "sum" -> childrenScores.stream().mapToDouble(Double::doubleValue).sum();
                case "min" -> childrenScores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                case "max" -> childrenScores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                default -> 0.0;
            };
        }
        
        combineNode.setActualScore(sum * combineNode.getWeight());
    }


     // Debugging methods
     private void debugPrintGradingStructure(CombineNode node, String prefix) {
        System.out.println("\n=== DEBUG: Grading Structure ===");
        debugPrintNode(node, "");
        System.out.println("==============================\n");
    }
    
    private void debugPrintNode(GradingNode node, String indent) {
        // Basic node information
        System.out.println(indent + "Node Type: " + node.getType());
        System.out.println(indent + "ID: " + node.getRefId());
        System.out.println(indent + "Title: " + node.getTitle());
        System.out.println(indent + "Indent Level: " + node.getIndentLevel());
        System.out.println(indent + "Scores:");
        System.out.println(indent + "  - Raw Score: " + node.getRawScore());
        System.out.println(indent + "  - Weight: " + node.getWeight());
        System.out.println(indent + "  - Max Score: " + node.getMaxScore());
        double finalScore = node.isNullified() ? 0 : node.getActualScore();
        System.out.println(indent + "  - Actual Score: " + finalScore);
        
        // Add nullification information
        System.out.println(indent + "Nullification:");
        System.out.println(indent + "  - Is Nullified: " + node.isNullified());
        if (node.isNullified() && node.getNullifyReason() != null && !node.getNullifyReason().isEmpty()) {
            System.out.println(indent + "  - Nullify Reason: " + node.getNullifyReason());
        }
    
        if (node instanceof TestNode testNode) {
            System.out.println(indent + "Test Node Specific:");
            System.out.println(indent + "  - SubRef ID: " + testNode.getSubRefId());
            System.out.println(indent + "  - Student Feedback Count: " + testNode.getStudentFeedback().size());
            System.out.println(indent + "  - Teacher Feedback Count: " + testNode.getTeacherFeedback().size());

        } else if (node instanceof CombineNode combineNode) {
            System.out.println(indent + "Combine Node Specific:");
            System.out.println(indent + "  - Function: " + combineNode.getFunction());
            System.out.println(indent + "  - Children Count: " + combineNode.getChildren().size());
            System.out.println(indent + "  - Nullification Checked: " + combineNode.isNullificationChecked());
            
            if (!combineNode.getChildren().isEmpty()) {
                System.out.println(indent + "  - Children:");
                combineNode.getChildren().forEach(child -> {
                    System.out.println(indent + "    ----");
                    debugPrintNode(child, indent + "    ");
                });
            }
        }
        System.out.println(indent + "------------------------");
    }
}