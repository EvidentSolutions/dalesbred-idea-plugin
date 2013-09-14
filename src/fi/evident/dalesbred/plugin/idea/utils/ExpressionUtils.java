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

package fi.evident.dalesbred.plugin.idea.utils;

import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.psi.impl.JavaConstantExpressionEvaluator.computeConstantExpression;

public final class ExpressionUtils {

    private ExpressionUtils() {
    }

    @Nullable
    public static String resolveQueryString(@NotNull PsiExpression parameter) {
        Object value = computeConstantExpression(parameter, false);
        return (value instanceof String) ? (String) value : null;
    }

    public static boolean isUninstantiable(@NotNull PsiClass cl) {
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

    public static boolean isNonStaticInnerClass(@NotNull PsiClass cl) {
        if (cl.getContainingClass() == null) return false;

        PsiModifierList modifiers = cl.getModifierList();
        return modifiers == null || !modifiers.hasModifierProperty(PsiModifier.STATIC);
    }

    public static boolean hasPublicAccessorsForProperties(@NotNull PsiClass type, @NonNls List<String> properties) {
        for (String property : properties)
            if (!hasPublicAccessorForProperty(type, property))
                return false;

        return true;
    }

    public static boolean hasPublicAccessorForProperty(@NotNull PsiClass type, @NotNull String property) {
        PsiField field = type.findFieldByName(property, true);

        return field != null && field.hasModifierProperty(PsiModifier.PUBLIC)
                || PropertyUtil.findPropertySetter(type, property, false, true) != null;

    }

    @Nullable
    public static PsiClass resolveType(@NotNull PsiExpression root) {
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