#!/usr/bin/env groovy

/**
 * Execute the checkout action
 * 
 * @param params The parameters for the action
 * @return The exit code of the action execution
 */
def execute(Map params) {
    // Convert parameters to JSON
    def jsonParams = groovy.json.JsonOutput.toJson(params)
    
    // Execute the Python action script with the parameters
    def result = sh(
        script: "python3 ${WORKSPACE}/actions/checkout/action.py '${jsonParams}'",
        returnStatus: true
    )
    
    if (result != 0) {
        error "Checkout action failed with exit code ${result}"
    }
    
    return result
}

// Return this as a Groovy closure
return this