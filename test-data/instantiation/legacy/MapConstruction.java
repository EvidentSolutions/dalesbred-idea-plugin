import fi.evident.dalesbred.Database;

import java.lang.String;

public class MapConstruction {

    Database db;

    public void correctConstruction() {
        db.findMap(String.class, String.class, "select foo, bar from baz");
    }

    public void starInSelectList() {
        db.findMap(String.class, String.class, <warning>"select * from baz"</warning>);
    }

    public void invalidSelectCount() {
        db.findMap(String.class, String.class, <warning>"select foo from baz"</warning>);
        db.findMap(String.class, String.class, <warning>"select foo, bar, baz from foobar"</warning>);
    }
}
