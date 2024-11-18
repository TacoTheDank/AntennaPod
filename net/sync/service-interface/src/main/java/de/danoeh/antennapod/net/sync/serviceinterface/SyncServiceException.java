package de.danoeh.antennapod.net.sync.serviceinterface;

public class SyncServiceException extends Exception {
    private static final long serialVersionUID = 1L;

    public SyncServiceException(String message) {
        super(message);
    }

    public SyncServiceException(Throwable cause) {
        super(cause);
    }
}
