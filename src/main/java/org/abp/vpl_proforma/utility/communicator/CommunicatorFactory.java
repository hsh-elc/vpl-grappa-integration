package org.abp.vpl_proforma.utility.communicator;

public class CommunicatorFactory {
    public static CommunicatorInterface getCommunicator(String type, String serviceURL, String lmsID, String lmsPassword, boolean acceptSelfSignedCerts) {
        switch (type.toLowerCase()) {
            case "grappa":
                return new GrappaCommunicator(serviceURL, lmsID, lmsPassword, acceptSelfSignedCerts);
            default:
                throw new IllegalArgumentException("Unknown communicator type: " + type);
        }
    }
}

