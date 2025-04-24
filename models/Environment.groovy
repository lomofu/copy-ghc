package models

/**
 * Represents a deployment environment in GitHub Actions
 */
class Environment implements Serializable {
    String name
    String url
    List<String> reviewers
    
    Environment() {
        reviewers = []
    }
}