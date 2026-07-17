import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineState {
    public Map<String, Double> machineVariables = new HashMap<>();
    public Map<String, Double> g178 = new HashMap<>();
    public Map<String, Double> g151 = new HashMap<>();
    public Map<String, Double> E30050 = new HashMap<>();
    public List<String> tools = new ArrayList<>();

    // Constructor implicit (Blank state)
    public MachineState() {}

    // Constructor de copiere (Inherits the state from the previous block!)
    public MachineState(MachineState previousState) {
        this.machineVariables.putAll(previousState.machineVariables);
        this.g178.putAll(previousState.g178);
        this.g151.putAll(previousState.g151);
        this.E30050.putAll(previousState.E30050); // <--- YOU NEED THIS LINE
        this.tools.addAll(previousState.tools);
    }

    // 4. METODA DE VERIFICARE A SUBPROGRAMELOR
    public void compareWithSubprogram(MachineState subprogramState, String subprogramName) {

        // Exemplu: Verificam daca G178 se potriveste
        if (!this.g178.equals(subprogramState.g178)) {
            System.err.println("WARNING: G178 parameters mismatch!");
        }
        if (!this.g151.equals(subprogramState.g151)) {
            System.err.println("WARNING: G178 parameters mismatch!");
        }
        if (!this.tools.equals(subprogramState.tools)) {
            System.err.println("WARNING: Tools list mismatch!");
        }
    }
}