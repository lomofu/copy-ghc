package models

/**
 * Represents a matrix configuration for GitHub Actions
 */
class Matrix implements Serializable {
    Map<String, List<Object>> dimensions
    List<Map<String, Object>> include
    List<Map<String, Object>> exclude
    
    Matrix() {
        dimensions = [:]
        include = []
        exclude = []
    }
}