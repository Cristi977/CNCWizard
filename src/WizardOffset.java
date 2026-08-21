/*
 * WizardOffset.java - Handles CNC program's offset macros
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
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class WizardOffset extends JFrame{
    private JButton nextButton;
    private JPanel MainPanel;
    private JButton backButton;
    private JTextField EDValue;
    private JTextField EBValue;
    private JTextField ECValue;
    int newEB=0;
    int newEC=0;
    int newED=0;

    public WizardOffset(){
        double EB = GCodeParser.getCurrentState().g178.get("EB");
        double EC = GCodeParser.getCurrentState().g178.get("EC");
        double ED = GCodeParser.getCurrentState().g178.get("ED");
        EBValue.setText(String.valueOf((int)EB));
        ECValue.setText(String.valueOf((int)EC));
        EDValue.setText(String.valueOf((int)ED));

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame offsets = new JFrame("Searching Progra");
                offsets.setContentPane(new WizardFile().getMainPanel());
                offsets.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                offsets.setSize(700, 600);
                offsets.setLocationRelativeTo(null);
                offsets.setVisible(true);

                // 2. Close the current window
                // We use SwingUtilities to find the JFrame that holds this panel
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(MainPanel);
                currentFrame.dispose();
            }
        });
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newEB = Integer.parseInt(EBValue.getText());
                newEC = Integer.parseInt(ECValue.getText());
                newED = Integer.parseInt(EDValue.getText());

                //updating the main file
                if (GCodeParser.activeProgramFile == null || !GCodeParser.activeProgramFile.exists()) {
                    System.err.println("Active program file is not set or doesn't exist.");
                    return;
                }

                try {
                    java.nio.file.Path path = GCodeParser.activeProgramFile.toPath();
                    List<String> lines = java.nio.file.Files.readAllLines(path);
                    boolean modified = false;

                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);

                        // Look for the G178 block line
                        if (line.contains("G178")) {
                            String updatedLine = line;

                            // Safely replace EB, EC, and ED values regardless of their new length
                            // This matches words like EB10.5, EB-5, or EB100.25
                            updatedLine = updatedLine.replaceAll("EB\\s*[-+]?[0-9]*\\.?[0-9]+", "EB" + newEB);
                            updatedLine = updatedLine.replaceAll("EC\\s*[-+]?[0-9]*\\.?[0-9]+", "EC" + newEC);
                            updatedLine = updatedLine.replaceAll("ED\\s*[-+]?[0-9]*\\.?[0-9]+", "ED" + newED);

                            if (!line.equals(updatedLine)) {
                                lines.set(i, updatedLine);
                                modified = true;
                            }
                        }
                    }

                    // Write changes back to disk only if modifications were made
                    if (modified) {
                        java.nio.file.Files.write(path, lines);
                        System.out.println("Main file successfully updated with new G178 offsets.");
                    }

                } catch (java.io.IOException er) {
                    System.err.println("Error updating main G-code file: " + er.getMessage());
                }

                JFrame wizardFrame = new JFrame("Tools");

                // Instantiate WizardTool once so its constructor runs and populates the table
                WizardTool wizardTool = new WizardTool();

                wizardFrame.setContentPane(wizardTool.getMainPanel());
                wizardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                wizardFrame.setSize(700, 600);
                wizardFrame.setLocationRelativeTo(null);
                wizardFrame.setVisible(true);
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(MainPanel);
                currentFrame.dispose();
            }
        });
    }

    public JPanel getMainPanel() {
        return this.MainPanel;
    }
}
