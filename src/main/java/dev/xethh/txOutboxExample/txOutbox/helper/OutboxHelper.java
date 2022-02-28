package dev.xethh.txOutboxExample.txOutbox.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.xethh.txOutboxExample.txOutbox.OutboxHandler;
import dev.xethh.txOutboxExample.txOutbox.OutboxRepo;
import dev.xethh.txOutboxExample.txOutbox.Schedule;
import dev.xethh.txOutboxExample.txOutbox.entity.Outbox;
import dev.xethh.txOutboxExample.txOutbox.entity.OutboxStatus;
import dev.xethh.txOutboxExample.txOutbox.entity.OutboxType;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
public class OutboxHelper {
    @Autowired
    OutboxRepo repo;
    ObjectMapper om = new ObjectMapper();


    // @Autowired
    // PostCommitAdapter postCommitAdapter;

    @Autowired
    OutboxHandler outboxHandler;

    @Autowired
    ApplicationEventPublisher publisher;

    // private void sentPendingOut(UUID uuid) {
    //     Try.of(()->repo.findByUuidAndStatus(uuid, OutboxStatus.INIT))
    //             .map(outbox -> {
    //                 log.info(String.format("Outbox message %s exists", uuid.toString()));
    //                 outboxHandler.handle(outbox);
    //                 return outbox;
    //             });
    // }

    public <T> Try<Outbox> deliver(OutboxMessage<T> msg) {
        return Try.success(msg)
                .mapTry(it -> {
                    Outbox outbox = new Outbox();
                    outbox.setOutboxType(OutboxType.Queue);
                    outbox.setStatus(OutboxStatus.INIT);
                    String header = om.writeValueAsString(msg.getHeaders());
                    String body = om.writeValueAsString(msg.getBody());
                    outbox.setHeaders(header);
                    outbox.setBody(body);
                    outbox.setTarget(msg.getTarget());
                    return outbox;
                })
                .mapTry(it -> {
                    Outbox outbox = repo.save(it);
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public int getOrder() {
                            return TransactionSynchronization.super.getOrder();
                        }

                        @Override
                        public void afterCompletion(int status) {
                            TransactionSynchronization.super.afterCompletion(status);
                            if(status == TransactionSynchronization.STATUS_COMMITTED){
                                log.info("Transaction status: " + status);
                                log.info("start deliver outbox message: " + outbox.getUuid());
                                publisher.publishEvent(new Schedule.OutboxMessageEvent(this, outbox.getUuid()));
                            } else {
                                log.info(String.format("Message not committed[%d]", status));
                            }
                        }
                    });
                    return it;
                })
                ;
    }


}
