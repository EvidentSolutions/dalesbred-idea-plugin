import fi.evident.dalesbred.Database;

import java.lang.String;

public class UninstantiatedProperties {

    Database db;

    public void annotationType() {
        db.findAll(SimpleClass.class, "select foo, bar from baz");
        db.findAll(<warning>SimpleClass.class</warning>, "select foo from baz");
        db.findAll(<warning>SimpleClass.class</warning>, "select bar from baz");
    }

    public void inspectionIsUsedOnlyWhenDefaultConstructorIsSelected() {
    }

    public void finalFieldsAreNotConsidered() {
        db.findAll(ClassWithFinalField.class, "select foo from baz");
    }

    public void staticFieldsAreNotConsidered() {
        db.findAll(ClassWithStaticField.class, "select foo from baz");
    }
}

class SimpleClass {
    public String foo;
    public String bar;
}

class ClassWithFinalField {
    public String foo;
    public final String bar = "";
}

class ClassWithStaticField {
    public String foo;
    public static String bar;
}
