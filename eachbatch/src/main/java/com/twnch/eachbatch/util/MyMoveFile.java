package com.twnch.eachbatch.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class MyMoveFile {
    public void myMove(String mySource, String myDestination){

        try {
            Path sourcePath = Paths.get(mySource);
            Path destinationPath = Paths.get(myDestination);

            Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        //Path relativePath = sourcePath.relativize(file);
                        //Path pendingFile = pendingPath.resolve(relativePath);
                        Path sourcefile = sourcePath.relativize(file);
                        Path destinationFile = destinationPath.resolve(sourcefile);
                        if (!Files.exists(destinationFile)) {
                            try {
                                Files.copy(file, destinationFile);
                            } catch (IOException e) {
                                log.info(String.valueOf(e));
                            }
                        }
                    });
        } catch (IOException e) {
            log.info(String.valueOf(e));
        }

    }

    public void myDelete(){
        String source = "../source";
        try {
            Path directory = Paths.get(source);

            if (Files.exists(directory) && Files.isDirectory(directory)) {
                Files.walk(directory)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                log.info(String.valueOf(e));
                            }
                        });
            }
        } catch (IOException e) {
            log.info(String.valueOf(e));
        }
    }
}

