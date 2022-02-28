package dev.xethh.txOutboxExample.txOutbox;

import dev.xethh.txOutboxExample.txOutbox.entity.Outbox;
import dev.xethh.txOutboxExample.txOutbox.entity.OutboxType;
import dev.xethh.txOutboxExample.txOutbox.helper.OutboxHelper;
import dev.xethh.txOutboxExample.txOutbox.helper.OutboxMessage;
import dev.xethh.txOutboxExample.txOutbox.queue.Queues;
import io.vavr.control.Try;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("api/v1/outbox")
public class OutboxController {
    @Autowired
    OutboxRepo outboxRepo;

    @Autowired
    OutboxHelper outboxHelper;

    @Data
    public static class TestData {
        private String message;
    }
    // @PostConstruct
    // public void init(){
    //     OutboxMessage<TestData> message = OutboxMessage.<TestData>builder()
    //             .headers(Optional.of(new HashMap<String, String>()).map(map -> {
    //                 map.put("a", "b");
    //                 map.put("b", "c");
    //                 map.put("c", "d");
    //                 return map;
    //             }).get())
    //             .body(TestData.builder().message("hi").build())
    //             .build();
    //
    //     System.out.println(message);
    //
    // }

    @GetMapping
    public List<Outbox> get() {
        return outboxRepo.findAll();
    }

    @PostMapping
    @Transactional
    public Outbox post(@RequestBody final TestData testData) {
        return Try.of(() -> testData)
                .map(testData1 -> {
                    OutboxMessage<TestData> msg = OutboxMessage.<TestData>builder()
                            .headers(new HashMap<>())
                            .target(Queues.QUEUE_NAME)
                            .type(OutboxType.Queue)
                            .body(testData1)
                            .build();
                    return outboxHelper.deliver(msg).get();
                })
                .get();
    }
}
