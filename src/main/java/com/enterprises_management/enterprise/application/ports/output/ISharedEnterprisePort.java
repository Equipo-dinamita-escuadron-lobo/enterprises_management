package com.enterprises_management.enterprise.application.ports.output;

import java.util.UUID;

public interface ISharedEnterprisePort {
    void saveShare(UUID enterpriseId, String sharedWithUserId);
}
