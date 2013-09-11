package fi.evident.dalesbred.plugin.idea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.Key;
import com.intellij.patterns.PsiExpressionPattern;
import com.intellij.patterns.PsiMethodPattern;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.patterns.PsiJavaPatterns.*;

public class DalesbredAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // TODO: handle key and value of findMap
        PsiMethodPattern method = psiMethod().definedInClass("fi.evident.dalesbred.Database").withName(string().oneOf("findUnique", "findAll", "findUniqueOrNull"));

        Key<PsiExpression> exp = Key.create("classObject");
        PsiExpressionPattern.Capture<PsiExpression> firstParameter =
                psiExpression().methodCallParameter(0, method).save(exp);

        ProcessingContext ctx = new ProcessingContext();
        if (firstParameter.accepts(element, ctx)) {
            PsiClass cl = resolveType(ctx.get(exp));
            if (cl != null && cl.isInterface()) {
                // TODO: allow interfaces of known (and registered) types
                holder.createErrorAnnotation(element, "Class may not refer to an interface.");
            }
        }
    }

    @Nullable
    private static PsiClass resolveType(@NotNull PsiExpression value) {
        if (value instanceof PsiClassObjectAccessExpression) {
            PsiType type = ((PsiClassObjectAccessExpression) value).getOperand().getType();
            if (type instanceof PsiClassType)
                return ((PsiClassType) type).resolve();
        }

        return null;
    }
}
