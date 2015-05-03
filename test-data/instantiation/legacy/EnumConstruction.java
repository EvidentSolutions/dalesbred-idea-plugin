import fi.evident.dalesbred.Database;

import java.lang.String;

public class EnumConstruction {

    Database db;

    public void enumInstantiation() {
        db.findAll(Foo.class, "select type from foo");
        db.findUnique(Foo.class, "select type from foo");
    }
}

enum Foo {
    Foo, BAR
}
