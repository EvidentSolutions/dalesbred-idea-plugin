/*
 * Copyright (c) 2015 Evident Solutions Oy
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

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiClassPattern;
import com.intellij.patterns.PsiMethodPattern;
import com.intellij.patterns.StringPattern;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PsiJavaPatterns.*;

final class NewDalesbredPatterns {

    private static final String CLASS_CLASS_NAME = "java.lang.Class";
    private static final String STRING_CLASS_NAME = "java.lang.String";
    private static final String OBJECT_ARGS_CLASS_NAME = "java.lang.Object...";
    private static final String DATABASE_CLASS_NAME = "org.dalesbred.Database";
    private static final String SQL_QUERY_CLASS_NAME = "org.dalesbred.query.SqlQuery";
    private static final String ROW_MAPPER_CLASS_NAME = "org.dalesbred.result.RowMapper";
    private static final String RESULT_SET_PROCESSOR_CLASS_NAME = "org.dalesbred.result.ResultSetProcessor";

    private NewDalesbredPatterns() {
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static ElementPattern<PsiMethod> findMethod() {
        return or(findTableMethod(), findUniqueMethod(), findUniquePrimitiveMethod(), findAllMethod(), findMapMethod());
    }

    @NotNull
    public static ElementPattern<PsiMethod> sqlQueryMethod() {
        return psiMethod().definedInClass(sqlQueryClass()).withName("query").withParameters(STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME);
    }

    @NotNull
    public static ElementPattern<PsiMethod> executeQueryMethod() {
        return or(
                databaseMethod("executeQuery").withParameters(RESULT_SET_PROCESSOR_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod("executeQuery").withParameters(RESULT_SET_PROCESSOR_CLASS_NAME, SQL_QUERY_CLASS_NAME));
    }

    @NotNull
    public static ElementPattern<PsiMethod> updateMethod() {
        return databaseMethod("update").withParameters(STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME);
    }

    @NotNull
    public static ElementPattern<PsiMethod> updateAndProcessGeneratedKeysMethod() {
        return or(
                databaseMethod("updateAndProcessGeneratedKeys").withParameters(RESULT_SET_PROCESSOR_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod("updateAndProcessGeneratedKeys").withParameters(RESULT_SET_PROCESSOR_CLASS_NAME, SQL_QUERY_CLASS_NAME));
    }

    @NotNull
    private static PsiClassPattern databaseClass() {
        return psiClass().withQualifiedName(DATABASE_CLASS_NAME);
    }

    @NotNull
    private static PsiClassPattern sqlQueryClass() {
        return psiClass().withQualifiedName(SQL_QUERY_CLASS_NAME);
    }

    @NotNull
    private static ElementPattern<PsiMethod> findUniqueMethod() {
        StringPattern namePattern = string().oneOf("findUnique", "findUniqueOrNull");
        return or(
                databaseMethod(namePattern).withParameters(CLASS_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod(namePattern).withParameters(CLASS_CLASS_NAME, SQL_QUERY_CLASS_NAME),
                databaseMethod(namePattern).withParameters(ROW_MAPPER_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod(namePattern).withParameters(ROW_MAPPER_CLASS_NAME, SQL_QUERY_CLASS_NAME));
    }

    @NotNull
    private static ElementPattern<PsiMethod> findUniquePrimitiveMethod() {
        StringPattern namePattern = string().oneOf("findUniqueInt", "findUniqueLong", "findUniqueBoolean");
        return or(
                databaseMethod(namePattern).withParameters(STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod(namePattern).withParameters(SQL_QUERY_CLASS_NAME));
    }

    @NotNull
    private static ElementPattern<PsiMethod> findMapMethod() {
        return or(
                databaseMethod("findMap").withParameters(CLASS_CLASS_NAME, CLASS_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod("findMap").withParameters(CLASS_CLASS_NAME, CLASS_CLASS_NAME, SQL_QUERY_CLASS_NAME));
    }

    @NotNull
    private static ElementPattern<PsiMethod> findAllMethod() {

        return or(
                databaseMethod("findAll").withParameters(CLASS_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod("findAll").withParameters(CLASS_CLASS_NAME, SQL_QUERY_CLASS_NAME),
                databaseMethod("findAll").withParameters(ROW_MAPPER_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod("findAll").withParameters(ROW_MAPPER_CLASS_NAME, SQL_QUERY_CLASS_NAME));
    }

    @NotNull
    private static ElementPattern<PsiMethod> findTableMethod() {
        return or(
                databaseMethod("findTable").withParameters(STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod("findTable").withParameters(SQL_QUERY_CLASS_NAME));
    }

    @NotNull
    private static PsiMethodPattern databaseMethod(@NotNull String name) {
        return databaseMethod(object(name));
    }

    @NotNull
    private static PsiMethodPattern databaseMethod(@NotNull ElementPattern<String> name) {
        return psiMethod().definedInClass(databaseClass()).withName(name);
    }
}
