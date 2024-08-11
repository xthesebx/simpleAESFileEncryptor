package com.seb;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

public class DecryptButton extends JButton implements ActionListener {

    private final Main main;

    public DecryptButton(Main main) {
        super("Decrypt");
        this.main = main;
        this.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        File selectedFile = main.fileChooser.getSelectedFile();
        if (selectedFile != null) {
            if (selectedFile.isDirectory()) {
                try {
                    Main.decryptDir(main.algorithm, Main.getKeyFromPassword(Arrays.toString(main.passwordField.getPassword()), main.salt), selectedFile);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    Main.decryptFile(main.algorithm, Main.getKeyFromPassword(Arrays.toString(main.passwordField.getPassword()), main.salt), selectedFile, new File(selectedFile.getAbsolutePath().replace("encrypted","")));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        main.passwordField.setText("");
    }
}
