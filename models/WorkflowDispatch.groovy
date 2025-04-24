package models

/**
 * Represents a workflow_dispatch event for manual workflow triggering
 */
class WorkflowDispatch implements Serializable {
    Boolean enabled = true
    Map<String, InputParameter> inputs
    
    WorkflowDispatch() {
        inputs = [:]
    }
    
    /**
     * Inner class to represent input parameters for workflow_dispatch events
     */
    static class InputParameter implements Serializable {
        String description
        String defaultValue
        Boolean required = false
        String type  // string, boolean, choice, environment
        List<String> options  // For choice type
        
        InputParameter() {
            type = "string"
            options = []
        }
    }
}