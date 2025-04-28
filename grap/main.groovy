def graph = new DepGrap()

// Add jobs with closures
graph.addJob("build", { println "Building project..." })
graph.addJob("build1", { println "Building project1..." })
graph.addJob("build2", { println "Building project2..." })
graph.addJob("test", { println "Running tests..." })
graph.addJob("test1", { println "Running tests1..." })
graph.addJob("deploy", { println "Deploying application..." })
graph.addJob("notify", { println "Sending notifications..." })

// Add dependencies
graph.addDependency("test1", "build1")
graph.addDependency("test", "build")
graph.addDependency("deploy", "test")
graph.addDependency("notify", "deploy")

// Execute all jobs in the correct order
def jobs = graph.findParallelJobs()
println jobs

