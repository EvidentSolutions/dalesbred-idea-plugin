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
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PsiJavaPatterns.psiExpression;
import static com.intellij.patterns.StandardPatterns.or;
import static fi.evident.dalesbred.plugin.idea.utils.DalesbredPatterns.*;
import static fi.evident.dalesbred.plugin.idea.utils.ExpressionUtils.resolveQueryString;
import static fi.evident.dalesbred.plugin.idea.utils.SqlUtils.countQueryParametersPlaceholders;

public class DalesbredIncorrectParameterCountInspection extends BaseJavaLocalInspectionTool {

    @SuppressWarnings("unchecked")
    private static final PsiMethodCallPattern FIND_METHOD_CALL = psiExpression().methodCall(or(findMethod(), executeQueryMethod()));
    private static final PsiMethodCallPattern SQL_QUERY_METHOD_CALL = psiExpression().methodCall(sqlQueryMethod());
    private static final PsiMethodCallPattern UPDATE_METHOD_CALL = psiExpression().methodCall(updateMethod());

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (FIND_METHOD_CALL.accepts(expression)) {
                    String methodName = expression.getMethodExpression().getReferenceName();
                    if (methodName == null)
                        return;

                    PsiExpression[] parameters = expression.getArgumentList().getExpressions();

                    if (methodName.equals("findMap")) {
                        verifyQueryParameterCount(parameters, 2, holder);
                    } else if (methodName.equals("findTable") || methodName.equals("findUniqueInt") || methodName.equals("findUniqueLong")) {
                        verifyQueryParameterCount(parameters, 0, holder);
                    } else {
                        verifyQueryParameterCount(parameters, 1, holder);
                    }

                } else if (SQL_QUERY_METHOD_CALL.accepts(expression)) {
                    PsiExpression[] parameters = expression.getArgumentList().getExpressions();
                    verifyQueryParameterCount(parameters, 0, holder);
                } else if (UPDATE_METHOD_CALL.accepts(expression)) {
                    PsiExpression[] parameters = expression.getArgumentList().getExpressions();
                    verifyQueryParameterCount(parameters, 0, holder);
                }
            }
        };
    }

    private static void verifyQueryParameterCount(@NotNull PsiExpression[] parameters, int queryIndex, @NotNull ProblemsHolder holder) {
        if (queryIndex >= parameters.length) return;

        PsiExpression queryParameter = parameters[queryIndex];
        String sql = resolveQueryString(queryParameter);
        if (sql != null) {
            int expected = countQueryParametersPlaceholders(sql);

            int actual = parameters.length - (queryIndex+1);
            if (actual != expected)
                holder.registerProblem(queryParameter, "Expected " + expected + " query parameters, but got " + actual + '.');
        }
    }
}
