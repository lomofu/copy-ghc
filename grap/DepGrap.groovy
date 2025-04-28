class DepGrap {
    // Node class to store both name and closure
    class Node {
        String name
        Closure closure

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
            return name + "(" + closure + ")"
        }
    }

    // Map to store dependencies: job name -> set of job names it depends on
    private Map<String, Set<String>> dependencies = [:]
    // Map to store nodes (name -> Node)
    private Map<String, Node> nodes = [:]

    /**
     * Add a job with no dependencies
     * @param name The job name
     * @param closure The job closure
     */
    void addJob(String name, Closure closure) {
        nodes[name] = new Node(name, closure)
    }

    /**
     * Add a dependency between two jobs
     * @param job The job name that depends on another
     * @param dependency The job name that is needed
     */
    void addDependency(String job, String dependency) {
        // Check for self-dependency (cycle)
        if (job == dependency) {
            throw new IllegalArgumentException("Self-dependency detected: job '${job}' cannot depend on itself.")
        }

        // Ensure both jobs exist in the nodes map
        if (!nodes.containsKey(job)) {
            throw new IllegalArgumentException("Job '${job}' does not exist")
        }
        if (!nodes.containsKey(dependency)) {
            throw new IllegalArgumentException("Job '${dependency}' does not exist")
        }

        // Add to dependencies map
        if (!dependencies.containsKey(job)) {
            dependencies[job] = [] as Set
        }
        dependencies[job].add(dependency)
    }

    /**
     * Find all jobs that can be executed in parallel, grouped by stages
     * @return List of sets, each set containing Node objects that can run in parallel
     */
    List<Set<Node>> findParallelJobs() {
        List<Set<Node>> stages = []
        Set<String> processedJobs = [] as Set
        Set<String> allJobNames = nodes.keySet()

        // Continue until all jobs are processed
        while (processedJobs.size() < allJobNames.size()) {
            // Find jobs with no unprocessed dependencies
            Set<String> currentStageNames = allJobNames.findAll { job ->
                // If job has no dependencies, or all its dependencies are already processed
                !dependencies.containsKey(job) ||
                        dependencies[job].every { dep -> processedJobs.contains(dep) }
            } - processedJobs

            if (currentStageNames.isEmpty()) {
                throw new IllegalStateException("Circular dependency detected or no jobs available to process.")
            }

            // Convert job names to Node objects
            Set<Node> currentStage = currentStageNames.collect { jobName ->
                nodes[jobName]
            } as Set

            // Add current stage to result
            stages.add(currentStage)

            // Mark jobs in this stage as processed
            processedJobs.addAll(currentStageNames)
        }

        return stages
    }
}