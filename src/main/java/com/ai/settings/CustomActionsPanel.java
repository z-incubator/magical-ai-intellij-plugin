
package com.ai.settings;

import com.ai.action.editor.ActionsUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.cellvalidators.*;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.*;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.ListTableModel;
import com.ai.settings.state.GeneralSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomActionsPanel implements Configurable {

    private Disposable myDisposable;
    private final ListTableModel<CustomAction> listTableModel = new ListTableModel<>() {
        @Override
        public void addRow() {
            addRow(new CustomAction("", ""));
        }
    };

    private final JBTable jbTable = new JBTable(listTableModel) {
        @Override
        public void editingCanceled(ChangeEvent e) {
            int row = getEditingRow();
            super.editingCanceled(e);
            if (row >= 0 && row < listTableModel.getRowCount() && StringUtil.isEmpty(listTableModel.getRowValue(row).getName())) {
                listTableModel.removeRow(row);
            }
        }
    };

    public CustomActionsPanel() {
    }

    @Override
    public String getDisplayName() {
        return "Custom Actions";
    }

    @Override
    public @Nullable JComponent createComponent() {
        myDisposable = Disposer.newDisposable();
        listTableModel.setColumnInfos(new ColumnInfo[] { new ColumnInfo<CustomAction, String>("Name") {

            @Override
            public @Nullable String valueOf(CustomAction action) {
                return action.getName();
            }

            @Override
            public boolean isCellEditable(CustomAction action) {
                return true;
            }

            @Override
            public void setValue(CustomAction action, String value) {
                action.setName(value);
                getSelected(action, value);
            }
        }, new ColumnInfo<CustomAction, String>("Command") {

            @Override
            public @Nullable String valueOf(CustomAction customAction) {
                return customAction.getCommand();
            }

            @Override
            public boolean isCellEditable(CustomAction customAction) {
                return true;
            }

            @Override
            public void setValue(CustomAction customAction, String value) {
                customAction.setCommand(value);
                getSelected(customAction, value);
            }
        }});
        jbTable.getColumnModel().setColumnMargin(0);
        jbTable.setShowColumns(true);
        jbTable.setShowGrid(false);
        jbTable.getEmptyText().setText("No prefix configured");
        jbTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jbTable.setToolTipText("Double click to edit");

        ExtendableTextField cellEditor = new ExtendableTextField();
        DefaultCellEditor editor = new StatefulValidatingCellEditor(cellEditor, myDisposable).
                withStateUpdater(vi -> ValidationUtils.setExtension(cellEditor, vi));
        editor.setClickCountToStart(2);
        jbTable.setDefaultEditor(Object.class, editor);

        jbTable.setDefaultRenderer(Object.class, new ValidatingTableCellRendererWrapper(new ColoredTableCellRenderer() {
            {
                setIpad(new JBInsets(0, 0, 0, 0));}

            @Override
            protected void customizeCellRenderer(@NotNull JTable table, @Nullable Object value, boolean selected, boolean hasFocus, int row, int column) {
                if (row >= 0 && row < listTableModel.getRowCount()) {
                    CustomAction action = listTableModel.getRowValue(row);
                    setForeground(selected ? table.getSelectionForeground() : table.getForeground());
                    setBackground(selected ? table.getSelectionBackground() : table.getBackground());
                    if (column == 0) {
                        append(action.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    } else {
                        append(action.getCommand(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    }
                    setToolTipText("Double click to edit");
                }
            }

            @Override
            protected SimpleTextAttributes modifyAttributes(SimpleTextAttributes attributes) {
                return attributes;
            }
        }).bindToEditorSize(cellEditor::getPreferredSize));

        return ToolbarDecorator.createDecorator(jbTable).disableUpDownActions().createPanel();
    }

    private void getSelected(CustomAction action, String value) {
        int row = jbTable.getSelectedRow();
        if (StringUtil.isEmpty(value) && row >= 0 && row < listTableModel.getRowCount()) {
            listTableModel.removeRow(row);
        }

        List<CustomAction> items = new ArrayList<>(listTableModel.getItems());
        if (row >= items.size()) {
            return;
        }
        items.set(row, action);
        listTableModel.setItems(items);
        listTableModel.fireTableCellUpdated(row, TableModelEvent.ALL_COLUMNS);

        jbTable.repaint();
    }

    @Override
    public boolean isModified() {
        List<CustomAction> actions = new ArrayList<>(listTableModel.getItems());
        return !GeneralSettings.getInstance().getCustomActionsPrefix().equals(actions);
    }

    @Override
    public void apply() {
        jbTable.editingStopped(null);

        List<CustomAction> list = GeneralSettings.getInstance().getCustomActionsPrefix();
        list.clear();
        list.addAll(listTableModel.getItems());
        ActionsUtil.refreshActions();
    }

    @Override
    public void reset() {
        List<CustomAction> prefix = new ArrayList<>(GeneralSettings.getInstance().getCustomActionsPrefix());
        listTableModel.setItems(prefix);
    }

    private void createUIComponents() {
        JPanel customActionsTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator tsUrl = new TitledSeparator("Custom Actions Settings");
        customActionsTitledBorderBox.add(tsUrl,BorderLayout.CENTER);
    }

    @Override
    public void disposeUIResources() {
        Disposer.dispose(myDisposable);
    }
}
