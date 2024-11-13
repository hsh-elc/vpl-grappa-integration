package org.abp.vpl_proforma.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static String determineMimeType(String filePath) {
        try {
            return Files.probeContentType(Paths.get(filePath));
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

}
