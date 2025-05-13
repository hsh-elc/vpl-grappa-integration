package org.abp.vpl_proforma.submission;

import org.abp.vpl_proforma.utility.GradingHintsHelper;
import org.abp.vpl_proforma.utility.Utility;

import proforma.util.TaskLive;
import proforma.util.div.Zip;
import proforma.util.resource.TaskResource;
import proforma.xml21.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.nio.file.*;

public class ProformaSubmissionFormatter {

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

    private static final String[] ATTACHED_TXT_MIME_TYPES = {"application/xml", "application/sql"};

    public TaskType getTaskType(String taskFilename) {
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

    public SubmissionType createSubmissionPojo(String taskFilenameOrUUID, String taskRefType,
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

    private void handleTaskReference(SubmissionType submissionPojo, String taskFilenameOrUUID, String taskRefType) {
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

    private void adjustGradingHints(SubmissionType submissionPojo, GradingHintsType taskGradingHintsElem,
                                           TestsType taskTestElem, String resultFormat, double maxScoreLMS) {
        if (taskGradingHintsElem == null || taskTestElem == null) {
            return;
        }

        try {
            GradingHintsHelper gradingHintsHelper = new GradingHintsHelper(taskGradingHintsElem, taskTestElem);
            if (!gradingHintsHelper.isEmpty()) {
                double maxScoreGradingHints = gradingHintsHelper.calculateMaxScore();
                if (Math.abs(maxScoreGradingHints - maxScoreLMS) > 1E-5 && maxScoreGradingHints > 0) {
                    double scalingFactor = maxScoreLMS / maxScoreGradingHints;
                    gradingHintsHelper.adjustWeights(scalingFactor);
                    if (ProformaSubmissionFormatter.PROFORMA_MERGED_FEEDBACK_TYPE.equals(resultFormat)) {
                        submissionPojo.setGradingHints(taskGradingHintsElem);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while adjusting grading hints.", e);
        }
    }

    private void handleSubmissionFiles(SubmissionType submissionPojo, List<String> files) {
        if (files == null) {
            throw new IllegalArgumentException("Submission file list cannot be null.");
        }

        SubmissionFilesType submissionFilesType = new SubmissionFilesType();
        for (String fileEntry : files) {
            try {
                SubmissionFileType submissionFileType = new SubmissionFileType();

                String mimeType = Utility.determineMimeType("submission/" + fileEntry); // "submission" needed for accessing file content
                submissionFileType.setMimetype(mimeType);

                if (mimeType != null && isTxtMimeType(mimeType)) {
                    AttachedTxtFileType attachedTxtFileType = new AttachedTxtFileType();
                    attachedTxtFileType.setEncoding("UTF-8");
                    attachedTxtFileType.setValue(fileEntry);
                    submissionFileType.setAttachedTxtFile(attachedTxtFileType);
                } else {
                    submissionFileType.setAttachedBinFile(fileEntry);
                }
                submissionFilesType.getFile().add(submissionFileType);

            } catch (Exception e) {
                throw new RuntimeException(
                    "Error processing submission file: '" + fileEntry + "'. Details: " + e.getMessage(), 
                    e
                );
            }
        }
        submissionPojo.setFiles(submissionFilesType);
    }

    private void setupLmsDetails(SubmissionType submissionPojo, String lmsURL, String courseID, String userID) {
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

    private void setupResultSpec(SubmissionType submissionPojo, String resultFormat, String resultStructure,
                                        String studentFeedbackLevel, String teacherFeedbackLevel) {
        ResultSpecType resultSpec = new ResultSpecType();
        resultSpec.setFormat(resultFormat);
        resultSpec.setStructure(resultStructure);
        resultSpec.setStudentFeedbackLevel(FeedbackLevelType.fromValue(studentFeedbackLevel));
        resultSpec.setTeacherFeedbackLevel(FeedbackLevelType.fromValue(teacherFeedbackLevel));
        submissionPojo.setResultSpec(resultSpec);
    }

    /**
     * Checks if given MIME Type is eligible for producing AttachedTxtFileType
     * 
     * @param mimeType MIME Type to check
     * @return true if MIME Type starts with "text/" or is in ATTACHED_TXT_MIME_TYPES
     */
    private boolean isTxtMimeType(String mimeType) {
        return mimeType.startsWith("text/") || Arrays.asList(ATTACHED_TXT_MIME_TYPES).contains(mimeType);
    }

    public byte[] createSubmissionZip(SubmissionType submissionType, String taskFilename, String taskRefType,
                                             List<String> submissionFilePaths) {
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
        } catch (IOException e) {
            throw new RuntimeException("Error while creating submission zip.", e);
        }
    }


    private void addSubmissionXmlToZipContent(Zip.ZipContent zipContent, SubmissionType submissionType) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException("Error while adding submission.xml to zip content.", e);
        }
    }

    private void addFileToZipContent(Zip.ZipContent zipContent, String entryPath, Path filePath) {
        try {
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
        } catch (IOException e) {
            throw new RuntimeException("Error while adding file to zip content.", e);
        }
    }

}