package org.abp.vpl_proforma.utility.communicator;

public class AsyncSubmissionResponse {
    private final String gradeProcessId;
    private final int estimatedSecondsRemaining;

    public AsyncSubmissionResponse(String gradeProcessId, int estimatedSecondsRemaining) {
        this.gradeProcessId = gradeProcessId;
        this.estimatedSecondsRemaining = estimatedSecondsRemaining;
    }

    public String getGradeProcessId() {
        return gradeProcessId;
    }

    public int getEstimatedSecondsRemaining() {
        return estimatedSecondsRemaining;
    }

    @Override
    public String toString() {
        return String.format("Grade Process ID: %s, Estimated Seconds Remaining: %d", gradeProcessId, estimatedSecondsRemaining);
    }
}
