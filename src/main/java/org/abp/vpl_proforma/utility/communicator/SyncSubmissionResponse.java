package org.abp.vpl_proforma.utility.communicator;

public class SyncSubmissionResponse {
    private final byte[] content;
    private final String contentType;

    public SyncSubmissionResponse(byte[] content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isXml() {
        return "application/xml".equalsIgnoreCase(contentType) || "text/xml".equalsIgnoreCase(contentType);
    }

    public boolean isZip() {
        return "application/octet-stream".equalsIgnoreCase(contentType);
    }
}
