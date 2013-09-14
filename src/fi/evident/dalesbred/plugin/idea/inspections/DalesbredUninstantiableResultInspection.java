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
import com.intellij.psi.util.PropertyUtil;
import fi.evident.dalesbred.plugin.idea.utils.SqlUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static fi.evident.dalesbred.plugin.idea.ui.ClassList.createClassesListControl;
import static fi.evident.dalesbred.plugin.idea.utils.DalesbredPatterns.dalesbredFindMethodCall;
import static fi.evident.dalesbred.plugin.idea.utils.ExpressionUtils.resolveQueryString;

public class DalesbredUninstantiableResultInspection extends BaseJavaLocalInspectionTool {

    @NonNls
    public List<String> allowedTypes = new ArrayList<String>();

    private static final PsiMethodCallPattern FIND_METHOD_CALL = dalesbredFindMethodCall();

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
                        // TODO: verify that select-list contains exactly two values
                    } else {
                        PsiClass resultType = resolveType(parameters[0]);
                        if (resultType != null && !allowedTypes.contains(resultType.getQualifiedName())) {
                            if (isUninstantiable(resultType))
                                holder.registerProblem(parameters[0], "Class is not instantiable.");
                            else {
                                String error = verifySelectListMatchesResultType(resultType, parameters[1]);
                                if (error != null)
                                    holder.registerProblem(parameters[0], error);
                            }

                        }
                    }
                }
            }
        };
    }

    @Nullable
    private static String verifySelectListMatchesResultType(@NotNull PsiClass resultType, @NotNull PsiExpression sqlParameter) {
        String sql = resolveQueryString(sqlParameter);
        if (sql != null) {
            if (sql.contains("*"))
                return "Can't verify construction when select list contains '*'.";

            List<String> selectItems = SqlUtils.selectVariables(sql);

            if (!hasMatchingConstructor(resultType, selectItems))
                return "Could not find a way to construct class with selected values.";
        }
        return null;
    }

    private static boolean hasMatchingConstructor(@NotNull PsiClass type, @NotNull List<String> selectItems) {
        int selectCount = selectItems.size();

        PsiMethod[] constructors = type.getConstructors();
        if (constructors.length != 0) {
            for (PsiMethod ctor : constructors) {
                if (ctor.hasModifierProperty(PsiModifier.PUBLIC)) {
                    int parameterCount = ctor.getParameterList().getParametersCount();
                    if (parameterCount == selectCount || (parameterCount < selectCount && hasPublicAccessorsForProperties(type, selectItems.subList(parameterCount, selectCount))))
                        return true;
                }
            }
            return false;
        } else {
            return hasPublicAccessorsForProperties(type, selectItems);
        }
    }

    private static boolean hasPublicAccessorsForProperties(@NotNull PsiClass type, @NonNls List<String> properties) {
        for (String property : properties)
            if (!hasPublicAccessorForProperty(type, property))
                return false;

        return true;
    }

    private static boolean hasPublicAccessorForProperty(@NotNull PsiClass type, @NotNull String property) {
        PsiField field = type.findFieldByName(property, true);

        return field != null && field.hasModifierProperty(PsiModifier.PUBLIC)
            || PropertyUtil.findPropertySetter(type, property, false, true) != null;

    }

    private void verifyParameterIsInstantiable(@NotNull PsiExpression parameter, @NotNull ProblemsHolder holder) {
        PsiClass cl = resolveType(parameter);
        if (cl != null && !allowedTypes.contains(cl.getQualifiedName()) && isUninstantiable(cl))
            holder.registerProblem(parameter, "Class is not instantiable.");
    }

    private static boolean isUninstantiable(@NotNull PsiClass cl) {
        return cl.isAnnotationType()
                || cl.isInterface()
                || isNonStaticInnerClass(cl)
                || cl.hasModifierProperty(PsiModifier.ABSTRACT)
                || allConstructorsAreInaccessible(cl);
    }

    private static boolean allConstructorsAreInaccessible(@NotNull PsiClass cl) {
        PsiMethod[] constructors = cl.getConstructors();
        if (constructors.length == 0)
            return false;

        for (PsiMethod ctor : constructors)
            if (ctor.hasModifierProperty(PsiModifier.PUBLIC))
                return false;

        return true;
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

    @Override
    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createClassesListControl(allowedTypes, "Allowed types"), BorderLayout.CENTER);
        return panel;
    }
}
