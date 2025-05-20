package org.abp.vpl_proforma.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.tika.Tika;

public class Utility {

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

    public static byte[] getFileAsBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    /**
     * Determines the MIME Type of a file provided by filePath.
     * Uses java.nio.file.Files.probeContentType and Apache Tika for MIME Type determination
     * 
     * @param filePath file path of file to be checked
     * @return MIME Type of given file as String
     */
    public static String determineMimeType(String filePath) {
        try {
            Path path = Paths.get(filePath);
            String mimeType = Files.probeContentType(path);
            if (mimeType == null) {
                Tika tika = new Tika();
                mimeType = tika.detect(path);
            }
            return mimeType;
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

}
