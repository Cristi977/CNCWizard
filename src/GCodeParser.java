/*
 * GCodeParser.java - translates the CNC program into specific structures
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCodeParser {
    public static MachineState currentState = new MachineState();
    public static MachineState getCurrentState(){return currentState;}
    public static File activeProgramFile;
    public static Map<String, File> subprogramFiles = new HashMap<>();
    public static File baseDirectory;

    public static Map<String, MachineState> sequenceHistory = new LinkedHashMap<>();

    // STRICT INTEGER REGEX: No decimal points allowed
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(?<mvar>[A-Za-z]+[0-9]*)\\s*=\\s*(?<mval>[-+]?[0-9]+)|(?<wprefix>[A-Za-z]+)(?<wval>[-+]?[0-9]+)");

    public static void parseLine(String line, MachineState targetState) {
        boolean isG178Line = line.contains("G178");
        boolean isG151Line = line.contains("G151");

        // 1. Strip comments
        String cleanLine = line.replaceAll("\\(.*?\\)", "").trim();
        if (cleanLine.isEmpty()) return;

        String currentNBlock = null;

        // 2. Pre-scan for N-block
        Matcher nMatcher = Pattern.compile("N[0-9]+").matcher(cleanLine);
        if (nMatcher.find()) {
            currentNBlock = nMatcher.group().toUpperCase();
        }

        // 3. Match pure integer tokens
        Matcher matcher = TOKEN_PATTERN.matcher(cleanLine);
        while (matcher.find()) {

            // --- MACRO HANDLING (e.g., E80050=34) ---
            if (matcher.group("mvar") != null) {
                String mvar = matcher.group("mvar").toUpperCase();
                String mvalStr = matcher.group("mval");

                int val = Integer.parseInt(mvalStr);

                if (mvar.equals("E30050") && currentNBlock != null) {
                    currentState.E30050.put(currentNBlock, (double) val);
                }

                if (mvar.equals("E80050")&& currentNBlock != null) {
                    currentState.E80050.put(currentNBlock, val);
                    currentState.machineVariable = val;
                }

                targetState.machineVariables.put(mvar, (double) val);

                // --- STANDARD G-CODE WORD HANDLING (e.g., G01, X100, T5) ---
            } else if (matcher.group("wprefix") != null) {
                String prefix = matcher.group("wprefix").toUpperCase();
                int number = Integer.parseInt(matcher.group("wval"));

                if (prefix.equals("N")) {
                    // Handled by pre-scan above
                } else if (prefix.equals("T")) {
                    currentState.T.put(currentNBlock, (double) number);
                } else if (prefix.equals("G") || prefix.equals("M")) {
                    // Ignored or stored if needed
                } else {
                    if (isG178Line) {
                        targetState.g178.put(prefix, (double) number);
                    } else if (isG151Line) {
                        targetState.g151.put(prefix, (double) number);
                    } else {
                        targetState.machineVariables.put(prefix, (double) number);
                    }
                }
            }
        }

        // 4. Detect subprogram calls
        if (targetState == currentState && line.contains("CLS")) {
            handleSubprogramCall(line, currentNBlock);
            return;
        }

        // 5. Save snapshot
        if (currentNBlock != null && targetState == currentState) {
            sequenceHistory.put(currentNBlock, new MachineState(currentState));
        }
    }

    private static void handleSubprogramCall(String subprogramName, String currentNblock) {
        System.out.println("Found subprogram call in line: " + subprogramName);

        Pattern namePattern = Pattern.compile("([a-zA-Z0-9]+_TYP[0-9]+_([0-9]{2})_((?:[A-Z]+_)?[A-Z0-9]+)\\.anc)");
        Matcher matcher = namePattern.matcher(subprogramName);
        if (matcher.find()){
            String subprogram = matcher.group(1);

            File subFile;
            if (baseDirectory != null) {
                subFile = new File(baseDirectory, subprogram);
            } else {
                subFile = new File(subprogram);
            }

            subprogramFiles.put(currentNblock, subFile);

            subprogramFiles.keySet().stream().sorted().forEach(blockKey -> {
                System.out.println("saved subprograms: " + blockKey +" "+ subprogramFiles.get(blockKey));
            });

            MachineState subprogramState = new MachineState();
            String tool = matcher.group(3);
            currentState.tools.put(currentNblock, tool);

            try {
                BufferedReader buffer = new BufferedReader(new FileReader(subFile));
                String line = "";
                while ((line = buffer.readLine()) != null) {
                    GCodeParser.parseLine(line, subprogramState);
                }
                buffer.close();
            } catch(IOException e){
                System.out.println("Subprogram parsing error");
            }
            currentState.compareWithSubprogram(subprogramState, subprogram);
        }
    }
    public static void autoSyncToolIndices() {
        if (activeProgramFile == null || !activeProgramFile.exists()) {
            System.out.println("AutoSync Aborted: No active program file found.");
            return;
        }

        System.out.println("--- Starting AutoSync for E80050 and T commands ---");

        try {
            java.nio.file.Path path = activeProgramFile.toPath();
            List<String> lines = java.nio.file.Files.readAllLines(path);
            boolean modified = false;

            // Grab the maps directly from the current state (this matches what TableModel used)
            Map<String, Double> tMap = currentState.T;
            Map<String, Integer> e80050Map = currentState.E80050;

            if (tMap == null || tMap.isEmpty()) {
                System.out.println("AutoSync Warning: T map is empty or null. Did the file parse correctly?");
                return;
            }

            System.out.println("AutoSync: Found " + tMap.size() + " T-commands in memory to check.");

            // Loop through all sequence keys that have a T command
            for (String nBlockKey : tMap.keySet()) {

                // Check if this same sequence key also has an E80050 command
                if (e80050Map != null && e80050Map.containsKey(nBlockKey)) {

                    int currentTValue = tMap.get(nBlockKey).intValue();
                    int rawE80050 = e80050Map.get(nBlockKey);

                    // Modulo 100 gets the last 2 digits of the T command
                    int tToolIndex = currentTValue % 100;

                    // Determine table base value using your existing method
                    char firstDigit = currentState.getE80050FirstDigit(nBlockKey);
                    int tableBaseValue = 0;
                    switch (firstDigit) {
                        case '0': tableBaseValue = 30; break;
                        case '1': tableBaseValue = 70; break;
                        case '2': tableBaseValue = 110; break;
                    }

                    if (tableBaseValue > 0) {
                        int e80050ToolIndex = rawE80050 - tableBaseValue;

                        // If mismatch detected, correct it
                        if (e80050ToolIndex != tToolIndex) {
                            int reconstructedE80050 = tableBaseValue + tToolIndex;
                            System.out.println(">> MISMATCH IN " + nBlockKey + "! T=" + tToolIndex + ", E80050=" + e80050ToolIndex);
                            System.out.println(">> Autocorrecting file to: " + reconstructedE80050);

                            // Find the line in the file and replace it safely
                            for (int i = 0; i < lines.size(); i++) {
                                String line = lines.get(i);
                                if (line.contains(nBlockKey) && line.contains("E80050")) {
                                    String updatedLine = line.replaceAll("E80050\\s*=\\s*" + rawE80050, "E80050=" + reconstructedE80050);
                                    if (!line.equals(updatedLine)) {
                                        lines.set(i, updatedLine);
                                        modified = true;

                                        // Update backend memory instantly
                                        e80050Map.put(nBlockKey, reconstructedE80050);
                                    }
                                }
                            }
                        } else {
                            System.out.println("Block " + nBlockKey + " is perfectly synced. (T=" + tToolIndex + ")");
                        }
                    }
                }
            }

            if (modified) {
                java.nio.file.Files.write(path, lines);
                System.out.println("--- AutoSync Complete: File was modified and saved! ---");
            } else {
                System.out.println("--- AutoSync Complete: No mismatches found, file untouched. ---");
            }

        } catch (Exception e) {
            System.err.println("Error auto-syncing tools in parser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void clearMemory() {
        currentState.machineVariables.clear();
        currentState.g178.clear();
        currentState.g151.clear();
        currentState.E30050.clear();
        currentState.tools.clear();
        sequenceHistory.clear();
    }
}