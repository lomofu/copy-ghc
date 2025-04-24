package models

/**
 * Represents a workflow_call event for reusable workflows
 */
class WorkflowCall implements Serializable {
    Map<String, Input> inputs
    Map<String, Output> outputs
    Map<String, Secret> secrets
    
    WorkflowCall() {
        inputs = [:]
        outputs = [:]
        secrets = [:]
    }
}