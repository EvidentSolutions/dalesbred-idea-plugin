/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package fi.evident.dalesbred.plugin.idea.utils

import com.intellij.psi.*
import com.intellij.psi.PsiModifier.*
import com.intellij.psi.impl.JavaConstantExpressionEvaluator.computeConstantExpression
import com.intellij.psi.util.PropertyUtil.getPropertyNameBySetter
import com.intellij.psi.util.PropertyUtil.isSimplePropertySetter
import java.util.*

private val IGNORE_ANNOTATIONS = listOf("org.dalesbred.annotation.DalesbredIgnore", "fi.evident.dalesbred.DalesbredIgnore")

private val INSTANTIATOR_ANNOTATION = "org.dalesbred.annotation.DalesbredInstantiator"

fun resolveQueryString(parameter: PsiExpression): String? =
    computeConstantExpression(parameter, false) as? String

fun PsiClass.isUninstantiable() =
    isAnnotationType || isInterface || isNonStaticInnerClass || hasModifierProperty(ABSTRACT) || allConstructorsAreInaccessible

private val PsiClass.allConstructorsAreInaccessible: Boolean
    get() = constructors.let { cs -> cs.size != 0 && !cs.any { it.isAccessible } }

private val PsiMethod.isAccessible: Boolean
    get() = hasModifierProperty(PUBLIC) && !isIgnored

val PsiClass.isNonStaticInnerClass: Boolean
    get() = containingClass != null && modifierList.let { ms -> ms == null || !ms.hasModifierProperty(STATIC) }

fun PsiClass.hasPublicAccessorsForColumns(columns: List<String>): Boolean =
    columns.all { hasPublicAccessorForResultField(it) }

private fun PsiClass.hasPublicAccessorForResultField(column: String): Boolean {
    val property = normalizeSelectName(column)

    return allFields.any { it.isSettableField && property.equals(it.name, ignoreCase = true) }
        || allMethods.any { it.isCallableSetter && getPropertyNameBySetter(it).equals(property, ignoreCase = true) }
}

fun unusedProperties(type: PsiClass, items: List<String>): List<String> {
    val usedNames = HashSet<String>(items.size)
    for (item in items)
        usedNames.add(normalizeSelectName(item))

    val result = ArrayList<String>()

    for (field in type.allFields)
        if (field.isSettableField) {
            val fieldName = field.name!!
            if (usedNames.add(fieldName.toLowerCase()))
                result.add(fieldName)
        }

    for (method in type.allMethods)
        if (method.isCallableSetter) {
            val propertyName = getPropertyNameBySetter(method)
            if (usedNames.add(propertyName.toLowerCase()))
                result.add(propertyName)
        }

    return result
}

private val PsiMethod.isCallableSetter: Boolean
    get() = !hasModifierProperty(STATIC) && isSimplePropertySetter(this) && !isIgnored

val PsiMember.isIgnored: Boolean
    get() = modifierList?.annotations?.any { it.qualifiedName in IGNORE_ANNOTATIONS } ?: false

val PsiMethod.isExplicitInstantiator: Boolean
    get() = modifierList.annotations.any { it.qualifiedName == INSTANTIATOR_ANNOTATION }

private val PsiField.isSettableField: Boolean
    get() = hasModifierProperty(PUBLIC)
            && !hasModifierProperty(FINAL)
            && !hasModifierProperty(STATIC)
            && !isIgnored

private fun normalizeSelectName(item: String) =
    item.replace("_", "").toLowerCase()

fun resolveType(root: PsiExpression): PsiClass? {
    var exp = root
    while (true) {
        when (exp) {
            is PsiClassObjectAccessExpression -> {
                val type = exp.operand.type
                when (type) {
                    is PsiClassType ->
                        return type.resolve()

                    is PsiPrimitiveType -> {
                        val boxedType = type.getBoxedType(root)
                        if (boxedType != null)
                            return boxedType.resolve()
                    }
                }

                return null
            }
            is PsiReferenceExpression -> {
                val resolved = exp.resolve()
                if (resolved is PsiVariable) {
                    val initializer = resolved.initializer
                    if (initializer != null)
                        exp = initializer
                }
            }
            else ->
                return null
        }
    }
}
