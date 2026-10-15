package com.waterbill.dto;

import java.util.ArrayList;
import java.util.List;

public class CsvUploadResult {

    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private int skippedDuplicates;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public CsvUploadResult() {}

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSuccessfulRows() { return successfulRows; }
    public void setSuccessfulRows(int successfulRows) { this.successfulRows = successfulRows; }

    public int getFailedRows() { return failedRows; }
    public void setFailedRows(int failedRows) { this.failedRows = failedRows; }

    public int getSkippedDuplicates() { return skippedDuplicates; }
    public void setSkippedDuplicates(int skippedDuplicates) { this.skippedDuplicates = skippedDuplicates; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public void addError(String error) { this.errors.add(error); }
    public void addWarning(String warning) { this.warnings.add(warning); }
}
