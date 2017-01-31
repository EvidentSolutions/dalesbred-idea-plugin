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
import com.intellij.patterns.PsiJavaPatterns.psiClass
import com.intellij.patterns.PsiJavaPatterns.psiMethod
import com.intellij.patterns.PsiMethodPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.StandardPatterns.or
import com.intellij.patterns.StandardPatterns.string
import com.intellij.psi.PsiMethod

private object ClassNames {
    val CLASS = "java.lang.Class"
    val STRING = "java.lang.String"
    val OBJECT_ARGS = "java.lang.Object..."
    val DATABASE = "org.dalesbred.Database"
    val SQL_QUERY = "org.dalesbred.query.SqlQuery"
    val ROW_MAPPER = "org.dalesbred.result.RowMapper"
    val RESULT_SET_PROCESSOR = "org.dalesbred.result.ResultSetProcessor"
}

private val QUERY_AND_ARGS = arrayOf(ClassNames.STRING, ClassNames.OBJECT_ARGS)

private val databaseClass = psiClass().withQualifiedName(ClassNames.DATABASE)

private val sqlQueryClass = psiClass().withQualifiedName(ClassNames.SQL_QUERY)

val findMethod: ElementPattern<PsiMethod> by lazy {
    val generalName = string().oneOf("findUnique", "findUniqueOrNull", "findOptional", "findAll")
    val withoutRowMapperName = string().oneOf("findUniqueInt", "findUniqueLong", "findUniqueBoolean", "findTable")

    or(
            databaseMethod(generalName).withParameters(ClassNames.CLASS, *QUERY_AND_ARGS),
            databaseMethod(generalName).withParameters(ClassNames.CLASS, ClassNames.SQL_QUERY),
            databaseMethod(generalName).withParameters(ClassNames.ROW_MAPPER, *QUERY_AND_ARGS),
            databaseMethod(generalName).withParameters(ClassNames.ROW_MAPPER, ClassNames.SQL_QUERY),
            databaseMethod(withoutRowMapperName).withParameters(*QUERY_AND_ARGS),
            databaseMethod(withoutRowMapperName).withParameters(ClassNames.SQL_QUERY),
            databaseMethod("findMap").withParameters(ClassNames.CLASS, ClassNames.CLASS, *QUERY_AND_ARGS),
            databaseMethod("findMap").withParameters(ClassNames.CLASS, ClassNames.CLASS, ClassNames.SQL_QUERY))
}

val sqlQueryMethod: ElementPattern<PsiMethod> =
        psiMethod().definedInClass(sqlQueryClass).withName("query").withParameters(*QUERY_AND_ARGS)

val executeQueryMethod: ElementPattern<PsiMethod> = or(
        databaseMethod("executeQuery").withParameters(ClassNames.RESULT_SET_PROCESSOR, *QUERY_AND_ARGS),
        databaseMethod("executeQuery").withParameters(ClassNames.RESULT_SET_PROCESSOR, ClassNames.SQL_QUERY))

val updateMethod: ElementPattern<PsiMethod> =
        databaseMethod("update").withParameters(*QUERY_AND_ARGS)

val updateAndProcessGeneratedKeysMethod: ElementPattern<PsiMethod> = or(
        databaseMethod("updateAndProcessGeneratedKeys").withParameters(ClassNames.RESULT_SET_PROCESSOR, *QUERY_AND_ARGS),
        databaseMethod("updateAndProcessGeneratedKeys").withParameters(ClassNames.RESULT_SET_PROCESSOR, ClassNames.SQL_QUERY))


private fun findMethodGeneral(): ElementPattern<PsiMethod> {
    val namePattern = string().oneOf("findUnique", "findUniqueOrNull", "findOptional", "findAll")
    return or(
            databaseMethod(namePattern).withParameters(ClassNames.CLASS, *QUERY_AND_ARGS),
            databaseMethod(namePattern).withParameters(ClassNames.CLASS, ClassNames.SQL_QUERY),
            databaseMethod(namePattern).withParameters(ClassNames.ROW_MAPPER, *QUERY_AND_ARGS),
            databaseMethod(namePattern).withParameters(ClassNames.ROW_MAPPER, ClassNames.SQL_QUERY))
}

private fun databaseMethod(name: String): PsiMethodPattern =
        databaseMethod(StandardPatterns.`object`(name))

private fun databaseMethod(name: ElementPattern<String>): PsiMethodPattern =
        psiMethod().definedInClass(databaseClass).withName(name)
