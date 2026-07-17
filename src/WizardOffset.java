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
                JFrame Wizzard = new JFrame("Searching Program");
                Wizzard.setContentPane(new WizardFile());
                Wizzard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setSize(700, 600);
                Wizzard.setLocationRelativeTo(null);
                Wizzard.setVisible(true);
            }
        });
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame Wizzard = new JFrame("Tools");
                Wizzard.setContentPane(new WizardTool().getMainPanel());
                Wizzard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                Wizzard.setSize(700, 600);
                Wizzard.setLocationRelativeTo(null);
                Wizzard.setVisible(true);
            }
        });
    }

    public JPanel getMainPanel() {
        return this.MainPanel;
    }
}
