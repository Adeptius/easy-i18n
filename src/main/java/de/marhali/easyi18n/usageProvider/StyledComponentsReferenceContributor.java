package de.marhali.easyi18n.usageProvider;

import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class StyledComponentsReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        PsiElementPattern.Capture<PsiElement> pattern = PlatformPatterns.psiElement()
                .withLanguage(JavascriptLanguage.INSTANCE)
                .andNot(PlatformPatterns.psiElement().inside(JSLiteralExpression.class));
        PsiReferenceProvider provider = new PsiReferenceProvider() {
            @Override
            public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                PsiElement parent = element.getParent();
                if (parent instanceof JSProperty) {
                    JSProperty property = (JSProperty) parent;
                    String propertyName = property.getName();

                    // Отримуємо батьківський об'єкт JSObjectLiteralExpression
                    PsiElement grandParent = parent.getParent();
                    if (grandParent instanceof JSObjectLiteralExpression) {
                        // Шукаємо ім'я змінної, якій присвоюється об'єкт
                        PsiElement declaration = grandParent.getParent();
                        if (declaration instanceof JSVariable) {
                            String objectName = ((JSVariable) declaration).getName();

                            if (element.getContainingFile().getName().endsWith(".styled.ts") ||
                                element.getContainingFile().getName().endsWith(".styled.js")) {

                                return new PsiReference[]{
                                        new StyledComponentReference(element, propertyName, objectName)
                                };
                            }
                        }
                    }
                }
                return PsiReference.EMPTY_ARRAY;
            }
        };
        registrar.registerReferenceProvider(pattern, provider);
    }

}
