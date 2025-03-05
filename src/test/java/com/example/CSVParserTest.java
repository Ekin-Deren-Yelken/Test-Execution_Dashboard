package com.example;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.List;

public class CSVParserTest {

    @Test
    public void testValidCSV() throws IOException {
        List<String[]> records = CSVParser.parseCSV("src/test/resources/test_results_basic.csv");
        Assert.assertEquals(records.size(), 3);
        Assert.assertEquals(records.get(0)[0], "LoginTest");
        Assert.assertEquals(records.get(1)[1], "Fail");
    }

    @Test
    public void testEmptyCSV() throws IOException {
        List<String[]> records = CSVParser.parseCSV("src/test/resources/test_results_empty.csv");
        Assert.assertTrue(records.isEmpty());
    }

    @Test
    public void testInvalidFormatCSV() throws IOException {
        List<String[]> records = CSVParser.parseCSV("src/test/resources/test_results_invalid_format.csv");
        Assert.assertEquals(records.size(), 3);
        Assert.assertNotEquals(records.get(1).length, 3); // Invalid row should be shorter
    }
}
