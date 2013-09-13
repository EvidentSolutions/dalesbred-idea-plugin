import fi.evident.dalesbred.Database;

public class SimpleCases {

    Database db;

    public void rightAmountOfParameters() {
        db.findAll(Integer.class, "select 42 from foo");
        db.findAll(Integer.class, "select 42 from foo where id=?", 4);
        db.findAll(Integer.class, "select 42 from foo where id=? and name=?", 42, "foo");
    }

    public void notEnoughParameters() {
        db.findAll(Integer.class, <warning descr="Expected 1 query parameters, but got 0.">"select 42 from foo id=?"</warning>);
        db.findAll(Integer.class, <warning descr="Expected 2 query parameters, but got 1.">"select 42 from foo where id=? and name=?"</warning>, 42);
    }

    public void tooManyParameters() {
        db.findAll(Integer.class, <warning descr="Expected 0 query parameters, but got 1.">"select 42 from foo"</warning>, 4);
        db.findAll(Integer.class, <warning descr="Expected 1 query parameters, but got 2.">"select 42 from foo where id=?"</warning>, 42, "foo");
        db.findAll(Integer.class, <warning descr="Expected 2 query parameters, but got 3.">"select 42 from foo where id=? and name=?"</warning>, 42, "foo", "bar");
    }
}
