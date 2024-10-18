package com.example.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class CranfieldIndexer {

    public static void main(String[] args) throws Exception {
        // Path to store the Lucene index
        String indexPath = "index";  // Directory to store the index
        
        // Ensure the directory exists
        File indexDir = new File(indexPath);
        if (indexDir.exists()) {
            for (File file : indexDir.listFiles()) {
                file.delete();
            }
            indexDir.delete();
        }

        // Initialize the Lucene components
        //Analyzer analyzer = new StandardAnalyzer();
         Analyzer analyzer = new EnglishAnalyzer();
        Directory index = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        
        try (IndexWriter writer = new IndexWriter(index, config);
             FileWriter logWriter = new FileWriter("parsed_data_log.txt")) { // Create a log file writer
            // Path to cran.all.1400 file
            String filePath = "src\\main\\java\\com\\example\\lucene\\cran.all.1400";
            parseAndIndexFile(writer, Paths.get(filePath), logWriter); // Pass logWriter to the method

            // Output the total number of documents indexed
            System.out.println("Total documents indexed: " + writer.getDocStats().numDocs);
        }

        // Search Example
        searchIndex(index, "aerodynamics");
    }

    // Parsing and indexing the file
    private static void parseAndIndexFile(IndexWriter writer, Path filePath, FileWriter logWriter) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            Document doc = null;
            String currentField = "";
            StringBuilder contentBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith(".I")) {
                    // Index the previous document
                    if (doc != null) {
                        // Add the accumulated content to the document
                        if (contentBuilder.length() > 0) {
                            doc.add(new TextField("Content", contentBuilder.toString(), Field.Store.YES));
                            contentBuilder.setLength(0);
                        }
                        writer.addDocument(doc);
                        logParsedData(logWriter, doc);
                    }
                    // Start a new document
                    doc = new Document();
                    String id = line.split("\\s+")[1];
                    doc.add(new StringField("ID", id, Field.Store.YES));
                } else if (line.equals(".T")) {
                    currentField = "Title";
                } else if (line.equals(".A")) {
                    currentField = "Author";
                } else if (line.equals(".B")) {
                    currentField = "Bibliography";
                } else if (line.equals(".W")) {
                    currentField = "Content";
                } else {
                    if (currentField.equals("Title")) {
                        doc.add(new TextField("Title", line, Field.Store.YES));
                    } else if (currentField.equals("Author")) {
                        doc.add(new TextField("Author", line, Field.Store.YES));
                    } else if (currentField.equals("Bibliography")) {
                        doc.add(new TextField("Bibliography", line, Field.Store.YES));
                    } else if (currentField.equals("Content")) {
                        contentBuilder.append(line).append(" ");
                    }
                }
            }

            // Index the last document
            if (doc != null) {
                if (contentBuilder.length() > 0) {
                    doc.add(new TextField("Content", contentBuilder.toString(), Field.Store.YES));
                }
                writer.addDocument(doc);
                logParsedData(logWriter, doc);
            }
        }
    }

    // Method to log parsed data to a file
    private static void logParsedData(FileWriter logWriter, Document doc) throws IOException {
        String id = doc.get("ID");
        String title = doc.get("Title");
        String author = doc.get("Author");
        String bibliography = doc.get("Bibliography");
        String content = doc.get("Content");

        // Log the data in a readable format
        logWriter.write("Document ID: " + id + "\n");
        logWriter.write("Title: " + title + "\n");
        logWriter.write("Author: " + author + "\n");
        logWriter.write("Bibliography: " + bibliography + "\n");
        logWriter.write("Content: " + content + "\n");
        logWriter.write("--------------------------------------------------\n"); // Separator for each document
        logWriter.flush(); // Ensure data is written to file
    }

    // Search method to query the indexed data
    private static void searchIndex(Directory index, String queryString) throws Exception {
        DirectoryReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        // Use the same analyzer for search as you did for indexing
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser("Content", analyzer);
        Query query = parser.parse(queryString);

        ScoreDoc[] hits = searcher.search(query, 10).scoreDocs;

        // Display search results
        System.out.println("Found " + hits.length + " hits.");
        for (ScoreDoc scoreDoc : hits) {
            Document d = searcher.doc(scoreDoc.doc);
            System.out.println("ID: " + d.get("ID") + ", Title: " + d.get("Title"));
        }

        reader.close();
    }

    // Inspect indexed terms (for debugging purposes)
    private static void inspectIndexedTerms(Directory index) throws Exception {
        DirectoryReader reader = DirectoryReader.open(index);
        Terms terms = reader.getTermVectors(0).terms("Content");
        if (terms != null) {
            TermsEnum iterator = terms.iterator();
            BytesRef byteRef;
            while ((byteRef = iterator.next()) != null) {
                String term = byteRef.utf8ToString();
                System.out.println("Indexed term: " + term);
            }
        }
        reader.close();
    }
}
