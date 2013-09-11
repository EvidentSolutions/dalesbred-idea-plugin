package fi.evident.dalesbred.plugin.idea;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.patterns.PsiMethodCallPattern;
import com.intellij.patterns.PsiMethodPattern;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.patterns.PsiJavaPatterns.*;

public class DalesbredUninstantiableResultInspection extends BaseJavaLocalInspectionTool {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                PsiMethodPattern method = psiMethod().definedInClass("fi.evident.dalesbred.Database").withName(string().oneOf("findUnique", "findAll", "findUniqueOrNull"));
                PsiMethodCallPattern call = psiExpression().methodCall(method);

                if (call.accepts(expression)) {
                    PsiExpression firstParameterExp = expression.getArgumentList().getExpressions()[0];
                    PsiClass cl = resolveType(firstParameterExp);
                    if (cl != null && cl.isInterface()) {
                        // TODO: allow interfaces of known (and registered) types
                        holder.registerProblem(firstParameterExp, "Class may not refer to an interface.");
                    }
                }
            }
        };
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
