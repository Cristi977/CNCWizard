/*
 * WizardTool.java - Hosts the editable table from TableModel
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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class WizardTool {
    private JTable ToolTable;
    private JButton nextButton;
    private JButton backButton;
    private JPanel MainPanel;
    private JScrollPane ScrollPanel;
    private JLabel Logs;

    public static WizardTool activeInstance;

    public WizardTool(){
        Map<String, String> tools = GCodeParser.getCurrentState().tools;
        Map<String, Double> e30050 = GCodeParser.getCurrentState().E30050;
        Map<String, Double> T = GCodeParser.getCurrentState().T;

        activeInstance = this;

        if (ToolTable != null && tools != null) {
            ToolTable.setModel(new TableModel(tools, T, e30050));
        }
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame Wizzard = new JFrame("Offset");
                Wizzard.setContentPane(new WizardOffset().getMainPanel());
                Wizzard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                Wizzard.setSize(700, 600);
                Wizzard.setLocationRelativeTo(null);
                Wizzard.setVisible(true);
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(MainPanel);
                currentFrame.dispose();
            }
        });
    }
    public void updateToolLabel(String newText) {
        if (Logs != null) {
            Logs.setText(newText);
            Logs.setForeground(Color.BLACK);
        }
    }
    public void showError(String errorMessage) {
        if (Logs != null) {
            Logs.setText(errorMessage);
            Logs.setForeground(Color.RED); // Turns the text red
        }
    }
    public JPanel getMainPanel() {
        return MainPanel;
    }
}
