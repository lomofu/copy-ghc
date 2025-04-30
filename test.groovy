import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ExpressionEvaluator {
    private Binding binding

    ExpressionEvaluator(Map<String, Object> context = [:]) {
        binding = new Binding(context)
        // 添加内置函数
        addBuiltInFunctions()
    }

    private void addBuiltInFunctions() {
        // 添加一些类似 GitHub Actions 内置函数
        binding.setVariable("contains", { String str, String search -> str.contains(search) })
        binding.setVariable("startsWith", { String str, String prefix -> str.startsWith(prefix) })
        binding.setVariable("endsWith", { String str, String suffix -> str.endsWith(suffix) })
        binding.setVariable("format", { String format, Object... args -> String.format(format, args) })
        binding.setVariable("join", { List items, String separator -> items.join(separator) })
        binding.setVariable("toJSON", { Object obj -> new JsonBuilder(obj).toString() })
        binding.setVariable("fromJSON", { String json -> new JsonSlurper().parseText(json) })
    }

    void setVariable(String name, Object value) {
        binding.setVariable(name, value)
    }

    Object evaluate(String input) {
        // Regular expression to find ${{ ... }} patterns
        def pattern = /\$\{\{(.*?)\}\}/
        def matcher = input =~ pattern

        // If no placeholders found, return the original string
        if (!matcher.find()) {
            return input
        }

        // Reset matcher to start from beginning
        matcher.reset()

        // StringBuilder for the result
        StringBuilder result = new StringBuilder(input)

        // Track offset as we replace parts of the string
        int offset = 0

        // Process each placeholder
        while (matcher.find()) {
            // Get the full match and the expression inside
            String fullMatch = matcher.group(0)
            String expression = matcher.group(1).trim()

            // Position in the original string
            int start = matcher.start() + offset
            int end = matcher.end() + offset

            // Evaluate the expression
            def value = evaluateExpression(expression)

            // Replace the placeholder with the evaluated result
            result.replace(start, end, value.toString())

            // Update offset for next replacement
            offset += value.toString().length() - fullMatch.length()
        }

        return result.toString()
    }

// Helper method to evaluate a single expression
    private Object evaluateExpression(String expression) {
        def shell = new GroovyShell(binding)
        return shell.evaluate(expression)
    }
}

// 使用示例
def evaluator = new ExpressionEvaluator()

// 设置上下文变量
evaluator.setVariable("env", [
        GITHUB_WORKFLOW: "CI Pipeline",
        GITHUB_ACTOR   : "lomofu"
])
evaluator.setVariable("github", [
        repository: "lomofu/my-repo",
        event     : [
                ref: "refs/heads/main"
        ]
])

// 示例1: 取值
def result1 = evaluator.evaluate('echo for ${{ env.GITHUB_WORKFLOW }}')
println "取值示例: $result1"