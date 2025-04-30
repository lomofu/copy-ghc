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

    Object evaluate(String expression) {
        // 移除表达式中的 ${{ }} 部分
        expression = expression.trim()
        if (expression.startsWith('${{') && expression.endsWith('}}')) {
            expression = expression[3..-3].trim()
        }

        // 解析并执行表达式
        def shell = new GroovyShell(binding)
        return shell.evaluate(expression)
    }
}

// 使用示例
def evaluator = new ExpressionEvaluator()

// 设置上下文变量
evaluator.setVariable("env", [
        GITHUB_WORKFLOW: "CI Pipeline",
        GITHUB_ACTOR: "lomofu"
])
evaluator.setVariable("github", [
        repository: "lomofu/my-repo",
        event: [
                ref: "refs/heads/main"
        ]
])

// 示例1: 取值
def result1 = evaluator.evaluate('${{ env.GITHUB_WORKFLOW }}')
println "取值示例: $result1"

// 示例2: 函数调用
def result2 = evaluator.evaluate('${{ contains(github.repository, "lomofu") }}')
println "函数示例: $result2"

// 示例3: 条件判断
def result3 = evaluator.evaluate('${{ github.event.ref == "refs/heads/main" ? "生产环境" : "测试环境" }}')
println "条件示例: $result3"

// 示例4: 复杂表达式
def result4 = evaluator.evaluate('''${{ 
    env.GITHUB_ACTOR == "lomofu" && 
    startsWith(github.repository, "lomofu") ? 
    format("欢迎 %s!", env.GITHUB_ACTOR) : 
    "访问受限" 
}}''')
println "复杂表达式: $result4"