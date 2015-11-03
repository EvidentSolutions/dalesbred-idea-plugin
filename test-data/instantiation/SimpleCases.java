import org.dalesbred.Database;

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

    public void selectListWithStar() {
        db.findAll(String.class, <warning descr="Can't verify construction when select list contains '*'.">"select * from foo"</warning>);
        db.findUnique(String.class, <warning descr="Can't verify construction when select list contains '*'.">"select * from foo"</warning>);
        db.findUniqueOrNull(String.class, <warning descr="Can't verify construction when select list contains '*'.">"select * from foo"</warning>);
        db.findUniqueInt(<warning descr="Can't verify construction when select list contains '*'.">"select * from foo"</warning>);
        db.findUniqueLong(<warning descr="Can't verify construction when select list contains '*'.">"select * from foo"</warning>);
        db.findOptional(String.class, <warning descr="Can't verify construction when select list contains '*'.">"select * from foo"</warning>);
    }

    public void findUniquePrimitiveWithMultipleSelectValues() {
        db.findUniqueInt(<warning>"select x, y from foo"</warning>);
        db.findUniqueLong(<warning>"select x, y from foo"</warning>);
    }

    public class NonStaticInnerClass { }
}

@interface MyAnnotation { }

interface MyInterface { }

abstract class MyAbstractClass { }

class MyClassWithPrivateConstructor {
    private MyClassWithPrivateConstructor() { }
}
