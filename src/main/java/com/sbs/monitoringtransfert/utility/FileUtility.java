/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sbs.monitoringtransfert.utility;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.sbs.monitoringtransfert.monitor.MonitoringSendJsch;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.schmizz.sshj.sftp.SFTPClient;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author Tuxbe
 * @Date 21/07/2023
 */
public class FileUtility {

    private static final Logger LOG = Logger.getLogger(FileUtility.class.getName());


    public static boolean sendFileViaftpClient(FTPClient ftpClient, String filePath, String destinationDir) throws IOException {

        File file = new File(filePath);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            boolean success = ftpClient.storeFile(file.getName(), fileInputStream);
            if (success){
                LOG.log(Level.INFO, "Fichier "+file.getName()+" envoyé avec succès !");
                return true;
            }else {
                LOG.log(Level.INFO, "Fichier "+file.getName()+" n'a pas pu être envoyé !");
                return false;
            }

        }

    }

    public static boolean sendFileViaSftpJsch(ChannelSftp channelSftp, String filePath, String destinationDir) {

        try {

            File file = new File(filePath);
            channelSftp.put(filePath, destinationDir + file.getName());
            LOG.log(Level.INFO, "Envoi des fichiers terminé avec succès !");
        } catch (SftpException ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
            Logger.getLogger(MonitoringSendJsch.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;

    }

    public static boolean sendFileViaSftpClient(SFTPClient sftpClient, String filePath, String destinationDir) {

        try {

            File file = new File(filePath);
            sftpClient.put(filePath, destinationDir + file.getName());
            LOG.log(Level.INFO, "Envoi des fichiers terminé avec succès !");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
            Logger.getLogger(MonitoringSendJsch.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;

    }

    public static void archiveFile(String filePath, String archiveDirectory) {

        String filename = Paths.get(filePath).getFileName().toString();

        //Archivage du fichier transférer
        try {
            LOG.log(Level.INFO, "Archivage du fichier {0} en cours !", filename);
            Files.createDirectories(Paths.get(archiveDirectory + File.separator + LocalDate.now()));
            Path pathDest = Paths.get(archiveDirectory + File.separator + LocalDate.now() + File.separator + filename);
            Files.move(Paths.get(filePath), pathDest);
            LOG.log(Level.INFO, "Archivage du fichier  termin\u00e9 !");
        } catch (IOException ex) {
            renameFile(filePath);
            LOG.log(Level.SEVERE, "l'archivage du fichier a \u00e9chou\u00e9 !");
        }

    }

    public static void renameFile(String filePath) {
        File file = new File(filePath);
        File renamedFile = new File(file.getParent(), file.getName() + "#errorarchive");

        if (file.renameTo(renamedFile)) {
            LOG.log(Level.INFO, "Fichier renomm\u00e9 avec succ\u00e8s : {0}", renamedFile.getAbsolutePath());
        } else {
            LOG.log(Level.SEVERE, "\u00c9chec du renommage du fichier : {0}", file.getAbsolutePath());
        }
    }

    public static byte[] encrypt(String value, String secretKey, String initVector) throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        return cipher.doFinal(value.getBytes("UTF-8"));
    }

    public static String decrypt(byte[] encryptedData, String secretKey, String initVector) throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes, "UTF-8");
    }
}
