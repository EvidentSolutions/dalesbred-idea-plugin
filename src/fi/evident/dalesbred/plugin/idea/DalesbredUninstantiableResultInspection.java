package fi.evident.dalesbred.plugin.idea;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.patterns.PsiMethodCallPattern;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static fi.evident.dalesbred.plugin.idea.DalesbredPatterns.psiDalesbredFindMethodCall;

public class DalesbredUninstantiableResultInspection extends BaseJavaLocalInspectionTool {

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
                        verifyParameterIsInstantiable(parameters[0], holder);
                        verifyParameterIsInstantiable(parameters[1], holder);
                    } else {
                        verifyParameterIsInstantiable(parameters[0], holder);
                    }
                }
            }
        };
    }

    private static void verifyParameterIsInstantiable(@NotNull PsiExpression parameter, @NotNull ProblemsHolder holder) {
        PsiClass cl = resolveType(parameter);
        if (cl == null) return;

        if (cl.isAnnotationType()) {
            holder.registerProblem(parameter, "Class may not refer to an annotation type.");

        } else if (cl.isInterface()) {
            // TODO: allow interfaces of known (and registered) types
            holder.registerProblem(parameter, "Class may not refer to an interface.");
        }
    }

    @Nullable
    private static PsiClass resolveType(@NotNull PsiExpression root) {
        PsiExpression exp = root;
        while (true) {
            if (exp instanceof PsiClassObjectAccessExpression) {
                PsiType type = ((PsiClassObjectAccessExpression) exp).getOperand().getType();
                if (type instanceof PsiClassType)
                    return ((PsiClassType) type).resolve();

            } else if (exp instanceof PsiReferenceExpression) {
                PsiElement resolved = ((PsiReference) exp).resolve();
                if (resolved instanceof PsiVariable) {
                    PsiExpression initializer = ((PsiVariable) resolved).getInitializer();
                    if (initializer != null)
                        exp = initializer;
                }

            } else {
                return null;
            }
        }
    }
}
