package org.lions.backend.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.lions.backend.application.port.NotificationSenderPort;
import org.lions.backend.application.support.LgpdMasker;
import org.lions.backend.domain.enums.NotificationChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Adaptador padrão de envio (sem provedor real). Modo por {@code lions.notification.mode}:
 * <ul>
 *   <li>{@code manual} (padrão): toda notificação vira tarefa de contato manual com o link wa.me pronto;</li>
 *   <li>{@code simulate}: registra a mensagem no log e considera enviada (útil em desenvolvimento/demonstração).</li>
 * </ul>
 */
@Slf4j
@Component
public class LogNotificationSenderAdapter implements NotificationSenderPort {

    private final boolean simulate;

    public LogNotificationSenderAdapter(@Value("${lions.notification.mode:manual}") String mode) {
        this.simulate = "simulate".equals(mode == null ? "" : mode.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public DeliveryResult send(NotificationChannel channel, String destination, String message) {
        if (!simulate) {
            return DeliveryResult.MANUAL_REQUIRED;
        }
        String maskedDestination = channel == NotificationChannel.EMAIL
                ? maskEmail(destination)
                : LgpdMasker.maskPhone(destination);
        log.info("[NOTIFICAÇÃO SIMULADA] canal={} destino={} mensagem={}", channel, maskedDestination, message);
        return DeliveryResult.SENT;
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        return "***" + email.substring(email.indexOf('@'));
    }
}
