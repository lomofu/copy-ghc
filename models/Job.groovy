package models

/**
 * Represents a job in a GitHub Actions workflow
 */
class Job implements Serializable {
    String name
    String runsOn
    Map<String, String> needs
    Map<String, String> outputs
    Map<String, String> env
    String if_condition
    Strategy strategy
    String timeoutMinutes
    List<Step> steps
    Environment environment
    Container container
    Map<String, Service> services
    Concurrency concurrency
    Permissions permissions
    
    Job() {
        needs = [:]
        outputs = [:]
        env = [:]
        steps = []
        strategy = new Strategy()
        services = [:]
    }
}