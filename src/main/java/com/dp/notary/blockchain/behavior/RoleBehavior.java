package com.dp.notary.blockchain.behavior;

import com.dp.notary.blockchain.blockchain.model.TransactionEntity;

public interface RoleBehavior {
    void deleteTransaction(String txId);//onDelete
    void approveTransaction(String txId);//onApprove
    void declineTransaction(String txId); // onDecline
    void resubmit(String txId); //onResubmit
    void addDraft(TransactionEntity tx, String mode); // onSaveDraft
    boolean onSubmitDraft(TransactionEntity tx,String mode); // onSubmit
}
