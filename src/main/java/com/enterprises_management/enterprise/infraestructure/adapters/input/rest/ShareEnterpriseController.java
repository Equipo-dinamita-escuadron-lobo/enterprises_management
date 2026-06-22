package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import com.enterprises_management.enterprise.application.ports.input.IShareEnterpriseInputPort;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request.ShareEnterpriseRequest;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.ShareEnterpriseResponse;
import com.enterprises_management.enterprise.infraestructure.security.IJwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprises")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
public class ShareEnterpriseController {

    private final IShareEnterpriseInputPort shareEnterpriseInputPort;
    private final IJwtUtils jwtUtils;

    @PostMapping("/share")
    public ResponseEntity<ShareEnterpriseResponse> share(@Valid @RequestBody ShareEnterpriseRequest request) {
        String senderName = jwtUtils.getName();
        String senderEmail = jwtUtils.getEmail();

        IShareEnterpriseInputPort.ShareResult result = shareEnterpriseInputPort.share(
                request.enterpriseId(),
                request.emails(),
                request.role(),
                senderName != null ? senderName : "Usuario CONTAPP",
                senderEmail != null ? senderEmail : ""
        );

        return ResponseEntity.ok(new ShareEnterpriseResponse(result.notified(), result.rejected(), result.notRegistered()));
    }
}
