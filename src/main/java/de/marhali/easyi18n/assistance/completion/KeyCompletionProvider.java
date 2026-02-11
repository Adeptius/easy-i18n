package de.marhali.easyi18n.assistance.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.ProcessingContext;

import de.marhali.easyi18n.InstanceManager;
import de.marhali.easyi18n.assistance.OptionalAssistance;
import de.marhali.easyi18n.model.KeyPath;
import de.marhali.easyi18n.model.Translation;
import de.marhali.easyi18n.model.TranslationData;
import de.marhali.easyi18n.settings.ProjectSettings;
import de.marhali.easyi18n.settings.ProjectSettingsService;
import de.marhali.easyi18n.util.KeyPathConverter;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides existing translation keys for code completion.
 * @author marhali
 */
class KeyCompletionProvider extends CompletionProvider<CompletionParameters> implements OptionalAssistance {

    private static final Icon icon = IconLoader.getIcon("/icons/translate13.svg", KeyCompletionProvider.class);

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        Project project = parameters.getOriginalFile().getProject();

        if (!isAssistance(project)) {
            return;
        }

        ProjectSettings settings = ProjectSettingsService.get(project).getState();
        TranslationData data = InstanceManager.get(project).store().getData();
        Set<KeyPath> fullKeys = data.getFullKeys();

        if (parameters.getEditor().toString().contains(".tsx")) {
            addTsxCompletions(result, data, fullKeys, settings);
            return;
        }

        for (KeyPath key : fullKeys) {
            result.addElement(constructLookup(new Translation(key, data.getTranslation(key)), settings));
        }
    }

    private LookupElement constructLookup(Translation translation, ProjectSettings settings) {
        KeyPathConverter converter = new KeyPathConverter(settings);

        return LookupElementBuilder
                .create(converter.toString(translation.getKey()))
                .withTailText(" " + translation.getValue().get(settings.getPreviewLocale()), true)
                .withIcon(icon);
    }


    private void addTsxCompletions(@NotNull CompletionResultSet originalResult, TranslationData data,
                                   Set<KeyPath> fullKeys, ProjectSettings settings) {
        String originalPrefix = originalResult.getPrefixMatcher().getPrefix();
        if (!originalPrefix.startsWith("ll")) {
            return;
        }

        String prefix = originalPrefix.substring(2);
        String actualPrefix = originalResult.getPrefixMatcher().getPrefix().substring(2);
        CompletionResultSet result = originalResult.withPrefixMatcher(new PlainPrefixMatcher(actualPrefix));

        fullKeys.stream().limit(100).forEach(key -> {
            LookupElement lookupElement = constructLookup(new Translation(key, data.getTranslation(key)), settings);
            String prompt = key.getFirst() + ":" + key.getLast();
            if (!prompt.contains(prefix)) {
                return;
            }

            lookupElement = LookupElementDecorator.withInsertHandler(lookupElement, (InsertHandler<LookupElement>) (context1, item) -> {
                Document document = context1.getDocument();
                int startOffset = context1.getStartOffset();
                if (document.getText(new TextRange(startOffset - 2, startOffset)).equals("ll")) {
                    document.deleteString(startOffset - 2, startOffset);
                }
                if (item instanceof LookupElementDecorator) {
                    ((LookupElementDecorator<?>) item).getDelegate().handleInsert(context1);
                }
            });
            result.addElement(lookupElement);
        });

    }
}
