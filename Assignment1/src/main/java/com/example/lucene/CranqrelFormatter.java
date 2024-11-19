package com.example.lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CranqrelFormatter {
    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\cranqrel"; 
        String outputFilePath = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\cranqrel_formatted.txt";
        
        // Use a Set to keep track of unique queryID-docID pairs
        Set<String> uniqueEntries = new HashSet<>();
        
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

                // Create a unique key for each queryID-docID pair
                String uniqueKey = queryID + "-" + docID;

                // Check if this queryID-docID pair is already processed
                if (!uniqueEntries.contains(uniqueKey)) {
                    // If it's unique, add it to the set and write to the output file
                    uniqueEntries.add(uniqueKey);
                    bw.write(String.format("%s 0 %s %s\n", queryID, docID, relevanceScore));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Formatted cranqrel saved to " + outputFilePath);
    }
}
