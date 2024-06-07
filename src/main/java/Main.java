import Table.Processor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class Main {
    private static void showSuccessDialog (JFrame window, String outputPath) throws IOException {
        String[] options = {"Ver resultado", "Ok"};
        int result = JOptionPane.showOptionDialog(
                window,
                "O arquivo foi processado e \nos dados foram extraídos",
                "Sucesso",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if(result == JOptionPane.YES_OPTION)
            Desktop.getDesktop().open(new File(outputPath));
    }

    private static void showGUI () {
        JFrame window = new JFrame("SEFIP Parser");
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container container = window.getContentPane();

        FileSelector fileSelector = new FileSelector(window, true);
        TitledBorder inputTitle = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Selecione o arquivo");
        inputTitle.setTitleJustification(TitledBorder.LEFT);
        fileSelector.setBorder(inputTitle);

        JPanel formatPanel = new JPanel(new GridLayout(1, 2));
        JRadioButton csvRadio = new JRadioButton("Csv");
        JRadioButton jsonRadio = new JRadioButton("Json");
        csvRadio.setSelected(true);
        formatPanel.add(csvRadio);
        formatPanel.add(jsonRadio);

        ButtonGroup formatGroup = new ButtonGroup();
        formatGroup.add(csvRadio);
        formatGroup.add(jsonRadio);

        FileSelector folderSelector = new FileSelector(window, false);
        TitledBorder outputTitle = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Selecione onde salvar");
        outputTitle.setTitleJustification(TitledBorder.LEFT);
        folderSelector.setBorder(outputTitle);

        TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Selecione o formato de saída");
        title.setTitleJustification(TitledBorder.LEFT);
        formatPanel.setBorder(title);

        JButton extractBtn = new JButton("Extrair Dados");
        extractBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    extract(fileSelector.path, folderSelector.path, jsonRadio.isSelected());
                    showSuccessDialog(window, folderSelector.path);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(fileSelector);
        panel.add(formatPanel);
        panel.add(folderSelector);
        panel.add(extractBtn);
        container.add(panel);

        window.pack();
        window.setVisible(true);
    }

    private static void extractFile (String inputPath, String outputPath, Boolean toJson) throws IOException {
        Processor processor = new Processor(inputPath);
        processor.loadPdf();
        processor.parseTable();
        processor.saveFile(outputPath, toJson);
    }

    public static void extract (String inputPath, String outputPath, Boolean toJson) throws IOException {
        File inputFile = new File (inputPath);
        if (inputFile.isDirectory()) {
            for (File file: inputFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"))) {
                extractFile(file.getAbsolutePath(), outputPath, toJson);
            }
        } else {
            extractFile(inputPath, outputPath, toJson);
        }
    }

    public static void main (String[] args) {
        if (args.length < 2) {
            if (args.length == 1 && args[0].equals("-h"))
                System.err.println("Usage: java -jar pdf.jar file_path output_path csv|json");
            else
                showGUI();
        } else {
            try {
                extract(args[0], args[1], args[2].equals("json"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
