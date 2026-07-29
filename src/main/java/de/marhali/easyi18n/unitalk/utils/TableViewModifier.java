package de.marhali.easyi18n.unitalk.utils;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class TableViewModifier {

    public static void modify(JBTable table, JBScrollPane tableScrollPane) {
        tableScrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyColumnPercentWidths(table,  tableScrollPane);
            }
        });

        SwingUtilities.invokeLater(() -> applyColumnPercentWidths(table, tableScrollPane));
    }

    private static void applyColumnPercentWidths(JBTable table, JBScrollPane tableScrollPane) {
        if (table.getColumnModel().getColumnCount() == 0) {
            return;
        }

        int available = tableScrollPane.getViewport().getWidth();
        if (available <= 0) {
            return;
        }

        int colCount = table.getColumnModel().getColumnCount();
        int firstN = Math.min(4, colCount);

        int w25 = (int) Math.round(available * 0.23);

        for (int i = 0; i < firstN; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w25);
        }

        int remainingCols = colCount - firstN;
        if (remainingCols > 0) {
            int remainingWidth = Math.max(available - (w25 * firstN), 0);
            int per = Math.max(remainingWidth / remainingCols, 30); // мінімум, щоб не схлопувалось
            for (int i = firstN; i < colCount; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(per);
            }
        }

        table.doLayout();
    }
}
