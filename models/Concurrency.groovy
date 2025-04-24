package models

/**
 * Represents concurrency configuration in GitHub Actions
 */
class Concurrency implements Serializable {
    String group
    Boolean cancelInProgress
    
    Concurrency() {
        cancelInProgress = false
    }
}