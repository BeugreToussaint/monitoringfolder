/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sbs.monitoringtransfert.monitor;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sbs.monitoringtransfert.config.ConfigProperties;
import com.sbs.monitoringtransfert.utility.FileUtility;
import com.sbs.monitoringtransfert.utility.logUtility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tuxbe
 */
public class FileSearchExample {

    private final static Properties CONFIG = ConfigProperties.loadConfig();

    static String[] directories = CONFIG.getProperty("monitor.listen.dir", "C:/envoie,C:/clearing/interface/sortie").split(",");

    static final String[] EXTENSIONS = CONFIG.getProperty("monitor.listen.file.extension", "C:/envoi").split(",");

    static List<String> extensionsStream = Arrays.asList(EXTENSIONS);

    static String archiveDirectory = CONFIG.getProperty("monitor.listen.archive.dir", "C:/archive");
    static String typeauth = CONFIG.getProperty("server.typeauth", "passwordtext"); // or use key-based authentication
    static String logsDir = CONFIG.getProperty("monitor.logs.file");
    static String host = CONFIG.getProperty("server.host", "localhost");
    static int port = Integer.parseInt(CONFIG.getProperty("server.port", "22"));
    static String username = CONFIG.getProperty("server.username", "tester");
    static String password = CONFIG.getProperty("server.pssword", "password"); // or use key-based authentication
    static String keyprivatepath = CONFIG.getProperty("server.keyprivatepath", "/.ssh/id_rsa");
    static String destinationDir = CONFIG.getProperty("server.destination.dir", "/");

    static int javalinport = Integer.parseInt(CONFIG.getProperty("javalin.port", "8085"));

    static boolean isStarting = false;
    private static final Logger LOG = Logger.getLogger(FileSearchExample.class.getName());

    public static void main(String[] args) {

        String logDirectory = logsDir; // Remplacez cela par le chemin souhaité pour le répertoire de logs
        logUtility.configureLogging(logDirectory);

        // Création d'un ExecutorService avec un pool de 5 threads
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        // Connexion SFTP
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {

            session = jsch.getSession(username, host, port);

            if (typeauth.equalsIgnoreCase("keyprivate")) {
                jsch.addIdentity(keyprivatepath);
            } else {
                session.setPassword(password);
            }

            session.setConfig("StrictHostKeyChecking", "no"); // Disable host key checking
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            LOG.log(Level.INFO, "Connexion sftp réussi !");

            while (true) {
                // Liste pour stocker les résultats des futures
                List<Future<List<String>>> futures = new ArrayList<>();
                if (channelSftp.isConnected()) {
                    // Parcours de chaque répertoire et soumission des tâches aux threads du pool
                    for (String directory : directories) {
                        FileSearchTask task = new FileSearchTask(directory, extensionsStream);
                        Future<List<String>> future = executorService.submit(task);
                        futures.add(future);
                    }

                    // Attente de la fin de toutes les tâches et récupération des résultats
                    for (Future<List<String>> future : futures) {
                        try {
                            List<String> files = future.get();

                            LOG.log(Level.INFO, "Fichiers trouv\u00e9s dans le r\u00e9pertoire: {0}", files);

                            for (String filePath : files) {
                                // Envoyer les fichiers via SFTP                            
                                boolean sentSuccessfully = FileUtility.sendFileViaSftp(channelSftp, filePath, destinationDir);
                                if (sentSuccessfully) {
                                    // Archiver le fichier une fois envoyé avec succès
                                    FileUtility.archiveFile(filePath, archiveDirectory);
                                } else {
                                    LOG.log(Level.INFO, "L''envoie du fichier : {0} a \u00e9chou\u00e9 !", filePath);
                                }
                            }

                        } catch (InterruptedException | ExecutionException e) {
                            LOG.log(Level.SEVERE, e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    // Attente avant de recommencer la boucle
                    try {
                        Thread.sleep(5000); // Attente de 5 secondes avant la prochaine boucle
                    } catch (InterruptedException e) {
                        LOG.log(Level.SEVERE, e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    break;
                }

            }
        } catch (JSchException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermer la connexion SFTP lorsque vous avez terminé l'envoi des fichiers
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

    }

}
