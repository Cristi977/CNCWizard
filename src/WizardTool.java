import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class WizardTool {
    private JTable ToolTable;
    private JButton nextButton;
    private JButton backButton;
    private JPanel MainPanel;
    private JScrollPane ScrollPanel;

    public WizardTool(){
        Map<String, String> tools = GCodeParser.getCurrentState().tools;
        Map<String, Double> e30050 = GCodeParser.getCurrentState().E30050;
        Map<String, Double> T = GCodeParser.getCurrentState().T;

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
    public JPanel getMainPanel() {
        return MainPanel;
    }
}
