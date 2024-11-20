package com.example.lucene;

public class App {
    public static void main(String[] args) throws Exception {
        CranfieldIndexer cranIndexer = new CranfieldIndexer();
        cranIndexer.indexDocuments();

        CranfieldSearcher newSearcher = new CranfieldSearcher();
        newSearcher.processQueries();

        //String inputFilePath = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\cranqrel";
        //String outputFilePath = "C:\\Users\\Maham Fatima\\Desktop\\InfoAssignment1\\Assignment1\\cranqrel_formatted.txt";


        //.convertFile(inputFilePath, outputFilePath);

    }
}