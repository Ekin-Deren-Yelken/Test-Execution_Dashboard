package com.example;
/*
 * Automated Test Execution Dashboard
 * Author: Ekin Yelken
 * 
 * Goal:
 * 1. Upload a test execution results file (CSV)
 * 2. Displauy the test results in a table
 * 3. Filter or sort results
 * 4. Export Results into a certain format
 * 
 * Functionality:
 * 1. GUI
 *      Table, button, scrollable plannel for larger test logs
 *      Tools: SWING, AWT
 * 
 * 2. File Upload and Parsing
 *      User selects CSV
 *      Read the file and extra testName, STATUS, and exeTime
 *      Populate GUI
 * 
 * 3. Data Managment and Error Handling
 *      Smooth Parsing to handle missing or incorrectly formatted data and error messages
 * 
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.util.*;
 import java.util.List;

public class TestExecutionDashboard {
    private JFrame frame, chartFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton uploadButton, clearTableButton, removeFileButton, exportCSVButton, exportJSONButton, showChartButton;
    private JLabel errorLabel;
    private Set<String> uploadedFiles;  // Track uploaded file names
    private JComboBox<String> fileDropdown;
    private JCheckBox filterFailCheckbox, filterTimeCheckbox;
    private ChartPanel chartPanel;
    private JFreeChart chart = null;


    // Constructor
    public TestExecutionDashboard() {
        // Set up Application Window using JFrame
        frame = new JFrame("Test Execution Dashboard");
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Table Model with Column Headers
        {
            String[] columnNames = {"File Name", "Test Name", "Status", "Execution Time", "Errors"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make table cells uneditable
                }
            };
            table = new JTable(tableModel);
            table.setAutoCreateRowSorter(true); // Enable sorting
            frame.add(new JScrollPane(table), BorderLayout.CENTER);
        }
        // Top Panel > sorting and error
        {
            // Add a dropdown menu for sorting
            fileDropdown = new JComboBox<>();
            fileDropdown.addItem("All Files"); // Default Option
            
            // Error Label (Initially hidden)
            errorLabel = new JLabel("");
            errorLabel.setForeground(Color.RED);
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // Remove file button
            removeFileButton = new JButton("Remove File Data");

            // Check box for sorting Errors
            filterFailCheckbox = new JCheckBox("Show only Failed tests");
            filterTimeCheckbox = new JCheckBox("Execution time > 2s");

            filterFailCheckbox.addActionListener(e -> applyFilters());
            filterTimeCheckbox.addActionListener(e -> applyFilters());

            showChartButton = new JButton("Show Test Statistics");
            showChartButton.addActionListener(e -> showChartFrame());
                
            JPanel topPanel = new JPanel();
            topPanel.add(fileDropdown);
            topPanel.add(errorLabel);
            topPanel.add(removeFileButton);

            topPanel.add(filterFailCheckbox);
            topPanel.add(filterTimeCheckbox);

            topPanel.add(showChartButton);

            frame.add(topPanel, BorderLayout.NORTH);
        }
        
        // Bottom Panel > Modifying data in table
        {
            // Upload and Clear Data Buttons
            uploadButton = new JButton("Upload Test Results");
            clearTableButton = new JButton("Clear Table");

            // Export Data Buttons
            exportCSVButton = new JButton("Export CSV");
            exportJSONButton = new JButton("Export JSON");


            JPanel bottomPanel = new JPanel();
            bottomPanel.add(uploadButton);
            bottomPanel.add(clearTableButton);

            bottomPanel.add(exportCSVButton);
            bottomPanel.add(exportJSONButton);

            frame.add(bottomPanel, BorderLayout.SOUTH);
        }

        // Track Uploaded Files
        uploadedFiles = new HashSet<>();

        // Action Listeners
        {
            // Upload
            uploadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadTestResults();
                }
            });

            // Clear
            clearTableButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    clearTable();
                }
            });

            // File Drop-Down Menu
            fileDropdown.addActionListener(e -> {
                filterTableByFile((String) fileDropdown.getSelectedItem());
                updateChart();
            });            

            // Remove
            removeFileButton.addActionListener(e -> removeFileData((String) fileDropdown.getSelectedItem()));
            
            // Export as CSV
            exportCSVButton.addActionListener(e -> exportData("csv"));
            
            // Export as JSON
            exportJSONButton.addActionListener(e -> exportData("json"));
        }

        // Show Frame
        frame.setVisible(true);
    }

    private void loadTestResults() {
        // Open Dialog for file chooser
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        // ensure file isnt already uploaded
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getName(); // get file and set a filename

            if (fileName.contains(".csv")) {
                fileName = fileName.replace(".csv", "");
            }

            // Prevent duplicate file uploads
            if (uploadedFiles.contains(fileName)) {
                showError("File already uploaded: " + fileName);
                return;
            }
            if (!uploadedFiles.contains(fileName)) {
                fileDropdown.addItem(fileName);
            }

            uploadedFiles.add(fileName); // Mark as uploaded by adding it to uploadedfiles

            try (BufferedReader br = new BufferedReader(new FileReader(file))) { //read
                String line;
                int rowCountBefore = tableModel.getRowCount();

                while ((line = br.readLine()) != null) {
                    String[] data = line.split(","); // comma delimiter

                    String testName = (data.length > 0) ? data[0].trim() : "";
                    String status = (data.length > 1) ? data[1].trim() : "";
                    String executionTime = (data.length > 2) ? data[2].trim() : "";
                    String errorMessage = (data.length > 3) ? "Extra columns detected" : "";

                    if (testName.isEmpty() || status.isEmpty() || executionTime.isEmpty()) {
                        errorMessage = "Missing required fields";
                    }

                    tableModel.addRow(new String[]{fileName, testName, status, executionTime, errorMessage});
                }

                if (tableModel.getRowCount() == rowCountBefore) {
                    showError("No valid data found in: " + fileName);
                } else {
                    clearError(); // Clear error message on success
                }

            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error loading file.", "Error", JOptionPane.ERROR_MESSAGE); // mark if there is an error
            }
        }
    }


    // Filter Data Logic
    private void filterTableByFile(String selectedFile) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        if ("All Files".equals(selectedFile)) {
            sorter.setRowFilter(null); // Show all results
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("^" + selectedFile + "$", 0)); // Filter by file name
        }

        //update chartFrame
        fileDropdown.addActionListener(e -> {
            System.out.println("Dropdown changed: " + fileDropdown.getSelectedItem());
            filterTableByFile((String) fileDropdown.getSelectedItem());
            updateChart();
        });        
    }

    // Pass/Fail Frame 
    private void showChartFrame() {
        if (chartFrame == null) {
            chartFrame = new JFrame("Test Statistics");
            chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            chartFrame.setSize(600, 500);
            chartFrame.setLayout(new BorderLayout());
    
            // Explicitly set an empty content pane to prevent overlays
            chartFrame.setContentPane(new JPanel(new BorderLayout()));
    
            // Ensure chartPanel is initialized
            chartPanel = new ChartPanel(null);
            chartFrame.getContentPane().add(chartPanel, BorderLayout.CENTER);
        }
    
        System.out.println("Showing chart frame...");
        chartFrame.setVisible(true); // Show the frame first
        updateChart(); // Ensure the chart updates properly
    
        if (chartFrame.getContentPane().getComponentCount() == 0) {
            System.out.println("Error: No chart detected in chartFrame!");
        } else {
            System.out.println("Chart successfully added to the frame.");
        }
    
        chartFrame.revalidate();
        chartFrame.repaint();
    }

    // Update the chart in the chartFrame
    private void updateChart() {
        if (chartFrame == null || !chartFrame.isVisible()) {
            return; // Don't update if chart is not open
        }
    
        String selectedFile = (String) fileDropdown.getSelectedItem();
        Map<String, Integer> testCounts = getTestCounts(selectedFile);
    
        if (testCounts.get("Pass") == 0 && testCounts.get("Fail") == 0) {
            System.out.println("Warning: No data available for pie chart.");
            return; // Prevent showing an empty chart
        }
    
        System.out.println("Updating chart with: " + testCounts);
    
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Pass", testCounts.getOrDefault("Pass", 0));
        dataset.setValue("Fail", testCounts.getOrDefault("Fail", 0));
    
        if (this.chart == null) { 
            System.out.println("Creating new chart...");
            this.chart = ChartFactory.createPieChart(
                "Test Results for " + selectedFile,
                dataset,
                true, true, false
            );
    
            // Ensure the chart panel is created and resizes correctly
            chartPanel = new ChartPanel(this.chart);
            chartPanel.setPreferredSize(new Dimension(600, 500)); 
            chartFrame.getContentPane().removeAll();
            chartFrame.getContentPane().add(chartPanel, BorderLayout.CENTER);
        } else {
            System.out.println("Updating existing chart...");
            this.chart = ChartFactory.createPieChart(
                "Test Results for " + selectedFile,
                dataset,
                true, true, false
            );
            chartPanel.setChart(this.chart);
        }
    
        chartFrame.revalidate();
        chartFrame.repaint();
    
        System.out.println("Chart updated successfully.");
    }
    
    // Remove File Data Logic
    private void removeFileData(String fileName) {
        
        // Make sure specific data is actually selected
        if ("All Files".equals(fileName)) {
            showError("Select a Specific File to Remove.");
            return;
        }

        for (int i = tableModel.getRowCount() - 1; i >=0; i--) {
            if (tableModel.getValueAt(i, 0).equals(fileName)) {
                tableModel.removeRow(i);
            }
        }

        uploadedFiles.remove(fileName);
        fileDropdown.removeItem(fileName);

        if (fileDropdown.getItemCount() == 0) {
            fileDropdown.setSelectedItem("All Files");
        }
    }
    
    // Apply Error Filter Logic
    private void applyFilters() {
        RowFilter<TableModel, Object> failFilter = null;
        RowFilter<TableModel, Object> timeFilter = null;

        if (filterFailCheckbox.isSelected()) {
            failFilter = RowFilter.regexFilter("^Fail$", 2); // Column 2 is "Status"
        }

        if (filterTimeCheckbox.isSelected()) {
            timeFilter = new RowFilter<TableModel, Object>() {
                @Override
                public boolean include(Entry<? extends TableModel, ? extends Object> entry) {
                    String timeStr = (String) entry.getValue(3); // Column 3 is "Execution Time"
                    return timeStr.endsWith("s") && Double.parseDouble(timeStr.replace("s", "")) > 2;
                }
            };
        }

        List<RowFilter<TableModel, Object>> filters = new ArrayList<>();
        if (failFilter != null) filters.add(failFilter);
        if (timeFilter != null) filters.add(timeFilter);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        table.setRowSorter(sorter);
    }

    // Export Data Logic
    private void exportData(String format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save File");
        fileChooser.setSelectedFile(new File("test_results." + format));
    
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue != JFileChooser.APPROVE_OPTION) return;
    
        File file = fileChooser.getSelectedFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            if (format.equals("csv")) {
                writer.write("File Name,Test Name,Status,Execution Time,Errors\n");
            } else {
                writer.write("[\n"); // Start JSON array
            }
    
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String fileName = (String) tableModel.getValueAt(i, 0);
                String testName = (String) tableModel.getValueAt(i, 1);
                String status = (String) tableModel.getValueAt(i, 2);
                String executionTime = (String) tableModel.getValueAt(i, 3);
                String error = (String) tableModel.getValueAt(i, 4);
    
                if (format.equals("csv")) {
                    writer.write(String.join(",", fileName, testName, status, executionTime, error) + "\n");
                } else {
                    writer.write(String.format(
                        "  {\"file\": \"%s\", \"test\": \"%s\", \"status\": \"%s\", \"time\": \"%s\", \"error\": \"%s\"}%s\n",
                        fileName, testName, status, executionTime, error, i < tableModel.getRowCount() - 1 ? "," : ""
                    ));
                }
            }
    
            if (format.equals("json")) {
                writer.write("]\n"); // Close JSON array
            }
    
            JOptionPane.showMessageDialog(frame, "Export successful: " + file.getAbsolutePath());
        } catch (IOException e) {
            showError("Error exporting file.");
        }
    }


    // Get the number of passed of failed tests showing for teh chartFrame
    private Map<String, Integer> getTestCounts(String selectedFile) {
        int passCount = 0;
        int failCount = 0;
        
        System.out.println("Fetching test counts for: " + selectedFile);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String fileName = (String) tableModel.getValueAt(i, 0);
            String status = (String) tableModel.getValueAt(i, 2);
    
            if ("All Files".equals(selectedFile) || fileName.equals(selectedFile)) {
                if (status.equalsIgnoreCase("Pass")) passCount++;
                if (status.equalsIgnoreCase("Fail")) failCount++;
            }
        }
        
        System.out.println("Collected Test Counts: Pass=" + passCount + ", Fail=" + failCount); // Debugging

        Map<String, Integer> counts = new HashMap<>();
        counts.put("Pass", passCount);
        counts.put("Fail", failCount);
        return counts;
    }
    
    

    private void clearTable() {
        tableModel.setRowCount(0);
        uploadedFiles.clear();
        clearError();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void clearError() {
        errorLabel.setText("");
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(TestExecutionDashboard::new);
    }
}
