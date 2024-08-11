package com.seb;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Objects;

public class Main {

    public final String salt = "SaltyBoy";
    public final String algorithm = "AES/CBC/PKCS5Padding";

    public final JPasswordField passwordField = new JPasswordField();
    public final JFileChooser fileChooser = new JFileChooser();
    public final JFrame fileChooserFrame = new JFrame();

    public static void main(String[] args) {
        new Main();
    }

    public Main () {
        JFrame frame = new JFrame();
        frame.setTitle("Encryptor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        frame.add(panel);
        BorderLayout borderLayout = new BorderLayout();
        panel.setLayout(borderLayout);
        passwordField.setToolTipText("Password");

        JButton chooseFileButton = new JButton("Choose File");
        panel.add(chooseFileButton, BorderLayout.NORTH);
        chooseFileButton.addActionListener(e -> {
            fileChooser.rescanCurrentDirectory();
            fileChooserFrame.setTitle("Choose File");
            fileChooserFrame.setSize(1000,500);
            fileChooserFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            fileChooserFrame.add(fileChooser);
            fileChooserFrame.setVisible(true);
        });
        JLabel currentFile = new JLabel("Current File: ");
        panel.add(currentFile, BorderLayout.CENTER);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.addActionListener(e -> {
            fileChooserFrame.dispose();
            System.out.println(e.getActionCommand());
            if (e.getActionCommand().equals("ApproveSelection"))
                currentFile.setText("Current File: " + fileChooser.getSelectedFile().getName());
        });
        fileChooser.setCurrentDirectory(new File("."));
        EncryptButton encryptButton = new EncryptButton(this);
        DecryptButton decryptButton = new DecryptButton(this);
        panel.add(decryptButton, BorderLayout.EAST);
        panel.add(encryptButton, BorderLayout.WEST);
        panel.add(passwordField, BorderLayout.SOUTH);
        frame.setSize(500,500);
        frame.setVisible(true);
    }

    public static void encryptDir(String algorithm, SecretKey key, File inputFile) throws Exception {
        inputFile.mkdirs();
        for (File file : Objects.requireNonNull(inputFile.listFiles())) {
            if (file.isDirectory()) {
                encryptDir(algorithm, key, file);
            }
            else {
                File outputFile = new File(inputFile.getName() + "/" + file.getName() + "encrypted");
                encryptFile(algorithm, key, file, outputFile);
            }
        }
        inputFile.delete();
    }

    public static void decryptDir(String algorithm, SecretKey key, File inputFile) throws Exception {
        inputFile.mkdirs();
        for (File file : Objects.requireNonNull(inputFile.listFiles())) {
            if (file.isDirectory()) decryptDir(algorithm, key, file);
            else {
                File outputFile = new File((inputFile.getName() + "/" + file.getName()).replace("encrypted", ""));
                decryptFile(algorithm, key, file, outputFile);
            }
        }
        inputFile.delete();
    }

    public static SecretKey getKeyFromPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public static IvParameterSpec generateIV() {
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static void encryptFile (String algorithm, SecretKey key, File inputFile, File outputFile) throws Exception {
        IvParameterSpec iv = generateIV();
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        FileInputStream fis = new FileInputStream(inputFile);
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(iv.getIV());
        byte[] buffer = new byte[64];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            byte[] cipherText = cipher.update(buffer, 0, bytesRead);
            if (cipherText != null) {
                fos.write(cipherText);
            }
        }
        byte[] output = cipher.doFinal();
        if (output != null) {
            fos.write(output);
        }
        fis.close();
        fos.close();
        inputFile.delete();
    }

    public static void decryptFile (String algorithm, SecretKey key, File inputFile, File outputFile) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        FileInputStream fis = new FileInputStream(inputFile);
        FileOutputStream fos = new FileOutputStream(outputFile);
        byte[] buffer = new byte[16];
        int bytesRead;
        fis.read(buffer);
        IvParameterSpec iv = new IvParameterSpec(buffer);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        while ((bytesRead = fis.read(buffer)) != -1) {
            byte[] cipherText = cipher.update(buffer, 0, bytesRead);
            if (cipherText != null) {
                fos.write(cipherText);
            }
        }
        try {
            byte[] output = cipher.doFinal();
            if (output != null) {
                fos.write(output);
            }
            fis.close();
            fos.close();
            if (!inputFile.getName().equals(outputFile.getName()))
                inputFile.delete();
        } catch (BadPaddingException e) {
            System.err.println(e);
            fis.close();
            fos.close();
            JDialog dia = new JDialog();
            dia.setTitle("Error");
            dia.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dia.add(new JLabel("Wrong password"));
            dia.setSize(500,250);
            dia.setModal(true);
            dia.setVisible(true);
            outputFile.delete();
        }
    }
}