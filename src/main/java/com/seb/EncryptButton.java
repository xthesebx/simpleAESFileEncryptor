package com.seb;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class EncryptButton extends JButton implements ActionListener {

    private final Main main;

    public EncryptButton(Main main) {
        super("Encrypt");
        this.main = main;
        this.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        File selectedFile = main.fileChooser.getSelectedFile();
        if (selectedFile != null) {
            if (selectedFile.isDirectory()) {
                try {
                    Main.encryptDir(main.algorithm, Main.getKeyFromPassword(main.passwordField.getText(), main.salt), selectedFile);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    Main.encryptFile(main.algorithm, Main.getKeyFromPassword(main.passwordField.getText(), main.salt), selectedFile, new File(selectedFile.getAbsolutePath() + "encrypted"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        main.passwordField.setText("");
    }
}
