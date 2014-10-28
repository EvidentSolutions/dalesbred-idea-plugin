import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.SqlQuery;
import fi.evident.dalesbred.results.ResultSetProcessor;
import fi.evident.dalesbred.results.RowMapper;

import java.lang.Integer;
import java.lang.String;

public class Updates {

    Database db;

    public void rightAmountOfParameters() {
        db.update("update foo set bar=4 where bar=3");
        db.update("update foo set bar=? where bar=?", 4, 3);
    }

    public void notEnoughParameters() {
        db.update(<warning descr="Expected 2 query parameters, but got 1.">"update foo set bar=? where bar=?"</warning>, 4);
    }

    public void tooManyParameters() {
        db.update(<warning descr="Expected 2 query parameters, but got 3.">"update foo set bar=? where bar=?"</warning>, 4, 3, 2);
    }
}
