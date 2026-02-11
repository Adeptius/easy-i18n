package de.marhali.easyi18n.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;

import de.marhali.easyi18n.InstanceManager;
import de.marhali.easyi18n.model.KeyPath;
import de.marhali.easyi18n.model.Translation;
import de.marhali.easyi18n.model.TranslationValue;
import de.marhali.easyi18n.model.action.TranslationUpdate;
import de.marhali.easyi18n.settings.ProjectSettings;
import de.marhali.easyi18n.settings.ProjectSettingsService;
import de.marhali.easyi18n.util.KeyPathConverter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Base for add and edit translation dialogs.
 * @author marhali
 */
abstract class TranslationDialog extends DialogWrapper {

    protected static final ResourceBundle bundle = ResourceBundle.getBundle("messages");

    protected final @NotNull Project project;
    protected final @NotNull ProjectSettings settings;
    protected final @NotNull KeyPathConverter converter;
    protected final @NotNull Translation origin;

    protected final JTextField keyField;
    protected final Map<String, JTextArea> localeValueFields;

    private final Set<Consumer<TranslationUpdate>> callbacks;

    /**
     * Constructs a new translation dialog.
     * @param project Opened project
     * @param origin Prefill translation
     */
    protected TranslationDialog(@NotNull Project project, @NotNull Translation origin) {
        super(project);

        this.project = project;
        this.settings = ProjectSettingsService.get(project).getState();
        this.converter = new KeyPathConverter(settings);
        this.origin = origin;

        this.callbacks = new HashSet<>();

        // Fields
        TranslationValue value = origin.getValue();

        this.keyField = new JBTextField(converter.toString(origin.getKey()));
        this.localeValueFields = new LinkedHashMap<>();

        Set<String> locales = InstanceManager.get(project).store().getData().getLocales();

        List<String> sortedLocales = new ArrayList<>(locales);
        sortedLocales.sort(Comparator.comparingInt((String l) -> switch (l) {
            case "en" -> 0;
            case "uk" -> 1;
            case "ru" -> 2;
            default -> 3;
        }).thenComparing(Comparator.naturalOrder()));

        for (String locale : sortedLocales) {
            var field = new JBTextArea(value != null ? value.get(locale) : null, 1, 1);
            field.setLineWrap(true);
            field.setWrapStyleWord(true);
            field.setBorder(BorderFactory.createTitledBorder(locale));

            localeValueFields.put(locale, field);
        }
    }

    public JTextField getKeyField() {
        return keyField;
    }

    /**
     * Registers a callback that is called on dialog close with the final state.
     * If the user aborts the dialog no callback is called.
     * @param callback Callback to register
     */
    public void registerCallback(Consumer<TranslationUpdate> callback) {
        callbacks.add(callback);
    }

    /**
     * Implementation needs to handle exit
     * @param exitCode See {@link com.intellij.openapi.ui.DialogWrapper} for exit codes
     * @return update conclusion, null if aborted
     */
    protected abstract @Nullable TranslationUpdate handleExit(int exitCode);

    /**
     * Opens the translation modal and applies the appropriate logic on modal close.
     * Internally, the {@link #handleExit(int)} method will be called to determine finalization logic.
     */
    public void showAndHandle() {
        init();
        show();

        int exitCode = getExitCode();
        TranslationUpdate update = handleExit(exitCode);

        if(update != null) {
            InstanceManager.get(project).processUpdate(update);
            callbacks.forEach(callback -> callback.consume(update));
        }
    }

    /**
     * Retrieve current modal state.
     * @return Translation
     */
    protected @NotNull Translation getState() {
        KeyPath key = converter.fromString(keyField.getText());

        TranslationValue value = new TranslationValue();

        for(Map.Entry<String, JTextArea> entry : localeValueFields.entrySet()) {
            value.put(entry.getKey(), entry.getValue().getText());
        }

        return new Translation(key, value);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(bundle.getString("translation.key"), keyField, true)
                .addComponent(createLocalesPanel(), 12)
                .getPanel();

        panel.setMinimumSize(new Dimension(200, 150));
        panel.setPreferredSize(new Dimension(1000, 380));

        return panel;
    }

    private JComponent createLocalesPanel() {
        FormBuilder builder = FormBuilder.createFormBuilder();

        for(Map.Entry<String, JTextArea> localeEntry : localeValueFields.entrySet()) {
            builder.addComponent(localeEntry.getValue(), 6);
        }

        JScrollPane scrollPane = new JBScrollPane(builder.getPanel());

        scrollPane.setBorder(BorderFactory.createTitledBorder(
                new EtchedBorder(), bundle.getString("translation.locales")));

        return scrollPane;
    }
}