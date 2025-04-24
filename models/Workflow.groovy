package models

/**
 * Represents a GitHub Actions Workflow
 */
class Workflow implements Serializable {
    String name
    String description
    Map<String, Trigger> on
    Map<String, Job> jobs
    Map<String, String> env
    Map<String, String> defaults
    Concurrency concurrency
    Permissions permissions
    
    Workflow() {
        on = [:]
        jobs = [:]
        env = [:]
        defaults = [:]
        permissions = new Permissions()
    }
}