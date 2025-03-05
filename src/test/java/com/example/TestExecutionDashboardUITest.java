package com.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;

import java.nio.file.Paths;
import java.util.List;

public class TestExecutionDashboardUITest {
    private WebDriver driver;

    @BeforeClass
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
        driver = new ChromeDriver();
        driver.get("file:///path/to/TestExecutionDashboard.html"); // Adjust path if needed
    }

    // ============================================
    //          UPLOAD & TABLE DISPLAY TESTS
    // ============================================

    @Test
    public void testUploadCSV() {
        // Uploads a valid CSV file and checks if data appears in the table
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.sendKeys(Paths.get("src/test/resources/test_results_basic.csv").toAbsolutePath().toString());

        WebElement table = driver.findElement(By.id("testResultsTable"));
        Assert.assertTrue(table.getText().contains("LoginTest"), "Test result should be in the table");
    }

    @Test
    public void testUploadExtraColumns() {
        // Uploads a CSV file with extra columns and checks if the error message appears
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.sendKeys(Paths.get("src/test/resources/test_results_invalid_format.csv").toAbsolutePath().toString());

        WebElement table = driver.findElement(By.id("testResultsTable"));
        WebElement errorColumn = table.findElement(By.xpath("//td[contains(text(), 'Extra columns detected')]"));

        Assert.assertNotNull(errorColumn, "Error message should appear for extra columns");
    }

    @Test
    public void testUploadMissingFields() {
        // Uploads a CSV file with missing values and checks if the error message appears
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.sendKeys(Paths.get("src/test/resources/test_results_missing_fields.csv").toAbsolutePath().toString());

        WebElement table = driver.findElement(By.id("testResultsTable"));
        WebElement errorColumn = table.findElement(By.xpath("//td[contains(text(), 'Missing required fields')]"));

        Assert.assertNotNull(errorColumn, "Error message should appear for missing fields");
    }

    @Test
    public void testPreventDuplicateFileUpload() {
        // Tries to upload the same file twice and verifies error message appears
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.sendKeys(Paths.get("src/test/resources/test_results_basic.csv").toAbsolutePath().toString());

        WebElement errorMessage = driver.findElement(By.id("errorMessage"));
        Assert.assertTrue(errorMessage.getText().contains("File already uploaded"), "Error should appear when re-uploading the same file.");
    }

    // ============================================
    //          FILTERING & SORTING TESTS
    // ============================================

    @Test
    public void testFilteringFailedTests() {
        // Clicks on "Show only Failed tests" and verifies only failed results appear
        WebElement failCheckbox = driver.findElement(By.id("filterFailCheckbox"));
        failCheckbox.click();

        WebElement table = driver.findElement(By.id("testResultsTable"));
        Assert.assertFalse(table.getText().contains("Pass"), "Pass results should not be shown");
    }

    @Test
    public void testMultipleFilters() {
        // Applies both "Fail" and "Execution Time > 2s" filters and verifies results
        WebElement failCheckbox = driver.findElement(By.id("filterFailCheckbox"));
        WebElement timeCheckbox = driver.findElement(By.id("filterTimeCheckbox"));

        failCheckbox.click();
        timeCheckbox.click();

        WebElement table = driver.findElement(By.id("testResultsTable"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));

        for (WebElement row : rows) {
            String status = row.findElement(By.xpath("td[3]")).getText();
            String time = row.findElement(By.xpath("td[4]")).getText();
            
            Assert.assertTrue(status.equals("Fail") && Double.parseDouble(time.replace("s", "")) > 2, 
                "Only failed tests with execution time >2s should be displayed");
        }
    }

    // ============================================
    //         DROPDOWN & FILE MANAGEMENT TESTS
    // ============================================

    @Test
    public void testDropdownFileSelection() {
        // Selects a file from the dropdown and checks if only that file's results are shown
        Select fileDropdown = new Select(driver.findElement(By.id("fileDropdown")));
        fileDropdown.selectByVisibleText("test_results_basic.csv");

        WebElement table = driver.findElement(By.id("testResultsTable"));
        Assert.assertTrue(table.getText().contains("test_results_basic.csv"), "Only selected file results should be shown");
    }

    @Test
    public void testDropdownShowAllFiles() {
        // Selects "All Files" and verifies that all uploaded test results appear
        Select fileDropdown = new Select(driver.findElement(By.id("fileDropdown")));
        fileDropdown.selectByVisibleText("All Files");

        WebElement table = driver.findElement(By.id("testResultsTable"));
        Assert.assertFalse(table.getText().isEmpty(), "All test results should be visible");
    }

    @Test
    public void testPreventAllFilesRemoval() {
        // Ensures that "All Files" cannot be removed
        WebElement removeButton = driver.findElement(By.id("removeFileButton"));
        Select fileDropdown = new Select(driver.findElement(By.id("fileDropdown")));
        fileDropdown.selectByVisibleText("All Files");

        removeButton.click();

        WebElement errorMessage = driver.findElement(By.id("errorMessage"));
        Assert.assertTrue(errorMessage.getText().contains("Select a specific file to remove"), "Should prevent removing 'All Files'");
    }

    // ============================================
    //         TABLE FUNCTIONALITY TESTS
    // ============================================

    @Test
    public void testTableCellsUneditable() {
        // Ensures table cells are read-only and cannot be edited
        WebElement cell = driver.findElement(By.xpath("//table[@id='testResultsTable']//td[2]"));
        
        String originalText = cell.getText();
        cell.click();
        
        Actions actions = new Actions(driver);
        actions.sendKeys("Modified").perform();

        String afterEditText = cell.getText();
        Assert.assertEquals(afterEditText, originalText, "Table cells should not be editable");
    }

    @Test
    public void testClearTable() {
        // Clicks "Clear Table" and verifies that all data is removed
        WebElement clearButton = driver.findElement(By.id("clearTableButton"));
        clearButton.click();

        WebElement table = driver.findElement(By.id("testResultsTable"));
        Assert.assertEquals(table.getText(), "", "Table should be empty");
    }

    // ============================================
    //               EXPORT TESTS
    // ============================================

    @Test
    public void testExportCSV() {
        // Verifies that clicking "Export CSV" triggers the file save dialog
        WebElement exportCSVButton = driver.findElement(By.id("exportCSVButton"));
        exportCSVButton.click();

        WebElement fileDialog = driver.findElement(By.id("fileSaveDialog")); // Mocked element for testing
        Assert.assertTrue(fileDialog.isDisplayed(), "File save dialog should appear for CSV export");
    }

    @Test
    public void testExportEmptyTable() {
        // Tries to export an empty table and checks if an error message appears
        WebElement clearButton = driver.findElement(By.id("clearTableButton"));
        clearButton.click();

        WebElement exportCSVButton = driver.findElement(By.id("exportCSVButton"));
        exportCSVButton.click();

        WebElement errorMessage = driver.findElement(By.id("errorMessage"));
        Assert.assertTrue(errorMessage.getText().contains("No data to export"), "Should prevent exporting empty table");
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }
}
