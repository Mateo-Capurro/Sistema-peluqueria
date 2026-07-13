package be.parcial.services.implementations;

import be.parcial.domain.entities.TurnoEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.services.NotificationChannel;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Email channel. Cuando {@code app.mail.enabled=true} envía un correo HTML real (SMTP)
 * al cliente con un botón para confirmar el turno sin necesidad de iniciar sesión.
 * Cuando está deshabilitado (default, tests/offline) solo loguea — el envío real es
 * pluggable sin tocar a los callers.
 */
@Component
@Slf4j
public class EmailNotificationChannel implements NotificationChannel {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String frontendUrl;
    private final String from;

    public EmailNotificationChannel(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:false}") boolean enabled,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl,
            @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.frontendUrl = frontendUrl;
        this.from = from;
    }

    @Override
    public void enviar(TurnoEntity turno) {
        UserEntity cliente = turno.getCliente();
        String link = frontendUrl + "/confirmar/" + turno.getConfirmToken();

        if (!enabled) {
            log.info("[EMAIL] Para {} <{}>: turno para el {}. Confirmá en {}",
                    cliente.getName(), cliente.getEmail(), turno.getInicio(), link);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(cliente.getEmail());
            helper.setSubject("Confirmá tu turno en la peluquería");
            helper.setText(buildHtml(turno, cliente, link), true);
            mailSender.send(message);
            log.info("[EMAIL] Enviado a <{}> para turno del {}", cliente.getEmail(), turno.getInicio());
        } catch (Exception e) {
            // No romper la reserva si el mail falla: el turno ya quedó PENDIENTE.
            log.error("[EMAIL] Falló el envío a <{}>: {}", cliente.getEmail(), e.getMessage());
        }
    }

    private String buildHtml(TurnoEntity turno, UserEntity cliente, String link) {
        String tratamiento = turno.getTratamiento().getNombre();
        String peluquero = turno.getPeluquero().getUser().getName();
        String cuando = turno.getInicio().format(FECHA);

        return """
            <div style="margin:0;padding:24px;background:#f3f4f6;font-family:Arial,Helvetica,sans-serif;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                <tr><td align="center">
                  <table role="presentation" width="480" cellpadding="0" cellspacing="0"
                         style="background:#ffffff;border-radius:12px;overflow:hidden;
                                box-shadow:0 1px 3px rgba(0,0,0,.1);">
                    <tr><td style="background:#2563eb;padding:24px 32px;">
                      <h1 style="margin:0;color:#ffffff;font-size:20px;">✂️ Peluquería</h1>
                    </td></tr>
                    <tr><td style="padding:32px;">
                      <p style="margin:0 0 16px;font-size:16px;color:#111827;">
                        Hola <strong>%s</strong>, reservaste un turno:
                      </p>
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                             style="background:#f9fafb;border-radius:8px;margin-bottom:24px;">
                        <tr><td style="padding:16px 20px;font-size:15px;color:#374151;line-height:1.9;">
                          <strong>Tratamiento:</strong> %s<br>
                          <strong>Peluquero:</strong> %s<br>
                          <strong>Fecha:</strong> %s hs
                        </td></tr>
                      </table>
                      <p style="margin:0 0 24px;font-size:15px;color:#374151;">
                        ¿Confirmás tu turno? Hacé click en el botón:
                      </p>
                      <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                        <tr><td align="center">
                          <a href="%s"
                             style="display:inline-block;background:#2563eb;color:#ffffff;
                                    text-decoration:none;font-size:16px;font-weight:bold;
                                    padding:14px 40px;border-radius:8px;">
                            Confirmar turno
                          </a>
                        </td></tr>
                      </table>
                      <p style="margin:24px 0 0;font-size:12px;color:#9ca3af;">
                        Si no fuiste vos, ignorá este mensaje. El link:<br>
                        <span style="color:#6b7280;word-break:break-all;">%s</span>
                      </p>
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </div>
            """.formatted(cliente.getName(), tratamiento, peluquero, cuando, link, link);
    }
}
