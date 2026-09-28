package com.transactionprocessor.model;

import java.time.Instant;

/**
 * A generic Kafka event.
 */
public interface Event {

    Instant getEventTime();
}
