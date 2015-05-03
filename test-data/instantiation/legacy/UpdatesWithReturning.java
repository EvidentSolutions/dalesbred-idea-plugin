import fi.evident.dalesbred.Database;

import java.lang.String;

public class UpdatesWithReturning {

    Database db;

    public void updateReturning() {
        db.findUniqueInt("insert into foo (x) values ('bar') returning id");
        db.findUniqueInt("update foo set x='bar' where y='baz' returning id");
        db.findUniqueInt(<warning>"update foo set x='bar' where y='baz'"</warning>);
        db.findUniqueInt(<warning>"update foo set x='bar' where y='baz' returning id, x"</warning>);
    }

    public void returningMultipleValues() {
        db.findUnique(ClassWithThreeParameters.class, "insert into foo (x) values ('bar') returning x, y, z");
        db.findUnique(<warning>ClassWithThreeParameters.class</warning>, "insert into foo (x) values ('bar') returning x");
        db.findUnique(<warning>ClassWithThreeParameters.class</warning>, "insert into foo (x) values ('bar') returning x, y, z, w");
    }
}

class ClassWithThreeParameters {
    public ClassWithThreeParameters(Object x, Object y, Object z) { }
}
