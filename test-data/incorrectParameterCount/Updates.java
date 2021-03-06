import org.dalesbred.Database;
import org.dalesbred.query.SqlQuery;
import org.dalesbred.result.ResultSetProcessor;
import org.dalesbred.result.RowMapper;

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
