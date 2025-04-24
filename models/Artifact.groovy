package models

/**
 * Represents an artifact in GitHub Actions
 */
class Artifact implements Serializable {
    String name
    String path
    String retention
    Boolean ifNoFilesFound
    
    // Options for ifNoFilesFound: 'warn', 'error', 'ignore'
    Artifact() {
        ifNoFilesFound = false
        retention = "90"  // Default retention in days
    }
}