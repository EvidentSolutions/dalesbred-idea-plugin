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

package fi.evident.dalesbred.plugin.idea.ui

import com.intellij.ide.DataManager
import com.intellij.ide.util.ClassFilter
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vcs.ComparableComparator
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.SeparatorFactory.createSeparator
import com.intellij.ui.SortedListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import java.awt.BorderLayout
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

object ClassList {

    fun createClassesListControl(classNames: MutableList<String>, title: String): JPanel {
        val listModel: SortedListModel<String> = SortedListModel.create(ComparableComparator<String>())
        listModel.addAll(classNames)

        listModel.addListDataListener(object : ListDataListener {
            override fun intervalAdded(e: ListDataEvent) {
                contentsChanged(e)
            }

            override fun intervalRemoved(e: ListDataEvent) {
                contentsChanged(e)
            }

            override fun contentsChanged(e: ListDataEvent) {
                classNames.clear()
                classNames.addAll(listModel.items)
            }
        })

        val list: JList<*> = JBList(listModel)
        list.selectionMode = ListSelectionModel.SINGLE_INTERVAL_SELECTION

        val panel = JPanel(BorderLayout())
        panel.add(createSeparator(title, null), BorderLayout.NORTH)
        panel.add(createListWithActions(list, listModel).createPanel(), BorderLayout.CENTER)
        return panel
    }

    private fun createListWithActions(list: JList<*>, listModel: SortedListModel<String>): ToolbarDecorator {
        val decorator = ToolbarDecorator.createDecorator(list)
        decorator.setAddAction {
            var project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(list))
            if (project == null)
                project = ProjectManager.getInstance().defaultProject
            val chooser = TreeClassChooserFactory.getInstance(project).createWithInnerClassesScopeChooser("Class", GlobalSearchScope.allScope(project), ClassFilter.ALL, null)
            chooser.showDialog()
            val selected = chooser.selected
            if (selected != null)
                listModel.add(selected.qualifiedName)
        }
        decorator.setAddActionName("Add Class")
        decorator.disableUpDownActions()
        return decorator
    }
}
