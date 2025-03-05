# Test Execution Dashboard with Performance Analytics

## Overview
This project is a **Java-based Test Execution Dashboard** that enables users to **upload test results**, **filter execution data**, and **visualize performance metrics** through interactive charts. It provides insights into test case performance and execution trends.

## Features

### Test Result Management
- Upload test result CSV files.
- Display execution details in a **read-only table**.
- Apply filters for **pass/fail status** and **execution time**.
- Remove specific test data or clear all results.
- Export test results as **CSV or JSON**.

### Data Visualization
- **Pie Chart:** Displays the **pass/fail distribution** of test cases.
- Charts update dynamically based on **dropdown selection**.
- Implemented using **JFreeChart** in separate UI windows.

### Testing & Automation
- **TestNG** for unit testing.
- **Selenium WebDriver** for UI automation.
- **Maven** for dependency management.

## Key Challenges & Solutions

| Issue | Solution |
|-------|----------|
| Pie chart not appearing | Ensured `updateChart()` runs after `chartFrame.setVisible(true);` |
| Extra panel covering chart | Used `chartFrame.setContentPane(new JPanel(new BorderLayout()))` to prevent overlay |
| Chart not resizing properly | Applied `chartPanel.setPreferredSize(new Dimension(600, 500));` |
| Graphs not updating dynamically | Updated dataset and repainted using `chartPanel.setChart(chart);` |
| UI not updating smoothly | Used `chartFrame.revalidate();` and `chartFrame.repaint();` |

## Technologies Used
- **Java (Swing, AWT)** – GUI development
- **JFreeChart** – Data visualization
- **Apache Maven** – Dependency management
- **TestNG** – Unit testing
- **JUnit & Selenium WebDriver** – UI testing automation

## Future Enhancements
- Improve UI layout for better scalability.
- Add error handling for invalid CSV formats.
- Expand test automation coverage using Selenium.
- Implement execution time forecasting for trend analysis.

This project provides a structured approach to **test execution monitoring and performance analysis**, allowing users to identify trends and optimize test efficiency.
