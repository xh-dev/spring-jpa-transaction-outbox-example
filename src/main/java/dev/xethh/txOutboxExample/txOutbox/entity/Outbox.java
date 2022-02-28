package dev.xethh.txOutboxExample.txOutbox.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@DynamicUpdate
public class Outbox {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid;

    @Version
    private Long version;
    private Date createDate;
    private Date updateDate;

    private OutboxType outboxType;

    private String target;
    private OutboxStatus status;


    @Lob
    @Column(columnDefinition="CLOB")
    private String headers;
    @Lob
    @Column(columnDefinition="CLOB")
    private String body;

    @PrePersist
    public void beforePersist(){
        if(createDate == null){
            createDate = new Date();
        }
    }

    @PreUpdate
    public void beforeUpdate(){
        updateDate = new Date();
    }



}
