import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.results.RowMapper;

import java.lang.Integer;
import java.lang.String;

public class SimpleCases {

    Database db;
    RowMapper rowMapper;

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

    public void findAllWithRowMapper() {
        db.findAll(rowMapper, "select * from foo where id=?", 4);
        db.findAll(rowMapper, <warning>"select * from foo where id=?"</warning>);
        db.findAll(rowMapper, <warning>"select * from foo where id=?"</warning>, 4, 4);
    }

    public void findMap() {
        db.findMap(String.class, String.class, "select a, b from foo where id=?", 4);
        db.findMap(String.class, String.class, <warning>"select a, b from foo where id=?"</warning>);
        db.findMap(String.class, String.class, <warning>"select a, b from foo where id=?"</warning>, 4, 4);
    }

    public void findUniqueClass() {
        db.findUnique(String.class, "select a from foo where id=?", 4);
        db.findUnique(String.class, <warning>"select a from foo where id=?"</warning>);
        db.findUnique(String.class, <warning>"select a from foo where id=?"</warning>, 4, 4);
    }

    public void findUniqueRowMapper() {
        db.findUnique(rowMapper, "select a from foo where id=?", 4);
        db.findUnique(rowMapper, <warning>"select a from foo where id=?"</warning>);
        db.findUnique(rowMapper, <warning>"select a from foo where id=?"</warning>, 4, 4);
    }

    public void findUniqueOrNullClass() {
        db.findUniqueOrNull(String.class, "select a from foo where id=?", 4);
        db.findUniqueOrNull(String.class, <warning>"select a from foo where id=?"</warning>);
        db.findUniqueOrNull(String.class, <warning>"select a from foo where id=?"</warning>, 4, 4);
    }

    public void findUniqueOrNullRowMapper() {
        db.findUniqueOrNull(rowMapper, "select a from foo where id=?", 4);
        db.findUniqueOrNull(rowMapper, <warning>"select a from foo where id=?"</warning>);
        db.findUniqueOrNull(rowMapper, <warning>"select a from foo where id=?"</warning>, 4, 4);
    }
}
