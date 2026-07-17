import java.io.File;

public class SearchExplorer {

    public static File findFile(String rootDirectory, String fileNamePattern) {
        File directory = new File(rootDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        // Convert wildcard to regex if needed, or use pattern directly
        String regex = fileNamePattern.replace(".", "\\.").replace("*", ".*");

        return searchRecursive(directory, regex);
    }

    private static File searchRecursive(File directory, String regex) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                // 1. Check if it matches
                if (file.isFile() && file.getName().matches(regex)) {
                    return file; // Found it! Return immediately
                }
                // 2. If it's a directory, search inside it
                else if (file.isDirectory()) {
                    File foundInSub = searchRecursive(file, regex);
                    if (foundInSub != null) {
                        return foundInSub; // Found it in sub-folder, pass it up!
                    }
                }
            }
        }
        return null; // Not found in this branch
    }
}