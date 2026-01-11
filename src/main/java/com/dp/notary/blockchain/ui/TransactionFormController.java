package com.dp.notary.blockchain.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.util.Objects;

public class TransactionFormController {

    // ===== UI =====
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;

    @FXML private Label createdByLabel;

    @FXML private ComboBox<TransactionType> typeCombo;
    @FXML private TextField initiatorField;
    @FXML private TextField targetField;
    @FXML private TextField amountField;

    @FXML private Label errorLabel;

    @FXML private Button cancelBtn;
    @FXML private Button draftBtn;
    @FXML private Button submitBtn;

    // ===== MODE / ROLE =====
    private FormMode mode = FormMode.CREATE;
    private MainController.Role role = MainController.Role.REPLICA;

    // ===== CURRENT USER (editor / creator for new tx) =====
    private String currentUser = "Unknown";

    // ===== EXISTING TRANSACTION (for EDIT) =====
    // Здесь мы храним то, что пришло из списка (ID, старый timestamp и т.д.)
    // ВАЖНО: при сохранении мы делаем новый timestamp = now()
    private TransactionFormVM existing; // можешь заменить на свой доменный Transaction

    // ===== CALLBACKS =====
    private Actions actions = new Actions() {
        @Override public void onCancel() { System.out.println("Cancel"); }
        @Override public void onSaveDraft(TransactionPayload data) { System.out.println("Save draft: " + data); }
        @Override public void onSubmit(TransactionPayload data, boolean approveImmediately) {
            System.out.println("Submit: " + data + " approve=" + approveImmediately);
        }
    };

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

    public void setRole(MainController.Role role) {
        this.role = Objects.requireNonNull(role);
        applyModeUI();
    }

    public void setCurrentUser(String fullName) {
        this.currentUser = (fullName == null || fullName.isBlank()) ? "Unknown" : fullName;
        // createdByLabel обновим в applyModeUI()
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
        TransactionPayload payload = buildAndValidatePayload();
        if (payload == null) return;
        actions.onSaveDraft(payload);
    }

    @FXML
    private void onSubmit() {
        clearError();
        TransactionPayload payload = buildAndValidatePayload();
        if (payload == null) return;

        boolean approveImmediately = (role == MainController.Role.LEADER);
        actions.onSubmit(payload, approveImmediately);
    }

    // ================= PAYLOAD + VALIDATION =================

    /**
     * Главное изменение:
     * - timestamp ставим АВТОМАТИЧЕСКИ при любом сохранении (draft/submit):
     *   LocalDateTime.now()
     *
     * Для EDIT:
     * - ID берём из existing (чтобы backend понял, что обновляем)
     * Для CREATE:
     * - ID = null (backend создаст новый)
     */
    private TransactionPayload buildAndValidatePayload() {
        TransactionType type = typeCombo.getValue();
        String initiator = trimToNull(initiatorField.getText());
        String target = trimToNull(targetField.getText());
        String amountRaw = trimToNull(amountField.getText());

        if (type == null) return error("Type is required.");
        if (initiator == null) return error("Initiator is required.");
        if (target == null) return error("Target is required.");
        if (amountRaw == null) return error("Amount is required.");

        int amount;
        try {
            amount = Integer.parseInt(amountRaw);
        } catch (NumberFormatException e) {
            return error("Amount must be an integer.");
        }
        if (amount < 0) return error("Amount must be >= 0.");

        // timestamp обновляется на сохранении
        LocalDateTime newTimestamp = LocalDateTime.now();

        // id: только для EDIT
        String id = (mode == FormMode.EDIT && existing != null) ? existing.id() : null;

        // createdBy:
        // - при CREATE → currentUser (тот, кто создал)
        // - при EDIT → оставляем original createdBy (кто создал изначально), если он есть
        String createdBy = (mode == FormMode.EDIT && existing != null && trimToNull(existing.createdBy()) != null)
                ? existing.createdBy()
                : currentUser;

        return new TransactionPayload(
                id,
                newTimestamp,
                type,
                createdBy,
                initiator,
                target,
                amount
        );
    }

    private TransactionPayload error(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        return null;
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
        String createdByToShow = (isEdit && existing != null && trimToNull(existing.createdBy()) != null)
                ? existing.createdBy()
                : currentUser;
        createdByLabel.setText(createdByToShow);

        // submit button text by role
        submitBtn.setText(role == MainController.Role.LEADER ? "Submit and approve" : "Submit");
    }

    private void setupTypeCombo() {
        typeCombo.getItems().setAll(
                TransactionType.PURCHASE,
                TransactionType.SELL,
                TransactionType.GRANT,
                TransactionType.DIVIDEND
        );

        typeCombo.setConverter(new StringConverter<>() {
            @Override public String toString(TransactionType t) { return t == null ? "" : t.name(); }
            @Override public TransactionType fromString(String s) { return TransactionType.valueOf(s); }
        });
    }

    private void setupAmountField() {
        amountField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            if (!newV.matches("\\d*")) {
                amountField.setText(newV.replaceAll("[^\\d]", ""));
            }
        });
    }

    // ================= TYPES =================

    public enum FormMode { CREATE, EDIT }

    public enum TransactionType {
        PURCHASE, SELL, GRANT, DIVIDEND
    }

    /**
     * То, что уходит наружу на save/submit.
     * timestamp всегда now() на момент сохранения.
     * id = null для CREATE, id != null для EDIT.
     */
    public record TransactionPayload(
            String id,
            LocalDateTime timestamp,
            TransactionType type,
            String createdBy,
            String initiator,
            String target,
            int amount
    ) {}

    /**
     * Это то, что приходит из списка (existingData).
     * Ты можешь заменить на свой реальный класс Transaction.
     */
    public record TransactionFormVM(
            String id,
            LocalDateTime timestamp,
            TransactionType type,
            String createdBy,
            String initiator,
            String target,
            int amount
    ) {}

    public interface Actions {
        void onCancel();
        void onSaveDraft(TransactionPayload data);
        void onSubmit(TransactionPayload data, boolean approveImmediately);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String n2e(String s) { return s == null ? "" : s; }
}
