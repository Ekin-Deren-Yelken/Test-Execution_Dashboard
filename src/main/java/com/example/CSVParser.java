package com.example;

import java.io.*;
import java.util.*;

public class CSVParser {
    public static List<String[]> parseCSV(String filePath) throws IOException {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                records.add(data);
            }
        }
        return records;
    }
}
