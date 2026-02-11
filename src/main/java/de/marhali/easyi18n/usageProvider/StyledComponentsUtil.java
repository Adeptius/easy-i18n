package de.marhali.easyi18n.usageProvider;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class StyledComponentsUtil {

    public static Collection<PsiElement> findComponentUsages(Project project, String componentName) {
        Collection<PsiElement> results = new ArrayList<>();

        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        // Шукаємо використання компонента в JSX/TSX файлах
        Processor<PsiFileSystemItem> processor = new Processor<>() {  // processor
            @Override
            public boolean process(PsiFileSystemItem fileItem) {
                if (fileItem instanceof PsiFile) {
                    PsiFile psiFile = (PsiFile) fileItem;
                    psiFile.accept(new PsiRecursiveElementWalkingVisitor() {
                        @Override
                        public void visitElement(@NotNull PsiElement element) {
                            if (isStyledComponentUsage(element, componentName)) {
                                results.add(element);
                            }
                            super.visitElement(element);
                        }
                    });
                }
                return true;
            }
        };

        FilenameIndex.processFilesByName(
                componentName + ".styled.*",  // name
                false,                        // directories
                processor,
                scope,                        // scope
                project,                      // project
                null                          // idFilter
        );

        return results;
    }

    private static boolean isStyledComponentUsage(PsiElement element, String componentName) {
        // Більш загальна перевірка без прив'язки до конкретного типу
        return element.getText().contains(componentName) &&
               (element.getContainingFile().getName().endsWith(".jsx") ||
                element.getContainingFile().getName().endsWith(".tsx"));
    }
}
