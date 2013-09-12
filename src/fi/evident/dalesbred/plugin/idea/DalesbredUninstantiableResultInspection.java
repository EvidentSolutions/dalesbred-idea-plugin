package fi.evident.dalesbred.plugin.idea;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.patterns.PsiMethodCallPattern;
import com.intellij.psi.*;
import fi.evident.dalesbred.plugin.idea.ui.ClassList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static fi.evident.dalesbred.plugin.idea.DalesbredPatterns.psiDalesbredFindMethodCall;

public class DalesbredUninstantiableResultInspection extends BaseJavaLocalInspectionTool {

    @NonNls
    public List<String> allowedTypes = new ArrayList<String>();

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

    private void verifyParameterIsInstantiable(@NotNull PsiExpression parameter, @NotNull ProblemsHolder holder) {
        PsiClass cl = resolveType(parameter);
        if (cl != null) {
            if (allowedTypes.contains(cl.getQualifiedName()))
                return;

            String error = findErrorForType(cl);
            if (error != null)
                holder.registerProblem(parameter, error);
        }
    }

    @Nullable
    private static String findErrorForType(@NotNull PsiClass cl) {
        if (cl.isAnnotationType()) {
            return "Class may not be an annotation type.";

        } else if (cl.isInterface()) {
            return "Class may not be an interface.";

        } else if (isNonStaticInnerClass(cl)) {
            return "Class may not be a non-static inner class.";
        } else {
            return null;
        }
    }

    private static boolean isNonStaticInnerClass(@NotNull PsiClass cl) {
        if (cl.getContainingClass() == null) return false;

        PsiModifierList modifiers = cl.getModifierList();
        return modifiers == null || !modifiers.hasModifierProperty(PsiModifier.STATIC);
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

    @SuppressWarnings("MagicNumber")
    @Override
    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(ClassList.createSpecialAnnotationsListControl(allowedTypes, "Allowed types"), BorderLayout.CENTER);
        return panel;
    }
}
