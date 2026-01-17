package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.App;
import com.dp.notary.blockchain.api.client.LeaderClient;
import com.dp.notary.blockchain.api.client.ReplicaClient;
import com.dp.notary.blockchain.auth.Role;
import com.dp.notary.blockchain.auth.SessionService;
import com.dp.notary.blockchain.behavior.RoleBehavior;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import com.dp.notary.blockchain.config.NotaryProperties;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Component
public class TransactionFormController {

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
    private final SessionService sessionService;
    private BigDecimal parsedAmount;
    private final RoleBehavior roleBehavior;
    private final NotaryProperties props;

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

    public TransactionFormController(BlockchainService blockchainService, NotaryProperties props, SessionService sessionService, LeaderClient leaderClient, ReplicaClient replicaClient, RoleBehavior roleBehavior) {
        this.sessionService = sessionService;
        this.roleBehavior = roleBehavior;
        this.props = props;
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
        if(mode == FormMode.CREATE) {
            existing = null;
        }
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
        if (!sessionService.isAuthenticated()){
            App.get().showLogin();
            return;
        }

        clearError();
        actions.onCancel();
    }

    @FXML
    private void onSaveDraft() {
        if (!sessionService.isAuthenticated()){
            App.get().showLogin();
            return;
        }

        clearError();
        if (buildAndValidatePayload()) {
            String txId = (existing!=null) ? existing.id() : UUID.randomUUID().toString();
            TransactionEntity tx = new TransactionEntity(
                    txId,
                    Instant.now(),
                    typeCombo.getValue(),
                    sessionService.getName(),
                    TransactionStatus.DRAFT,
                    parsedAmount,
                    targetField.getText(),
                    initiatorField.getText()
            );
            roleBehavior.addDraft(tx,mode.toString());
            actions.onSaveDraft();
        }

    }

    @FXML
    private void onSubmit() {
        if (!sessionService.isAuthenticated()){
            App.get().showLogin();
            return;
        }

        clearError();
        if (buildAndValidatePayload()) {
            String txId = (existing!=null) ? existing.id() : UUID.randomUUID().toString();
            TransactionEntity tx = new TransactionEntity(
                    txId,
                    Instant.now(),
                    typeCombo.getValue(),
                    sessionService.getName(),
                    TransactionStatus.DRAFT,
                    parsedAmount,
                    targetField.getText(),
                    initiatorField.getText()
            );


            boolean approveImmediately = roleBehavior.onSubmitDraft(tx,mode.toString(), sessionService.validateRole(Role.LEADER));

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

        createdByLabel.setText(sessionService.getName());

        // submit button text by role
        if (!sessionService.isAuthenticated()){
            App.get().showLogin();
            return;
        }
        submitBtn.setText(sessionService.validateRole(Role.LEADER)
                && props.role().equals("LEADER") ? "Submit and approve" : "Submit");
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
