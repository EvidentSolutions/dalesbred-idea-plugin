import org.dalesbred.Database;

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
        db.findMap(String.class, <warning>OneParameterClass.class</warning>, "select foo from baz");
        db.findMap(String.class, <warning>OneParameterClass.class</warning>, "select foo, bar, baz from foobar");
    }


    public void valueWithMultipleParameters() {
        db.findMap(String.class, ThreeParameterClass.class, "select foo, bar, baz, quux from foobar");
        db.findMap(String.class, <warning>ThreeParameterClass.class</warning>, "select foo, bar from baz");
    }

    public static class OneParameterClass {
        public OneParameterClass(int x) { }
    }

    public static class ThreeParameterClass {
        public ThreeParameterClass(int x, int y, int z) { }
    }
}
