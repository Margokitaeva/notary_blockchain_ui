package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.api.dto.SubmitActRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ReplicaDraftBuffer {

    private static final Logger log = LoggerFactory.getLogger(ReplicaDraftBuffer.class);

    private final JdbcTemplate jdbc;
    private final ObjectMapper om;

    public ReplicaDraftBuffer(JdbcTemplate jdbc, ObjectMapper om) {
        this.jdbc = jdbc;
        this.om = om;
    }

    public void buffer(String clientKey, SubmitActRequest request) {
        try {
            String json = om.writeValueAsString(request);
            jdbc.update(
                    """
                    INSERT INTO replica_draft_buffer(client_key, payload, created_at)
                    VALUES(?, ?, ?)
                    ON CONFLICT(client_key) DO NOTHING
                    """,
                    clientKey,
                    json,
                    Instant.now().toString()
            );
        } catch (Exception e) {
            log.warn("Failed to buffer draft {}: {}", clientKey, e.getMessage());
        }
    }

    public List<BufferedDraft> all() {
        return jdbc.query(
                "SELECT client_key, payload FROM replica_draft_buffer",
                (rs, i) -> toDraft(rs.getString("client_key"), rs.getString("payload"))
        );
    }

    public void remove(String clientKey) {
        jdbc.update("DELETE FROM replica_draft_buffer WHERE client_key = ?", clientKey);
    }

    private BufferedDraft toDraft(String clientKey, String payload) {
        try {
            SubmitActRequest req = om.readValue(payload, SubmitActRequest.class);
            return new BufferedDraft(clientKey, req);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize buffered draft", e);
        }
    }

    public record BufferedDraft(String clientKey, SubmitActRequest request) {}
}
