package com.dp.notary.blockchain.owner;

import com.dp.notary.blockchain.blockchain.model.OwnerEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public void submitTransaction(TransactionEntity tx) {
        validateParticipants(tx);
        validateAmount(tx.getAmount());

        OwnerEntity initiator = ownerRepository.findById(tx.getInitiator());

        initiator.lockShares(tx.getAmount());
        ownerRepository.updateBalances(initiator);
    }

    public void approveTransaction(TransactionEntity tx) {
        validateParticipants(tx);
        validateAmount(tx.getAmount());

        OwnerEntity initiator = ownerRepository.findById(tx.getInitiator());
        OwnerEntity target = ownerRepository.findById(tx.getTarget());

        initiator.consumeLockedShares(tx.getAmount());
        target.addShares(tx.getAmount());

        ownerRepository.updateBalances(initiator);
        ownerRepository.updateBalances(target);
    }

    public void rejectTransaction(TransactionEntity tx) {
        validateParticipants(tx);
        validateAmount(tx.getAmount());

        OwnerEntity initiator = ownerRepository.findById(tx.getInitiator());

        initiator.unlockShares(tx.getAmount());
        ownerRepository.updateBalances(initiator);
    }

    public List<OwnerEntity> getOwnersShares() {
        return ownerRepository.findAll();
    }

    public List<OwnerEntity> getOwnersShares(String filter) {
        return ownerRepository.findAll(filter);
    }

    public List<String> getOwnersNames() {
        return ownerRepository.findAllOwnerIds();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");
    }

    private void validateParticipants(TransactionEntity tx) {
        switch (tx.getType()) {
            case GRANT:
                if (!tx.getInitiator().equals("Company"))
                    throw new IllegalArgumentException("In GRANT transaction initiator must be Company");
                if (tx.getTarget().equals("Company"))
                    throw new IllegalArgumentException("Company can't be target in GRANT transaction");
                break;

            case TRANSFER:
                if (tx.getInitiator().equals("Company") || tx.getTarget().equals("Company"))
                    throw new IllegalArgumentException("Company can't take part in TRANSFER transaction");
                if (tx.getInitiator().equals(tx.getTarget()))
                    throw new IllegalArgumentException("Can't transfer shares to self");
                break;

            case SELL:
                if (!tx.getTarget().equals("Company"))
                    throw new IllegalArgumentException("In SELL transaction target must be Company");
                if (tx.getInitiator().equals("Company"))
                    throw new IllegalArgumentException("Company can't be initiator in SELL transaction");
                break;
        }
    }


}
