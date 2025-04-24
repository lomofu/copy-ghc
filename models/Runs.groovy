package models

/**
 * Represents the execution configuration for a GitHub Action
 */
class Runs implements Serializable {
    String using
    String main
    String pre
    String post
    Map<String, String> env
    
    // For composite actions
    List<Step> steps
    
    // For Docker actions
    String image
    List<String> entrypoint
    List<String> args
    
    Runs() {
        env = [:]
        steps = []
        args = []
        entrypoint = []
    }
}