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

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"JUnitTestMethodWithNoAssertions", "unchecked"})
public class DalesbredInstantiationInspectionTest extends InspectionTestCase {

    public void testSimpleCases() {
        verifyHighlighting("instantiation/SimpleCases.java");
    }

    public void testConstructorValidation() {
        verifyHighlighting("instantiation/ConstructorValidation.java");
    }

    public void testMapConstruction() {
        verifyHighlighting("instantiation/MapConstruction.java");
    }

    public void testUpdatesWithReturning() {
        verifyHighlighting("instantiation/UpdatesWithReturning.java");
    }

    public void testNonExactPropertyNames() {
        verifyHighlighting("instantiation/NonExactPropertyNames.java");
    }

    public void testEnumConstruction() {
        verifyHighlighting("instantiation/EnumConstruction.java");
    }

    public void testUninstantiatedProperties() {
        verifyHighlighting("instantiation/UninstantiatedProperties.java");
    }

    public void testIgnoredMembers() {
        verifyHighlighting("instantiation/IgnoredMembers.java");
    }

    public void testInstantiatorAnnotation() {
        verifyHighlighting("instantiation/InstantiatorAnnotation.java");
    }

    public void testSimpleCasesLegacy() {
        verifyHighlighting("instantiation/legacy/SimpleCases.java");
    }

    public void testConstructorValidationLegacy() {
        verifyHighlighting("instantiation/legacy/ConstructorValidation.java");
    }

    public void testMapConstructionLegacy() {
        verifyHighlighting("instantiation/legacy/MapConstruction.java");
    }

    public void testUpdatesWithReturningLegacy() {
        verifyHighlighting("instantiation/legacy/UpdatesWithReturning.java");
    }

    public void testNonExactPropertyNamesLegacy() {
        verifyHighlighting("instantiation/legacy/NonExactPropertyNames.java");
    }

    public void testEnumConstructionLegacy() {
        verifyHighlighting("instantiation/legacy/EnumConstruction.java");
    }

    public void testUninstantiatedPropertiesLegacy() {
        verifyHighlighting("instantiation/legacy/UninstantiatedProperties.java");
    }

    public void testIgnoredMembersLegacy() {
        verifyHighlighting("instantiation/legacy/IgnoredMembers.java");
    }

    private void verifyHighlighting(@NotNull String file) {
        myFixture.enableInspections(DalesbredInstantiationInspection.class);
        myFixture.testHighlighting(file);
    }
}
