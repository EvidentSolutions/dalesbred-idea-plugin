import fi.evident.dalesbred.Database;

public class FindPrimitive {

    Database db;

    public void findPrimitive() {
        db.findUnique(int.class, "select 22");
    }
}
