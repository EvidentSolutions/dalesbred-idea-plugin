/*
 * Copyright (c) 2013 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.evident.dalesbred.plugin.idea.inspections;

@SuppressWarnings({"JUnitTestMethodWithNoAssertions", "unchecked"})
public class DalesbredInstantiationInspectionTest extends InspectionTestCase {

    public void testSimpleCases() {
        myFixture.enableInspections(DalesbredInstantiationInspection.class);

        myFixture.testHighlighting("instantiation/SimpleCases.java");
    }

    public void testConstructorValidation() {
        myFixture.enableInspections(DalesbredInstantiationInspection.class);

        myFixture.testHighlighting("instantiation/ConstructorValidation.java");
    }

    public void testMapConstruction() {
        myFixture.enableInspections(DalesbredInstantiationInspection.class);

        myFixture.testHighlighting("instantiation/MapConstruction.java");
    }

    public void testUpdatesWithReturning() {
        myFixture.enableInspections(DalesbredInstantiationInspection.class);

        myFixture.testHighlighting("instantiation/UpdatesWithReturning.java");
    }

    public void testNonExactPropertyNames() {
        myFixture.enableInspections(DalesbredInstantiationInspection.class);

        myFixture.testHighlighting("instantiation/NonExactPropertyNames.java");
    }

    public void testEnumConstruction() {
        myFixture.enableInspections(DalesbredInstantiationInspection.class);

        myFixture.testHighlighting("instantiation/EnumConstruction.java");
    }
}
