/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sbs.monitoringtransfert.monitor;

import com.sbs.monitoringtransfert.utility.FileUtility;
import com.sbs.monitoringtransfert.utility.logUtility;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.PuTTYKeyFile;
import org.bouncycastle.openssl.PasswordFinder;

/**
 * @author Tuxbe
 * @Date 21/07/2023
 */
public class MonitoringSendSSHj implements MonitoringSendInterf {

    private static final Logger LOG = Logger.getLogger(MonitoringSendJsch.class.getName());

    @Override
    public void monitorStart(String logsDir, String[] directories, List<String> extensionsStream,
            String username, String password, int portSftp, String host, String archiveDirectory, String destinationDir, ExecutorService executorService, String known_hosts, String keyprivatepath) {

        logUtility.configureLogging(logsDir);

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

                    if (!files.isEmpty()) {
                        LOG.log(Level.INFO, "Fichiers trouv\u00e9s dans le r\u00e9pertoire: {0}", files);
                        // Connexion SFTP
                        SSHClient ssh = new SSHClient();
                        try {

                            ssh.loadKeys(keyprivatepath);
                            ssh.addHostKeyVerifier((hostname, port, key) -> true);
                            ssh.addAlgorithmsVerifier(algorithms -> true);
                            ssh.connect(host);
                            ssh.authPublickey(username);
                            // Load the public key from the file
                            LOG.log(Level.INFO, "Connexion sftp réussi !"); // Fermer la connexion SFTP lorsque vous avez terminé l'envoi des fichiers

                        } catch (IOException ex) {
                            Logger.getLogger(MonitoringSendSSHj.class.getName()).log(Level.SEVERE, null, ex);

                        }

                        /*                    try {
                            ssh.authPassword(username, password);
                        } catch (UserAuthException | TransportException ex) {
                            Logger.getLogger(MonitoringSendSSHj.class.getName()).log(Level.SEVERE, null, ex);
                        }
                         */
                        try (SFTPClient sftp = ssh.newSFTPClient()) {

                            for (String filePath : files) {
                                // Envoyer les fichiers via SFTP
                                boolean sentSuccessfully = FileUtility.sendFileViaSftpClient(sftp, filePath, destinationDir);
                                if (sentSuccessfully) {
                                    // Archiver le fichier une fois envoyé avec succès
                                    FileUtility.archiveFile(filePath, archiveDirectory);
                                } else {
                                    LOG.log(Level.INFO, "L''envoie du fichier : {0} a \u00e9chou\u00e9 !", filePath);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(MonitoringSendSSHj.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            // Attente avant de recommencer la boucle
            try {
                Thread.sleep(5000); // Attente de 5 secondes avant la prochaine boucle
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, e.getMessage());
            }

        }
    }

    @Override
    public void monitorStop(ExecutorService executorService) {
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

}
