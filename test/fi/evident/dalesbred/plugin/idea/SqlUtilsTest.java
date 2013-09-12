package fi.evident.dalesbred.plugin.idea;

import org.junit.Test;

import static fi.evident.dalesbred.plugin.idea.SqlUtils.countQueryParametersPlaceholders;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SqlUtilsTest {

    @Test
    public void countQueryPlaceHolders() {
       assertThat(countQueryParametersPlaceholders("select * from foo where x=4"), is(0));
       assertThat(countQueryParametersPlaceholders("select * from foo where x=?"), is(1));
       assertThat(countQueryParametersPlaceholders("select * from foo where x=? and y=?"), is(2));
       assertThat(countQueryParametersPlaceholders("select ? from foo where x=? and y=?"), is(3));
    }

    @Test
    public void questionMarksInsideLiteralsAreNotPlaceholders() {
        assertThat(countQueryParametersPlaceholders("select * from foo where x='foo?'"), is(0));
        assertThat(countQueryParametersPlaceholders("select * from foo where x='foo '' ?'"), is(0));
    }
}
