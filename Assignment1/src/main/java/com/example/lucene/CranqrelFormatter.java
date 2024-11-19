package com.example.lucene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CranqrelFormatter {
    public static void convertFile(String inputFilePath, String outputFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             FileWriter writer = new FileWriter(outputFilePath)) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 3) {
                    String queryId = parts[0];
                    String docId = parts[1];
                    String relevance = parts[2];

                    if ("-1".equals(relevance)) {
                        relevance = "5";
                    }

                    writer.write(queryId + " 0 " + docId + " " + relevance + "\n");
                }
            }
            System.out.println("File has been reformatted successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
