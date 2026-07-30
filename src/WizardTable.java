import javax.swing.*;

public class WizardTable extends JFrame {
    private JLabel TableValue;
    private JPanel MainPanel;

    public WizardTable(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(MainPanel);
        setSize(700, 600);
        setTitle("Searching Program");
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

        Double E80050 = GCodeParser.currentState.E80050.get();
        TableValue.setText(E80050);
    }
}
