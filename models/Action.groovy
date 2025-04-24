package models

/**
 * Represents a GitHub Action
 */
class Action implements Serializable {
    String name
    String description
    Map<String, Input> inputs
    Map<String, Output> outputs
    Runs runs
    String author
    String branding

    Action() {
        inputs = [:]
        outputs = [:]
        runs = new Runs()
    }
}