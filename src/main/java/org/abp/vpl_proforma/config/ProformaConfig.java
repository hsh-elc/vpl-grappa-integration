package org.abp.vpl_proforma.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ProformaConfig {
    private String serviceURL;
    private String lmsID;
    private String lmsPassword;
    private String graderName;
    private String graderVersion;
    private String feedbackFormat;
    private String feedbackStructure;
    private String studentFeedbackLevel;
    private String teacherFeedbackLevel;

    public ProformaConfig(String fileName) throws IOException {
        loadConfigFromFile(fileName);
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public String getLmsID() {
        return lmsID;
    }

    public void setLmsID(String lmsID) {
        this.lmsID = lmsID;
    }

    public String getLmsPassword() {
        return lmsPassword;
    }

    public void setLmsPassword(String lmsPassword) {
        this.lmsPassword = lmsPassword;
    }

    public String getGraderName() {
        return graderName;
    }

    public void setGraderName(String graderName) {
        this.graderName = graderName;
    }

    public String getGraderVersion() {
        return graderVersion;
    }

    public void setGraderVersion(String graderVersion) {
        this.graderVersion = graderVersion;
    }

    public String getFeedbackFormat() {
        return feedbackFormat;
    }

    public void setFeedbackFormat(String feedbackFormat) {
        this.feedbackFormat = feedbackFormat;
    }

    public String getFeedbackStructure() {
        return feedbackStructure;
    }

    public void setFeedbackStructure(String feedbackStructure) {
        this.feedbackStructure = feedbackStructure;
    }

    public String getStudentFeedbackLevel() {
        return studentFeedbackLevel;
    }

    public void setStudentFeedbackLevel(String studentFeedbackLevel) {
        this.studentFeedbackLevel = studentFeedbackLevel;
    }

    public String getTeacherFeedbackLevel() {
        return teacherFeedbackLevel;
    }

    public void setTeacherFeedbackLevel(String teacherFeedbackLevel) {
        this.teacherFeedbackLevel = teacherFeedbackLevel;
    }

    /**
     * Reads configuration values from the 'proforma_settings.sh' script needed for grader and submission settings.
     */
    private void loadConfigFromFile(String fileName) throws IOException {
        try (BufferedReader proformaSettingsFile = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = proformaSettingsFile.readLine()) != null) {
                if (line.startsWith("export SERVICE_URL=")) {
                    this.serviceURL = line.substring(line.indexOf("=") + 2, line.length() - 1);
                } else if (line.startsWith("export LMS_ID=")) {
                    this.lmsID = line.substring(line.indexOf("=") + 2, line.length() - 1);
                } else if (line.startsWith("export LMS_PASSWORD=")) {
                    this.lmsPassword = line.substring(line.indexOf("=") + 2, line.length() - 1);
                } else if (line.startsWith("export GRADER_NAME=")) {
                    this.graderName = line.substring(line.indexOf("=") + 2, line.length() - 1);
                } else if (line.startsWith("export GRADER_VERSION=")) {
                    this.graderVersion = line.substring(line.indexOf("=") + 2, line.length() - 1);
                } else if (line.startsWith("export FEEDBACK_FORMAT=")) {
                    this.feedbackFormat = line.substring(line.indexOf("=") + 2, line.length() - 1);
                } else if (line.startsWith("export FEEDBACK_STRUCTURE=")) {
                    this.feedbackStructure = line.substring(line.indexOf("=") + 2, line.length() - 1);
                } else if (line.startsWith("export STUDENT_FEEDBACK_LEVEL=")) {
                    this.studentFeedbackLevel = line.substring(line.indexOf("=") + 2, line.length() - 1);
                } else if (line.startsWith("export TEACHER_FEEDBACK_LEVEL=")) {
                    this.teacherFeedbackLevel = line.substring(line.indexOf("=") + 2, line.length() - 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Error opening file: " + fileName);
            throw e;
        }
    }
}
