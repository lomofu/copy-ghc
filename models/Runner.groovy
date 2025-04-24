package models

/**
 * Represents a GitHub Actions runner configuration
 */
class Runner implements Serializable {
    String os
    String architecture
    List<String> labels
    String name
    String group
    
    Runner() {
        labels = []
    }
}