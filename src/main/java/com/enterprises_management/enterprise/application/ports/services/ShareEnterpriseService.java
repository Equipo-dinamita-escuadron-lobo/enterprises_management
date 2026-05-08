package com.enterprises_management.enterprise.application.ports.services;

import com.enterprises_management.enterprise.application.ports.input.IShareEnterpriseInputPort;
import com.enterprises_management.enterprise.application.ports.output.IKeycloakRolePort;
import com.enterprises_management.enterprise.application.ports.output.IShareEnterpriseEmailPort;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ShareEnterpriseService implements IShareEnterpriseInputPort {

    private static final String ALLOWED_DOMAIN = "@unicauca.edu.co";

    private final IShareEnterpriseEmailPort emailPort;
    private final IEnterpriseSearchOutputPort enterpriseSearchOutputPort;
    private final IKeycloakRolePort keycloakRolePort;

    public ShareEnterpriseService(IShareEnterpriseEmailPort emailPort,
                                  IEnterpriseSearchOutputPort enterpriseSearchOutputPort,
                                  IKeycloakRolePort keycloakRolePort) {
        this.emailPort = emailPort;
        this.enterpriseSearchOutputPort = enterpriseSearchOutputPort;
        this.keycloakRolePort = keycloakRolePort;
    }

    @Override
    public ShareResult share(String enterpriseId, List<String> emails, String role,
                             String senderName, String senderEmail) {
        Enterprise enterprise = enterpriseSearchOutputPort.getEnterpriseById(UUID.fromString(enterpriseId));
        String enterpriseName = enterprise != null ? enterprise.getName() : enterpriseId;

        List<String> notified = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        List<String> notRegistered = new ArrayList<>();

        for (String email : emails) {
            if (email == null || !email.toLowerCase().endsWith(ALLOWED_DOMAIN)) {
                rejected.add(email);
                continue;
            }
            try {
                boolean userExists = keycloakRolePort.assignRoleByEmail(email, role);
                if (!userExists) {
                    notRegistered.add(email);
                    continue;
                }
                emailPort.sendShareNotification(email, enterpriseName, role, senderName, senderEmail);
                notified.add(email);
            } catch (Exception e) {
                rejected.add(email);
            }
        }

        return new ShareResult(notified, rejected, notRegistered);
    }
}
