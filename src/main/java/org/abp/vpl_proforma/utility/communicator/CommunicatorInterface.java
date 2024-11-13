package org.abp.vpl_proforma.utility.communicator;

public interface CommunicatorInterface {
    boolean isTaskCached(String uuid) throws Exception;
    SyncSubmissionResponse enqueueSyncSubmission(String graderName, String graderVersion, byte[] submissionContent);
    AsyncSubmissionResponse enqueueAsyncSubmission(String graderName, String graderVersion, byte[] submissionContent);
    String getGradingResult(String graderName, String graderVersion, String gradeProcessId) throws Exception;
}
