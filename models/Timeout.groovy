package models

/**
 * Represents timeout configuration in GitHub Actions
 */
class Timeout implements Serializable {
    Integer minutes
    Integer hours
    
    Timeout() {
        minutes = 360  // Default GitHub Actions timeout is 6 hours
        hours = 6
    }
}