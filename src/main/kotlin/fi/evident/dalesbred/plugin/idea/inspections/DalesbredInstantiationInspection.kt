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

package fi.evident.dalesbred.plugin.idea.inspections

import com.intellij.codeInspection.BaseJavaLocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.patterns.PsiJavaPatterns.psiExpression
import com.intellij.psi.*
import fi.evident.dalesbred.plugin.idea.ui.ClassList.createClassesListControl
import fi.evident.dalesbred.plugin.idea.utils.*
import java.awt.BorderLayout
import java.util.*
import javax.swing.JComponent
import javax.swing.JPanel

class DalesbredInstantiationInspection : BaseJavaLocalInspectionTool() {

    var allowedTypes: MutableList<String> = ArrayList()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {

            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                if (FIND_METHOD_CALL.accepts(expression)) {
                    val parameters = expression.argumentList.expressions

                    when (expression.methodExpression.referenceName) {
                        "findMap" ->
                            verifyFindMap(parameters, holder)

                        "findTable" -> {
                            // Nothing
                        }

                        "findUniqueInt",
                        "findUniqueLong" ->
                            verifyUniquePrimitive(parameters, holder)

                        else ->
                            verifyFind(parameters, holder)
                    }
                }
            }
        }
    }

    private fun verifyFind(parameters: Array<PsiExpression>, holder: ProblemsHolder) {
        if (parameters.size < 2) return

        val resultType = resolveType(parameters[0])
        if (resultType != null && !isAllowed(resultType.qualifiedName)) {
            if (resultType.isUninstantiable()) {
                holder.registerProblem(parameters[0], "Class is not instantiable.")
            } else {
                val sql = resolveQueryString(parameters[1])
                if (sql != null) {
                    val selectItems = selectVariables(sql)

                    if ("*" in selectItems)
                        holder.registerProblem(parameters[1], "Can't verify construction when select list contains '*'.")
                    else
                        verifyMatchingConstructor(parameters[0], holder, resultType, selectItems)
                }
            }
        }
    }

    /**
     * Is this type-name allowed either because Dalesbred has special support for it
     * or user has added it to the list of allowed types?
     */
    private fun isAllowed(name: String?) =
        name in allowedTypes || name in BUILTIN_ALLOWED_TYPES

    private fun verifyFindMap(parameters: Array<PsiExpression>, holder: ProblemsHolder) {
        if (parameters.size < 3) return

        val (keyParam, valueParam, sqlParam) = parameters

        verifyParameterIsInstantiable(keyParam, holder)

        val valueType = resolveType(valueParam)
        val selectItems = resolveQueryString(sqlParam)?.let { selectVariables(it) }

        if (valueType != null && !isAllowed(valueType.qualifiedName)) {
            if (valueType.isUninstantiable()) {
                holder.registerProblem(valueParam, "Class is not instantiable.")
            } else if (selectItems != null) {
                if ("*" in selectItems)
                    holder.registerProblem(sqlParam, "Can't verify construction when select list contains '*'.")
                else
                    verifyMatchingConstructor(valueParam, holder, valueType, selectItems.drop(1))
            }
        }
    }

    private fun verifyParameterIsInstantiable(parameter: PsiExpression, holder: ProblemsHolder) {
        val cl = resolveType(parameter)
        if (cl != null && !isAllowed(cl.qualifiedName) && cl.isUninstantiable())
            holder.registerProblem(parameter, "Class is not instantiable.")
    }

    override fun createOptionsPanel(): JComponent? {
        val panel = JPanel(BorderLayout())
        panel.add(createClassesListControl(allowedTypes, "Allowed types"), BorderLayout.CENTER)
        return panel
    }

    companion object {

        private val BUILTIN_ALLOWED_TYPES = listOf(
                "java.io.InputStream",
                "java.io.Reader",
                "java.sql.Blob",
                "java.sql.Clob",
                "java.sql.SQLXML",
                "java.time.Instant",
                "java.time.LocalDate",
                "java.time.LocalDateTime",
                "java.time.LocalTime",
                "java.time.ZoneId",
                "java.util.TimeZone",
                "org.w3c.dom.Document")

        private val FIND_METHOD_CALL = psiExpression().methodCall(findMethod())

        private fun verifyUniquePrimitive(parameters: Array<PsiExpression>, holder: ProblemsHolder) {
            if (parameters.size == 0) return

            val sql = resolveQueryString(parameters[0])
            if (sql != null) {
                val selectItems = selectVariables(sql)

                if ("*" in selectItems)
                    holder.registerProblem(parameters[0], "Can't verify construction when select list contains '*'.")
                else if (selectItems.size != 1)
                    holder.registerProblem(parameters[0], "Expected 1 column in result set, but got " + selectItems.size + '.')
            }
        }

        private fun verifyMatchingConstructor(parameter: PsiExpression,
                                              holder: ProblemsHolder,
                                              resultType: PsiClass,
                                              selectItems: List<String>) {

            val problem = checkConstruction(resultType, selectItems)
            if (problem != null)
                holder.registerProblem(parameter, problem)
        }

        private fun checkConstruction(type: PsiClass, selectItems: List<String>): String? {
            val selectCount = selectItems.size

            if (type.isEnum) {
                return if (selectCount == 1) null else "Instantiating enum requires 1 argument, but got $selectCount."
            }

            val explicitInstantiators = findExplicitInstantiators(type)
            if (!explicitInstantiators.isEmpty()) {
                if (explicitInstantiators.size == 1) {
                    val instantiator = explicitInstantiators[0]
                    val parameterCount = instantiator.parameterList.parametersCount
                    if (parameterCount == selectCount)
                        return null
                    else
                        return "Instantiator tagged with @DalesbredInstantiator expected $parameterCount parameters, but got $selectCount."

                } else {
                    return "Found multiple constructors with @DalesbredInstantiator-annotation."
                }
            }

            val constructors = type.constructors
            if (constructors.size != 0) {
                for (ctor in constructors) {
                    if (ctor.hasModifierProperty(PsiModifier.PUBLIC) && !ctor.isIgnored) {
                        val parameterCount = ctor.parameterList.parametersCount
                        if (parameterCount == selectCount || parameterCount < selectCount && type.hasPublicAccessorsForColumns(selectItems.subList(parameterCount, selectCount)))
                            return null
                    }
                }
            } else if (type.hasPublicAccessorsForColumns(selectItems)) {
                val uninstantiatedProperties = unusedProperties(type, selectItems)
                return if (uninstantiatedProperties.isEmpty())
                    null
                else
                    uninstantiatedProperties.joinToString(", ", "Following properties are not initialized: ")
            }

            return "Could not find a way to construct class with selected values."
        }

        private fun findExplicitInstantiators(type: PsiClass) =
            type.constructors.filter { it.isExplicitInstantiator }
    }
}
