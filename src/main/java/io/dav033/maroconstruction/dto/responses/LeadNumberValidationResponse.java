package io.dav033.maroconstruction.dto.responses;

public class LeadNumberValidationResponse {
    private boolean valid;
    private String reason;

    public LeadNumberValidationResponse() {}

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private final LeadNumberValidationResponse instance = new LeadNumberValidationResponse();
        public Builder valid(boolean v) { instance.setValid(v); return this; }
        public Builder reason(String r) { instance.setReason(r); return this; }
        public LeadNumberValidationResponse build() { return instance; }
    }
}
