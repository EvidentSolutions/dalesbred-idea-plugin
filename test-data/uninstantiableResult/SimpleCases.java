import fi.evident.dalesbred.Database;

public class SimpleCases {

    Database db;

    public void annotationType() {
        db.findAll(<warning descr="Class may not be an annotation type.">MyAnnotation.class</warning>, "select 42 from foo");
    }

    public void interfaceType() {
        db.findAll(<warning descr="Class may not be an interface.">MyInterface.class</warning>, "select 42 from foo");
    }

    public void nonStaticInnerClass() {
        db.findAll(<warning descr="Class may not be a non-static inner class.">NonStaticInnerClass.class</warning>, "select 42 from foo");
    }

    public class NonStaticInnerClass { }
}

@interface MyAnnotation { }

interface MyInterface { }
