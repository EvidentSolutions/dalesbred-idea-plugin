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
