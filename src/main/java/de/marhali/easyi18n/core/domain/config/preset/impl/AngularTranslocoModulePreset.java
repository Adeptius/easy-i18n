package de.marhali.easyi18n.core.domain.config.preset.impl;

import de.marhali.easyi18n.core.domain.config.FileCodec;
import de.marhali.easyi18n.core.domain.config.ProjectConfigModule;
import de.marhali.easyi18n.core.domain.config.preset.PresetProvider;
import de.marhali.easyi18n.core.domain.model.ModuleId;
import de.marhali.easyi18n.core.domain.rules.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Preset for Angular Transloco (jsverse/transloco).
 *
 * <p>Matches {@code translocoService.translate('key')} and {@code translocoService.selectTranslate('key')}
 * calls in TypeScript files. Translation files follow the standard Transloco layout:
 * {@code src/assets/i18n/{locale}.json}.
 *
 * @author marhali
 */
public class AngularTranslocoModulePreset implements PresetProvider<ProjectConfigModule> {

    @Override
    public @NotNull ProjectConfigModule applyPreset(@Nullable ProjectConfigModule previousState) {
        return ProjectConfigModule.builder()
            .id(previousState != null ? previousState.id() : new ModuleId("angular-transloco"))
            .pathTemplate("$PROJECT_DIR$/src/assets/i18n/{locale}.json")
            .fileCodec(FileCodec.JSON)
            .fileTemplate("[{fileKey}]")
            .keyTemplate("{fileKey:.}")
            .rootDirectory("$PROJECT_DIR$/src")
            .defaultKeyPrefixes()
            .editorFlavorTemplate("this.translocoService.translate(\"{i18nKey}\")")
            .editorRules()
            // translocoService.translate('key') — synchronous lookup
            .editorRule(new EditorRule(
                "transloco-translate",
                Set.of(EditorLanguage.TYPESCRIPT),
                TriggerKind.CALL_ARGUMENT,
                List.of(
                    EditorRuleConstraint.exact(RuleConstraintType.CALLABLE_NAME, "translate"),
                    EditorRuleConstraint.exact(RuleConstraintType.ARGUMENT_INDEX, "0")
                ),
                10,
                false
            ))
            // translocoService.selectTranslate('key') — reactive Observable lookup
            .editorRule(new EditorRule(
                "transloco-select-translate",
                Set.of(EditorLanguage.TYPESCRIPT),
                TriggerKind.CALL_ARGUMENT,
                List.of(
                    EditorRuleConstraint.exact(RuleConstraintType.CALLABLE_NAME, "selectTranslate"),
                    EditorRuleConstraint.exact(RuleConstraintType.ARGUMENT_INDEX, "0")
                ),
                0,
                false
            ))
            // <element transloco="key"> — attribute directive in Angular templates
            .editorRule(new EditorRule(
                "transloco-attribute",
                Set.of(EditorLanguage.HTML),
                TriggerKind.PROPERTY_VALUE,
                List.of(
                    EditorRuleConstraint.exact(RuleConstraintType.PROPERTY_NAME, "transloco")
                ),
                0,
                false
            ))
            .build();
    }
}
