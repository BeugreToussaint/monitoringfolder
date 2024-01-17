/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sbs.monitoringtransfert.monitor;

import com.sbs.monitoringtransfert.config.ConfigProperties;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Tuxbe
 * @Date 19/09/2023
 */
public class MonitorMain {

    private final static Properties CONFIG = ConfigProperties.loadConfig();

    private final static String[] directories = CONFIG.getProperty("monitor.listen.dir", "C:/envoie,C:/clearing/interface/sortie").split(",");

    private final static String[] EXTENSIONS = CONFIG.getProperty("monitor.listen.file.extension", "C:/envoi").split(",");

    private final static List<String> extensionsStream = Arrays.asList(EXTENSIONS);

    static String archiveDirectory = CONFIG.getProperty("monitor.listen.archive.dir", "C:/archive");
    static String logsDir = CONFIG.getProperty("monitor.logs.file");
    static String host = CONFIG.getProperty("server.host", "localhost");
    static int port = Integer.parseInt(CONFIG.getProperty("server.port", "22"));
    static String username = CONFIG.getProperty("server.username", "tester");
    static String password = CONFIG.getProperty("server.password", "password"); // or use key-based authentication
    static String destinationDir = CONFIG.getProperty("server.destination.dir", "/");

    static String keyprivatepath = CONFIG.getProperty("server.keyprivatepath", ".ssh/id_rsa");
    static String known_hosts = CONFIG.getProperty("server.known_hosts", "/known_hosts");

    static String type_send = CONFIG.getProperty("app.type.send", "FTP");


    
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // Création d'un ExecutorService avec un pool de 5 threads
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        MonitoringSendInterf monitor = null;

        if (type_send.equals("SFTP")) {
            monitor = new MonitoringSendJsch();
        } else {
            monitor = new MonitoringSendFTPClient();
        }

        monitor.monitorStart(logsDir, directories, extensionsStream, username, password, port, host, archiveDirectory, destinationDir, executorService, known_hosts, keyprivatepath);

    }
}
