package dev.xethh.txOutboxExample.txOutbox.queue;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.TextMessage;
import javax.transaction.Transactional;


@Component
public class JmsListeners {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @JmsListener(destination = Queues.QUEUE_NAME)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void handle(Message message) {
        if (message instanceof TextMessage) {
            Try.success(1)
                    .mapTry(it -> {
                        System.out.println(String.format("Messsage: %s", ((TextMessage) message).getText()));
                        return it;
                    })
                    .onFailure(throwable -> {
                        logger.error("Fail to extract message", throwable);
                    })
                    .toEither();
        } else {
            logger.info("Not supported message");
        }


    }
}
