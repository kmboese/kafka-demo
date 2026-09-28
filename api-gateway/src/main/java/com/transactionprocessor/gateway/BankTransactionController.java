package com.transactionprocessor.gateway;

import com.transactionprocessor.model.BankTransaction;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Routes inbound {@link BankTransaction} requests to the bank-transactions Kafka topic.
 */
@RestController
@RequestMapping("/api/bank-transactions")
public class BankTransactionController {

    private static final Logger log = LoggerFactory.getLogger(BankTransactionController.class);

    private final KafkaTemplate<String, BankTransaction> kafkaTemplate;
    private final String topic;

    public BankTransactionController(KafkaTemplate<String, BankTransaction> kafkaTemplate,
                                     @Value("${app.kafka.topics.bank-transactions.name}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PublishedEvent publish(@Valid @RequestBody BankTransaction transaction)
            throws ExecutionException, InterruptedException, TimeoutException {
        if (transaction.getEventTime() == null) {
            transaction.setEventTime(Instant.now());
        }

        // Keying by source account keeps each account's transactions in order on one partition.
        String key = transaction.getFromAccount().naturalKey();
        SendResult<String, BankTransaction> result = kafkaTemplate.send(topic, key, transaction)
                .get(10, TimeUnit.SECONDS);

        var metadata = result.getRecordMetadata();
        log.info("Published {} to {}-{}@{}", transaction, metadata.topic(), metadata.partition(), metadata.offset());
        return new PublishedEvent(metadata.topic(), metadata.partition(), metadata.offset(), key,
                transaction.getEventTime());
    }

    public record PublishedEvent(String topic, int partition, long offset, String key, Instant eventTime) {
    }
}
