import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                }
            }

            if (toolMap != null && toolMap.containsKey(key)) {
                // Since the subprogram is on the tool's N-block (key), fetch it here!
                File currentFile = null;
                if (GCodeParser.subprogramFiles != null) {
                    currentFile = GCodeParser.subprogramFiles.get(key);
                }

                displayNCodes.add(key);
                displayTools.add(toolMap.get(key));
                displayTs.add(lastKnownT);
                displaySpeeds.add(lastKnownSpeed);
                displayTKeys.add(lastKnownTKey);
                displayTFiles.add(currentFile);
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

                // Get the T-Block Key (used for updating table state & backend T values)
                String tBlockKey = displayTKeys.get(rowIndex);
                if (tBlockKey == null || tBlockKey.isEmpty()) return;
                if (GCodeParser.activeProgramFile != null && GCodeParser.activeProgramFile.exists()) {
                    try {
                        List<String> mainLines = java.nio.file.Files.readAllLines(GCodeParser.activeProgramFile.toPath());
                        boolean mainModified = false;

                        for (int i = 0; i < mainLines.size(); i++) {
                            String line = mainLines.get(i);

                            // Check if this line contains the correct N-block for this row
                            if (line.contains(tBlockKey)) {
                                System.out.println("FOUND N-BLOCK IN MAIN FILE: " + line);

                                // Update the T value or variable in the main file line
                                // (Adjust this replacement depending on how your main file stores the T value, e.g., T5 -> T100)
                                String updatedLine = line.replaceAll("T\\d+", "T" + newTValue);

                                mainLines.set(i, updatedLine);
                                mainModified = true;
                            }
                        }

                        if (mainModified) {
                            java.nio.file.Files.write(GCodeParser.activeProgramFile.toPath(), mainLines);
                            System.out.println("SUCCESSFULLY UPDATED MAIN FILE!");
                        }
                    } catch (IOException e) {
                        System.err.println("Error updating main file: " + e.getMessage());
                    }
                }

                // 1. UPDATE ALL TABLE ROWS THAT SHARE THIS SAME T-BLOCK KEY
                for (int i = 0; i < displayTKeys.size(); i++) {
                    if (displayTKeys.get(i).equals(tBlockKey)) {
                        displayTs.set(i, newTValue);
                    }
                }

                // 2. FETCH THE FILE DIRECTLY USING THE ROW'S PRE-MAPPED FILE
                File currentSubprogram = displayTFiles.get(rowIndex);

                if (currentSubprogram != null && currentSubprogram.exists()) {
                    System.out.println("Found subprogram file for row " + rowIndex + ": " + currentSubprogram.getAbsolutePath());

                    Pattern toolPattern = Pattern.compile("\\(T(\\d+)\\s+T<(\\d)(\\d+)>\\)");

                    try (RandomAccessFile raf = new RandomAccessFile(currentSubprogram, "rw")) {
                        String line;
                        long lineStart = 0;

                        while ((line = raf.readLine()) != null) {
                            long lineEnd = raf.getFilePointer();

                            Matcher matcher = toolPattern.matcher(line);
                            if (matcher.find()) {
                                System.out.println("FOUND MATCH in file: " + line);

                                String replacementBlock = "(T" + newTValue + " T<" + GCodeParser.currentState.E80050 + newTValue + ">)";

                                String newLine = line.substring(0, matcher.start()) +
                                        replacementBlock +
                                        line.substring(matcher.end());

                                if (newLine.length() == line.length()) {
                                    raf.seek(lineStart);
                                    raf.writeBytes(newLine);
                                    raf.seek(lineEnd);
                                    System.out.println("SUCCESSFULLY WROTE TO FILE!");
                                } else {
                                    System.err.println("BLOCKED BY LENGTH CHECK! Old length: " + line.length() + " | New length: " + newLine.length());
                                }
                            }
                            lineStart = lineEnd;
                        }
                    } catch (IOException er) {
                        System.err.println("File write error: " + er);
                    }
                } else {
                    if (currentSubprogram == null) {
                        System.err.println("ERROR: File is NULL for row " + rowIndex);
                    } else {
                        System.err.println("ERROR: File object exists, but physical file NOT FOUND at: " + currentSubprogram.getAbsolutePath());
                    }                }

                // 3. Update the global parser backend state
                GCodeParser.currentState.T.put(tBlockKey, (double) newTValue);

                // 4. Tell Swing to redraw the table visually
                fireTableDataChanged();

            } catch (NumberFormatException e) {
                System.err.println("Invalid input: Please enter a valid number for the Tool Index.");
            }
        }
    }
}