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

            // Files.walk(sourcePath)這個方法用於遞歸地遍歷指定的源路徑 sourcePath 下的所有文件和子目錄
            // .filter(Files::isRegularFile)：這個操作會對遍歷到的每個路徑進行過濾, 只保留其中的普通文件（非目錄）
            Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        // Path sourcefile = sourcePath.relativize(file) 計算每個文件相對於源路徑的相對路徑
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
        } catch (IOException | SecurityException e) {
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
        } catch (IOException | SecurityException e) {
            log.info(String.valueOf(e));
        }
    }
}

