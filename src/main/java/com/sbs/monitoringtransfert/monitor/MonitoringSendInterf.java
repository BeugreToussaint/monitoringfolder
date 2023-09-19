/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.sbs.monitoringtransfert.monitor;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author SamuelWAYORO
 */
public interface MonitoringSendInterf {

    void monitorStart(String logsDir, String[] directories, List<String> extensionsStream,
            String username, String password, int port, String host, String archiveDirectory, String destinationDir, ExecutorService executorService
    );

    void monitorStop(ExecutorService executorService);
}
