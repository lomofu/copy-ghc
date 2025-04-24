package models

/**
 * Represents permissions for the GITHUB_TOKEN in a workflow
 */
class Permissions implements Serializable {
    // Can be 'read', 'write', or 'none'
    String actions
    String checks
    String contents
    String deployments
    String discussions
    String idToken
    String issues
    String packages
    String pages
    String pullRequests
    String repositoryProjects
    String securityEvents
    String statuses
    
    // Constructor sets default to read-only
    Permissions() {
        actions = "read"
        checks = "read"
        contents = "read"
        deployments = "read"
        discussions = "read"
        idToken = "read"
        issues = "read"
        packages = "read"
        pages = "read"
        pullRequests = "read"
        repositoryProjects = "read"
        securityEvents = "read"
        statuses = "read"
    }
}