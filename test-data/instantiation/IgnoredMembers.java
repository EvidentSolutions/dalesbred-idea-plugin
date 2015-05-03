import org.dalesbred.Database;
import org.dalesbred.annotation.DalesbredIgnore;

import java.lang.String;

public class IgnoredMembers {

    Database db;

    public void ignoredFieldsAndSetters() {
        db.findUnique(ClassWithIgnoredProperty.class, "select usedField from foo");
    }
}

class ClassWithIgnoredProperty {

    public String usedField;

    @DalesbredIgnore
    public String ignoredField;

    @DalesbredIgnore
    public void setIgnoredSetter(String s) { }
}
