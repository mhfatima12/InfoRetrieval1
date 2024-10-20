package com.example.lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CranqrelFormatter {
    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\cranqrel"; // Change this to your cranqrel path
        String outputFilePath = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\cranqrel_formatted.txt"; // Change this to your desired output path
        
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                String queryID = parts[0];
                String docID = parts[1];
                String relevanceScore = parts[2];

                // Change relevanceScore from -1 to 5 if necessary
                if ("-1".equals(relevanceScore)) {
                    relevanceScore = "5"; // Replace -1 with 5
                }

                // Write in the new format: <query_id> 0 <document_id> <relevance_score>
                bw.write(String.format("%s 0 %s %s\n", queryID, docID, relevanceScore));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Formatted cranqrel saved to " + outputFilePath);
    }
}
