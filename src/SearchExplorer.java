/*
 * SearchExplorer.java - Searches for the CNC program for WizardFile
 *
 * Copyright 2026 Cristian Boitor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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