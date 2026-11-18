package org.lions.backend.application.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.lions.backend.domain.enums.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Contrato §6.2 — dono: Fila + Notificações (Frente E). */
@Slf4j
@Service
public class NotificationService {

    /** Registra/envia notificação ao beneficiário pelos canais consentidos (RN07, RN46–RN48). */
    @Transactional
    public void notifyBeneficiary(Long beneficiaryId, Long loanId, NotificationType type, String message) {
        log.info("[Frente E pendente] Notificação para beneficiário {}: tipo={}, msg={}", beneficiaryId, type, message);
    }

    /** Notifica os administradores (RN39, RN49). */
    @Transactional
    public void notifyAdmins(NotificationType type, String message) {
        log.info("[Frente E pendente] Notificação para administradores: tipo={}, msg={}", type, message);
    }
}
