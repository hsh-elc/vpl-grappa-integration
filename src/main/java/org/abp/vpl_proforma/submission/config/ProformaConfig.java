package org.abp.vpl_proforma.submission.config;

import java.io.*;
import java.util.*;

public class ProformaConfig {
    private String serviceURL;
    private String lmsID;
    private String lmsPassword;
    private boolean acceptSelfSignedCerts;
    private String graderName;
    private String graderVersion;
    private String feedbackFormat;
    private String feedbackStructure;
    private String studentFeedbackLevel;
    private String teacherFeedbackLevel;

    private static final List<String> REQUIRED_KEYS = Arrays.asList(
            "SERVICE_URL", "LMS_ID", "LMS_PASSWORD", "GRADER_NAME", "GRADER_VERSION"
        );
        
    private static final Map<String, String> OPTIONAL_KEYS = new HashMap<>();
    static {
        // Default values
        OPTIONAL_KEYS.put("ACCEPT_SELF_SIGNED_CERTS", "no");
        OPTIONAL_KEYS.put("FEEDBACK_FORMAT", "zip");
        OPTIONAL_KEYS.put("FEEDBACK_STRUCTURE", "separate-test-feedback");
        OPTIONAL_KEYS.put("STUDENT_FEEDBACK_LEVEL", "info");
        OPTIONAL_KEYS.put("TEACHER_FEEDBACK_LEVEL", "info");
    }
    
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
                        String value = parts[1].substring(1, parts[1].length() - 1); // Remove front and trailing " or '
                        configMap.put(parts[0].trim(), value.trim());
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
            throw new IllegalStateException("Failed to process 'proforma_settings.sh' file. " +
                "The following required configurations are missing or empty: " +
                String.join(", ", missingKeys) +
                ". Please ensure all required configurations are defined correctly in 'proforma_settings.sh'.");
        }

        for (String key : OPTIONAL_KEYS.keySet()) {
            if (!configMap.containsKey(key) || configMap.get(key).isEmpty()) {
                configMap.put(key, OPTIONAL_KEYS.get(key));
            }
        }

        this.serviceURL = configMap.get("SERVICE_URL").trim();
        // chop trailing slashes, if any:
        while (this.serviceURL.endsWith("/") || this.serviceURL.endsWith("\\")) {
            this.serviceURL = this.serviceURL.substring(0, this.serviceURL.length()-1);
        }

        this.lmsID = configMap.get("LMS_ID").trim();
        
        this.lmsPassword = configMap.get("LMS_PASSWORD").trim();
        
        String a = configMap.get("ACCEPT_SELF_SIGNED_CERTS").trim().toUpperCase();
        boolean yes = a.equals("YES") || a.equals("TRUE") || a.equals("ON"); 
        boolean no = a.equals("NO") || a.equals("FALSE") || a.equals("OFF"); 
        if (!yes && !no) 
            throw new IllegalStateException("Failed to process 'proforma_settings.sh' file. Illegal value '" + configMap.get("ACCEPT_SELF_SIGNED_CERTS") + "' for ACCEPT_SELF_SIGNED_CERTS");
        this.acceptSelfSignedCerts = yes;
        
        this.graderName = configMap.get("GRADER_NAME").trim();
        this.graderVersion = configMap.get("GRADER_VERSION").trim();
        this.feedbackFormat = configMap.get("FEEDBACK_FORMAT").trim();
        this.feedbackStructure = configMap.get("FEEDBACK_STRUCTURE").trim();
        this.studentFeedbackLevel = configMap.get("STUDENT_FEEDBACK_LEVEL").trim();
        this.teacherFeedbackLevel = configMap.get("TEACHER_FEEDBACK_LEVEL").trim();
    }

    public String getServiceURL() { return serviceURL; }
    public String getLmsID() { return lmsID; }
    public String getLmsPassword() { return lmsPassword; }
    public boolean getAcceptSelfSignedCerts() { return acceptSelfSignedCerts; }
    public String getGraderName() { return graderName; }
    public String getGraderVersion() { return graderVersion; }
    public String getFeedbackFormat() { return feedbackFormat; }
    public String getFeedbackStructure() { return feedbackStructure; }
    public String getStudentFeedbackLevel() { return studentFeedbackLevel; }
    public String getTeacherFeedbackLevel() { return teacherFeedbackLevel; }
}
