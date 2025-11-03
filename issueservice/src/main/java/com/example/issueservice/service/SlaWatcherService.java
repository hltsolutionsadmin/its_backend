package com.example.issueservice.service;

import com.example.issueservice.model.TicketHistoryModel;
import com.example.issueservice.model.TicketModel;
import com.example.issueservice.repository.TicketHistoryRepository;
import com.example.issueservice.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaWatcherService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository historyRepository;

    @Scheduled(fixedDelayString = "${sla.watcher.delay-ms:60000}")
    @Transactional
    public void checkSlaBreaches() {
        Instant now = Instant.now();
        List<TicketModel> overdue = ticketRepository.findOverdueSlaTickets(now);
        if (overdue.isEmpty()) {
            return;
        }
        log.info("SLA Watcher: found {} overdue ticket(s)", overdue.size());
        for (TicketModel t : overdue) {
            if (Boolean.TRUE.equals(t.getSlaBreached())) {
                continue;
            }
            t.setSlaBreached(true);
            if (t.getSlaBreachedAt() == null) {
                t.setSlaBreachedAt(now);
            }
            ticketRepository.save(t);

            TicketHistoryModel history = new TicketHistoryModel();
            history.setTicket(t);
            history.setChangedBy(0L); // system
            history.setFieldName("SLA");
            history.setOldValue("ON_TIME");
            history.setNewValue("BREACHED");
            history.setChangeDescription("SLA breached at " + now);
            historyRepository.save(history);
        }
    }
}
