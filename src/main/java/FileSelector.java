import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileSelector extends JPanel {
    public String path;

    FileSelector (JFrame window, Boolean open) {
        JFileChooser fileChooser = new JFileChooser();;
        if (open) {
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Files", "pdf");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setFileFilter(filter);
        } else {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }


        JTextField filePathInput = new JTextField(30);
        filePathInput.setEditable(false);
        JButton getFileBtn = new JButton("Buscar...");
        this.add(filePathInput);
        this.add(getFileBtn);

        getFileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int result = open ? fileChooser.showOpenDialog(window) : fileChooser.showSaveDialog(window);
                if (result == JFileChooser.APPROVE_OPTION) {
                    filePathInput.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    path = fileChooser.getSelectedFile().getAbsolutePath();
                }
            }
        });
    }
}
