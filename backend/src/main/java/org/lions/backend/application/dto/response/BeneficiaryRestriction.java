package org.lions.backend.application.dto.response;

import java.util.List;

/**
 * Situação do beneficiário para novos empréstimos/fila (RN20).
 *
 * @param blocked             {@code true} se não pode receber novo empréstimo nem entrar na fila
 * @param hasOpenLoan         possui empréstimo em aberto
 * @param hasOpenOccurrence   possui ocorrência (inadimplência/avaria) não resolvida
 * @param reasons             motivos legíveis do bloqueio
 * @param hasOverdueLoan      possui empréstimo em atraso
 * @param hasPendingSignature possui termo pendente de assinatura (bloqueio transitório)
 * @param inactive            cadastro inativo ou anonimizado
 */
public record BeneficiaryRestriction(
        boolean blocked,
        boolean hasOpenLoan,
        boolean hasOpenOccurrence,
        List<String> reasons,
        boolean hasOverdueLoan,
        boolean hasPendingSignature,
        boolean inactive) {

    public BeneficiaryRestriction {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    /** Construtor do contrato original (§6.2). */
    public BeneficiaryRestriction(boolean blocked, boolean hasOpenLoan, boolean hasOpenOccurrence,
                                  List<String> reasons) {
        this(blocked, hasOpenLoan, hasOpenOccurrence, reasons, false, false, false);
    }

    /**
     * RN34 — bloqueio que remove o beneficiário da fila: atraso, ocorrência aberta ou cadastro inativo.
     * Termo pendente de assinatura é transitório: impede novas entradas, mas não tira a posição já existente.
     */
    public boolean removesFromWaitlist() {
        return hasOverdueLoan || hasOpenOccurrence || inactive;
    }
}
