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

package fi.evident.dalesbred.plugin.idea;

import com.intellij.patterns.PsiClassPattern;
import com.intellij.patterns.PsiMethodCallPattern;
import com.intellij.patterns.PsiMethodPattern;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PsiJavaPatterns.*;
import static com.intellij.patterns.StandardPatterns.string;

final class DalesbredPatterns {

    private DalesbredPatterns() {
    }

    @NotNull
    static PsiClassPattern psiDatabaseClass() {
        return psiClass().withQualifiedName("fi.evident.dalesbred.Database");
    }

    @NotNull
    static PsiClassPattern psiSqlQueryClass() {
        return psiClass().withQualifiedName("fi.evident.dalesbred.SqlQuery");
    }

    @NotNull
    static PsiMethodCallPattern psiDalesbredSqlQueryMethodCall() {
        return psiExpression().methodCall(psiDalesbredSqlQueryMethod());
    }

    @NotNull
    private static PsiMethodPattern psiDalesbredSqlQueryMethod() {
        return psiMethod().definedInClass(psiSqlQueryClass()).withName("query").withParameters("java.lang.String", "java.lang.Object[]");
    }

    @NotNull
    static PsiMethodPattern psiDalesbredFindMethod() {
        return psiMethod().definedInClass(psiDatabaseClass()).withName(string().oneOf("findUnique", "findAll", "findUniqueOrNull", "findMap"));
    }

    @NotNull
    static PsiMethodCallPattern psiDalesbredFindMethodCall() {
        return psiExpression().methodCall(psiDalesbredFindMethod());
    }
}
