package fi.evident.dalesbred.plugin.idea;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

public abstract class InspectionTestCase extends LightCodeInsightFixtureTestCase {

    private static final LightProjectDescriptor PROJECT_WITH_DALESBRED = new DefaultLightProjectDescriptor() {
        @Override
        public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
            super.configureModule(module, model, contentEntry);

            model.getModuleExtension(LanguageLevelModuleExtension.class).setLanguageLevel(LanguageLevel.JDK_1_7);

            Library library = model.getModuleLibraryTable().createLibrary("dalesbred");

            Library.ModifiableModel libraryModel = library.getModifiableModel();

            String userHome = System.getProperty("user.home");
            String jarPath = userHome + "/.m2/repository/fi/evident/dalesbred/dalesbred/0.5.0/dalesbred-0.5.0.jar";
            VirtualFile jar = JarFileSystem.getInstance().refreshAndFindFileByPath(jarPath + "!/");
            if (jar != null) {
                libraryModel.addRoot(jar, OrderRootType.CLASSES);
                libraryModel.commit();
            } else {
                Assert.fail("could not find dalesbred jar at " + jarPath);
            }
        }

        @Override
        public Sdk getSdk() {
            return JavaSdk.getInstance().createJdk("Java", System.getProperty("java.home"));
        }
    };

    @Override
    protected String getTestDataPath() {
        return "test-data";
    }

    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return PROJECT_WITH_DALESBRED;
    }
}
