package org.abp.vpl_proforma.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class VplEnvironmentConfig {
    private String moodleUserId;
    private String moodleCourseId;
    private String lmsURL;
    private double maxScoreLMS;

    public VplEnvironmentConfig(String fileName) throws IOException {
        loadConfigFromFile(fileName);
    }

    public String getMoodleUserId() {
        return moodleUserId;
    }

    public void setMoodleUserId(String moodleUserId) {
        this.moodleUserId = moodleUserId;
    }

    public String getMoodleCourseId() {
        return moodleCourseId;
    }

    public void setMoodleCourseId(String moodleCourseId) {
        this.moodleCourseId = moodleCourseId;
    }

    public String getLmsURL() {
        return lmsURL;
    }

    public void setLmsURL(String lmsURL) {
        this.lmsURL = lmsURL;
    }

    public double getMaxScoreLMS() {
        return maxScoreLMS;
    }

    public void setMaxScoreLMS(double maxScoreLMS) {
        this.maxScoreLMS = maxScoreLMS;
    }

    /**
     * Reads variables from the 'vpl_environment.sh' script provided by VPL that are needed for the grading process.
     */
    private void loadConfigFromFile(String fileName) throws IOException {
        try (BufferedReader environmentFile = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = environmentFile.readLine()) != null) {
                if (line.startsWith("export MOODLE_USER_ID=")) {
                    this.moodleUserId = line.substring(line.indexOf("=") + 2, line.length() - 1); // Remove quotes
                } else if (line.startsWith("export MOODLE_COURSE_ID=")) {
                    this.moodleCourseId = line.substring(line.indexOf("=") + 2, line.length() - 1);
                } else if (line.startsWith("export MOODLE_URL=")) {
                    this.lmsURL = line.substring(line.indexOf("=") + 2, line.length() - 1);
                } else if (line.startsWith("export VPL_GRADEMAX=")) {
                    String maxGradeStr = line.substring(line.indexOf("=") + 2, line.length() - 1);
                    this.maxScoreLMS = Double.parseDouble(maxGradeStr);
                }
            }
        } catch (IOException e) {
            System.err.println("Error opening file: " + fileName);
            throw e;
        }
    }
}
