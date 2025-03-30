package org.abp.vpl_proforma.submission.config;

import java.io.*;
import java.util.*;

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

    private static final List<String> REQUIRED_KEYS = Arrays.asList(
        "SERVICE_URL", "LMS_ID", "LMS_PASSWORD", "GRADER_NAME", "GRADER_VERSION", 
        "FEEDBACK_FORMAT", "FEEDBACK_STRUCTURE", "STUDENT_FEEDBACK_LEVEL", "TEACHER_FEEDBACK_LEVEL"
    );
    
    public ProformaConfig(String fileName) throws IOException {
        loadConfigFromFile(fileName);
    }

    private void loadConfigFromFile(String fileName) throws IOException {
        Map<String, String> configMap = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("export ")) {
                    String[] parts = line.replace("export ", "").split("=", 2);
                    if (parts.length == 2) {
                        configMap.put(parts[0].trim(), parts[1].replaceAll("\"", "").trim());
                    }
                }
            }
        }
        
        List<String> missingKeys = new ArrayList<>();
        for (String key : REQUIRED_KEYS) {
            if (!configMap.containsKey(key) || configMap.get(key).isEmpty()) {
                missingKeys.add(key);
            }
        }
        
        if (!missingKeys.isEmpty()) {
            System.err.println("Error: Failed to process 'proforma_settings.sh' file.");
            System.err.println("The following required configurations are missing or empty:");
            for (String missingKey : missingKeys) {
                System.err.println("- " + missingKey);
            }
            System.err.println("Please ensure all required configurations are defined correctly in 'proforma_settings.sh'.");
            System.exit(1);
        }
        
        this.serviceURL = configMap.get("SERVICE_URL");
        this.lmsID = configMap.get("LMS_ID");
        this.lmsPassword = configMap.get("LMS_PASSWORD");
        this.graderName = configMap.get("GRADER_NAME");
        this.graderVersion = configMap.get("GRADER_VERSION");
        this.feedbackFormat = configMap.get("FEEDBACK_FORMAT");
        this.feedbackStructure = configMap.get("FEEDBACK_STRUCTURE");
        this.studentFeedbackLevel = configMap.get("STUDENT_FEEDBACK_LEVEL");
        this.teacherFeedbackLevel = configMap.get("TEACHER_FEEDBACK_LEVEL");
    }

    public String getServiceURL() { return serviceURL; }
    public String getLmsID() { return lmsID; }
    public String getLmsPassword() { return lmsPassword; }
    public String getGraderName() { return graderName; }
    public String getGraderVersion() { return graderVersion; }
    public String getFeedbackFormat() { return feedbackFormat; }
    public String getFeedbackStructure() { return feedbackStructure; }
    public String getStudentFeedbackLevel() { return studentFeedbackLevel; }
    public String getTeacherFeedbackLevel() { return teacherFeedbackLevel; }
}
