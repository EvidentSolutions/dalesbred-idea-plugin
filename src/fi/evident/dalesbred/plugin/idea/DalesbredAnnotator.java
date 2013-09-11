package fi.evident.dalesbred.plugin.idea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.patterns.PsiExpressionPattern;
import com.intellij.patterns.PsiMethodPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PsiJavaPatterns.psiExpression;
import static com.intellij.patterns.PsiJavaPatterns.psiMethod;

public class DalesbredAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PsiMethodPattern method = psiMethod().definedInClass("fi.evident.dalesbred.Database").withName("findAll");

        PsiExpressionPattern.Capture<PsiExpression> firstParameter = psiExpression().methodCallParameter(0, method);

        if (firstParameter.accepts(element)) {
            holder.createErrorAnnotation(element, "foo");
        }
    }
}
