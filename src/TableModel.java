import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableModel extends AbstractTableModel {

    private final List<String> rowKeys;
    private final Map<String, String> stringMap;  // First map (String values)
    private final Map<String, Double> doubleMap;  // Second map (Double values)

    // Adjust these headers to fit your actual data
    private final String[] columns = {"sequence", "Tool", "speed"};

    public TableModel(Map<String, String> stringMap, Map<String, Double> doubleMap) {
        this.stringMap = stringMap;
        this.doubleMap = doubleMap;

        // Gather all unique keys from BOTH maps, preserving insertion order
        Set<String> uniqueKeys = new LinkedHashSet<>(stringMap.keySet());
        uniqueKeys.addAll(doubleMap.keySet());

        this.rowKeys = new ArrayList<>(uniqueKeys);
    }

    @Override
    public int getRowCount() {
        return rowKeys.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    // This is the critical update:
    // Column 0 (Key) = String
    // Column 1 (String Map) = String
    // Column 2 (Double Map) = Double
    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 2) {
            return Double.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        String key = rowKeys.get(row);

        if (col == 0) {
            return key;
        }
        if (col == 1) {
            return stringMap.get(key);
        }
        if (col == 2) {
            return doubleMap.get(key);
        }

        return null;
    }
}