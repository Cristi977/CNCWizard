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
                String directory = "C:\\Users\\boito\\OneDrive\\Documente\\faculta\\practica\\prog"; //Change
                File file = SearchExplorer.findFile(directory, productCode + "_" + "TYP" + productType +".num");
                if (file == null){
                    errorLabel.setText("File not found");
                    return;
                }
                System.out.println(file);
                GCodeParser.activeProgramFile = file;
                GCodeParser.clearMemory();
                    //Read file
                try {
                    BufferedReader buffer = new BufferedReader(new FileReader(file));
                    String line;
                    Map<String, Object> atributes = new HashMap<>();
                    while ((line =buffer.readLine()) != null) {
                        GCodeParser.parseLine(line, GCodeParser.currentState);
                        GCodeParser.activeProgramFile = file;
                        GCodeParser.baseDirectory = file.getParentFile();
                    }
                } catch (IOException er) {
                    System.err.println(er);
                }
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
        });
    }
    public JPanel getMainPanel() {
        return this.MainPanel;
    }
}
