import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCodeParser {
    // Starea curenta a masinii "in timp real"
    public static MachineState currentState = new MachineState();
    public static MachineState getCurrentState(){return currentState;}
    public static File activeProgramFile;
    public static Map<String, File> subprogramFiles = new HashMap<>();
    public static File baseDirectory; // Add this

    // Istoricul salvarilor pe blocuri N (Ex: "N0090" -> Starea la acel moment)
    public static Map<String, MachineState> sequenceHistory = new LinkedHashMap<>();

    private static final Pattern CNC_PATTERN = Pattern.compile("^([A-Za-z]+)([-+]?[0-9]*\\.?[0-9]*)$");

    // ==========================================
    // 3. THE PARSER LOGIC
    // ==========================================
    public static void parseLine(String line, MachineState targetState) {

        boolean isG178Line = line.contains("G178");
        boolean isG151Line = line.contains("G151");

        String[] tokens = line.split("[\\s&]+");
        String currentNBlock = null;

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            // Handle Macros (E80050=34)
            if (token.contains("=")) {
                String[] parts = token.split("=");
                if (parts.length > 0){
                    if (parts[0].equals("E30050")){
                        currentState.E30050.put(currentNBlock, Double.parseDouble(parts[1]));
                    }
                    if (parts[0].equals("E80050")){
                        switch (parts[1]){
                            case "34": currentState.E80050="1";
                        }
                    }
                    if (parts.length == 2) {
                        try {
                            targetState.machineVariables.put(parts[0].toUpperCase(), Double.parseDouble(parts[1]));
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping invalid macro: " + token);
                        }
                    }
                }
            } else {
                Matcher matcher = CNC_PATTERN.matcher(token);
                if (matcher.matches()) {
                    try {
                        String prefix = matcher.group(1).toUpperCase();
                        double number = Double.parseDouble(matcher.group(2));
                        // Daca gasim un N-Code, inregistram blocul curent
                        if (prefix.equals("N")) {
                            currentNBlock = token.toUpperCase(); // ex: N0090
                        } else if (prefix.equals("T")) {
                            currentState.T.put(currentNBlock, number);
                        } else if (prefix.equals("G") || prefix.equals("M")) {
                            // (G and M could be stored in targetState if you want to track them)
                        }
                        else {
                            if (isG178Line) {
                                targetState.g178.put(prefix, number);
                            } else if (isG151Line) {
                                targetState.g151.put(prefix, number);
                            } else {
                                targetState.machineVariables.put(prefix, number);
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            // 5. DETECT SUBPROGRAM CALLS
            // Adjust this check based on how your files call subprograms (e.g., .num, CALL, M98)
            if (targetState == currentState && line.contains("CLS")) {  //targetState == currentState just to be sure if nested subprograms
                handleSubprogramCall(line, currentNBlock);
                return;
            }
        }

        // Save a snapshot at the end
        if (currentNBlock != null && targetState == currentState) {
            sequenceHistory.put(currentNBlock, new MachineState(currentState));
        }
    }

    // ==========================================
    // 6. SUBPROGRAM HANDLER
    // ==========================================
    private static void handleSubprogramCall(String subprogramName, String currentNblock) {
        System.out.println("Found subprogram call in line: " + subprogramName);

        Pattern namePattern = Pattern.compile("([a-zA-Z0-9]+_TYP[0-9]+_([0-9]{2})_((?:[A-Z]+_)?[A-Z0-9]+)\\.anc)");
        Matcher matcher = namePattern.matcher(subprogramName);
        if (matcher.find()){
            String subprogram = matcher.group(1);

            // --- FIX: Build the File path using the base directory ---
            File subFile;
            if (baseDirectory != null) {
                subFile = new File(baseDirectory, subprogram);
            } else {
                subFile = new File(subprogram);
            }

            subprogramFiles.put(currentNblock, subFile);   //.put overwrites if multiple values with the same key

            subprogramFiles.keySet().stream().sorted().forEach(blockKey -> {
                System.out.println("saved subprograms: " + blockKey +" "+ subprogramFiles.get(blockKey));
            });

            MachineState subprogramState = new MachineState();
            String subNumber = matcher.group(2);
            String tool = matcher.group(3);
            currentState.tools.put(currentNblock, tool);

            try {
                // Use subFile (which has the correct absolute path) instead of the string name
                BufferedReader buffer = new BufferedReader(new FileReader(subFile));
                String line = "";
                while ((line = buffer.readLine()) != null) {
                    GCodeParser.parseLine(line, subprogramState);
                }
            } catch(IOException e){
                System.out.println("Subprogram parsing error");
            }
            currentState.compareWithSubprogram(subprogramState, subprogram);
        }
    }


    public static void clearMemory() {
        // Empty the existing object's data instead of replacing the object itself
        currentState.machineVariables.clear();
        currentState.g178.clear();
        currentState.g151.clear();
        currentState.E30050.clear();
        currentState.tools.clear();

        // Clear the history map
        sequenceHistory.clear();
    }
}
//TO DO:
