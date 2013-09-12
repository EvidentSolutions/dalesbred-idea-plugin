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
    static PsiMethodPattern psiDalesbredFindMethod() {
        // TODO: findMap
        return psiMethod().definedInClass(psiDatabaseClass()).withName(string().oneOf("findUnique", "findAll", "findUniqueOrNull"));
    }

    @NotNull
    static PsiMethodCallPattern psiDalesbredFindMethodCall() {
        return psiExpression().methodCall(psiDalesbredFindMethod());
    }
}
