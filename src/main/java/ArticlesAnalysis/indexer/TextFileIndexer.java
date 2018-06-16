package ArticlesAnalysis.indexer;

import ArticlesAnalysis.utils.FileUtils;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This terminal application creates an Apache Lucene index
 * in a folder and adds files into this index.
 */
public class TextFileIndexer {
    private RussianAnalyzer analyzer;
    private IndexWriter writer;
    private ArrayList<File> queue;
    private String indexPath;

    /**
     * Constructor
     *
     * @param indexDir the name of the folder in which the index should be created
     * @throws java.io.IOException when exception creating index.
     */
    public TextFileIndexer(String indexDir) throws IOException {
        FileUtils.deleteFilesInFolder(indexPath);

        indexPath = indexDir;
        analyzer = new RussianAnalyzer();
        writer = new IndexWriter(FSDirectory.open(Paths.get(indexPath)),
                new IndexWriterConfig(analyzer));
        queue = new ArrayList<>();
    }

    /**
     * Indexes a file or directory
     *
     * @param fileName the name of a text file or a folder we wish to add to the index
     * @throws java.io.IOException when exception
     */
    public void indexFileOrDirectory(String fileName) throws IOException {
        addFiles(new File(fileName));

        int originalNumDocs = writer.numDocs();
        for (File f : queue) {
            FileReader fr = null;
            try {
                Document doc = new Document();
                fr = new FileReader(f);
                doc.add(new TextField("contents", fr));
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(), Field.Store.YES));
                writer.addDocument(doc);
                System.out.println("Added: " + f);
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                fr.close();
            }
        }

        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    public void searchByQuery() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(5);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s = "";
        try {
            System.out.println("Enter the search query:");
            s = br.readLine();
            Query q = new QueryParser("contents", analyzer).parse(s);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            System.out.println("Found " + hits.length + " hits.");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                System.out.println((i + 1) + ". " + d.get("path") + " score=" + hits[i].score);
            }

        } catch (Exception e) {
            System.out.println("Error searching " + s + " : " + e.getMessage());
        }
    }

    /**
     * Close the index.
     *
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
    }

    private void addFiles(File file) {
        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            if (filename.endsWith(".htm") || filename.endsWith(".html") ||
                    filename.endsWith(".xml") || filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }
}