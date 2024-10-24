package org.abp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Utility {

    /**
     * Reads variables from the 'vpl_environment.sh' script provided by VPL that are needed for the grading process.
     * Reading the values of these variables in this manner allows for easy expansion later when a hybrid approach is to be implemented.
     *
     * @return A map containing the environment variables required for grading, or null if the file could not be read.
     */
    public static Map<String, Object> getArgsFromVplEnvironmentScript() {
        Map<String, Object> envMap = new HashMap<>();

        try (BufferedReader environmentFile = new BufferedReader(new FileReader("vpl_environment.sh"))) {
            String line;

            while ((line = environmentFile.readLine()) != null) {
                if (line.startsWith("export MOODLE_USER_ID=")) {
                    String moodleUserId = line.substring(line.indexOf("=") + 2, line.length() - 1); // Remove the quotes
                    envMap.put("moodleUserId", moodleUserId);
                } else if (line.startsWith("export MOODLE_COURSE_ID=")) {
                    String moodleCourseId = line.substring(line.indexOf("=") + 2, line.length() - 1);
                    envMap.put("moodleCourseId", moodleCourseId);
                } else if (line.startsWith("export MOODLE_URL=")) {
                    String moodleURL = line.substring(line.indexOf("=") + 2, line.length() - 1);
                    envMap.put("moodleURL", moodleURL);
                } else if (line.startsWith("export VPL_GRADEMAX=")) {
                    String maxGradeStr = line.substring(line.indexOf("=") + 2, line.length() - 1);
                    double vplGradeMax = Double.parseDouble(maxGradeStr);
                    envMap.put("vplGradeMax", vplGradeMax);
                }
            }
        } catch (IOException e) {
            System.err.println("Error opening file: vpl_environment.sh");
            return null;
        }

        return envMap;
    }

    /**
     * Retrieves the file extension from the provided file name.
     *
     * @param fileName The name of the file.
     * @return The file extension, or an empty string if no extension is found.
     */
    public static String getFileExtension(String fileName) {
        int dotPosition = fileName.lastIndexOf('.');
        if (dotPosition > 0 && dotPosition < fileName.length() - 1) {
            return fileName.substring(dotPosition + 1);
        } else {
            return "";
        }
    }

    /**
     * Reads configuration values from the 'proforma_settings.sh' script needed for grader and submission settings.
     * The configuration values are stored in a map and returned for further use.
     *
     * @return A map containing the configuration values required for grading, or null if the file could not be read.
     */
    public static Map<String, String> getArgsFromProformaSettingsScript() {
        Map<String, String> configValues = new HashMap<>();

        try (BufferedReader proformaSettingsFile = new BufferedReader(new FileReader("proforma_settings.sh"))) {
            String line;

            while ((line = proformaSettingsFile.readLine()) != null) {
                if (line.startsWith("export SERVICE_URL=")) {
                    configValues.put("SERVICE_URL", line.substring(line.indexOf("=") + 2, line.length() - 1));
                } else if (line.startsWith("export LMS_ID=")) {
                    configValues.put("LMS_ID", line.substring(line.indexOf("=") + 2, line.length() - 1));
                } else if (line.startsWith("export LMS_PASSWORD=")) {
                    configValues.put("LMS_PASSWORD", line.substring(line.indexOf("=") + 2, line.length() - 1));
                } else if (line.startsWith("export GRADER_NAME=")) {
                    configValues.put("GRADER_NAME", line.substring(line.indexOf("=") + 2, line.length() - 1));
                } else if (line.startsWith("export GRADER_VERSION=")) {
                    configValues.put("GRADER_VERSION", line.substring(line.indexOf("=") + 2, line.length() - 1));
                } else if (line.startsWith("export FEEDBACK_TYPE=")) {
                    configValues.put("FEEDBACK_TYPE", line.substring(line.indexOf("=") + 2, line.length() - 1));
                } else if (line.startsWith("export FEEDBACK_STRUCTURE=")) {
                    configValues.put("FEEDBACK_STRUCTURE", line.substring(line.indexOf("=") + 2, line.length() - 1));
                } else if (line.startsWith("export STUDENT_FEEDBACK_LEVEL=")) {
                    configValues.put("STUDENT_FEEDBACK_LEVEL", line.substring(line.indexOf("=") + 2, line.length() - 1));
                } else if (line.startsWith("export TEACHER_FEEDBACK_LEVEL=")) {
                    configValues.put("TEACHER_FEEDBACK_LEVEL", line.substring(line.indexOf("=") + 2, line.length() - 1));
                }
            }
        } catch (IOException e) {
            System.err.println("Error opening file: proforma_settings.sh");
            return null;
        }

        return configValues;
    }

}
