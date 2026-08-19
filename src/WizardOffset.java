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

public class WizardOffset extends JFrame{
    private JButton nextButton;
    private JPanel MainPanel;
    private JButton backButton;
    private JLabel EDValue;
    private JLabel EBValue;
    private JLabel ECValue;

    public WizardOffset(){
        Double EB = GCodeParser.getCurrentState().g178.get("EB");
        Double EC = GCodeParser.getCurrentState().g178.get("EC");
        Double ED = GCodeParser.getCurrentState().g178.get("ED");
        EBValue.setText(String.valueOf(EB));
        ECValue.setText(String.valueOf(EC));
        EDValue.setText(String.valueOf(ED));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame offsets = new JFrame("table selection");
                offsets.setContentPane(new WizardTableSelection().getMainPanel());
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
