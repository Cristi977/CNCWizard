import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class WizardTool extends JFrame {
    private JTable ToolTable;
    private JButton nextButton;
    private JButton backButton;
    private JPanel MainPanel;

    public WizardTool(){
        ToolTable.setModel(new TableModel(GCodeParser.getCurrentState().tools, GCodeParser.getCurrentState().E30050));

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame Wizzard = new JFrame("Offset");
                Wizzard.setContentPane(new WizardOffset().getMainPanel());
                Wizzard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                Wizzard.setSize(700, 600);
                Wizzard.setLocationRelativeTo(null);
                Wizzard.setVisible(true);
            }
        });
    }
    public JPanel getMainPanel() {
        return MainPanel;
    }
}
