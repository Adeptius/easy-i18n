package de.marhali.easyi18n.unitalk.utils;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldingModel;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ForceFolding {

    private static final Set<String> processedFiles = ConcurrentHashMap.newKeySet();
    private static final Alarm cleanupAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD);

    public static void forceFold(List<FoldingDescriptor> descriptors, Project project, PsiFile containingFile) {
        // Примусово фолдимо тільки якщо файл ще не був оброблений
        if (!descriptors.isEmpty() && shouldAlwaysFold(project) && isFirstTimeProcessing(containingFile)) {
            ApplicationManager.getApplication().invokeLater(() -> {
                forceFoldRegions(project, containingFile, descriptors);
            });
        }
    }


    private static boolean isFirstTimeProcessing(@NotNull PsiFile file) {
        if (file.getVirtualFile() == null) return false;

        String fileKey = file.getProject().getName() + ":" + file.getVirtualFile().getPath();

        // Якщо файл вже був оброблений, повертаємо false
        if (processedFiles.contains(fileKey)) {
            return false;
        }

        // Додаємо файл до оброблених
        processedFiles.add(fileKey);

        // Видаляємо файл з кешу через 30 секунд
        cleanupAlarm.addRequest(() -> processedFiles.remove(fileKey), 30000);

        return true;
    }


    private static boolean shouldAlwaysFold(@NotNull Project project) {
        // Тут можна додати перевірку налаштувань проекту
        // Поки що повертаємо true для завжди фолдинга
        return true;
    }

    private static void forceFoldRegions(@NotNull Project project, @NotNull PsiFile file, @NotNull List<FoldingDescriptor> descriptors) {
        if (file.getVirtualFile() == null) return;

        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        FileEditor[] fileEditors = editorManager.getAllEditors(file.getVirtualFile());

        for (FileEditor fileEditor : fileEditors) {
            if (fileEditor instanceof TextEditor textEditor) {
                Editor editor = textEditor.getEditor();
                FoldingModel foldingModel = editor.getFoldingModel();

                foldingModel.runBatchFoldingOperation(() -> {
                    for (FoldingDescriptor descriptor : descriptors) {
                        var foldRegion = foldingModel.getFoldRegion(
                            descriptor.getRange().getStartOffset(),
                            descriptor.getRange().getEndOffset()
                        );

                        if (foldRegion != null && foldRegion.isExpanded()) {
                            foldRegion.setExpanded(false);
                        }
                    }
                });
            }
        }
    }

}
