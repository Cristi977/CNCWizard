/*
 * WizardTableSelection.java - Handles CNC's working table selection macros
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

public class WizardTableSelection extends JFrame{
    private JPanel MainPanel;
    private JLabel Table;
    private JButton backButton;
    private JButton nextButton;
    private JLabel ToolIndexValue;

    public WizardTableSelection(){
        /*Table.setText("Table: " + GCodeParser.currentState.getE80050FirstDigit());
        * ToolIndexValue.setText("Tool Index: " + GCodeParser.currentState.getE80050ToolIndex());
        */
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame offsets = new JFrame("Offset");
                offsets.setContentPane(new WizardOffset().getMainPanel());
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
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame offsets = new JFrame("Searching Program");
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
    }
    public JPanel getMainPanel() {
        return this.MainPanel;
    }
}
