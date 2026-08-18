import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.*;

public class TableModel extends AbstractTableModel {

    private final List<String> displayNCodes = new ArrayList<>();
    private final List<String> displayTools = new ArrayList<>();
    private final List<Integer> displayTs = new ArrayList<>();
    private final List<Double> displaySpeeds = new ArrayList<>();
    private final List<String> displayTKeys = new ArrayList<>();
    private final List<File> displayTFiles = new ArrayList<>();

    private final String[] columns = {"Sequence / N-Code", "Tool Used", "Tool Index (Editable)", "Speed / E30050"};

    public TableModel(Map<String, String> toolMap, Map<String, Double> tMap, Map<String, Double> e30050Map) {
        Set<String> allKeys = new HashSet<>();
        if (toolMap != null) allKeys.addAll(toolMap.keySet());
        if (tMap != null) allKeys.addAll(tMap.keySet());
        if (e30050Map != null) allKeys.addAll(e30050Map.keySet());

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

        double lastKnownSpeed = 0.0;
        int lastKnownT = 0;
        String lastKnownTKey = "";
        File lastKnownFile = null;

        for (String key : sortedKeys) {
            if (e30050Map != null && e30050Map.containsKey(key)) {
                Double val = e30050Map.get(key);
                if (val != null) lastKnownSpeed = val;
            }

            if (tMap != null && tMap.containsKey(key)) {
                Double tVal = tMap.get(key);
                if (tVal != null) {
                    lastKnownT = (int) tVal.doubleValue();
                    lastKnownTKey = key;
                    if (GCodeParser.fileMap != null) {
                        lastKnownFile = GCodeParser.fileMap.get(key);
                    }
                }
            }

            if (toolMap != null && toolMap.containsKey(key)) {
                displayNCodes.add(key);
                displayTools.add(toolMap.get(key));
                displayTs.add(lastKnownT);
                displaySpeeds.add(lastKnownSpeed);
                displayTKeys.add(lastKnownTKey);
                displayTFiles.add(lastKnownFile);
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
        if (col == 2) return Integer.class;
        if (col == 3) return Double.class;
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0: return displayNCodes.get(rowIndex);
            case 1: return displayTools.get(rowIndex);
            case 2: return displayTs.get(rowIndex);
            case 3: return displaySpeeds.get(rowIndex);
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 2) {
            String inputStr = aValue.toString().trim();

            if (!inputStr.matches("\\d{3}")) {
                System.err.println("Invalid input: Please enter a numeric value up to 3 digits.");
                return;
            }

            try {
                int previousTValue = displayTs.get(rowIndex);
                int newTValue = Integer.parseInt(inputStr);

                if (newTValue == previousTValue) return;

                // Get the exact T-Block Key for this row
                String tBlockKey = displayTKeys.get(rowIndex);
                if (tBlockKey == null || tBlockKey.isEmpty()) return;

                // 1. UPDATE ALL ROWS IN THE TABLE THAT SHARE THIS SAME T-BLOCK KEY
                for (int i = 0; i < displayTKeys.size(); i++) {
                    if (displayTKeys.get(i).equals(tBlockKey)) {
                        displayTs.set(i, newTValue);
                    }
                }

                // 2. Update the global parser backend state
                GCodeParser.currentState.T.put(tBlockKey, (double) newTValue);

                // 3. Tell Swing to redraw the entire table so all affected rows update visually
                fireTableDataChanged();

                // 4. In-Place File Overwrite (Updates the physical file once)
                File currentFile = displayTFiles.get(rowIndex);
                if (currentFile != null) {
                    String oldTStr = String.format("%03d", previousTValue);
                    String newTStr = String.format("%03d", newTValue);

                    System.out.println("Editing File: " + currentFile.getAbsolutePath());
                    System.out.println("Target T-Block: " + tBlockKey + " | Changing T" + oldTStr + " -> T" + newTStr);

                    try (RandomAccessFile raf = new RandomAccessFile(currentFile, "rw")) {
                        String line;
                        long lineStartPointer = raf.getFilePointer();

                        while ((line = raf.readLine()) != null) {

                            if (line.matches("^\\s*" + tBlockKey + "\\b.*")) {
                                String updatedLine = line.replaceFirst("\\bT\\s*=?\\s*" + oldTStr + "\\b", "T" + newTStr);

                                if (!line.equals(updatedLine)) {
                                    raf.seek(lineStartPointer);
                                    raf.writeBytes(updatedLine);
                                    System.out.println("Successfully updated file in-place: " + updatedLine);
                                }
                                break;
                            }
                            lineStartPointer = raf.getFilePointer();
                        }
                    } catch (IOException er) {
                        System.err.println("File write error: " + er);
                    }
                }

            } catch (NumberFormatException e) {
                System.err.println("Invalid input: Please enter a valid number for the Tool Index.");
            }
        }
    }
}