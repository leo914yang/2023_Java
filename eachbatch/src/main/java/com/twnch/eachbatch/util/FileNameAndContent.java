package com.twnch.eachbatch.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class FileNameAndContent {
    @Value("${source}")
    private String filePath;

    public List<String> readAndProcessFileContent(String filePath, List<String> fileContentArrayList) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = "";
            String nextLine = reader.readLine();
            StringBuilder fileContent = new StringBuilder();
            while (nextLine != null) {
                line = nextLine;
                nextLine = reader.readLine();
                if (nextLine != null) {
                    fileContent.append(line).append("\n");
                } else {
                    fileContent.append(line);
                }
            }

            fileContentArrayList.add(fileContent.toString());
        } catch (Exception e) {
            log.info(String.valueOf(e));
        }
        return fileContentArrayList;
    }
}
