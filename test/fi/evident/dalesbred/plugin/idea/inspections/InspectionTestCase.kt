/*
 * Copyright (c) 2016 Evident Solutions Oy
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

import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.LanguageLevelModuleExtension
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import org.junit.Assert

abstract class InspectionTestCase : LightCodeInsightFixtureTestCase() {

    override fun getTestDataPath() = "test-data"

    override fun getProjectDescriptor() = PROJECT_WITH_DALESBRED

    companion object {

        private val PROJECT_WITH_DALESBRED = object : DefaultLightProjectDescriptor() {
            override fun configureModule(module: Module, model: ModifiableRootModel, contentEntry: ContentEntry) {
                super.configureModule(module, model, contentEntry)

                model.getModuleExtension(LanguageLevelModuleExtension::class.java).languageLevel = LanguageLevel.JDK_1_8

                val library = model.moduleLibraryTable.createLibrary("dalesbred")

                val libraryModel = library.modifiableModel
                libraryModel.addJar("libs/dalesbred-0.8.0.jar")
                libraryModel.addJar("libs/dalesbred-1.2.2.jar")
                libraryModel.commit()
            }

            override fun getSdk() = JavaSdk.getInstance().createJdk("Java", System.getProperty("java.home"))
        }

        private fun Library.ModifiableModel.addJar(jarPath: String) {
            val jar = JarFileSystem.getInstance().refreshAndFindFileByPath("$jarPath!/")
            if (jar != null) {
                addRoot(jar, OrderRootType.CLASSES)

            } else {
                Assert.fail("could not find dalesbred jar at $jarPath")
            }
        }
    }
}
