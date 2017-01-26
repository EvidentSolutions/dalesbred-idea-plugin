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

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PsiClassPattern
import com.intellij.patterns.PsiJavaPatterns.psiClass
import com.intellij.patterns.PsiJavaPatterns.psiMethod
import com.intellij.patterns.PsiMethodPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiMethod

internal object NewDalesbredPatterns {

    private val CLASS_CLASS_NAME = "java.lang.Class"
    private val STRING_CLASS_NAME = "java.lang.String"
    private val OBJECT_ARGS_CLASS_NAME = "java.lang.Object..."
    private val DATABASE_CLASS_NAME = "org.dalesbred.Database"
    private val SQL_QUERY_CLASS_NAME = "org.dalesbred.query.SqlQuery"
    private val ROW_MAPPER_CLASS_NAME = "org.dalesbred.result.RowMapper"
    private val RESULT_SET_PROCESSOR_CLASS_NAME = "org.dalesbred.result.ResultSetProcessor"

    fun findMethod(): ElementPattern<PsiMethod> =
        StandardPatterns.or(findTableMethod(), findUniqueMethod(), findUniquePrimitiveMethod(), findOptionalMethod(), findAllMethod(), findMapMethod())

    fun sqlQueryMethod(): ElementPattern<PsiMethod> =
        psiMethod().definedInClass(sqlQueryClass()).withName("query").withParameters(STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME)

    fun executeQueryMethod(): ElementPattern<PsiMethod> =
        StandardPatterns.or(
            databaseMethod("executeQuery").withParameters(RESULT_SET_PROCESSOR_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
            databaseMethod("executeQuery").withParameters(RESULT_SET_PROCESSOR_CLASS_NAME, SQL_QUERY_CLASS_NAME))

    fun updateMethod(): ElementPattern<PsiMethod> =
        databaseMethod("update").withParameters(STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME)

    fun updateAndProcessGeneratedKeysMethod(): ElementPattern<PsiMethod> =
        StandardPatterns.or(
            databaseMethod("updateAndProcessGeneratedKeys").withParameters(RESULT_SET_PROCESSOR_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
            databaseMethod("updateAndProcessGeneratedKeys").withParameters(RESULT_SET_PROCESSOR_CLASS_NAME, SQL_QUERY_CLASS_NAME))

    private fun databaseClass(): PsiClassPattern =
        psiClass().withQualifiedName(DATABASE_CLASS_NAME)

    private fun sqlQueryClass(): PsiClassPattern =
        psiClass().withQualifiedName(SQL_QUERY_CLASS_NAME)

    private fun findUniqueMethod(): ElementPattern<PsiMethod> {
        val namePattern = StandardPatterns.string().oneOf("findUnique", "findUniqueOrNull")
        return StandardPatterns.or(
                databaseMethod(namePattern).withParameters(CLASS_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod(namePattern).withParameters(CLASS_CLASS_NAME, SQL_QUERY_CLASS_NAME),
                databaseMethod(namePattern).withParameters(ROW_MAPPER_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod(namePattern).withParameters(ROW_MAPPER_CLASS_NAME, SQL_QUERY_CLASS_NAME))
    }

    private fun findOptionalMethod(): ElementPattern<PsiMethod> =
        StandardPatterns.or(
            databaseMethod("findOptional").withParameters(CLASS_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
            databaseMethod("findOptional").withParameters(CLASS_CLASS_NAME, SQL_QUERY_CLASS_NAME),
            databaseMethod("findOptional").withParameters(ROW_MAPPER_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
            databaseMethod("findOptional").withParameters(ROW_MAPPER_CLASS_NAME, SQL_QUERY_CLASS_NAME))

    private fun findUniquePrimitiveMethod(): ElementPattern<PsiMethod> {
        val namePattern = StandardPatterns.string().oneOf("findUniqueInt", "findUniqueLong", "findUniqueBoolean")
        return StandardPatterns.or(
                databaseMethod(namePattern).withParameters(STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
                databaseMethod(namePattern).withParameters(SQL_QUERY_CLASS_NAME))
    }

    private fun findMapMethod(): ElementPattern<PsiMethod> =
        StandardPatterns.or(
            databaseMethod("findMap").withParameters(CLASS_CLASS_NAME, CLASS_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
            databaseMethod("findMap").withParameters(CLASS_CLASS_NAME, CLASS_CLASS_NAME, SQL_QUERY_CLASS_NAME))

    private fun findAllMethod(): ElementPattern<PsiMethod> =
        StandardPatterns.or(
            databaseMethod("findAll").withParameters(CLASS_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
            databaseMethod("findAll").withParameters(CLASS_CLASS_NAME, SQL_QUERY_CLASS_NAME),
            databaseMethod("findAll").withParameters(ROW_MAPPER_CLASS_NAME, STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
            databaseMethod("findAll").withParameters(ROW_MAPPER_CLASS_NAME, SQL_QUERY_CLASS_NAME))

    private fun findTableMethod(): ElementPattern<PsiMethod> =
        StandardPatterns.or(
            databaseMethod("findTable").withParameters(STRING_CLASS_NAME, OBJECT_ARGS_CLASS_NAME),
            databaseMethod("findTable").withParameters(SQL_QUERY_CLASS_NAME))

    private fun databaseMethod(name: String): PsiMethodPattern =
        databaseMethod(StandardPatterns.`object`(name))

    private fun databaseMethod(name: ElementPattern<String>): PsiMethodPattern =
        psiMethod().definedInClass(databaseClass()).withName(name)
}
