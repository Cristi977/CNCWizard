import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/*
 * MachineState.java - Saves parsed macros macros
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
import java.util.Map;

public class MachineState {
    public Map<String, Double> machineVariables = new HashMap<>();
    public String E80050; //if only one value                        //Display as code (34,75,115) or as one int (0,1,2...) && Does parameter T bother me?
    public Map<String, Double> g178 = new HashMap<>();
    public Map<String, Double> g151 = new HashMap<>();              //checking memory addresses for overwriting G171
    public Map<String, Double> E30050 = new HashMap<>();
    public Map<String, String> tools = new HashMap<>();             //What you need for +1 spindles CNCs
    public Map<String, Double> T = new HashMap<>();
    public int machineVariable;

    // Constructor implicit (Blank state)
    public MachineState() {}

    // Constructor de copiere (Inherits the state from the previous block!)
    public MachineState(MachineState previousState) {
        this.machineVariables.putAll(previousState.machineVariables);
        this.g178.putAll(previousState.g178);
        this.g151.putAll(previousState.g151);
        this.E30050 = previousState.E30050;
        this.tools.putAll(previousState.tools);
    }

    // 4. METODA DE VERIFICARE A SUBPROGRAMELOR
    public void compareWithSubprogram(MachineState subprogramState, String subprogramName) {

        // Exemplu: Verificam daca G178 se potriveste
        if (!this.g178.equals(subprogramState.g178)) {
            System.out.println("WARNING: G178 parameters mismatch!");
            System.out.println(this.g178 + "|" + subprogramState.g151);
        }
        if (!this.g151.equals(subprogramState.g151)) {
            //System.err.println("WARNING: G178 parameters mismatch!");
        }
    }
}