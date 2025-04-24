package models

/**
 * Represents a strategy for job execution in GitHub Actions
 */
class Strategy implements Serializable {
    Matrix matrix
    Boolean failFast
    Integer maxParallel
    
    Strategy() {
        matrix = new Matrix()
        failFast = true
    }
}