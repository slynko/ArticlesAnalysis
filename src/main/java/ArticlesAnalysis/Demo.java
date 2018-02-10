package ArticlesAnalysis;

import ArticlesAnalysis.indexer.TextFileIndexer;

import java.io.IOException;

public class Demo {

    public static void main(String[] args) throws IOException {
        String indexPath = "index";
        String inputDir = "input";

        TextFileIndexer indexer = new TextFileIndexer(indexPath);

        indexer.indexFileOrDirectory(inputDir);
        indexer.closeIndex();
        indexer.searchByQuery();
    }
}
