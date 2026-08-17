import javax.swing.table.AbstractTableModel;
import java.util.*;

public class TableModel extends AbstractTableModel {

    private final List<String> displayNCodes = new ArrayList<>();
    private final List<String> displayTools = new ArrayList<>();
    private final List<Integer> displayTs = new ArrayList<>();     // Track T as integers
    private final List<Double> displaySpeeds = new ArrayList<>();

    private final String[] columns = {"Sequence / N-Code", "Tool Used", "Tool Index (Editable)", "Speed / E30050"};

    // Updated constructor taking all three maps
    public TableModel(Map<String, String> toolMap, Map<String, Double> tMap, Map<String, Double> e30050Map) {

        // 1. Collect keys from ALL maps so we don't miss lines that update modal states
        Set<String> allKeys = new HashSet<>();
        if (toolMap != null) allKeys.addAll(toolMap.keySet());
        if (tMap != null) allKeys.addAll(tMap.keySet());
        if (e30050Map != null) allKeys.addAll(e30050Map.keySet());

        // 2. Sort all combined keys chronologically
        List<String> sortedKeys = new ArrayList<>(allKeys);
        sortedKeys.sort((k1, k2) -> {
            try {
                int num1 = Integer.parseInt(k1.replaceAll("[^0-9]", ""));
                int num2 = Integer.parseInt(k2.replaceAll("[^0-9]", ""));
                return Integer.compare(num1, num2);
            } catch (NumberFormatException e) {
                return k1.compareTo(k2);
            }
        });

        // Track the running modal states as we go through the sequence
        double lastKnownSpeed = 0.0;
        int lastKnownT = 0;

        for (String key : sortedKeys) {

            // 3. Update modal speed if this line has a new E30050 value
            if (e30050Map != null && e30050Map.containsKey(key)) {
                Double val = e30050Map.get(key);
                if (val != null) {
                    lastKnownSpeed = val;
                }
            }

            // 4. Update modal T if this line has a new T value
            if (tMap != null && tMap.containsKey(key)) {
                Double tVal = tMap.get(key);
                if (tVal != null) {
                    lastKnownT = (int) tVal.doubleValue(); // Explicitly cast double to int
                }
            }

            // 5. If this N-code line specifies a tool, capture the row
            if (toolMap != null && toolMap.containsKey(key)) {
                displayNCodes.add(key);
                displayTools.add(toolMap.get(key));
                displayTs.add(lastKnownT);         // Save active T
                displaySpeeds.add(lastKnownSpeed); // Save active Speed
            }
        }
    }

    @Override
    public int getRowCount() {
        return displayNCodes.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 2) return Integer.class; // Column 2 is Tool Index (T)
        if (col == 3) return Double.class;  // Column 3 is Speed (E30050)
        return String.class;                // Columns 0 and 1 are Strings
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // Read directly from the perfectly aligned lists created in the constructor
        switch (columnIndex) {
            case 0:
                return displayNCodes.get(rowIndex);
            case 1:
                return displayTools.get(rowIndex);
            case 2:
                return displayTs.get(rowIndex);
            case 3:
                return displaySpeeds.get(rowIndex);
            default:
                return null;
        }
    }

    // ==========================================
    // NEW METHODS TO MAKE COLUMN 2 EDITABLE
    // ==========================================

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Allow editing only for the "Tool Index" column (index 2)
        return columnIndex == 2;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 2) {
            try {
                // 1. Parse the entered text into an Integer
                int newTValue = Integer.parseInt(aValue.toString());

                // 2. Update the visual table display list
                displayTs.set(rowIndex, newTValue);

                // 3. Update the global parser state so your backend knows about the change!
                // We convert it back to a Double because your tMap originally stores Doubles.
                String nBlockKey = displayNCodes.get(rowIndex);
                // Make sure to replace `tVariables` with the actual name of your T map in MachineState
                GCodeParser.currentState.T.put(nBlockKey, (double) newTValue);

                // 4. Tell Swing to redraw this specific cell
                fireTableCellUpdated(rowIndex, columnIndex);

                updateGCodeLine(nBlockKey, newTValue);

            } catch (NumberFormatException e) {
                System.err.println("Invalid input: Please enter a whole number for the Tool Index.");
            }
        }
    }
    public void updateGCodeLine(String NBlock, double newTValue){
        //update GCode but first get the file from WizardFile
    }
}