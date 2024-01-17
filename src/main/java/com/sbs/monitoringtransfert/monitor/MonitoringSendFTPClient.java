package com.sbs.monitoringtransfert.monitor;

import com.sbs.monitoringtransfert.utility.FileUtility;
import com.sbs.monitoringtransfert.utility.logUtility;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonitoringSendFTPClient implements MonitoringSendInterf {

    private static final Logger LOG = Logger.getLogger(MonitoringSendFTPClient.class.getName());

    @Override
    public void monitorStart(String logsDir, String[] directories, List<String> extensionsStream, String username, String password, int port, String host, String archiveDirectory, String destinationDir, ExecutorService executorService, String known_hosts, String keyprivatepath) throws ExecutionException, InterruptedException {
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

                    if (!files.isEmpty()) {
                        LOG.log(Level.INFO, "Fichiers trouv\u00e9s dans le r\u00e9pertoire: {0}", files);

                        FTPClient ftpClient = new FTPClient();

                        try {
                            ftpClient.connect(host, port);
                            ftpClient.login(username, password);
                            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                            // Change working directory on the FTP server
                            ftpClient.changeWorkingDirectory(destinationDir);
                            LOG.log(Level.INFO, "Connexion sftp réussi !");

                            for (String filePath : files) {
                                // Envoyer les fichiers via SFTP
                                boolean sentSuccessfully = FileUtility.sendFileViaftpClient(ftpClient, filePath, destinationDir);
                                if (sentSuccessfully) {
                                    // Archiver le fichier une fois envoyé avec succès
                                    FileUtility.archiveFile(filePath, archiveDirectory);
                                } else {
                                    LOG.log(Level.INFO, "L'envoie du fichier : {0} a \u00e9chou\u00e9 !", filePath);
                                }
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e.getMessage());
                        } finally {
                            if (ftpClient.isConnected()) {
                                ftpClient.logout();
                                ftpClient.disconnect();
                            }

                        }
                    }
                } catch (InterruptedException | ExecutionException | RuntimeException | IOException e) {
                    throw new RuntimeException(e.getMessage());
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
        if (!executorService.isShutdown() || !executorService.isTerminated()) {
            executorService.shutdownNow();
        }
    }
}
