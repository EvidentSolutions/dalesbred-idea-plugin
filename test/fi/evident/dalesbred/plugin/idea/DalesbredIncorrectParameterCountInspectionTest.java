package fi.evident.dalesbred.plugin.idea;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class DalesbredIncorrectParameterCountInspectionTest extends InspectionTestCase {

    public void testSimpleCases() {
        //noinspection unchecked
        myFixture.enableInspections(DalesbredIncorrectParameterCountInspection.class);

        myFixture.testHighlighting("incorrectParameterCount/SimpleCases.java");
    }
}
