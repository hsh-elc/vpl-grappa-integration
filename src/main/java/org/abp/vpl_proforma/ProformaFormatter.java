package org.abp.vpl_proforma;

import org.abp.vpl_proforma.config.ProformaConfig;
import org.abp.vpl_proforma.config.VplEnvironmentConfig;
import org.abp.vpl_proforma.utility.GradingHintsHelper;
import org.abp.vpl_proforma.utility.Utility;
import org.abp.vpl_proforma.utility.communicator.SyncSubmissionResponse;
import org.abp.vpl_proforma.utility.communicator.CommunicatorFactory;
import org.abp.vpl_proforma.utility.communicator.CommunicatorInterface;
import proforma.util.TaskLive;
import proforma.util.div.Zip;
import proforma.util.resource.TaskResource;
import proforma.xml21.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import java.io.*;
import java.nio.file.*;


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

    public static void main(String[] args) throws Exception {

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

        /////////////////////////////////////////////////////////////////////////////////////
        //// Start building submission //////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////

        CommunicatorInterface communicator = CommunicatorFactory.getCommunicator(
                "grappa", config.getServiceURL(), config.getLmsID(), config.getLmsPassword());

        TaskType taskPojo = getTaskType(taskFilename);

        // Check if the task is cached on the middleware server
        String taskFileUUID = taskPojo.getUuid();
        try {
            if (communicator.isTaskCached(taskFileUUID)) {
                taskFilename = taskFileUUID;
                taskRefType = "uuid";
            }
        } catch (Exception e) {
            System.err.println("Error checking task cache: " + e.getMessage());
        }

        TestsType tests = taskPojo.getTests();
        GradingHintsType gradingHints = taskPojo.getGradingHints();

        // Create submission.xml file
        SubmissionType submissionType = createSubmissionPojo(taskFilename, taskRefType,
                submissionFilesNames, config.getFeedbackFormat(), config.getFeedbackStructure(),
                config.getStudentFeedbackLevel(), config.getTeacherFeedbackLevel(),
                gradingHints, tests, maxScoreLMS, lmsURL, courseID, userID);

        // Create submission zip
        byte[] submissionZipBytesArray = createSubmissionZip(submissionType, taskFilename, taskRefType, submissionFilesNames);

        SyncSubmissionResponse response = communicator.enqueueSyncSubmission(config.getGraderName(), config.getGraderVersion(), submissionZipBytesArray);
        if (response.isXml()) {
            String xmlContent = new String(response.getContent(), StandardCharsets.UTF_8);
            System.out.println("Received XML Response: " + xmlContent);
        } else if (response.isZip()) {
            byte[] zipContent = response.getContent();
            Files.write(Paths.get("response.zip"), zipContent);
            System.out.println("Received ZIP Response: Saved to response.zip");
        } else {
            System.err.println("Unexpected response type: " + response.getContentType());
        }
    }

    public static TaskType getTaskType(String taskFilename) {
        String filePath = "task/" + taskFilename;

        byte[] xmlOrZipBytes = null;
        try {
            xmlOrZipBytes = Utility.getFileAsBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("""
                    Failed to load bytes for the XML or ZIP ProFormA-Task file. Please ensure the task file is properly configured.
                    1. Make sure the task file is included in a 'task/' directory in the execution files.
                    2. Make sure task.xml is directly included in the task folder either as a zip or xml.
                    3. Make sure the task is marked as non-removable under the 'Files to keep when running' tab.""");
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

    public static SubmissionType createSubmissionPojo(String taskFilenameOrUUID, String taskRefType,
                                                      List<String> files, String resultFormat, String resultStructure,
                                                      String studentFeedbackLevel, String teacherFeedbackLevel,
                                                      GradingHintsType taskGradingHintsElem, TestsType taskTestElem,
                                                      double maxScoreLMS, String lmsURL, String courseID, String userID) {

        SubmissionType submissionPojo = new SubmissionType();

        handleTaskReference(submissionPojo, taskFilenameOrUUID, taskRefType);

        adjustGradingHints(submissionPojo, taskGradingHintsElem, taskTestElem, resultStructure, maxScoreLMS);

        handleSubmissionFiles(submissionPojo, files);

        setupLmsDetails(submissionPojo, lmsURL, courseID, userID);

        setupResultSpec(submissionPojo, resultFormat, resultStructure, studentFeedbackLevel, teacherFeedbackLevel);

        return submissionPojo;
    }

    private static void handleTaskReference(SubmissionType submissionPojo, String taskFilenameOrUUID, String taskRefType) {
        if ("uuid".equals(taskRefType)) {
            ExternalTaskType externalTaskType = new ExternalTaskType();
            externalTaskType.setUuid(taskFilenameOrUUID);
            submissionPojo.setExternalTask(externalTaskType);
        } else {
            IncludedTaskFileType includedTaskFileType = new IncludedTaskFileType();
            if (taskRefType.equals(PROFORMA_RESULT_SPEC_FORMAT_ZIP)) {
                includedTaskFileType.setAttachedZipFile(taskFilenameOrUUID);
            } else {
                AttachedTxtFileType attachedTxtFileType = new AttachedTxtFileType();
                attachedTxtFileType.setEncoding("UTF-8");
                attachedTxtFileType.setValue(taskFilenameOrUUID);
                includedTaskFileType.setAttachedXmlFile(attachedTxtFileType);
            }
            submissionPojo.setIncludedTaskFile(includedTaskFileType);
        }
    }

    private static void adjustGradingHints(SubmissionType submissionPojo, GradingHintsType taskGradingHintsElem,
                                           TestsType taskTestElem, String resultFormat, double maxScoreLMS) {
        if (!ProformaFormatter.PROFORMA_MERGED_FEEDBACK_TYPE.equals(resultFormat) || taskGradingHintsElem == null || taskTestElem == null) {
            System.out.println(resultFormat);
            return;
        }

        try {
            GradingHintsHelper gradingHintsHelper = new GradingHintsHelper(taskGradingHintsElem, taskTestElem);
            if (!gradingHintsHelper.isEmpty()) {
                double maxScoreGradingHints = gradingHintsHelper.calculateMaxScore();
                if (Math.abs(maxScoreGradingHints - maxScoreLMS) > 1E-5 && maxScoreGradingHints > 0) {
                    double scalingFactor = maxScoreLMS / maxScoreGradingHints;
                    gradingHintsHelper.adjustWeights(scalingFactor);
                    submissionPojo.setGradingHints(taskGradingHintsElem);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while adjusting grading hints.", e);
        }
    }

    private static void handleSubmissionFiles(SubmissionType submissionPojo, List<String> files) {
        SubmissionFilesType submissionFilesType = new SubmissionFilesType();
        for (String fileEntry : files) {
            SubmissionFileType submissionFileType = new SubmissionFileType();

            String mimeType = Utility.determineMimeType(fileEntry);
            submissionFileType.setMimetype(mimeType);

            if (mimeType.startsWith("text/")) {
                AttachedTxtFileType attachedTxtFileType = new AttachedTxtFileType();
                attachedTxtFileType.setEncoding("UTF-8");
                attachedTxtFileType.setValue(fileEntry);
                submissionFileType.setAttachedTxtFile(attachedTxtFileType);
            } else {
                submissionFileType.setAttachedBinFile(fileEntry);
            }
            submissionFilesType.getFile().add(submissionFileType);
        }
        submissionPojo.setFiles(submissionFilesType);
    }

    private static void setupLmsDetails(SubmissionType submissionPojo, String lmsURL, String courseID, String userID) {
        LmsType lms = new LmsType();
        lms.setUrl(lmsURL);
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(new java.util.Date());
            XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
            lms.setSubmissionDatetime(xmlGregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Error while setting the LMS submission datetime.", e);
        }
        lms.getUserId().add(userID);
        lms.setCourseId(courseID);
        submissionPojo.setLms(lms);
    }

    private static void setupResultSpec(SubmissionType submissionPojo, String resultFormat, String resultStructure,
                                        String studentFeedbackLevel, String teacherFeedbackLevel) {
        ResultSpecType resultSpec = new ResultSpecType();
        resultSpec.setFormat(resultFormat);
        resultSpec.setStructure(resultStructure);
        resultSpec.setStudentFeedbackLevel(FeedbackLevelType.fromValue(studentFeedbackLevel));
        resultSpec.setTeacherFeedbackLevel(FeedbackLevelType.fromValue(teacherFeedbackLevel));
        submissionPojo.setResultSpec(resultSpec);
    }

    public static byte[] createSubmissionZip(SubmissionType submissionType, String taskFilename, String taskRefType,
                                             List<String> submissionFilePaths) throws Exception {
        Zip.ZipContent zipContent = new Zip.ZipContent();

        addSubmissionXmlToZipContent(zipContent, submissionType);

        if (!"uuid".equals(taskRefType)) {
            String taskEntryPath = "task/" + taskFilename;
            addFileToZipContent(zipContent, taskEntryPath, Paths.get("task", taskFilename));
        }

        for (String submissionFileName : submissionFilePaths) {
            Path submissionPath = Paths.get("submission", submissionFileName);
            String submissionEntryPath = "submission/" + submissionFileName;
            addFileToZipContent(zipContent, submissionEntryPath, submissionPath);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Zip.writeMapToZipFile(zipContent, baos);
            return baos.toByteArray();
        }
    }


    private static void addSubmissionXmlToZipContent(Zip.ZipContent zipContent, SubmissionType submissionType) throws Exception {
        // Marshal the SubmissionType to XML in memory
        JAXBContext jaxbContext = JAXBContext.newInstance(SubmissionType.class);
        ByteArrayOutputStream xmlOutputStream = new ByteArrayOutputStream();
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(submissionType, xmlOutputStream);

        // Create a ZipContentElement for submission.xml
        byte[] xmlBytes = xmlOutputStream.toByteArray();
        Zip.ZipContentElement element = new Zip.ZipContentElement("submission.xml", xmlBytes, System.currentTimeMillis());

        // Add to the ZIP content
        zipContent.put("submission.xml", element);
    }

    private static void addFileToZipContent(Zip.ZipContent zipContent, String entryPath, Path filePath) throws IOException {
        if (Files.isDirectory(filePath)) {
            // Add the directory itself
            Zip.ZipContentElement dirElement = new Zip.ZipContentElement(entryPath + "/", null, Files.getLastModifiedTime(filePath).toMillis());
            zipContent.put(entryPath.endsWith("/") ? entryPath : entryPath + "/", dirElement);

            // Recursively add contents of the directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(filePath)) {
                for (Path child : stream) {
                    String childEntryPath = entryPath + "/" + child.getFileName();
                    addFileToZipContent(zipContent, childEntryPath, child);
                }
            }
        } else {
            // Add a file
            byte[] fileBytes = Files.readAllBytes(filePath);
            Zip.ZipContentElement fileElement = new Zip.ZipContentElement(entryPath, fileBytes, Files.getLastModifiedTime(filePath).toMillis());
            zipContent.put(entryPath, fileElement);
        }
    }

}