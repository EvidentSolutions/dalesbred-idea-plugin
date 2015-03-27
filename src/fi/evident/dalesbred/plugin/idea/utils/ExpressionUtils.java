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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.psi.impl.JavaConstantExpressionEvaluator.computeConstantExpression;
import static com.intellij.psi.util.PropertyUtil.getPropertyNameBySetter;
import static com.intellij.psi.util.PropertyUtil.isSimplePropertySetter;

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
            if (ctor.hasModifierProperty(PsiModifier.PUBLIC) && !isIgnored(cl))
                return false;

        return true;
    }

    public static boolean isNonStaticInnerClass(@NotNull PsiClass cl) {
        if (cl.getContainingClass() == null) return false;

        PsiModifierList modifiers = cl.getModifierList();
        return modifiers == null || !modifiers.hasModifierProperty(PsiModifier.STATIC);
    }

    public static boolean hasPublicAccessorsForColumns(@NotNull PsiClass type, @NonNls List<String> columns) {
        for (String column : columns)
            if (!hasPublicAccessorForResultField(type, column))
                return false;

        return true;
    }

    private static boolean hasPublicAccessorForResultField(@NotNull PsiClass type, @NotNull String column) {
        String property = normalizeSelectName(column);

        for (PsiField field : type.getAllFields())
            if (isSettableField(field) && property.equalsIgnoreCase(field.getName()))
                return true;

        for (PsiMethod method : type.getAllMethods())
            if (isCallableSetter(method) && getPropertyNameBySetter(method).equalsIgnoreCase(property))
                return true;

        return false;
    }

    @NotNull
    public static List<String> unusedProperties(@NotNull PsiClass type, @NotNull List<String> items) {
        Set<String> usedNames = new HashSet<String>(items.size());
        for (String item : items)
            usedNames.add(normalizeSelectName(item));

        List<String> result = new ArrayList<String>();

        for (PsiField field : type.getAllFields())
            if (isSettableField(field)) {
                String fieldName = field.getName();
                if (usedNames.add(fieldName.toLowerCase()))
                    result.add(fieldName);
            }

        for (PsiMethod method : type.getAllMethods())
            if (isCallableSetter(method)) {
                String propertyName = getPropertyNameBySetter(method);
                if (usedNames.add(propertyName.toLowerCase()))
                    result.add(propertyName);
            }

        return result;
    }

    private static boolean isCallableSetter(@NotNull PsiMethod method) {
        return !method.hasModifierProperty(PsiModifier.STATIC)
            && isSimplePropertySetter(method)
            && !isIgnored(method);
    }

    public static boolean isIgnored(@NotNull PsiMember member) {
        PsiModifierList modifierList = member.getModifierList();
        if (modifierList == null) return false;

        for (PsiAnnotation annotation : modifierList.getAnnotations())
            if ("fi.evident.dalesbred.DalesbredIgnore".equals(annotation.getQualifiedName()))
                return true;

        return false;
    }

    private static boolean isSettableField(@NotNull PsiField field) {
        return field.hasModifierProperty(PsiModifier.PUBLIC)
            && !field.hasModifierProperty(PsiModifier.FINAL)
            && !field.hasModifierProperty(PsiModifier.STATIC)
            && !isIgnored(field);
    }

    @NotNull
    private static String normalizeSelectName(@NotNull String item) {
        return item.replace("_", "").toLowerCase();
    }

    @Nullable
    public static PsiClass resolveType(@NotNull PsiExpression root) {
        PsiExpression exp = root;
        while (true) {
            if (exp instanceof PsiClassObjectAccessExpression) {
                PsiType type = ((PsiClassObjectAccessExpression) exp).getOperand().getType();
                if (type instanceof PsiClassType) {
                    return ((PsiClassType) type).resolve();
                } else if (type instanceof PsiPrimitiveType) {
                    PsiClassType boxedType = ((PsiPrimitiveType) type).getBoxedType(root);
                    if (boxedType != null)
                        return boxedType.resolve();
                }

                return null;

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
