import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Map;

public class WizardTool {
    private JTable ToolTable;
    private JButton nextButton;
    private JButton backButton;
    private JPanel MainPanel;

    public WizardTool(){
        ToolTable.setModel(new TableModel(GCodeParser.getCurrentState().tools, GCodeParser.getCurrentState().E30050));

    }
    public JPanel getMainPanel() {
        return MainPanel;
    }
}
