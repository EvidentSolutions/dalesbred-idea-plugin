import fi.evident.dalesbred.Database;

import java.lang.String;

public class ConstructorValidation {

    Database db;

    public void instantiationByFields() {
        db.findAll(MyClassWithImplicitDefaultConstructor.class, "select bar from foo");
        db.findAll(MyClassWithExplicitDefaultConstructor.class, "select bar from foo");
    }

    public void instantiotionBySetters() {
        db.findAll(MyClassWithSetter.class, "select bar from foo");
        db.findAll(<warning>MyClassWithSetter.class</warning>, "select baz from foo");
    }

    public void instantiotionByConstructor() {
        db.findAll(MyClassWithTwoArgumentConstructor.class, "select x, y from foo");
        db.findAll(<warning>MyClassWithTwoArgumentConstructor.class</warning>, "select x from foo");
        db.findAll(<warning>MyClassWithTwoArgumentConstructor.class</warning>, "select x, y, z from foo");
    }

    public void instantiotionByConstructorAndFields() {
        db.findAll(MyClassWithConstructorFieldAndSetter.class, "select foo, bar, baz from foobar");
    }

    public class NonStaticInnerClass { }
}

class MyClassWithImplicitDefaultConstructor {
    public String bar;
}

class MyClassWithExplicitDefaultConstructor {
    public String bar;

    public MyClassWithExplicitDefaultConstructor() { }
}

class MyClassWithTwoArgumentConstructor {
    public MyClassWithTwoArgumentConstructor(int x, int y) { }
}

class MyClassWithSetter {
    public void setBar(String bar) { }
}

class MyClassWithConstructorFieldAndSetter {
    public String bar;
    public void setBaz(String baz) { }

    public MyClassWithConstructorFieldAndSetter(String foo) { }
}
