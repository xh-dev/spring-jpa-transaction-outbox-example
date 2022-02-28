package dev.xethh.txOutboxExample.txOutbox;

import dev.xethh.txOutboxExample.txOutbox.entity.Outbox;
import dev.xethh.txOutboxExample.txOutbox.entity.OutboxStatus;
import dev.xethh.txOutboxExample.txOutbox.queue.Queues;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

@Configuration
@Slf4j
public class OutboxHandler {
    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    ApplicationEventPublisher publisher;

    public void handle(Outbox msg) {
        log.info(String.format("Handle outbox message[%s]", msg.getUuid().toString()));
        Try.success(msg)
                .map(it -> {
                    msg.setStatus(OutboxStatus.Processing);
                    jmsTemplate.send(Queues.QUEUE_NAME, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            TextMessage txtMsg = session.createTextMessage();
                            txtMsg.setText(msg.getBody());
                            return txtMsg;
                        }
                    });

                    return it;
                })
                .map(it -> {
                    log.info("Send message: "+it.getUuid());
                    return it;
                })
                .onFailure(throwable -> {
                    log.info("Fail on send message");
                    msg.setStatus(OutboxStatus.INIT);
                })
                .onSuccess(it -> {
                    log.info("Success sending message");
                    it.setStatus(OutboxStatus.DONE);
                })
                .map(it->{
                    log.info("The operation should be done");
                    return it;
                })
        ;

    }
}
