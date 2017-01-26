/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package fi.evident.dalesbred.plugin.idea.inspections

class DalesbredInstantiationInspectionTest : InspectionTestCase() {

    fun testSimpleCases() {
        verifyHighlighting("instantiation/SimpleCases.java")
    }

    fun testConstructorValidation() {
        verifyHighlighting("instantiation/ConstructorValidation.java")
    }

    fun testMapConstruction() {
        verifyHighlighting("instantiation/MapConstruction.java")
    }

    fun testUpdatesWithReturning() {
        verifyHighlighting("instantiation/UpdatesWithReturning.java")
    }

    fun testNonExactPropertyNames() {
        verifyHighlighting("instantiation/NonExactPropertyNames.java")
    }

    fun testEnumConstruction() {
        verifyHighlighting("instantiation/EnumConstruction.java")
    }

    fun testUninstantiatedProperties() {
        verifyHighlighting("instantiation/UninstantiatedProperties.java")
    }

    fun testIgnoredMembers() {
        verifyHighlighting("instantiation/IgnoredMembers.java")
    }

    fun testInstantiatorAnnotation() {
        verifyHighlighting("instantiation/InstantiatorAnnotation.java")
    }

    fun testDefaultInstantiable() {
        verifyHighlighting("instantiation/DefaultInstantiable.java")
    }

    fun testSimpleCasesLegacy() {
        verifyHighlighting("instantiation/legacy/SimpleCases.java")
    }

    fun testConstructorValidationLegacy() {
        verifyHighlighting("instantiation/legacy/ConstructorValidation.java")
    }

    fun testMapConstructionLegacy() {
        verifyHighlighting("instantiation/legacy/MapConstruction.java")
    }

    fun testUpdatesWithReturningLegacy() {
        verifyHighlighting("instantiation/legacy/UpdatesWithReturning.java")
    }

    fun testNonExactPropertyNamesLegacy() {
        verifyHighlighting("instantiation/legacy/NonExactPropertyNames.java")
    }

    fun testEnumConstructionLegacy() {
        verifyHighlighting("instantiation/legacy/EnumConstruction.java")
    }

    fun testUninstantiatedPropertiesLegacy() {
        verifyHighlighting("instantiation/legacy/UninstantiatedProperties.java")
    }

    fun testIgnoredMembersLegacy() {
        verifyHighlighting("instantiation/legacy/IgnoredMembers.java")
    }

    private fun verifyHighlighting(file: String) {
        myFixture.enableInspections(DalesbredInstantiationInspection::class.java)
        myFixture.testHighlighting(file)
    }
}
