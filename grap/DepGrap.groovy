class DepGrap {
    class Node {
        final String name
        final Closure closure

        Node(String name, Closure closure) {
            this.name = name
            this.closure = closure
        }

        @Override
        boolean equals(Object obj) {
            if (!(obj instanceof Node)) return false
            return this.name == ((Node) obj).name
        }

        @Override
        int hashCode() {
            return name.hashCode()
        }

        @Override
        String toString() {
            return name
        }
    }

    // Using immutable collections where possible for better thread safety
    private final Map<String, Set<String>> dependencies = [:].withDefault { [] as Set }
    private final Map<String, Node> nodes = [:]

    /**
     * Add a job with no dependencies
     */
    void addJob(String name, Closure closure) {
        nodes[name] = new Node(name, closure)
    }

    /**
     * Add a dependency between two jobs
     */
    void addDependency(String job, String dependency) {
        // Self-dependency check
        if (job == dependency) {
            throw new IllegalArgumentException("Self-dependency detected: '$job' cannot depend on itself")
        }

        // Validate both jobs exist
        [job, dependency].each { checkJobExists(it) }

        // Add to dependencies
        dependencies[job] << dependency
    }

    /**
     * Find all jobs that can be executed in parallel, grouped by stages
     */
    List<Set<Node>> findParallelJobs() {
        final List<Set<Node>> stages = []
        final Set<String> processed = [] as Set
        final Set<String> allJobs = nodes.keySet()

        while (processed.size() < allJobs.size()) {
            // Find all jobs that can run in current stage (with all dependencies satisfied)
            Set<String> currentStageNames = allJobs.findAll { job ->
                !processed.contains(job) && // Not yet processed
                        (dependencies[job].isEmpty() || // No dependencies
                                dependencies[job].every { processed.contains(it) }) // All deps processed
            }

            if (currentStageNames.isEmpty()) {
                throw new IllegalStateException("Circular dependency detected")
            }

            // Convert names to nodes and add stage
            stages << currentStageNames.collect { nodes[it] } as Set
            processed.addAll(currentStageNames)
        }

        return stages
    }

    /**
     * Execute all jobs in topological order
     */
    void execute() {
        findParallelJobs().eachWithIndex { stage, index ->
            println "Stage ${index + 1}: ${stage*.name.join(', ')}"

            // In real implementation, you could use GPars or parallel streams here
            stage.each { Node node ->
                println "  Running job: ${node.name}"
                node.closure.call()
            }
        }
    }

    private void checkJobExists(String jobName) {
        if (!nodes.containsKey(jobName)) {
            throw new IllegalArgumentException("Job '$jobName' does not exist")
        }
    }

    @Override
    String toString() {
        "DepGrap(jobs: ${nodes.keySet()}, dependencies: ${dependencies.findAll { !it.value.isEmpty() }})"
    }
}