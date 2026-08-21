/*
 * TableModel.java - Defines the editable table's structure and handles changes when an edit is made
 *
 * Copyright 2026 Cristian Boitor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    private final List<String> displayTables = new ArrayList<>();

    private final String[] columns = {"Sequence / N-Code", "Tool Used", "Tool Index (Editable)", "Selected Table"};

    public TableModel(Map<String, String> toolMap, Map<String, Double> tMap, Map<String, Integer> e80050Map) {
        Set<String> allKeys = new HashSet<>();
        if (toolMap != null) allKeys.addAll(toolMap.keySet());
        if (tMap != null) allKeys.addAll(tMap.keySet());
        if (e80050Map != null) allKeys.addAll(e80050Map.keySet());

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
        String lastKnownTableIndex = "0";

        for (String key : sortedKeys) {
            if (e80050Map != null && e80050Map.containsKey(key)) {
                Integer val = e80050Map.get(key);
                if (val != null) lastKnownSpeed = val;
            }

            if (tMap != null && tMap.containsKey(key)) {
                Double tVal = tMap.get(key);
                if (tVal != null) {
                    lastKnownT = (int) tVal.doubleValue();
                    lastKnownTKey = key;
                }
            }
            if (e80050Map != null && e80050Map.containsKey(key)) {
                Integer val = e80050Map.get(key);
                if (val != null) {
                    char firstDigit = GCodeParser.currentState.getE80050FirstDigit(key);
                    lastKnownTableIndex = String.valueOf(firstDigit);
                }
            }

            if (toolMap != null && toolMap.containsKey(key)) {
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
                displayTables.add(lastKnownTableIndex);
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
        if (col == 1) return String.class;
        if (col == 2) return Integer.class;
        if (col == 3) return Integer.class;
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0: return displayNCodes.get(rowIndex);
            case 1: return displayTools.get(rowIndex);
            case 2: return displayTs.get(rowIndex);
            case 3: return displayTables.get(rowIndex);
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2 || columnIndex == 3;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String tBlockKey = displayTKeys.get(rowIndex);
        if (tBlockKey == null || tBlockKey.isEmpty()) return;

        if (columnIndex == 3) {
            String inputStr = aValue.toString().toUpperCase().trim();

            // Constrain to a single digit so length check doesn't fail
            if (!inputStr.matches("\\d")) {
                System.err.println("Invalid input: Please enter a single numeric digit for the table.");
                return;
            }

            try {
                String previousTValue = displayTables.get(rowIndex);
                if (inputStr.equals(previousTValue)) return;

                if (GCodeParser.activeProgramFile != null && GCodeParser.activeProgramFile.exists()) {
                    try {
                        List<String> mainLines = java.nio.file.Files.readAllLines(GCodeParser.activeProgramFile.toPath());
                        boolean mainModified = false;

                        // Safely format as 3 digits to avoid out-of-bounds errors on substring
                        int currentToolInt = displayTs.get(rowIndex);
                        int rawTableValue = 0;
                        switch (inputStr) {
                            case "0":
                                rawTableValue = 30;
                                break;
                            case "1":
                                rawTableValue = 70;
                                break;
                            case "2":
                                rawTableValue = 110;
                                break;
                            default:
                                return;
                        }
                        // Fetch tool index using your MachineState method
                        String tIdxStr = GCodeParser.currentState.getE80050ToolIndex(tBlockKey);
                        int toolIndexVal = (tIdxStr != null && !tIdxStr.isEmpty()) ? Integer.parseInt(tIdxStr) : displayTs.get(rowIndex);

                        // Calculate new E80050 value using the switch mapping
                        int newE80050Value = toolIndexVal + rawTableValue;

                        for (int i = 0; i < mainLines.size(); i++) {
                            String line = mainLines.get(i);

                            if (line.contains(tBlockKey)) {
                                System.out.println("FOUND N-BLOCK IN MAIN FILE: " + line);
                                // Replace only the E80050 assignment
                                String updatedLine = line.replaceAll("E80050\\s*=\\s*\\d+", "E80050=" + newE80050Value);

                                if (!line.equals(updatedLine)) {
                                    mainLines.set(i, updatedLine);
                                    mainModified = true;
                                }
                            }
                        }

                        if (mainModified) {
                            java.nio.file.Files.write(GCodeParser.activeProgramFile.toPath(), mainLines);
                            System.out.println("SUCCESSFULLY UPDATED E80050 IN MAIN FILE!");
                        }
                    } catch (IOException e) {
                        System.err.println("Error updating main file: " + e.getMessage());
                    }
                }

                Set<File> processedFiles = new HashSet<>();
                Pattern toolPattern = Pattern.compile("\\(T(\\d+)\\s+T<(\\d)(\\d+)>\\)");

                // 1. UPDATE ALL TABLE ROWS THAT SHARE THIS SAME T-BLOCK KEY & THEIR SUBPROGRAM FILES
                for (int i = 0; i < displayTKeys.size(); i++) {
                    if (displayTKeys.get(i).equals(tBlockKey)) {
                        // Update UI data
                        displayTables.set(i, inputStr);

                        // Update physical subprogram file
                        File currentSubprogram = displayTFiles.get(i);

                        if (currentSubprogram != null && currentSubprogram.exists()) {
                            // Only process the file if we haven't already updated it in this loop
                            if (processedFiles.add(currentSubprogram)) {
                                System.out.println("Updating related subprogram file for row " + i + ": " + currentSubprogram.getAbsolutePath());

                                try (RandomAccessFile raf = new RandomAccessFile(currentSubprogram, "rw")) {
                                    String line;
                                    long lineStart = 0;

                                    while ((line = raf.readLine()) != null) {
                                        long lineEnd = raf.getFilePointer();

                                        Matcher matcher = toolPattern.matcher(line);
                                        if (matcher.find()) {
                                            System.out.println("FOUND MATCH in file: " + line);

                                            String currentTool = matcher.group(1);
                                            String currentTail = matcher.group(3);

                                            // Replace ONLY the Table digit (Group 2) with inputStr
                                            String replacementBlock = "(T" + currentTool + " T<" + inputStr + currentTail + ">)";
                                            String newLine = line.substring(0, matcher.start()) +
                                                    replacementBlock +
                                                    line.substring(matcher.end());

                                            if (newLine.length() == line.length()) {
                                                raf.seek(lineStart);
                                                raf.writeBytes(newLine);
                                                raf.seek(lineEnd);
                                                System.out.println("SUCCESSFULLY WROTE TABLE INDEX TO FILE!");
                                            } else {
                                                System.err.println("BLOCKED BY LENGTH CHECK! Old length: " + line.length() + " | New length: " + newLine.length());
                                            }
                                        }
                                        lineStart = lineEnd;
                                    }
                                } catch (IOException er) {
                                    System.err.println("File write error: " + er);
                                }
                            }
                        } else {
                            if (currentSubprogram == null) {
                                System.err.println("ERROR: File is NULL for row " + i);
                            } else {
                                System.err.println("ERROR: File object exists, but physical file NOT FOUND at: " + currentSubprogram.getAbsolutePath());
                            }
                        }
                    }
                }

                fireTableDataChanged();

            } catch (Exception e) {
                System.err.println("Error processing Table Index: " + e.getMessage());
            }
        }

        if (columnIndex == 2) {
            String inputStr = aValue.toString().trim();

            if (!inputStr.matches("\\d{3}")) {
                System.err.println("Invalid input: Please enter a numeric value exactly 3 digits long.");
                return;
            }

            try {
                int previousTValue = displayTs.get(rowIndex);
                int newTValue = Integer.parseInt(inputStr);

                if (newTValue == previousTValue) return;

                // 1. UPDATE THE MAIN FILE ONCE
                if (GCodeParser.activeProgramFile != null && GCodeParser.activeProgramFile.exists()) {
                    try {
                        List<String> mainLines = java.nio.file.Files.readAllLines(GCodeParser.activeProgramFile.toPath());
                        boolean mainModified = false;

                        for (int i = 0; i < mainLines.size(); i++) {
                            String line = mainLines.get(i);

                            if (line.contains(tBlockKey)) {
                                System.out.println("FOUND N-BLOCK IN MAIN FILE: " + line);
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

                Set<File> processedFiles = new HashSet<>();
                Pattern toolPattern = Pattern.compile("\\(T(\\d+)\\s+T<(\\d)(\\d+)>\\)");

                // 2. UPDATE ALL TABLE ROWS THAT SHARE THIS SAME T-BLOCK KEY & THEIR SUBPROGRAM FILES
                for (int i = 0; i < displayTKeys.size(); i++) {
                    if (displayTKeys.get(i).equals(tBlockKey)) {
                        // Update UI data
                        displayTs.set(i, newTValue);

                        // Update physical subprogram file
                        File currentSubprogram = displayTFiles.get(i);

                        if (currentSubprogram != null && currentSubprogram.exists()) {
                            // Only process the file if we haven't already updated it in this loop
                            if (processedFiles.add(currentSubprogram)) {
                                System.out.println("Updating related subprogram file for row " + i + ": " + currentSubprogram.getAbsolutePath());

                                try (RandomAccessFile raf = new RandomAccessFile(currentSubprogram, "rw")) {
                                    String line;
                                    long lineStart = 0;

                                    while ((line = raf.readLine()) != null) {
                                        long lineEnd = raf.getFilePointer();

                                        Matcher matcher = toolPattern.matcher(line);
                                        if (matcher.find()) {
                                            System.out.println("FOUND MATCH in file: " + line);

                                            String zeroTValue = String.valueOf(newTValue).substring(1);
                                            // Get the EXISTING table digit directly from the matched file string
                                            String existingTableDigit = matcher.group(2);

                                            String replacementBlock = "(T" + newTValue + " T<" + existingTableDigit + "0" + zeroTValue + ">)";
                                            String newLine = line.substring(0, matcher.start()) +
                                                    replacementBlock +
                                                    line.substring(matcher.end());

                                            if (newLine.length() == line.length()) {
                                                raf.seek(lineStart);
                                                raf.writeBytes(newLine);
                                                raf.seek(lineEnd);
                                                System.out.println("SUCCESSFULLY WROTE TOOL INDEX TO FILE!");
                                            } else {
                                                System.err.println("BLOCKED BY LENGTH CHECK! Old length: " + line.length() + " | New length: " + newLine.length());
                                            }
                                        }
                                        lineStart = lineEnd;
                                    }
                                } catch (IOException er) {
                                    System.err.println("File write error: " + er);
                                }
                            }
                        } else {
                            if (currentSubprogram == null) {
                                System.err.println("ERROR: File is NULL for row " + i);
                            } else {
                                System.err.println("ERROR: File object exists, but physical file NOT FOUND at: " + currentSubprogram.getAbsolutePath());
                            }
                        }
                    }
                }

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