package fi.evident.dalesbred.plugin.idea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class DalesbredAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement instanceof PsiLiteralExpression) {
            PsiLiteralExpression literal = (PsiLiteralExpression) psiElement;

            String value = (String) literal.getValue();
            if (value != null && value.startsWith("dalesbred:")) {
                annotationHolder.createErrorAnnotation(psiElement, "dalesbred!");
            }
        }
    }
}
