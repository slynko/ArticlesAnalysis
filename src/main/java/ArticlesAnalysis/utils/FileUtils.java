package ArticlesAnalysis.utils;

import java.io.File;

public class FileUtils {
    public static void deleteFilesInFolder(String folderPath) {
        try {
            File folder = new File(folderPath);
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteFilesInFolder(f.getPath());
                    } else {
                        f.delete();
                    }
                }
            }
        } catch (NullPointerException e){
            // folder doesn't exist
        }
    }
}
