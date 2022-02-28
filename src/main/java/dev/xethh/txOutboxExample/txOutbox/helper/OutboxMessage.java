package dev.xethh.txOutboxExample.txOutbox.helper;

import dev.xethh.txOutboxExample.txOutbox.entity.OutboxType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OutboxMessage<T> {

    private OutboxType type;
    private String target;
    private Map<String, String> headers;
    private T body;

}
