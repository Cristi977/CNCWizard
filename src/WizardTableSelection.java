import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WizardTableSelection extends JFrame{
    private JPanel MainPanel;
    private JLabel Table;
    private JLabel TableValue;
    private JButton backButton;
    private JButton nextButton;

    public WizardTableSelection(){
        int table = GCodeParser.getCurrentState().E80050;
        //aditional processing: switch(table): case
        switch (table){
            case 34: TableValue.setText("1");
        }
        // if u need 34, let: TableValue.setText(String.valueOf(table));
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
