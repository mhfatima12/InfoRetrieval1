package com.example.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws Exception {
        //part1: parsing and indexing cran 1400 files
        CranfieldIndexer cranIndexer = new CranfieldIndexer();
        cranIndexer.indexDocuments();

        //part2: process queries and search index
        CranfieldSearcher newSearcher = new CranfieldSearcher();
        newSearcher.processQueries();

        //part3: converting cranqrel file to correct format
        String inputFilePath = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\cranqrel";
        String outputFilePath = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\cranqrel_formatted.txt";


        CranqrelFormatter.convertFile(inputFilePath, outputFilePath);

    }
}