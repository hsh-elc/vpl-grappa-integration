package org.abp.vpl_proforma.utility.communicator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GrappaCommunicator implements CommunicatorInterface {
    private final String serviceURL;
    private final String lmsID;
    private final String lmsPassword;

    public GrappaCommunicator(String serviceURL, String lmsID, String lmsPassword) {
        this.serviceURL = serviceURL;
        this.lmsID = lmsID;
        this.lmsPassword = lmsPassword;
    }

    /**
     * Checks if the task file identified by the UUID is cached on the server.
     *
     * @param uuid The UUID of the task file.
     */
    @Override
    public boolean isTaskCached(String uuid) throws Exception {
        String urlString = serviceURL + "/tasks/" + uuid;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("HEAD");
        String credentials = Base64.getEncoder().encodeToString((lmsID + ":" + lmsPassword).getBytes());
        connection.setRequestProperty("Authorization", "Basic " + credentials);

        int responseCode = connection.getResponseCode();
        connection.disconnect();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            return true;
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            return false;
        } else {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
    }

    public AsyncSubmissionResponse enqueueAsyncSubmission(String graderName, String graderVersion, byte[] submissionContent) {
        String urlString = serviceURL + "/" + lmsID + "/gradeprocesses?graderName=" + graderName + "&graderVersion=" + graderVersion + "&async=true";
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((lmsID + ":" + lmsPassword).getBytes()));
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(submissionContent);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                String jsonResponse = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                return parseAsyncResponse(jsonResponse);
            } else {
                InputStream errorStream = connection.getErrorStream();
                String errorDetails = errorStream != null ? new String(errorStream.readAllBytes(), StandardCharsets.UTF_8) : "No error details provided";
                throw new RuntimeException(String.format("Failed to enqueue asynchronous submission. HTTP %d: %s. Error Details: %s",
                        responseCode, connection.getResponseMessage(), errorDetails));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve response from grappa-middleware. Following error occurred: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
 
    private AsyncSubmissionResponse parseAsyncResponse(String jsonResponse) {
        // Extract gradeProcessId
        int idStart = jsonResponse.indexOf("\"gradeProcessId\":\"") + 18;
        int idEnd = jsonResponse.indexOf("\"", idStart);
        String gradeProcessId = jsonResponse.substring(idStart, idEnd);

        // Extract estimatedSecondsRemaining
        int timeStart = jsonResponse.indexOf("\"estimatedSecondsRemaining\":\"") + 29;
        int timeEnd = jsonResponse.indexOf("\"", timeStart);
        int estimatedSecondsRemaining = Integer.parseInt(jsonResponse.substring(timeStart, timeEnd));

        return new AsyncSubmissionResponse(gradeProcessId, estimatedSecondsRemaining);
    }

    public SyncSubmissionResponse enqueueSyncSubmission(String graderName, String graderVersion, byte[] submissionContent) {
        String urlString = serviceURL + "/" + lmsID + "/gradeprocesses?graderName=" + graderName + "&graderVersion=" + graderVersion + "&async=false";
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((lmsID + ":" + lmsPassword).getBytes()));
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(submissionContent);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String contentType = connection.getContentType();
                byte[] content = connection.getInputStream().readAllBytes();

                if ("application/xml".equalsIgnoreCase(contentType) || "text/xml".equalsIgnoreCase(contentType)) {
                    return new SyncSubmissionResponse(content, "application/xml");
                } else if ("application/octet-stream".equalsIgnoreCase(contentType)) {
                    return new SyncSubmissionResponse(content, "application/octet-stream");
                } else {
                    throw new RuntimeException("Unexpected Content-Type: " + contentType);
                }
            } else {
                InputStream errorStream = connection.getErrorStream();
                String errorDetails = errorStream != null ? new String(errorStream.readAllBytes(), StandardCharsets.UTF_8) : "No error details provided";
                throw new RuntimeException(String.format("Failed to enqueue synchronous submission. HTTP %d: %s. Error Details: %s",
                        responseCode, connection.getResponseMessage(), errorDetails));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to enqueue submission from vpl-jail-system to grappa-middleware. Following error occurred: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public String getGradingResult(String graderName, String graderVersion, String gradeProcessId) throws Exception {
        String urlString = serviceURL + "/gradeprocesses/" + gradeProcessId;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((lmsID + ":" + lmsPassword).getBytes()));

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return "Grading Result Retrieved Successfully";
        } else {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
    }
}

