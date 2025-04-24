package models

/**
 * Represents a workflow trigger in GitHub Actions
 */
class Trigger implements Serializable {
    String event
    List<String> branches
    List<String> branchesIgnore
    List<String> tags
    List<String> tagsIgnore
    List<String> paths
    List<String> pathsIgnore
    Map<String, String> types
    
    Trigger() {
        branches = []
        branchesIgnore = []
        tags = []
        tagsIgnore = []
        paths = []
        pathsIgnore = []
        types = [:]
    }
}