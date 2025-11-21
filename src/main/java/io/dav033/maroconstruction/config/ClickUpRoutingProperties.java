package io.dav033.maroconstruction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "clickup.routes")
public class ClickUpRoutingProperties {
    public static class Fields {
        private String contactNameId;
        private String customerNameId;
        private String emailId;
        private String phoneId;
        private String phoneTextId;
        private String leadNumberId;
        private String locationTextId;
        private String locationId;
        public String getContactNameId() { return cleanFieldId(contactNameId); }
        public void setContactNameId(String v) { this.contactNameId = v; }
        public String getCustomerNameId() { return cleanFieldId(customerNameId); }
        public void setCustomerNameId(String v) { this.customerNameId = v; }
        public String getEmailId() { return cleanFieldId(emailId); }
        public void setEmailId(String v) { this.emailId = v; }
        public String getPhoneId() { return cleanFieldId(phoneId); }
        public void setPhoneId(String v) { this.phoneId = v; }
        public String getPhoneTextId() { return cleanFieldId(phoneTextId); }
        public void setPhoneTextId(String v) { this.phoneTextId = v; }
        public String getLeadNumberId() { return cleanFieldId(leadNumberId); }
        public void setLeadNumberId(String v) { this.leadNumberId = v; }
        public String getLocationTextId() { return cleanFieldId(locationTextId); }
        public void setLocationTextId(String v) { this.locationTextId = v; }
        public String getLocationId() { return cleanFieldId(locationId); }
        public void setLocationId(String v) { this.locationId = v; }
        private String cleanFieldId(String fieldId) {
            if (fieldId == null) {
                return null;
            }
            int commentIndex = fieldId.indexOf('#');
            if (commentIndex != -1) {
                fieldId = fieldId.substring(0, commentIndex);
            }
            return fieldId.trim();
        }
    }

    public static class Route {
        private String listId;
        private Fields fields;
        public String getListId() { return listId; }
        public void setListId(String listId) { this.listId = listId; }
        public Fields getFields() { return fields; }
        public void setFields(Fields fields) { this.fields = fields; }
    }
    private Map<String, Route> map;
    public Map<String, Route> getMap() { return map; }
    public void setMap(Map<String, Route> map) { this.map = map; }
}
