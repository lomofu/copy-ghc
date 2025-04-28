def graph = new DepGrap()

// Add jobs with closures
graph.addJob("build", { println "Building project..." })
graph.addJob("test", { println "Running tests..." })
graph.addJob("deploy", { println "Deploying application..." })
graph.addJob("notify", { println "Sending notifications..." })

// Add dependencies
graph.addDependency("test", "build")
graph.addDependency("deploy", "test")
graph.addDependency("notify", "deploy")

// Execute all jobs in the correct order
def jobs = graph.findParallelJobs()

jobs.forEach {
    it.forEach {
        println "Executing job: ${it.name}"
        it.closure.call()
    }
}
