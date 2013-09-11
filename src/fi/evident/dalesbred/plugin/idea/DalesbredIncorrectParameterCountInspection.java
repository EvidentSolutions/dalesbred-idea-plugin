package fi.evident.dalesbred.plugin.idea;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.patterns.PsiMethodCallPattern;
import com.intellij.patterns.PsiMethodPattern;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PsiJavaPatterns.*;

public class DalesbredIncorrectParameterCountInspection extends BaseJavaLocalInspectionTool {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                PsiMethodPattern method = psiMethod().definedInClass("fi.evident.dalesbred.Database").withName(string().oneOf("findUnique", "findAll", "findUniqueOrNull"));
                PsiMethodCallPattern call = psiExpression().methodCall(method);

                if (call.accepts(expression)) {
                    PsiExpression[] parameters = expression.getArgumentList().getExpressions();

                    if (parameters[1] instanceof PsiLiteralExpression) {
                        PsiLiteralExpression exp = (PsiLiteralExpression) parameters[1];
                        Object value = exp.getValue();
                        if (value instanceof String) {
                            String sql = (String) value;
                            int expected = countParameters(sql);

                            int actual = parameters.length-2;
                            if (actual != expected) {
                                holder.registerProblem(expression, "Expected " + expected + " query parameters, but got " + actual + '.');
                            }
                        }
                    }
                }
            }
        };
    }

    private static int countParameters(@NotNull String sql) {
        int count = 0;
        for (int i = 0, len = sql.length(); i < len; i++)
            if (sql.charAt(i) == '?')
                count++;
        return count;
    }
}
