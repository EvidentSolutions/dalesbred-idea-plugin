/*
 * Copyright (c) 2013 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.evident.dalesbred.plugin.idea.inspections;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.patterns.PsiMethodCallPattern;
import com.intellij.psi.*;
import fi.evident.dalesbred.plugin.idea.utils.DalesbredPatterns;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.patterns.PsiJavaPatterns.psiExpression;
import static fi.evident.dalesbred.plugin.idea.ui.ClassList.createClassesListControl;
import static fi.evident.dalesbred.plugin.idea.utils.ExpressionUtils.*;
import static fi.evident.dalesbred.plugin.idea.utils.SqlUtils.selectVariables;

public class DalesbredUninstantiableResultInspection extends BaseJavaLocalInspectionTool {

    @NonNls
    public List<String> allowedTypes = new ArrayList<String>();

    private static final PsiMethodCallPattern FIND_METHOD_CALL = psiExpression().methodCall(DalesbredPatterns.findMethod());

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
                        verifyFindMap(parameters, holder);
                    } else if ("findTable".equals(methodName)) {
                        // Nothing
                    } else if ("findUniqueInt".equals(methodName) || "findUniqueLong".equals(methodName)) {
                        verifyUniquePrimitive(parameters, holder);
                    } else {
                        verifyFind(parameters, holder);
                    }
                }
            }
        };
    }

    private static void verifyUniquePrimitive(@NotNull PsiExpression[] parameters, @NotNull ProblemsHolder holder) {
        if (parameters.length == 0) return;

        String sql = resolveQueryString(parameters[0]);
        if (sql != null) {
            List<String> selectItems = selectVariables(sql);

            if (selectItems.contains("*"))
                holder.registerProblem(parameters[0], "Can't verify construction when select list contains '*'.");
            else if (selectItems.size() != 1)
                holder.registerProblem(parameters[0], "Expected 1 column in result set, but got " + selectItems.size() + '.');
        }
    }

    private void verifyFind(@NotNull PsiExpression[] parameters, @NotNull ProblemsHolder holder) {
        if (parameters.length < 2) return;

        PsiClass resultType = resolveType(parameters[0]);
        if (resultType != null && !allowedTypes.contains(resultType.getQualifiedName())) {
            if (isUninstantiable(resultType))
                holder.registerProblem(parameters[0], "Class is not instantiable.");
            else {
                String sql = resolveQueryString(parameters[1]);
                if (sql != null) {
                    List<String> selectItems = selectVariables(sql);

                    if (selectItems.contains("*"))
                        holder.registerProblem(parameters[1], "Can't verify construction when select list contains '*'.");
                    else if (!hasMatchingConstructor(resultType, selectItems))
                        holder.registerProblem(parameters[0], "Could not find a way to construct class with selected values.");
                }
            }
        }
    }

    private void verifyFindMap(@NotNull PsiExpression[] parameters, @NotNull ProblemsHolder holder) {
        if (parameters.length < 3) return;

        verifyParameterIsInstantiable(parameters[0], holder);
        verifyParameterIsInstantiable(parameters[1], holder);

        String sql = resolveQueryString(parameters[2]);
        if (sql != null) {
            List<String> selectItems = selectVariables(sql);
            if (selectItems.contains("*"))
                holder.registerProblem(parameters[2], "Can't verify construction when select list contains '*'.");
            else if (selectItems.size() != 2)
                holder.registerProblem(parameters[2], "Select should return exactly 2 columns.");
        }
    }

    private void verifyParameterIsInstantiable(@NotNull PsiExpression parameter, @NotNull ProblemsHolder holder) {
        PsiClass cl = resolveType(parameter);
        if (cl != null && !allowedTypes.contains(cl.getQualifiedName()) && isUninstantiable(cl))
            holder.registerProblem(parameter, "Class is not instantiable.");
    }

    private static boolean hasMatchingConstructor(@NotNull PsiClass type, @NotNull List<String> selectItems) {
        int selectCount = selectItems.size();

        PsiMethod[] constructors = type.getConstructors();
        if (constructors.length != 0) {
            for (PsiMethod ctor : constructors) {
                if (ctor.hasModifierProperty(PsiModifier.PUBLIC)) {
                    int parameterCount = ctor.getParameterList().getParametersCount();
                    if (parameterCount == selectCount || (parameterCount < selectCount && hasPublicAccessorsForColumns(type, selectItems.subList(parameterCount, selectCount))))
                        return true;
                }
            }
            return false;
        } else {
            return hasPublicAccessorsForColumns(type, selectItems);
        }
    }

    @Override
    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createClassesListControl(allowedTypes, "Allowed types"), BorderLayout.CENTER);
        return panel;
    }
}
