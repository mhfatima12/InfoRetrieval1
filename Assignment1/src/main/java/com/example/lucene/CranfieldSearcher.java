package com.example.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
        // Path to the index directory
        String indexPath = "index";
        Directory index = FSDirectory.open(Paths.get(indexPath));

        // Path to the cran.qry (225 textual queries) file
        String queryFilePath = "src\\main\\java\\com\\example\\lucene\\cran.qry";
        
        // Path to store the results for trec_eval
        String resultsFilePath = "results.txt";

        // Load the index and initialize searcher
        DirectoryReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity()); // BM25 similarity

        // Analyzer (should match the one used during indexing)
        Analyzer analyzer = new StandardAnalyzer();

        // Multi-field query parser to search across Title, Content, and Author
        MultiFieldQueryParser parser = new MultiFieldQueryParser(
                new String[]{"Title", "Content", "Author"}, analyzer);

        // Read and process each query
        try (BufferedReader queryReader = new BufferedReader(new FileReader(queryFilePath));
             FileWriter resultsWriter = new FileWriter(new File(resultsFilePath))) {

            String line;
            Map<String, String> queryMap = new HashMap<>();
            StringBuilder queryBuilder = new StringBuilder();
            String currentQueryID = null;

            while ((line = queryReader.readLine()) != null) {
                if (line.startsWith(".I")) {
                    // Save the previous query
                    if (currentQueryID != null) {
                        queryMap.put(currentQueryID, queryBuilder.toString().trim());
                        queryBuilder.setLength(0);
                    }
                    currentQueryID = line.split("\\s+")[1]; // Capture query ID
                } else if (line.startsWith(".W")) {
                    continue; // Start of query content, skip
                } else {
                    queryBuilder.append(line).append(" ");
                }
            }

            // Save the last query
            if (currentQueryID != null) {
                queryMap.put(currentQueryID, queryBuilder.toString().trim());
            }

            // For each query, perform the search and save top 50 results
            for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                String queryID = entry.getKey();
                String queryString = entry.getValue();

                // Preprocess and parse the query
                Query query = parser.parse(QueryParser.escape(queryString)); // Escape special characters

                // Execute search
                ScoreDoc[] hits = searcher.search(query, 50).scoreDocs; // Get top 50 results

                // Record results in TREC format (query-id Q0 rank score STANDARD)
                for (int i = 0; i < hits.length; i++) {
                    float score = hits[i].score;
                    int rank = i + 1;

                    // Write in TREC format: queryID Q0 rank score STANDARD
                    resultsWriter.write(String.format("%s Q0 %d %f STANDARD\n", queryID, rank, score));
                }
            }
        }

        reader.close(); // Close the index reader
        System.out.println("Results saved to " + resultsFilePath);
    }
}
