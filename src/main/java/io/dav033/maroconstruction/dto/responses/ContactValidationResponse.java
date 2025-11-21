package io.dav033.maroconstruction.dto.responses;

public class ContactValidationResponse {
    private boolean nameAvailable;
    private boolean emailAvailable;
    private boolean phoneAvailable;
    private String nameReason;
    private String emailReason;
    private String phoneReason;

    public ContactValidationResponse() {}

    public boolean isNameAvailable() { return nameAvailable; }
    public void setNameAvailable(boolean nameAvailable) { this.nameAvailable = nameAvailable; }
    public boolean isEmailAvailable() { return emailAvailable; }
    public void setEmailAvailable(boolean emailAvailable) { this.emailAvailable = emailAvailable; }
    public boolean isPhoneAvailable() { return phoneAvailable; }
    public void setPhoneAvailable(boolean phoneAvailable) { this.phoneAvailable = phoneAvailable; }
    public String getNameReason() { return nameReason; }
    public void setNameReason(String nameReason) { this.nameReason = nameReason; }
    public String getEmailReason() { return emailReason; }
    public void setEmailReason(String emailReason) { this.emailReason = emailReason; }
    public String getPhoneReason() { return phoneReason; }
    public void setPhoneReason(String phoneReason) { this.phoneReason = phoneReason; }

    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private final ContactValidationResponse instance = new ContactValidationResponse();
        public Builder nameAvailable(boolean v) { instance.setNameAvailable(v); return this; }
        public Builder emailAvailable(boolean v) { instance.setEmailAvailable(v); return this; }
        public Builder phoneAvailable(boolean v) { instance.setPhoneAvailable(v); return this; }
        public Builder nameReason(String v) { instance.setNameReason(v); return this; }
        public Builder emailReason(String v) { instance.setEmailReason(v); return this; }
        public Builder phoneReason(String v) { instance.setPhoneReason(v); return this; }
        public ContactValidationResponse build() { return instance; }
    }
}
