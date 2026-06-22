package com.enterprises_management.enterprise.application.ports.services;

import com.enterprises_management.enterprise.application.ports.input.IUploadLogoInputPort;
import com.enterprises_management.enterprise.application.ports.input.LogoResource;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseUpdateOutputPort;
import com.enterprises_management.enterprise.application.ports.output.ILogoStorageOutputPort;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.UUID;

@Service
public class UploadLogoService implements IUploadLogoInputPort {

    private final ILogoStorageOutputPort logoStorageOutputPort;
    private final IEnterpriseSearchOutputPort enterpriseSearchOutputPort;
    private final IEnterpriseUpdateOutputPort enterpriseUpdateOutputPort;

    public UploadLogoService(
            @Qualifier("localLogoStorageAdapter") ILogoStorageOutputPort logoStorageOutputPort,
            IEnterpriseSearchOutputPort enterpriseSearchOutputPort,
            IEnterpriseUpdateOutputPort enterpriseUpdateOutputPort) {
        this.logoStorageOutputPort = logoStorageOutputPort;
        this.enterpriseSearchOutputPort = enterpriseSearchOutputPort;
        this.enterpriseUpdateOutputPort = enterpriseUpdateOutputPort;
    }

    @Override
    @Transactional
    public String uploadLogo(String enterpriseId, MultipartFile file) {
        // Validar archivo
        validateFile(file);
        UUID id = UUID.fromString(enterpriseId);
        // Buscar empresa
        Enterprise enterprise = enterpriseSearchOutputPort.getEnterpriseById(null);
        if (enterprise == null) {
            throw new IllegalArgumentException("Empresa no encontrada");
        }

        // Si ya tiene logo, eliminar el anterior
        if (enterprise.getLogoKey() != null) {
            logoStorageOutputPort.delete(enterprise.getLogoKey());
        }

        // Guardar nuevo archivo
        String logoKey = logoStorageOutputPort.save(file);

        // Actualizar empresa
        enterprise.setLogoKey(logoKey);
        enterprise.setLogoContentType(file.getContentType());
        enterpriseUpdateOutputPort.updateEnterprise(null, enterprise);

        return logoKey;
    }

    @Override
    public void deleteLogo(String enterpriseId) {
        // Buscar empresa
        UUID id = UUID.fromString(enterpriseId);
        Enterprise enterprise = enterpriseSearchOutputPort.getEnterpriseById(null);
        if (enterprise == null) {
            throw new IllegalArgumentException("Empresa no encontrada");
        }

        // Si tiene logo, eliminar archivo
        if (enterprise.getLogoKey() != null) {
            logoStorageOutputPort.delete(enterprise.getLogoKey());
        }

        // Actualizar empresa
        enterprise.setLogoKey(null);
        enterprise.setLogoContentType(null);
        enterpriseUpdateOutputPort.updateEnterprise(null, enterprise);
    }

    @Override
    public LogoResource getLogo(String enterpriseId) {
        // Buscar empresa
        UUID id = UUID.fromString(enterpriseId);
        Enterprise enterprise = enterpriseSearchOutputPort.getEnterpriseById(null);
        if (enterprise == null || enterprise.getLogoKey() == null) {
            throw new IllegalArgumentException("Logo no encontrado");
        }

        Resource resource = logoStorageOutputPort.load(enterprise.getLogoKey());
        return new LogoResource(resource, enterprise.getLogoContentType());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidContentType(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. Solo se permiten imágenes PNG, JPG, JPEG, SVG");
        }
    }

    private boolean isValidContentType(String contentType) {
        return contentType.equals("image/png") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/jpeg") ||
                contentType.equals("image/svg+xml");
    }
}