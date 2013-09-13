package fi.evident.dalesbred.plugin.idea;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.patterns.PsiMethodCallPattern;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.impl.JavaConstantExpressionEvaluator.computeConstantExpression;
import static fi.evident.dalesbred.plugin.idea.DalesbredPatterns.psiDalesbredFindMethodCall;
import static fi.evident.dalesbred.plugin.idea.DalesbredPatterns.psiDalesbredSqlQueryMethodCall;
import static fi.evident.dalesbred.plugin.idea.SqlUtils.countQueryParametersPlaceholders;

public class DalesbredIncorrectParameterCountInspection extends BaseJavaLocalInspectionTool {

    private static final PsiMethodCallPattern FIND_METHOD_CALL = psiDalesbredFindMethodCall();
    private static final PsiMethodCallPattern SQL_QUERY_METHOD_CALL = psiDalesbredSqlQueryMethodCall();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (FIND_METHOD_CALL.accepts(expression)) {
                    String methodName = expression.getMethodExpression().getReferenceName();
                    PsiExpression[] parameters = expression.getArgumentList().getExpressions();

                    if ("findMap".equals(methodName))
                        verifyQueryParameterCount(parameters, 2, holder);
                    else
                        verifyQueryParameterCount(parameters, 1, holder);

                } else if (SQL_QUERY_METHOD_CALL.accepts(expression)) {
                    PsiExpression[] parameters = expression.getArgumentList().getExpressions();
                    verifyQueryParameterCount(parameters, 0, holder);
                }
            }
        };
    }

    private static void verifyQueryParameterCount(@NotNull PsiExpression[] parameters, int queryIndex, @NotNull ProblemsHolder holder) {
        if (queryIndex >= parameters.length) return;

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
    private static String resolveQueryString(@NotNull PsiExpression parameter) {
        Object value = computeConstantExpression(parameter, false);
        return (value instanceof String) ? (String) value : null;
    }
}
