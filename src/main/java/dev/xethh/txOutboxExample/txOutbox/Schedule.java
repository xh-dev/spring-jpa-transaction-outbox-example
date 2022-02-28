package dev.xethh.txOutboxExample.txOutbox;

import dev.xethh.txOutboxExample.txOutbox.entity.OutboxStatus;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
public class Schedule {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    OutboxRepo repo;

    @Autowired
    OutboxHandler outboxHandler;

    // @Scheduled(fixedDelay = 60000L)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void scheduleProcess() {
        logger.info("Start scheduled outbox handler");
        repo.findByStatus(OutboxStatus.INIT)
                .parallel()
                .forEach(it -> processEvent(it.getUuid()));
        logger.info("complete scheduled outbox handler");
    }

    public static class OutboxMessageEvent extends ApplicationEvent {
        public OutboxMessageEvent(Object source, UUID uuid) {
            super(source);
            this.uuid = uuid;
        }

        private UUID uuid;

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
    }

    @Async
    @EventListener
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void outboxEvent(OutboxMessageEvent event) {
        logger.info("Receive outbox event: " + event.getUuid());
        processEvent(event.getUuid());
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void processEvent(UUID uuid){
        Try.of(() -> repo.findByUuidAndStatus(uuid, OutboxStatus.INIT))
                .onFailure(throwable -> {
                    logger.error("event not exists: ", throwable);
                })
                .map(it -> {
                    logger.info("Event message existing in db");
                    outboxHandler.handle(it);
                    return it;
                });
    }

}
