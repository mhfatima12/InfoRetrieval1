package com.example.lucene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

class DocumentData {
    String doc_id;
    String doc_title;
    String doc_author;
    String doc_content;

    public DocumentData(String doc_id, String doc_title, String doc_author, String doc_content) {
        this.doc_id = doc_id;
        this.doc_title = doc_title;
        this.doc_author = doc_author;
        this.doc_content = doc_content;
    }

    @Override
    public String toString() {
        return "Id: " + doc_id + "\nTitle: " + doc_title + "\nAuthor: " + doc_author + "\nContent: " + doc_content;
    }
}

public class CranfieldIndexer {
    private static String INDEX_DIRECTORY = "index";

    public void indexDocuments() {
        List<DocumentData> documents = new ArrayList<>();
        String id = "";
        String title = "";
        String author = "";
        StringBuilder content = new StringBuilder();

        boolean readingTitle = false;
        boolean readingAuthor = false;
        boolean readingContent = false;
        int documentCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader("src\\main\\java\\com\\example\\lucene\\cran.all.1400"))) {
            String line;
            boolean foundFirstDocument = false; // check if found first document
            //set up Lucene analyzer and indexing
            EnglishAnalyzer englishAnalyzer = new EnglishAnalyzer();
            //Analyzer standardAnalyzer = new StandardAnalyzer();
            //Analyzer whitespaceAnalyzer = new WhitespaceAnalyzer();
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
            IndexWriterConfig iwConfig = new IndexWriterConfig(englishAnalyzer);
            //IndexWriterConfig iwConfigStandard = new IndexWriterConfig(standardAnalyzer);
            //create new index
            iwConfig.setSimilarity(new BM25Similarity());
            iwConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter indexWriter = new IndexWriter(directory, iwConfig);

//            iwConfigStandard.setSimilarity(new LMDirichletSimilarity());
//            iwConfigStandard.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
//            IndexWriter indexWriter = new IndexWriter(directory, iwConfigStandard);

            while ((line = reader.readLine()) != null) {
                line = line.trim(); // trim whitespace

                if (line.startsWith(".I ")) { // Document ID
                    // save the previous document if it exists
                    if (!id.isEmpty()) {
                        //ensures that there is a previous document to save
                        documents.add(new DocumentData(id, title.trim(), author.trim(), content.toString().trim()));
                        documentCount++;
                        indexDocument(indexWriter, id, title, author, content.toString());
                    }
                    // reset for the new document
                    id = line.substring(3).trim(); // Extract ID (after ".I ")
                    title = ""; //clear title
                    author = ""; //clear author
                    content.setLength(0); // clear the content
                    foundFirstDocument = true; // this is first document found
                } else if (line.startsWith(".T")) { // Title
                    readingTitle = true;
                    readingAuthor = false;
                    readingContent = false;
                } else if (line.startsWith(".A")) { // Author
                    readingAuthor = true;
                    readingTitle = false;
                } else if (line.startsWith(".W")) { // Content
                    readingContent = true;
                    readingAuthor = false;
                    readingTitle = false;
                } else if (readingTitle) {
                    title += line + " "; // append title
                } else if (readingAuthor) {
                    author += line + " "; // append author
                } else if (readingContent) {
                    content.append(line).append(" "); // append content
                }
            }

            // add the last document if it exists
            if (!id.isEmpty()) {
                documents.add(new DocumentData(id, title.trim(), author.trim(), content.toString().trim()));
                documentCount++;
                indexDocument(indexWriter, id, title, author, content.toString());
            }

            indexWriter.close();
            directory.close();

            // if never found a document, output warning
            if (!foundFirstDocument) {
                System.out.println("No documents found in the file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (DocumentData document : documents) {
            System.out.println(document);
        }

        System.out.println("Total documents parsed: " + documentCount);
    }

    // index of 1400 document collection using Lucene with required fields
    private static void indexDocument(IndexWriter iwriter, String id, String title, String author, String content) throws IOException {
        Document indexDoc = new Document();
        indexDoc.add(new StringField("id", id, Field.Store.YES));
        indexDoc.add(new TextField("title", title, Field.Store.YES));
        indexDoc.add(new TextField("author", author, Field.Store.YES));
        indexDoc.add(new TextField("content", content, Field.Store.YES));
        iwriter.addDocument(indexDoc);
    }

    public static void main(String[] args) {
        CranfieldIndexer indexer = new CranfieldIndexer();
        indexer.indexDocuments();
    }
    
}