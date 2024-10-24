package org.abp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import proforma.util.TaskLive;
import proforma.util.resource.TaskResource;
import proforma.xml21.GradingHintsType;
import proforma.xml21.SubmissionType;
import proforma.xml21.TaskType;
import proforma.xml21.TestsType;


public class ProformaFormatter {

    public static final String[] PROFORMA_TASK_XML_NAMESPACES = {"urn:proforma:v2.1"};
    public static final String PROFORMA_MERGED_FEEDBACK_TYPE = "merged-test-feedback";
    public static final String PROFORMA_SEPARATE_FEEDBACK_TYPE = "separate-test-feedback";
    public static final String PROFORMA_RESULT_SPEC_FORMAT_ZIP = "zip";
    public static final String PROFORMA_RESULT_SPEC_FORMAT_XML = "xml";
    public static final String PROFORMA_FEEDBACK_LEVEL_ERROR = "error";
    public static final String PROFORMA_FEEDBACK_LEVEL_WARNING = "warn";
    public static final String PROFORMA_FEEDBACK_LEVEL_INFO = "info";
    public static final String PROFORMA_FEEDBACK_LEVEL_DEBUG = "debug";
    public static final String PROFORMA_FEEDBACK_LEVEL_NOTSPECIFIED = "notspecified";

    public static void main(String[] args) {

        // Process command-line arguments
        if (args.length < 2) {
            System.err.println("Usage: " + args[0] + " 'submission file list' 'task file name'");
            System.exit(1);
        }

        // Submission file list
        List<String> submissionFilesNames = new ArrayList<>();
        for (int i = 0; i < args.length - 1; i++) {
            submissionFilesNames.add(args[i]);
        }

        // Load environment variables from the vpl_environment.sh script
        Map<String, Object> env = Utility.getArgsFromVplEnvironmentScript();
        if (env == null) {
            System.err.println("Failed to load environment variables.");
        }

        String userID = (String) env.get("moodleUserId");
        String courseID = (String) env.get("moodleCourseId");
        String lmsURL = (String) env.get("moodleURL");
        double maxScoreLMS = (double) env.get("vplGradeMax");

        // ProFormA task file name and extension
        String taskFilename = args[args.length - 1];
        String taskRefType = Utility.getFileExtension(taskFilename);

        // Grader and submission settings
        Map<String, String> configValues = Utility.getArgsFromProformaSettingsScript();

        if (configValues == null) {
            System.err.println("Failed to open proforma_settings.sh file.");
            System.err.println("1. Make sure the proforma_settings.sh file is included in the 'Execution files' list");
            System.err.println("2. Make sure the file is marked as non-removable under the 'File to keep when running' tab.");
            System.exit(1);
        }

        String serviceURL = configValues.get("SERVICE_URL");
        String lmsID = configValues.get("LMS_ID");
        String lmsPassword = configValues.get("LMS_PASSWORD");
        String graderName = configValues.get("GRADER_NAME");
        String graderVersion = configValues.get("GRADER_VERSION");
        String resultFormat = configValues.get("FEEDBACK_TYPE");
        String resultStructure = configValues.get("FEEDBACK_STRUCTURE");
        String studentFeedbackLevel = configValues.get("STUDENT_FEEDBACK_LEVEL");
        String teacherFeedbackLevel = configValues.get("TEACHER_FEEDBACK_LEVEL");

        /////////////////////////////////////////////////////////////////////////////////////
        //// Start building submission //////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////

        TaskType taskPojo = getTaskType(taskFilename);

        // Check if the task is cached on the middleware server
        // String taskFileUUID = taskPojo.getUuid();
        // if (isTaskCached(taskFileUUID, serviceURL, lmsID, lmsPassword)) {
            // taskFilename = taskFileUUID;
            // taskRefType = "uuid";
        // }

        TestsType tests = taskPojo.getTests();
        GradingHintsType gradingHints = taskPojo.getGradingHints();

        // Create submission.xml file

    }

    public static byte[] getFileAsBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    public static TaskType getTaskType(String taskFilename) {
        String filePath = "task/" + taskFilename;

        byte[] xmlOrZipBytes = null;
        try {
            xmlOrZipBytes = getFileAsBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bytes for the XML or ZIP ProFormA-Task file. Please ensure the task file is properly configured."
                    + "\n1. Make sure the task file is included in a 'task/' directory in the execution files."
                    + "\n2. Make sure task.xml is directly included in the task folder either as a zip or xml."
                    + "\n3. Make sure the task is marked as non-removable under the 'File to keep when running' tab.");
        }

        TaskResource resource = new TaskResource(xmlOrZipBytes);

        TaskLive taskLive;
        try {
            taskLive = new TaskLive(resource);
        } catch (Exception e) {
            throw new RuntimeException("Error creating TaskLive instance for the task.", e);
        }

        TaskType taskPojo;
        try {
            taskPojo = taskLive.getTask();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching TaskType POJO from TaskLive.", e);
        }

        if (taskPojo == null) {
            throw new IllegalStateException("Failed to load TaskType from task.xml. Please ensure the task file is properly configured.");
        }

        return taskPojo;
    }


    /**
     * Checks if the task file identified by the UUID is cached on the server.
     *
     * @param taskFileUUID The UUID of the task file.
     * @param serviceURL The base URL of the service where the task might be cached.
     * @param lmsID The ID for authentication with the service.
     * @param lmsPassword The password for authentication with the service.
     * @return True if the task is cached, false otherwise.
     */
    public static boolean isTaskCached(String taskFileUUID, String serviceURL, String lmsID, String lmsPassword) {
        boolean isCached = false;
        String urlString = serviceURL + "tasks/" + taskFileUUID;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set HTTP method to HEAD
            connection.setRequestMethod("HEAD");

            // Set authentication credentials
            String credentials = lmsID + ":" + lmsPassword;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);

            // Connect and get the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                isCached = true;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                isCached = false;
            } else {
                System.err.println("Unexpected response code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            System.err.println("Error during HTTP request: " + e.getMessage());
        }

        return isCached;
    }

    public static SubmissionType createSubmissionPojo(String taskFilenameOrUUID, String taskRefType,
                                                      List<String> files, String resultFormat, String resultStructure,
                                                      String studentFeedbackLevel, String teacherFeedbackLevel,
                                                      GradingHintsType taskGradingHintsElem, TestsType taskTestElem,
                                                      double maxScoreLMS, String lmsURL, String courseID, String userID) throws Exception {



        return null;
    }
}