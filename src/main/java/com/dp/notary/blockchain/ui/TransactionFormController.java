package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.App;
import com.dp.notary.blockchain.api.client.LeaderClient;
import com.dp.notary.blockchain.api.client.ReplicaClient;
import com.dp.notary.blockchain.auth.AuthService;
import com.dp.notary.blockchain.auth.Role;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class TransactionFormController {

    private final LeaderClient leaderClient;
    private final ReplicaClient replicaClient;
    // ===== UI =====
    @FXML
    private Label formTitle;
    @FXML
    private Label formSubtitle;

    @FXML
    private Label createdByLabel;

    @FXML
    private ComboBox<TransactionType> typeCombo;
    @FXML
    private TextField initiatorField;
    @FXML
    private TextField targetField;
    @FXML
    private TextField amountField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button cancelBtn;
    @FXML
    private Button draftBtn;
    @FXML
    private Button submitBtn;
    private final AuthService authService;
    private final BlockchainService blockchainService;

    private BigDecimal parsedAmount;

    // ===== MODE =====
    private FormMode mode = FormMode.CREATE;

    // ===== EXISTING TRANSACTION (for EDIT) =====
    // Здесь мы храним то, что пришло из списка (ID, старый timestamp и т.д.)
    // ВАЖНО: при сохранении мы делаем новый timestamp = now()
    private TransactionFormVM existing; // можешь заменить на свой доменный Transaction

//    public TransactionFormController() {
////         ОБЯЗАТЕЛЬНО
//    }

    // ===== CALLBACKS =====
    private Actions actions = new Actions() {
        @Override
        public void onCancel() {
            System.out.println("Cancel");
        }

        @Override
        public void onSaveDraft() {
            System.out.println("Save draft");
        }

        @Override
        public void onSubmit(boolean approveImmediately) {
            System.out.println("Submit approve");
        }
    };

    public TransactionFormController(AuthService authService, BlockchainService blockchainService, LeaderClient leaderClient, ReplicaClient replicaClient) {
        this.authService = authService;
        this.blockchainService = blockchainService;
        this.leaderClient = leaderClient;
        this.replicaClient = replicaClient;
    }

    @FXML
    private void initialize() {
        setupTypeCombo();
        setupAmountField();
        clearError();
        applyModeUI();
    }

    // ================= PUBLIC API =================

    public void setMode(FormMode mode) {
        this.mode = Objects.requireNonNull(mode);
        applyModeUI();
    }

    public void setActions(Actions actions) {
        this.actions = (actions != null) ? actions : this.actions;
    }

    /**
     * Вызывается ТОЛЬКО для EDIT.
     * Мы сохраняем ссылку на existing, чтобы:
     * - знать ID при обновлении
     * - знать original createdBy (если надо показывать)
     * Но timestamp в UI не показываем и при сохранении ставим новый.
     */
    public void setTransaction(TransactionFormVM tx) {
        this.existing = tx;

        // Заполняем редактируемые поля:
        typeCombo.setValue(tx.type());
        initiatorField.setText(n2e(tx.initiator()));
        targetField.setText(n2e(tx.target()));
        amountField.setText(String.valueOf(tx.amount()));

        applyModeUI();
    }

    // ================= ACTIONS =================

    @FXML
    private void onCancel() {
        clearError();
        actions.onCancel();
        // НИЧЕГО не сохраняем → значит timestamp/данные останутся старые
    }

    @FXML
    private void onSaveDraft() {
        clearError();
        if (buildAndValidatePayload()) {
            TransactionEntity tx = new TransactionEntity(
                    "хуй",
                    Instant.now(),
                    typeCombo.getValue(),
                    authService.getNameFromToken(App.get().getToken()),
                    TransactionStatus.DRAFT,
                    parsedAmount,
                    targetField.getText(),
                    initiatorField.getText()
            );

            if (Objects.equals(App.get().getAppRole(), "LEADER")) {
                blockchainService.addDraft(tx);
                leaderClient.broadcastAddDraft(tx);
            } else {
                replicaClient.addDraft(tx);
            }
            actions.onSaveDraft();
        }

    }

    @FXML
    private void onSubmit() {
        clearError();
        if (buildAndValidatePayload()) {
            TransactionEntity tx = new TransactionEntity(
                    "хуй",
                    Instant.now(),
                    typeCombo.getValue(),
                    authService.getNameFromToken(App.get().getToken()),
                    TransactionStatus.DRAFT,
                    parsedAmount,
                    targetField.getText(),
                    initiatorField.getText()
            );
            String txId = blockchainService.addDraft(tx);
            blockchainService.submitTransaction(txId);

            boolean approveImmediately = false;

            if (Objects.equals(App.get().getAppRole(), "LEADER")) {
                if (authService.getRoleFromToken(App.get().getToken()) == Role.LEADER) {
                    approveImmediately = true;
                    blockchainService.approve(txId);
                    leaderClient.broadcastSubmit(txId);
                    leaderClient.broadcastApprove(txId);
                }
            } else {
                replicaClient.submit(txId);
            }


            // create draft -> change status submit -> approve
            // replica: draft -> change status sbumit

            actions.onSubmit(approveImmediately);
        }
    }

    // ================= PAYLOAD + VALIDATION =================

    /**
     * Главное изменение:
     * - timestamp ставим АВТОМАТИЧЕСКИ при любом сохранении (draft/submit):
     * LocalDateTime.now()
     * <p>
     * Для EDIT:
     * - ID берём из existing (чтобы backend понял, что обновляем)
     * Для CREATE:
     * - ID = null (backend создаст новый)
     */
    private boolean buildAndValidatePayload() {
        TransactionType type = typeCombo.getValue();
        String initiator = trimToNull(initiatorField.getText());
        String target = trimToNull(targetField.getText());
        String amountRaw = trimToNull(amountField.getText());

        if (type == null) {
            error("Type is required.");
            return false;
        }
        if (initiator == null) {
            error("Initiator is required.");
            return false;
        }
        if (target == null) {
            error("Target is required.");
            return false;
        }
        if (amountRaw == null) {
            error("Amount is required.");
            return false;
        }

        BigDecimal amount = new BigDecimal(-1);

        try {
            parsedAmount = new BigDecimal(amountRaw);
        } catch (NumberFormatException e) {
            error("Amount must be a number.");
            return false;
        }
        if (parsedAmount.compareTo(new BigDecimal(0)) < 0) {
            error("Amount must be >= 0.");
            return false;
        }
        return true;
        // createdBy:
        // - при CREATE → currentUser (тот, кто создал)
        // - при EDIT → оставляем original createdBy (кто создал изначально), если он есть
    }

    private void error(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    // ================= UI MODE =================

    private void applyModeUI() {
        if (formTitle == null) return;

        boolean isEdit = (mode == FormMode.EDIT);

        formTitle.setText(isEdit ? "Edit Transaction" : "New Transaction");
        formSubtitle.setText(isEdit
                ? "Change fields and submit / save as draft. Timestamp will be updated on save."
                : "Fill in fields and submit or save as draft.");

        // createdBy показываем:
        // - EDIT: original creator (если есть)
        // - CREATE: current user

        createdByLabel.setText(authService.getNameFromToken(App.get().getToken()));

        // submit button text by role
        submitBtn.setText(authService.getRoleFromToken(App.get().getToken()) == Role.LEADER ? "Submit and approve" : "Submit");
    }

    private void setupTypeCombo() {
        typeCombo.getItems().setAll(
                TransactionType.PURCHASE,
                TransactionType.SELL,
                TransactionType.GRANT,
                TransactionType.DIVIDEND
        );

        typeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TransactionType t) {
                return t == null ? "" : t.name();
            }

            @Override
            public TransactionType fromString(String s) {
                return TransactionType.valueOf(s);
            }
        });
    }

    private void setupAmountField() {
        amountField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;

            String normalized = newV.replace(',', '.');

            if (!normalized.matches("\\d*(\\.\\d*)?")) {
                amountField.setText(oldV);
            } else if (!normalized.equals(newV)) {
                amountField.setText(normalized);
            }
        });
    }

    // ================= TYPES =================

    public enum FormMode {CREATE, EDIT}

    /**
     * Это то, что приходит из списка (existingData).
     * Ты можешь заменить на свой реальный класс Transaction.
     */
    public record TransactionFormVM(
            String id,
            Instant timestamp,
            TransactionType type,
            String createdBy,
            BigDecimal amount,
            String target,
            String initiator
    ) {
    }

    public interface Actions {
        void onCancel();

        void onSaveDraft();

        void onSubmit(boolean approveImmediately);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String n2e(String s) {
        return s == null ? "" : s;
    }
}
