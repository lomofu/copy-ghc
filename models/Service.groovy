package models

/**
 * Represents a service container in GitHub Actions
 */
class Service implements Serializable {
    String image
    Map<String, String> credentials
    Map<String, String> env
    List<Integer> ports
    List<String> volumes
    String options
    
    Service() {
        credentials = [:]
        env = [:]
        ports = []
        volumes = []
    }
}