/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sbs.monitoringtransfert.monitor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author SamuelWAYORO
 */
public class FileSearchTask implements Callable<List<String>> {

    private String directory;
    private List<String> extensions;

    public FileSearchTask(String directory, List<String> extensions) {
        this.directory = directory;
        this.extensions = extensions;
    }

    @Override
    public List<String> call() {
        List<String> foundFiles = new ArrayList<>();
        File dir = new File(directory);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                for (String extension : extensions) {
                    if (name.endsWith(extension)) {
                        return true;
                    }
                }
                return false;
            }
        });

        if (files != null) {
            for (File file : files) {
                foundFiles.add(file.getAbsolutePath());
            }
        }
        return foundFiles;
    }
}
