import fi.evident.dalesbred.Database;

import java.lang.String;

public class NonExactPropertyNames {

    Database db;

    public void fieldNamesDifferByCase() {
        db.findAll(ExampleClassWithFields.class, "select FooBar from foo");
        db.findAll(ExampleClassWithFields.class, "select foobar from foo");
        db.findAll(ExampleClassWithFields.class, "select FOOBAR from foo");
        db.findAll(ExampleClassWithFields.class, "select fooBAR from foo");
        db.findAll(InheritedClassWithFields.class, "select FooBar from foo");
    }

    public void setterNamesDifferByCase() {
        db.findAll(ExampleClassWithSetter.class, "select FooBar from foo");
        db.findAll(ExampleClassWithSetter.class, "select foobar from foo");
        db.findAll(ExampleClassWithSetter.class, "select FOOBAR from foo");
        db.findAll(ExampleClassWithSetter.class, "select fooBAR from foo");
        db.findAll(InheritedClassWithSetter.class, "select fooBAR from foo");
    }

    public void mappingToFieldsUsingColumnsWithUnderscores() {
        db.findAll(ExampleClassWithFields.class, "select Foo_Bar from foo");
        db.findAll(ExampleClassWithFields.class, "select foo_bar from foo");
        db.findAll(InheritedClassWithFields.class, "select Foo_Bar from foo");
    }

    public void mappingToSettersUsingColumnsWithUnderscores() {
        db.findAll(ExampleClassWithSetter.class, "select Foo_Bar from foo");
        db.findAll(ExampleClassWithSetter.class, "select foo_bar from foo");
        db.findAll(InheritedClassWithSetter.class, "select foo_BAR from foo");
    }
}

class ExampleClassWithFields {
    public String fooBar;
}

class InheritedClassWithFields extends ExampleClassWithFields {
}

class ExampleClassWithSetter {
    public void setFooBar(String s) { }
}

class InheritedClassWithSetter extends ExampleClassWithSetter {
}
