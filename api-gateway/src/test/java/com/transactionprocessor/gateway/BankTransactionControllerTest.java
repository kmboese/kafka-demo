package com.transactionprocessor.gateway;

import com.transactionprocessor.model.BankTransaction;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankTransactionController.class)
@TestPropertySource(properties = "app.kafka.topics.bank-transactions.name=bank-transactions")
class BankTransactionControllerTest {

    private static final String VALID_BODY = """
            {
              "amount": 125.50,
              "currency": "USD",
              "fromAccount": {"accountName": "Checking", "ownerName": "Alice", "institutionName": "First Bank"},
              "toAccount":   {"accountName": "Savings",  "ownerName": "Bob",   "institutionName": "Second Bank"}
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KafkaTemplate<String, BankTransaction> kafkaTemplate;

    @Test
    void publishesValidTransactionKeyedBySourceAccount() throws Exception {
        String key = "First Bank|Alice|Checking";
        var metadata = new RecordMetadata(new TopicPartition("bank-transactions", 1), 42, 0, 0L, 0, 0);
        when(kafkaTemplate.send(eq("bank-transactions"), eq(key), any(BankTransaction.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        new SendResult<>(new ProducerRecord<>("bank-transactions", key, null), metadata)));

        mockMvc.perform(post("/api/bank-transactions").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.partition").value(1))
                .andExpect(jsonPath("$.offset").value(42))
                .andExpect(jsonPath("$.key").value(key))
                .andExpect(jsonPath("$.eventTime").exists());
    }

    @Test
    void rejectsTransactionWithoutAccounts() throws Exception {
        mockMvc.perform(post("/api/bank-transactions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 10, \"currency\": \"USD\"}"))
                .andExpect(status().isBadRequest());

        verify(kafkaTemplate, never()).send(any(String.class), any(String.class), any(BankTransaction.class));
    }
}
