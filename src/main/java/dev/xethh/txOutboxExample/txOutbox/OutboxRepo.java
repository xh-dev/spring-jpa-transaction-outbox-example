package dev.xethh.txOutboxExample.txOutbox;

import dev.xethh.txOutboxExample.txOutbox.entity.Outbox;
import dev.xethh.txOutboxExample.txOutbox.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.stream.Stream;

@Transactional(Transactional.TxType.MANDATORY)
public interface OutboxRepo extends JpaRepository<Outbox, UUID> {
    Stream<Outbox> findByStatus(OutboxStatus status);
    Outbox findByUuidAndStatus(UUID uuid, OutboxStatus outboxStatus);
}
