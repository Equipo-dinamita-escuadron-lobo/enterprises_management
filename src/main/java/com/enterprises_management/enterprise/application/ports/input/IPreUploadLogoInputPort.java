package com.enterprises_management.enterprise.application.ports.input;

import org.springframework.web.multipart.MultipartFile;

public interface IPreUploadLogoInputPort {
    String preUploadLogo(MultipartFile file);
}
