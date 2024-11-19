package com.example.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import   org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CranfieldSearcher {

    public static void main(String[] args) throws Exception {
       
        String indexPath = "index";
        Directory index = FSDirectory.open(Paths.get(indexPath));
        String queryFilePath = "src\\main\\java\\com\\example\\lucene\\cran.qry";
        String resultsFilePath = "results.txt";

        // Load the index and initialize searcher
        DirectoryReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity()); // BM25 similarity
        //searcher.setSimilarity(new BooleanSimilarity());
        //searcher.setSimilarity(new ClassicSimilarity());

        // Analyser
        //Analyzer analyser = new StandardAnalyzer();
        Analyzer analyser = new EnglishAnalyzer();

        // Multi-field query parser to search across Title, Content, and Author
        MultiFieldQueryParser parser = new MultiFieldQueryParser(
                new String[]{"Title", "Content", "Author"}, analyser);

        // Reading and processing each query
        try (BufferedReader queryReader = new BufferedReader(new FileReader(queryFilePath));
             FileWriter resultsWriter = new FileWriter(new File(resultsFilePath))) {

            String line;
            Map<String, String> queryMap = new HashMap<>();
            StringBuilder queryBuilder = new StringBuilder();
            String currentQueryID = null;

            while ((line = queryReader.readLine()) != null) {
                if (line.startsWith(".I")) {
                    // Savign the previous query
                    if (currentQueryID != null) {
                        queryMap.put(currentQueryID, queryBuilder.toString().trim());
                        queryBuilder.setLength(0);
                    }
                    currentQueryID = line.split("\\s+")[1]; 
                } else if (line.startsWith(".W")) {
                    continue; 
                } else {
                    queryBuilder.append(line).append(" ");
                }
            }

            // Saving the last query
            if (currentQueryID != null) {
                queryMap.put(currentQueryID, queryBuilder.toString().trim());
            }

            // For each query from 1 to 225, performign the search
            for (int queryID = 1; queryID <= 225; queryID++) { // Ensure we process 1 to 225
                String queryIDStr = String.format("%d", queryID);
                String queryString = queryMap.get(queryIDStr);

                if (queryString == null) {
                    // Handling the case where the query string is missing
                    System.err.println("No query found for ID: " + queryIDStr);
              
                    resultsWriter.write(String.format("%s Q0 -1 1 0 STANDARD\n", queryIDStr)); // Placeholder for no docID
                    continue; 
                }

                // Preprocessing and parsing the query
                Query query = parser.parse(QueryParser.escape(queryString)); 

                // Executing the search
                ScoreDoc[] hits = searcher.search(query, 50).scoreDocs; 

              
                for (int i = 0; i < hits.length; i++) {
                    Document doc = searcher.doc(hits[i].doc);
                    String docID = doc.get("ID"); 
                    float score = hits[i].score;
                    int rank = i + 1;

                    // Writ ingin TREC format for TREC metric
                    resultsWriter.write(String.format("%s Q0 %s %d %.6f STANDARD\n", queryIDStr, docID, rank, score));
                    //resultsWriter.write(String.format("%s Q0 %s %d %.6f STANDARD", queryCount, docId, rank, score));
                }
            }
        }

        reader.close(); 
        System.out.println("Results saved to " + resultsFilePath);
    }
}
