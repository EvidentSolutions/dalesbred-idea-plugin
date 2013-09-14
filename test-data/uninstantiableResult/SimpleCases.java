import fi.evident.dalesbred.Database;

public class SimpleCases {

    Database db;

    public void annotationType() {
        db.findAll(<warning descr="Class is not instantiable.">MyAnnotation.class</warning>, "select 42 from foo");
    }

    public void interfaceType() {
        db.findAll(<warning descr="Class is not instantiable.">MyInterface.class</warning>, "select 42 from foo");
    }

    public void nonStaticInnerClass() {
        db.findAll(<warning descr="Class is not instantiable.">NonStaticInnerClass.class</warning>, "select 42 from foo");
    }

    public void abstractClass() {
        db.findAll(<warning descr="Class is not instantiable.">MyAbstractClass.class</warning>, "select 42 from foo");
    }

    public void classWithPrivateConstructor() {
        db.findAll(<warning descr="Class is not instantiable.">MyClassWithPrivateConstructor.class</warning>, "select 42 from foo");
    }

    public class NonStaticInnerClass { }
}

@interface MyAnnotation { }

interface MyInterface { }

abstract class MyAbstractClass { }

class MyClassWithPrivateConstructor {
    private MyClassWithPrivateConstructor() { }
}
