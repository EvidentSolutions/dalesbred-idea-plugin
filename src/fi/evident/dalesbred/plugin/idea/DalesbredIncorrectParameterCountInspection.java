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
                    String methodName = expression.getMethodExpression().getReferenceName();
                    PsiExpression[] parameters = expression.getArgumentList().getExpressions();

                    if ("findMap".equals(methodName)) {
                        verifyQueryParameterCount(parameters, 2, holder);
                    } else {
                        verifyQueryParameterCount(parameters, 1, holder);
                    }
                }
            }
        };
    }

    private static void verifyQueryParameterCount(@NotNull PsiExpression[] parameters, int queryIndex, @NotNull ProblemsHolder holder) {
        PsiExpression queryParameter = parameters[queryIndex];
        String sql = resolveQueryString(queryParameter);
        if (sql != null) {
            int expected = countQueryParametersPlaceholders(sql);

            int actual = parameters.length - (queryIndex+1);
            if (actual != expected)
                holder.registerProblem(queryParameter, "Expected " + expected + " query parameters, but got " + actual + '.');
        }
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
