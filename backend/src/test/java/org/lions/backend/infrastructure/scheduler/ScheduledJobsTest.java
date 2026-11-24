package org.lions.backend.infrastructure.scheduler;

import org.junit.jupiter.api.Test;
import org.lions.backend.application.service.loan.LoanOverdueService;
import org.lions.backend.application.service.maintenance.PreventiveMaintenanceService;
import org.lions.backend.application.service.notification.NotificationService;
import org.lions.backend.application.service.waitlist.WaitlistService;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.support.CronExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduledJobsTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(LoanOverdueService.class, () -> mock(LoanOverdueService.class))
            .withBean(WaitlistService.class, () -> mock(WaitlistService.class))
            .withBean(NotificationService.class, () -> mock(NotificationService.class))
            .withBean(PreventiveMaintenanceService.class, () -> mock(PreventiveMaintenanceService.class))
            .withUserConfiguration(OverdueLoanJob.class, ReservationExpirationJob.class, NotificationRetryJob.class,
                    PreventiveMaintenanceJob.class);

    @Test
    void deveRegistrarJobsQuandoAgendamentoHabilitado() {
        contextRunner.withPropertyValues("lions.scheduling.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(OverdueLoanJob.class);
            assertThat(context).hasSingleBean(ReservationExpirationJob.class);
            assertThat(context).hasSingleBean(NotificationRetryJob.class);
            assertThat(context).hasSingleBean(PreventiveMaintenanceJob.class);
        });
    }

    @Test
    void deveRegistrarJobsQuandoPropriedadeAusente() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(OverdueLoanJob.class));
    }

    @Test
    void naoDeveRegistrarJobsQuandoAgendamentoDesabilitado() {
        contextRunner.withPropertyValues("lions.scheduling.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(OverdueLoanJob.class);
            assertThat(context).doesNotHaveBean(ReservationExpirationJob.class);
            assertThat(context).doesNotHaveBean(NotificationRetryJob.class);
            assertThat(context).doesNotHaveBean(PreventiveMaintenanceJob.class);
        });
    }

    @Test
    void deveTerCronValidoQuandoUsaPadrao() {
        for (String cron : new String[]{OverdueLoanJob.DEFAULT_CRON, ReservationExpirationJob.DEFAULT_CRON,
                NotificationRetryJob.DEFAULT_CRON, PreventiveMaintenanceJob.DEFAULT_CRON}) {
            assertThat(CronExpression.isValidExpression(cron)).as(cron).isTrue();
        }
    }

    @Test
    void deveDelegarParaOsServicosQuandoExecutado() {
        LoanOverdueService overdue = mock(LoanOverdueService.class);
        when(overdue.processOverdueLoans()).thenReturn(new LoanOverdueService.Result(2, 1));
        WaitlistService waitlist = mock(WaitlistService.class);
        NotificationService notifications = mock(NotificationService.class);
        PreventiveMaintenanceService preventive = mock(PreventiveMaintenanceService.class);

        new OverdueLoanJob(overdue).run();
        new ReservationExpirationJob(waitlist).run();
        new NotificationRetryJob(notifications).run();
        new PreventiveMaintenanceJob(preventive).run();

        verify(overdue).processOverdueLoans();
        verify(waitlist).expireReservations();
        verify(notifications).retryFailed();
        verify(preventive).notifyDue();
    }

    @Test
    void naoDevePropagarExcecaoQuandoServicoFalha() {
        LoanOverdueService overdue = mock(LoanOverdueService.class);
        when(overdue.processOverdueLoans()).thenThrow(new IllegalStateException("banco fora do ar"));
        WaitlistService waitlist = mock(WaitlistService.class);
        when(waitlist.expireReservations()).thenThrow(new IllegalStateException("falha"));

        assertThatCode(() -> new OverdueLoanJob(overdue).run()).doesNotThrowAnyException();
        assertThatCode(() -> new ReservationExpirationJob(waitlist).run()).doesNotThrowAnyException();
    }
}
