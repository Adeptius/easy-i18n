package de.marhali.easyi18n.usageProvider;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class StyledComponentReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private final String componentName;
    private final String objectName;

    public StyledComponentReference(@NotNull PsiElement element, String componentName, String objectName) {
        super(element);
        this.componentName = componentName;
        this.objectName = objectName;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Project project = myElement.getProject();
        Collection<PsiElement> results = new ArrayList<>();

        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        ProjectFileIndex.getInstance(project).iterateContent(fileOrDir -> {
            if (fileOrDir.isDirectory()) {
                return true;
            }
            String fileName = fileOrDir.getName();
            if (!fileName.endsWith(".tsx")) {
                return true;
            }
            PsiFile psiFile = PsiManager.getInstance(project).findFile(fileOrDir);
            if (psiFile == null) {
                return true;
            }
            psiFile.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof JSElement) {
                        String searchPattern = objectName + "." + componentName;
                        if (element.getText().contains(searchPattern)) {
                            results.add(element);
                        }
                    }
                    super.visitElement(element);
                }
            });
            return true;
        }, scope);

        return results.stream()
                .map(PsiElementResolveResult::new)
                .toArray(ResolveResult[]::new);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
}

