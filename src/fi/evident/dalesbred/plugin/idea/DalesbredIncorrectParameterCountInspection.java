package fi.evident.dalesbred.plugin.idea;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.patterns.PsiMethodCallPattern;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static fi.evident.dalesbred.plugin.idea.DalesbredPatterns.psiDalesbredFindMethodCall;
import static fi.evident.dalesbred.plugin.idea.SqlUtils.countQueryParametersPlaceholders;

public class DalesbredIncorrectParameterCountInspection extends BaseJavaLocalInspectionTool {

    private static final PsiMethodCallPattern FIND_METHOD_CALL = psiDalesbredFindMethodCall();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (FIND_METHOD_CALL.accepts(expression)) {
                    PsiExpression[] parameters = expression.getArgumentList().getExpressions();

                    if (parameters.length >= 1) {
                        String sql = resolveQueryString(parameters[1]);
                        if (sql != null) {
                            int expected = countQueryParametersPlaceholders(sql);

                            int actual = parameters.length - 2;
                            if (actual != expected)
                                holder.registerProblem(expression, "Expected " + expected + " query parameters, but got " + actual + '.');
                        }
                    }
                }
            }
        };
    }

    @Nullable
    private static String resolveQueryString(@NotNull PsiExpression queryParameter) {
        if (queryParameter instanceof PsiLiteralExpression) {
            PsiLiteralExpression exp = (PsiLiteralExpression) queryParameter;
            Object value = exp.getValue();
            if (value instanceof String)
                return (String) value;
        }
        return null;
    }
}
