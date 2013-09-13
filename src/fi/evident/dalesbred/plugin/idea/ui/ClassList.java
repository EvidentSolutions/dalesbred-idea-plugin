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

package fi.evident.dalesbred.plugin.idea.ui;

import com.intellij.ide.DataManager;
import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public final class ClassList {
    private ClassList() {
    }

    public static JPanel createSpecialAnnotationsListControl(final List<String> list, String borderTitle) {
        final SortedListModel<String> listModel = new SortedListModel<String>(new Comparator<String>() {
            @Override
            public int compare(@NotNull String o1, @NotNull String o2) {
                return o1.compareTo(o2);
            }
        });
        final JList injectionList = new JBList(listModel);
        for (String s : list) {
            listModel.add(s);
        }
        injectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        injectionList.getModel().addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(@NotNull ListDataEvent e) {
                listChanged();
            }

            private void listChanged() {
                list.clear();
                for (int i = 0; i < listModel.getSize(); i++) {
                    list.add((String)listModel.getElementAt(i));
                }
            }

            @Override
            public void intervalRemoved(@NotNull ListDataEvent e) {
                listChanged();
            }

            @Override
            public void contentsChanged(@NotNull ListDataEvent e) {
                listChanged();
            }
        });

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(injectionList)
                .setAddAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        Project project = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(injectionList));
                        if (project == null) project = ProjectManager.getInstance().getDefaultProject();
                        TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                                .createWithInnerClassesScopeChooser("Class", GlobalSearchScope.allScope(project), new ClassFilter() {
                                    @Override
                                    public boolean isAccepted(PsiClass aClass) {
                                        return true;
                                    }
                                }, null);
                        chooser.showDialog();
                        PsiClass selected = chooser.getSelected();
                        if (selected != null) {
                            listModel.add(selected.getQualifiedName());
                        }
                    }
                }).setAddActionName("Add Class").disableUpDownActions();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(SeparatorFactory.createSeparator(borderTitle, null), BorderLayout.NORTH);
        panel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
        return panel;
    }
}
