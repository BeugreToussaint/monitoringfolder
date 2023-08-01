/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sbs.monitoringtransfert.archivage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tuxbe
 * @Date 21/07/2023
 */
public class ArchivageFile implements ArchivageInterface {

    private static final Logger LOG = Logger.getLogger(ArchivageFile.class.getName());

    @Override
    public void archiver(String source, String desDir) {
        
        String filename = Paths.get(source).getFileName().toString();
        
        //Archivage du fichier transférer
        try {
            LOG.log(Level.INFO, "Archivage du fichier {0} en cours !", filename);
            Files.createDirectories(Paths.get(desDir + File.separator + LocalDate.now()));
            Path pathDest = Paths.get(desDir + File.separator + LocalDate.now() + File.separator + filename);
            Files.move(Paths.get(source), pathDest);
            LOG.log(Level.INFO, "Archivage du fichier {0} termin\u00e9 !", filename);
        } catch (IOException ex) {
            try {
                Files.move(Paths.get(source), Paths.get(source + "#ERRORARCHIVAGE"));
            } catch (IOException ex1) {
                LOG.log(Level.SEVERE, "Erreur survenu lors du renommage du fichier {0}", filename);
            }
            LOG.log(Level.SEVERE, "l''archivage du fichier{0} a \u00e9chou\u00e9 !", filename);
            LOG.log(Level.SEVERE, ex.getMessage());
        }
    }

}
