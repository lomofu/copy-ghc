package models

/**
 * Represents an output parameter for a GitHub Action
 */
class Output implements Serializable {
    String description
    String value
}