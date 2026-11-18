package org.lions.backend.application.dto.response;

import org.lions.backend.domain.entity.Beneficiary;
import org.lions.backend.domain.enums.DocumentType;

import java.time.LocalDateTime;

/** Item de listagem de beneficiários — documento sempre mascarado (LGPD). */
public record BeneficiarySummaryResponse(
        Long id,
        String fullName,
        DocumentType documentType,
        String maskedDocument,
        String phone,
        String city,
        String state,
        boolean active,
        boolean anonymized,
        LocalDateTime createdAt) {

    public static BeneficiarySummaryResponse from(Beneficiary b) {
        return new BeneficiarySummaryResponse(b.getId(), b.getFullName(), b.getDocumentType(), b.getMaskedDocument(),
                b.getPhone(), b.getCity(), b.getState(), b.isActive(), b.isAnonymized(), b.getCreatedAt());
    }
}
