package models

/**
 * Represents a step in a GitHub Actions workflow or composite action
 */
class Step implements Serializable {
    String id
    String name
    String uses
    String run
    String shell
    String workingDirectory
    Map<String, String> with
    Map<String, String> env
    Boolean continueOnError
    String timeoutMinutes
    String if_condition
    
    // For cache and artifact operations
    Cache cache
    Artifact artifact
    
    Step() {
        with = [:]
        env = [:]
        continueOnError = false
    }
}