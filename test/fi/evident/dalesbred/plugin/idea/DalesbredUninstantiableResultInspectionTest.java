package fi.evident.dalesbred.plugin.idea;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class DalesbredUninstantiableResultInspectionTest extends InspectionTestCase {

    public void testSimpleCases() {
        //noinspection unchecked
        myFixture.enableInspections(DalesbredUninstantiableResultInspection.class);

        myFixture.testHighlighting("uninstantiableResult/SimpleCases.java");
    }
}
