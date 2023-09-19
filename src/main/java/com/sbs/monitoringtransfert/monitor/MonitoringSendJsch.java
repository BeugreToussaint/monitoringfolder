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
import com.sbs.monitoringtransfert.utility.FileUtility;
import com.sbs.monitoringtransfert.utility.logUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tuxbe
 * @Date 21/07/2023
 */
public class MonitoringSendJsch implements MonitoringSendInterf{

    private static final Logger LOG = Logger.getLogger(MonitoringSendJsch.class.getName());

    @Override
    public void monitorStart(String logsDir, String[] directories, List<String> extensionsStream,
            String username, String password, int port, String host, String archiveDirectory, String destinationDir, ExecutorService executorService
    ) {
        if (executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(5);
        }

        String logDirectory = logsDir; // Remplacez cela par le chemin souhaité pour le répertoire de logs
        logUtility.configureLogging(logDirectory);

        while (true) {
            // Liste pour stocker les résultats des futures
            List<Future<List<String>>> futures = new ArrayList<>();
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

                    if (!files.isEmpty()) {
                        // Connexion SFTP
                        JSch jsch = new JSch();
                        Session session = null;
                        ChannelSftp channelSftp = null;
                        try {

                            session = jsch.getSession(username, host, port);

                            session.setPassword(password);
                            // Disable host key checking
                            session.setConfig("StrictHostKeyChecking", "no");

                            // session.setConfig("PreferredAuthentications", "password,keyboard-interactive");
                            session.connect();

                            channelSftp = (ChannelSftp) session.openChannel("sftp");
                            channelSftp.connect();
                            LOG.log(Level.INFO, "Connexion sftp réussi !");

                            for (String filePath : files) {
                                // Envoyer les fichiers via SFTP                            
                                boolean sentSuccessfully = FileUtility.sendFileViaSftpJsch(channelSftp, filePath, destinationDir);
                                if (sentSuccessfully) {
                                    // Archiver le fichier une fois envoyé avec succès
                                    FileUtility.archiveFile(filePath, archiveDirectory);
                                } else {
                                    LOG.log(Level.INFO, "L''envoie du fichier : {0} a \u00e9chou\u00e9 !", filePath);
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

                } catch (InterruptedException | ExecutionException | RejectedExecutionException ex) {
                    LOG.log(Level.SEVERE, ex.getMessage());
                    ex.printStackTrace();
                }
            }

            // Attente avant de recommencer la boucle
            try {
                Thread.sleep(5000); // Attente de 5 secondes avant la prochaine boucle
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, e.getMessage());
                e.printStackTrace();
            }

        }

    }

    @Override
    public void monitorStop(ExecutorService executorService) {
        if (!executorService.isShutdown() || !executorService.isTerminated()) {
            executorService.shutdownNow();
        }
    }

}
