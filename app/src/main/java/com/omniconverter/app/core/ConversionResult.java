package com.omniconverter.app.core;

import android.net.Uri;

public class ConversionResult {
    public enum Status {SUCCESS, FAILED, IN_PROGRESS}

    private Uri inputUri;
    private Uri outputUri;
    private Status status;
    private String message;

    public ConversionResult(Uri inputUri) {
        this.inputUri = inputUri;
        this.status = Status.IN_PROGRESS;
    }

    public Uri getInputUri() { return inputUri; }
    public Uri getOutputUri() { return outputUri; }
    public void setOutputUri(Uri outputUri) { this.outputUri = outputUri; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
