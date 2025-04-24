package models

/**
 * Represents a secret used in GitHub Actions
 */
class Secret implements Serializable {
    String name
    String description
    Boolean required = false
}