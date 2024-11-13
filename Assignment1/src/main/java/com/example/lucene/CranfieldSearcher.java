package com.example.lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CranfieldSearcher {
    private static String INDEX_DIRECTORY = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\index";
    private static String QUERY_FILE = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\src\\main\\java\\com\\example\\lucene\\cran.qry";

    public void processQueries() {
        try {
            // open the index
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
            DirectoryReader directoryReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
            //StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
            EnglishAnalyzer engAnalyzer = new EnglishAnalyzer();
            //Analyzer whitespaceAnalyzer = new WhitespaceAnalyzer();

            //pick Lucene similarity by commenting out the similarities you aren't using
            //Lucene's original term frequency-inverse document frequency (TF-IDF) similarity
            indexSearcher.setSimilarity(new BM25Similarity());
            //BM25 similarity
            //indexSearcher.setSimilarity(new BM25Similarity());
            //Instantiates the similarity with the default Î¼ value of 2000
            //indexSearcher.setSimilarity(new LMDirichletSimilarity());

            System.out.println("INDEX SIMILARITY " + indexSearcher.getSimilarity());

            // read queries
            try (BufferedReader queryReader = new BufferedReader(new FileReader(QUERY_FILE))) {
                String line;
                int queryCount = 0;

                while ((line = queryReader.readLine()) != null) {
                    // if line startswith .w then read in line of data until line starts with .i
                    if (line.startsWith(".W")) {
                        // get query ID
                        queryCount++;
                        StringBuilder queryText = new StringBuilder();

                        // read query text
                        while ((line = queryReader.readLine()) != null && !line.startsWith(".I")) {
                            queryText.append(line).append(" ");
                        }

                        // perform search for this query
                        String queryString = queryText.toString().trim();
                        searchQuery(queryCount, indexSearcher, engAnalyzer, queryString);
                    }
                }
                System.out.println("Processing query number: " + queryCount);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            directoryReader.close();
            directory.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String handleSpecialCharacters(String queryStr) {
        // escape Lucene special characters, allow * and ? as wildcards unless they appear at start
        String[] tokens = queryStr.split("\\s+");
        StringBuilder escapedQuery = new StringBuilder();

        for (String token : tokens) {
            // escape the token if it starts with * or ?
            if (token.startsWith("*") || token.startsWith("?")) {
                // avoid wildcards by appending whole token
                escapedQuery.append(QueryParser.escape(token)).append(" ");
            } else {
                // leave * and ? in place
                escapedQuery.append(token).append(" ");
            }
        }
        return escapedQuery.toString().trim();
    }

    private static void searchQuery(Integer queryCount, IndexSearcher indexSearcher, Analyzer analyzer, String queryString) throws Exception {
        String refinedQuery = handleSpecialCharacters(queryString);
        QueryParser queryParser = new QueryParser("content", analyzer);
        String analyzerName = analyzer.getClass().getSimpleName();
        String similarityName = indexSearcher.getSimilarity().getClass().getSimpleName();

        String resultsFolderPath = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\trec_eval_result";
        String resultsFile = String.format("%s/trec_eval_results_%s_%s.txt", resultsFolderPath, analyzerName, similarityName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile, true))) {
            Query query = queryParser.parse(refinedQuery);

            // query the index and get top 50 results as needed
            TopDocs topResults = indexSearcher.search(query, 50);
            ScoreDoc[] hits = topResults.scoreDocs;

            int rank = 1;

            // iterate through search results and write them to the results file
            for (ScoreDoc scoreDoc : hits) {
                Document doc = indexSearcher.doc(scoreDoc.doc);
                String docId = doc.get("id");
                float score = scoreDoc.score;

                // write result in TREC_eval format
                writer.write(String.format("%s Q0 %s %d %.6f STANDARD", queryCount, docId, rank, score));
                writer.newLine();

                rank++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CranfieldSearcher searcher = new CranfieldSearcher();
        searcher.processQueries();
    }
    


}