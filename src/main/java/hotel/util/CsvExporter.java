package hotel.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Utility for exporting tables, reports, and data grids to CSV files.
 */
public final class CsvExporter {

    private CsvExporter() {}

    public static void exportTableToCsv(JTable table, File targetFile) throws IOException {
        TableModel model = table.getModel();
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            // Write column headers
            for (int col = 0; col < model.getColumnCount(); col++) {
                bw.write(escapeCsv(model.getColumnName(col)));
                if (col < model.getColumnCount() - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();

            // Write row data
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object val = model.getValueAt(row, col);
                    bw.write(escapeCsv(val != null ? val.toString() : ""));
                    if (col < model.getColumnCount() - 1) {
                        bw.write(",");
                    }
                }
                bw.newLine();
            }
        }
    }

    public static void exportDataToCsv(List<String> headers, List<List<String>> rows, File targetFile) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            // Headers
            for (int i = 0; i < headers.size(); i++) {
                bw.write(escapeCsv(headers.get(i)));
                if (i < headers.size() - 1) bw.write(",");
            }
            bw.newLine();

            // Rows
            for (List<String> row : rows) {
                for (int i = 0; i < row.size(); i++) {
                    bw.write(escapeCsv(row.get(i)));
                    if (i < row.size() - 1) bw.write(",");
                }
                bw.newLine();
            }
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String clean = value.replace("\"", "\"\"");
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\"") || clean.contains(";")) {
            return "\"" + clean + "\"";
        }
        return clean;
    }
}
