import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCodeParser {
    // Starea curenta a masinii "in timp real"
    public static MachineState currentState = new MachineState();
    public static MachineState getCurrentState(){return currentState;}

    // Istoricul salvarilor pe blocuri N (Ex: "N0090" -> Starea la acel moment)
    public static Map<String, MachineState> sequenceHistory = new LinkedHashMap<>();

    private static final Pattern CNC_PATTERN = Pattern.compile("^([A-Za-z]+)([-+]?[0-9]*\\.?[0-9]*)$");

    // ==========================================
    // 3. THE PARSER LOGIC
    // ==========================================
    public static void parseLine(String line, MachineState targetState) {

        boolean isG178Line = line.contains("G178");
        boolean isG151Line = line.contains("G151");

        // 5. DETECT SUBPROGRAM CALLS
        // Adjust this check based on how your files call subprograms (e.g., .num, CALL, M98)
        if (targetState == currentState && line.contains("CLS")) {  //targetState == currentState just to be sure if nested subprograms
            handleSubprogramCall(line);
            return;
        }
        
        String[] tokens = line.split("[\\s&]+");
        String currentNBlock = null;

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            // Handle Macros (E80050=34)
            if (token.contains("=")) {
                String[] parts = token.split("=");
                if (parts.length == 2) {
                    try {
                        targetState.machineVariables.put(parts[0].toUpperCase(), Double.parseDouble(parts[1]));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid macro: " + token);
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
                        }
                        else if (prefix.equals("T")) {
                            // Only add tool if it isn't already loaded to prevent duplicates
                            String tool = prefix + number;
                            if (!targetState.tools.contains(tool)) {
                                targetState.tools.add(tool);
                            }
                        }
                        else if (prefix.equals("G") || prefix.equals("M")) {
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
        }

        // LA FINALUL LINIEI: Daca am avut un bloc N, salvam un "snapshot" al starii
        if (currentNBlock != null && targetState == currentState) {
            sequenceHistory.put(currentNBlock, new MachineState(currentState));
        }
    }

    // ==========================================
    // 6. SUBPROGRAM HANDLER
    // ==========================================
    private static void handleSubprogramCall(String subprogramName) {
        System.out.println("Found subprogram call in line: " + subprogramName);

        // 1. You would extract the filename from the line (e.g., "PART2.NUM")

        Pattern pattern = Pattern.compile("([a-zA-Z0-9]+_TYP[0-9]+_[0-9]{2}[a-zA-Z]{2}\\.anc)");
        Matcher matcher = pattern.matcher(subprogramName);
        if (matcher.find()){
            String subprogram = matcher.group(1);
            MachineState subprogramState = new MachineState();
            try {
                BufferedReader buffer = new BufferedReader(new FileReader(subprogram));
                String line = "";
                Map<String, Object> atributes = new HashMap<>();
                while ((line = buffer.readLine()) != null) {
                    GCodeParser.parseLine(line, subprogramState);
                }
            }catch(IOException e){
                System.out.println("Subprogram parsing error");
            }
            currentState.compareWithSubprogram(subprogramState, subprogram);
        }
        // 2. You would open that file and read its header/initial lines.
        // 3. You create a TEMPORARY MachineState for that subprogram to see what it requires.
        // 4. Finally, compare it to the main program's current state:

        /* Pseudo-code example:
           MachineState subprogramInitialState = parseSubprogramHeader(fileName);
           currentState.compareWithSubprogram(subprogramInitialState, fileName);
        */
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
//  -Scoate numele sculelor din numele fisierelor