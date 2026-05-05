package com.enterprises_management.enterprise.application.ports.input;

import java.util.List;

public interface IShareEnterpriseInputPort {

    ShareResult share(String enterpriseId, List<String> emails, String role, String senderName, String senderEmail);

    record ShareResult(List<String> notified, List<String> rejected, List<String> pendingRegistration) {}
}
