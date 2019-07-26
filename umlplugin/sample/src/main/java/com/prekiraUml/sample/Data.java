package com.prekiraUml.sample;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/
public class Data {

    /**
     * Read contents of file
     *
     * @param filename contains the name of file
     * @return a String containing the file's contents
     */

    public static String getFileContentsAsString(String filename) {

          final Path path = FileSystems.getDefault().getPath(filename);
        try {
             return new String(Files.readAllBytes(path));
        } catch (IOException e) {
             System.out.println("Couldn't find file: " + filename);
            System.exit(-1);
            return null;  
        }
    }

}