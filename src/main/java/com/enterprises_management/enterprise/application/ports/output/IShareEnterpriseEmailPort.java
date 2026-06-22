package com.enterprises_management.enterprise.application.ports.output;

public interface IShareEnterpriseEmailPort {

    void sendShareNotification(String to, String enterpriseName, String role, String senderName, String senderEmail);
}
