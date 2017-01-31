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
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiMethodCallExpression
import fi.evident.dalesbred.plugin.idea.utils.*

class DalesbredIncorrectParameterCountInspection : BaseJavaLocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = object : JavaElementVisitor() {

        override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
            val parameters = expression.argumentList.expressions
            when {
                FIND_METHOD_CALL.accepts(expression) ->
                    when (expression.methodExpression.referenceName ?: return) {
                        "findMap" ->
                            verifyQueryParameterCount(parameters, 2, holder)
                        "findTable",
                        "findUniqueInt",
                        "findUniqueLong" ->
                            verifyQueryParameterCount(parameters, 0, holder)
                        else ->
                            verifyQueryParameterCount(parameters, 1, holder)
                    }

                SQL_QUERY_METHOD_CALL.accepts(expression) ->
                    verifyQueryParameterCount(parameters, 0, holder)

                UPDATE_METHOD_CALL.accepts(expression) ->
                    verifyQueryParameterCount(parameters, 0, holder)

                UPDATE_AND_PROCESS_GENERATED_KEYS_METHOD_CALL.accepts(expression) ->
                    verifyQueryParameterCount(parameters, 1, holder)
            }
        }
    }

    companion object {

        private val FIND_METHOD_CALL = psiExpression().methodCall(or(findMethod, executeQueryMethod))
        private val SQL_QUERY_METHOD_CALL = psiExpression().methodCall(sqlQueryMethod)
        private val UPDATE_METHOD_CALL = psiExpression().methodCall(updateMethod)
        private val UPDATE_AND_PROCESS_GENERATED_KEYS_METHOD_CALL = psiExpression().methodCall(updateAndProcessGeneratedKeysMethod)

        private fun verifyQueryParameterCount(parameters: Array<PsiExpression>, queryIndex: Int, holder: ProblemsHolder) {
            if (queryIndex >= parameters.size) return

            val queryParameter = parameters[queryIndex]
            val sql = queryParameter.resolveQueryString()
            if (sql != null) {
                val expected = countQueryParametersPlaceholders(sql)

                val actual = parameters.size - (queryIndex + 1)
                if (actual != expected)
                    holder.registerProblem(queryParameter, "Expected $expected query parameters, but got $actual.")
            }
        }
    }
}
