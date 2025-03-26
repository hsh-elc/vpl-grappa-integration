package org.abp.vpl_proforma;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.abp.vpl_proforma.response.ProformaResponseFormatter;
import org.abp.vpl_proforma.submission.ProformaSubmissionFormatter;
import org.abp.vpl_proforma.submission.config.VplEnvironmentConfig;
import org.abp.vpl_proforma.submission.config.ProformaConfig;
import org.abp.vpl_proforma.utility.Utility;
import org.abp.vpl_proforma.utility.communicator.CommunicatorFactory;
import org.abp.vpl_proforma.utility.communicator.CommunicatorInterface;
import org.abp.vpl_proforma.utility.communicator.SyncSubmissionResponse;

import proforma.util.ResponseLive;
import proforma.util.resource.ResponseResource;
import proforma.xml21.GradingHintsType;
import proforma.xml21.ResponseType;
import proforma.xml21.SubmissionType;
import proforma.xml21.TaskType;
import proforma.xml21.TestsType;

public class Main {
    public static void main(String[] args) throws Exception {

        //////////////////////////////
        /// Prepare for submission ///
        //////////////////////////////
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
        VplEnvironmentConfig vplConfig;
        try {
            vplConfig = new VplEnvironmentConfig("vpl_environment.sh");
        } catch (IOException e) {
            System.err.println("Failed to open vpl_environment.sh file.");
            System.err.println("Ensure the 'vpl_environment.sh' file is included and accessible.");
            System.exit(1);
            return;
        }

        String userID = vplConfig.getMoodleUserId();
        String courseID = vplConfig.getMoodleCourseId();
        String lmsURL = vplConfig.getLmsURL();
        double maxScoreLMS = vplConfig.getMaxScoreLMS();

        // ProFormA task file name and extension
        String taskFilename = args[args.length - 1];
        String taskRefType = Utility.getFileExtension(taskFilename);

        // Grader and submission settings
        ProformaConfig config;
        try {
            config = new ProformaConfig("proforma_settings.sh");
        } catch (IOException e) {
            System.err.println("Failed to open proforma_settings.sh file.");
            System.err.println("1. Make sure the proforma_settings.sh file is included in the 'Execution files' list");
            System.err.println("2. Make sure the file is marked as non-removable under the 'File to keep when running' tab.");
            System.exit(1);
            return; // Exit after error
        }

        /////////////////////////////////
        /// Start building submission ///
        /////////////////////////////////
        ProformaSubmissionFormatter proformaSubmissionFormatter = new ProformaSubmissionFormatter();
        CommunicatorInterface communicator = CommunicatorFactory.getCommunicator(
                "grappa", config.getServiceURL(), config.getLmsID(), config.getLmsPassword());

        TaskType taskPojo = proformaSubmissionFormatter.getTaskType(taskFilename);

        // This works, it is only commented out for development purposes
        // Check if the task is cached on the middleware server
        // String taskFileUUID = taskPojo.getUuid();
        // try {
        //     if (communicator.isTaskCached(taskFileUUID)) {
        //         taskFilename = taskFileUUID;
        //         taskRefType = "uuid";
        //     }
        // } catch (Exception e) {
        //     System.err.println("Error checking task cache: " + e.getMessage());
        // }

        TestsType tests = taskPojo.getTests();
        GradingHintsType gradingHints = taskPojo.getGradingHints();

        // Create submission.xml file
        SubmissionType submissionType = proformaSubmissionFormatter.createSubmissionPojo(taskFilename, taskRefType,
                submissionFilesNames, config.getFeedbackFormat(), config.getFeedbackStructure(),
                config.getStudentFeedbackLevel(), config.getTeacherFeedbackLevel(),
                gradingHints, tests, maxScoreLMS, lmsURL, courseID, userID);

        // Create submission zip
        byte[] submissionZipBytesArray = proformaSubmissionFormatter.createSubmissionZip(submissionType, taskFilename, taskRefType, submissionFilesNames);

        /////////////////////////////////
        /// Send submission to grader ///
        /////////////////////////////////
        SyncSubmissionResponse response = communicator.enqueueSyncSubmission(config.getGraderName(), config.getGraderVersion(), submissionZipBytesArray);
        
        ////////////////////////
        /// Process response ///
        ////////////////////////
        ResponseResource responseResource = new ResponseResource(response.getContent());
        ResponseLive responseLive = new ResponseLive(responseResource);
        ResponseType responsePojo = responseLive.getResponse();
        
        ProformaResponseFormatter proformaResponseFormatter = new ProformaResponseFormatter(responsePojo, gradingHints, tests);
        proformaResponseFormatter.processResult(maxScoreLMS);
    }
}
