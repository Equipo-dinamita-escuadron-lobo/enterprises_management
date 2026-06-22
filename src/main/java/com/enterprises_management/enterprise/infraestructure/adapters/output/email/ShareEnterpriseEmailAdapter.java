package com.enterprises_management.enterprise.infraestructure.adapters.output.email;

import com.enterprises_management.enterprise.application.ports.output.IShareEnterpriseEmailPort;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ShareEnterpriseEmailAdapter implements IShareEnterpriseEmailPort {

    private final String resendApiKey;
    private final String resendFrom;

    public ShareEnterpriseEmailAdapter(
            @Value("${resend.key:}") String resendApiKey,
            @Value("${resend.from:onboarding@resend.dev}") String resendFrom) {
        this.resendApiKey = resendApiKey;
        this.resendFrom = resendFrom;
    }

    @Override
    public void sendShareNotification(String to, String enterpriseName, String role,
                                      String senderName, String senderEmail) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("Resend API key no configurada — omitiendo envío de correo a {}", to);
            return;
        }

        String subject = "Empresa compartida contigo en CONTAPP: " + enterpriseName;
        String body = buildBody(enterpriseName, role, senderName, senderEmail);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(resendFrom)
                .to(List.of(to))
                .subject(subject)
                .html(body)
                .build();

        try {
            CreateEmailResponse response = new Resend(resendApiKey).emails().send(params);
            log.info("Correo de compartir enviado a {} | empresa={} | rol={} | emailId={}",
                    to, enterpriseName, role, response != null ? response.getId() : null);
        } catch (ResendException ex) {
            throw new IllegalStateException("Error al enviar correo via Resend: " + ex.getMessage(), ex);
        }
    }

    private String buildBody(String enterpriseName, String role, String senderName, String senderEmail) {
        return """
                <div style="font-family:sans-serif;max-width:560px;margin:auto;padding:24px">
                  <h2 style="color:#1d4ed8">Se compartió una empresa contigo</h2>
                  <p>Hola,</p>
                  <p><strong>%s</strong> (%s) compartió la empresa <strong>%s</strong> contigo en CONTAPP.</p>
                  <table style="border-collapse:collapse;width:100%%;margin:16px 0">
                    <tr>
                      <td style="padding:8px;font-weight:bold;background:#f3f4f6">Empresa</td>
                      <td style="padding:8px;background:#f9fafb">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:8px;font-weight:bold;background:#f3f4f6">Rol asignado</td>
                      <td style="padding:8px;background:#f9fafb">%s</td>
                    </tr>
                  </table>
                  <p style="color:#6b7280;font-size:13px">
                    Si tenés preguntas, contactá a %s en %s.
                  </p>
                </div>
                """.formatted(senderName, senderEmail, enterpriseName,
                enterpriseName, capitalize(role), senderName, senderEmail);
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
