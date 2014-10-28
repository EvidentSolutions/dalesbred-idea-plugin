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
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vcs.ComparableComparator;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.SortedListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.List;

import static com.intellij.ui.SeparatorFactory.createSeparator;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class ClassList {
    private ClassList() {
    }

    @NotNull
    public static JPanel createClassesListControl(@NotNull final List<String> classNames, @NotNull String title) {
        final SortedListModel listModel = SortedListModel.create(new ComparableComparator<String>());
        listModel.addAll(classNames);

        listModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                contentsChanged(e);
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                contentsChanged(e);
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                classNames.clear();
                classNames.addAll(listModel.getItems());
            }
        });

        JList list = new JBList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createSeparator(title, null), BorderLayout.NORTH);
        panel.add(createListWithActions(list, listModel).createPanel(), BorderLayout.CENTER);
        return panel;
    }

    @NotNull
    private static ToolbarDecorator createListWithActions(@NotNull final JList list, @NotNull final SortedListModel listModel) {
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(list);
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton button) {
                Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(list));
                if (project == null)
                    project = ProjectManager.getInstance().getDefaultProject();
                TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                        .createWithInnerClassesScopeChooser("Class", GlobalSearchScope.allScope(project), ClassFilter.ALL, null);
                chooser.showDialog();
                PsiClass selected = chooser.getSelected();
                if (selected != null)
                    listModel.add(selected.getQualifiedName());
            }
        });
        decorator.setAddActionName("Add Class");
        decorator.disableUpDownActions();
        return decorator;
    }
}
