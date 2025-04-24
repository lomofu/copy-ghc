package models

/**
 * Represents a container configuration in GitHub Actions
 */
class Container implements Serializable {
    String image
    String credentials
    Map<String, String> env
    List<String> ports
    List<String> volumes
    String options
    
    Container() {
        env = [:]
        ports = []
        volumes = []
    }
}