/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sbs.monitoringtransfert.utility;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.sbs.monitoringtransfert.monitor.FileSearchExample;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tuxbe
 */
public class FileUtility {

    private static final Logger LOG = Logger.getLogger(FileUtility.class.getName());

    public static boolean sendFileViaSftp(ChannelSftp channelSftp, String filePath, String destinationDir) {

        try {

            File file = new File(filePath);
            channelSftp.put(filePath, destinationDir + file.getName());
            LOG.log(Level.INFO,"Envoi des fichiers terminé avec succès !");
        } catch (SftpException ex) {
            LOG.log(Level.SEVERE,ex.getMessage());
            Logger.getLogger(FileSearchExample.class.getName()).log(Level.SEVERE, null, ex);
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
            LOG.log(Level.INFO,"Archivage du fichier  termin\u00e9 !");
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
}
