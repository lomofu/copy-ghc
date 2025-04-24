package models

/**
 * Represents an input parameter for a GitHub Action
 */
class Input implements Serializable {
    String description
    Boolean required = false
    String defaultValue
    String deprecationMessage
    
    // For choice-type inputs
    List<String> options
}