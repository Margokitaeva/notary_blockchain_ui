package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.config.NotaryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LeaderAvailabilityNotifier {

    private static final Logger log = LoggerFactory.getLogger(LeaderAvailabilityNotifier.class);

    private final ReplicaNotifier notifier;
    private final NotaryProperties props;

    public LeaderAvailabilityNotifier(ReplicaNotifier notifier, NotaryProperties props) {
        this.notifier = notifier;
        this.props = props;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void notifyReplicasOnStartup() {
        if (isLeader()) {
            log.info("Leader is ready, notifying replicas");
            notifier.notifyReplicas();
        }
    }

    private boolean isLeader() {
        return !"REPLICA".equalsIgnoreCase(props.role());
    }
}
