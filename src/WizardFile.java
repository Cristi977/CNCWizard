/*
 * WizardFile.java - Handles the selection of a speciffic CNC program
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class WizardFile extends JFrame {
    private JTextField productCodeTField;
    private JPanel MainPanel;
    private JButton OKButton;
    private JTextField productTypeTField;
    private JLabel errorLabel;

    public WizardFile(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(MainPanel);
        setSize(700, 600);
        setTitle("Searching Program");
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        OKButton.addActionListener(e -> {
                String productCode = productCodeTField.getText();
                String productType = productTypeTField.getText();
                    //Search file
                String directory = "C:\Users\user\prog"; //Change
                File file = SearchExplorer.findFile(directory, productCode + "_" + "TYP" + productType +".num");
                if (file == null){
                    errorLabel.setText("File not found");
                    return;
                }
                System.out.println(file);
                GCodeParser.activeProgramFile = file;
                GCodeParser.clearMemory();
                    //Read file
                try (BufferedReader buffer = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = buffer.readLine()) != null) {
                        GCodeParser.parseLine(line, GCodeParser.currentState);
                        GCodeParser.activeProgramFile = file;
                        GCodeParser.baseDirectory = file.getParentFile();
                    }
                } catch (IOException er) {
                    System.err.println(er);
                }
                GCodeParser.autoSyncToolIndices();

                JFrame offsets = new JFrame("Offsets");
                offsets.setContentPane(new WizardOffset().getMainPanel());
                offsets.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                offsets.setSize(700, 600);
                offsets.setLocationRelativeTo(null);
                offsets.setVisible(true);

                // 2. Close the current window
                // We use SwingUtilities to find the JFrame that holds this panel
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(MainPanel);
                currentFrame.dispose();
        });
    }
    public JPanel getMainPanel() {
        return this.MainPanel;
    }
}
