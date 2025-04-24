package models

/**
 * Represents a cache configuration in GitHub Actions
 */
class Cache implements Serializable {
    String path
    String key
    List<String> restoreKeys
    
    Cache() {
        restoreKeys = []
    }
}