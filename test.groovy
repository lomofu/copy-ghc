import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

class GitHubExpressionEvaluator {
    private Map context
    private MarkupTemplateEngine engine
    
    GitHubExpressionEvaluator(Map contextData) {
        this.context = contextData
        
        // 配置 MarkupTemplateEngine
        def config = new TemplateConfiguration()
        config.setAutoEscape(false)
        config.setAutoIndent(false)
        config.setAutoNewLine(false)
        
        this.engine = new MarkupTemplateEngine(this.class.classLoader, config)
    }
    
    String evaluate(String text) {
        // 找出所有的 ${{ ... }} 表达式
        def pattern = ~/\$\{\{\s*(.*?)\s*\}\}/
        def matcher = pattern.matcher(text)
        def result = new StringBuilder(text)
        def offset = 0
        
        // 处理每个匹配的表达式
        while (matcher.find()) {
            def fullMatch = matcher.group(0)
            def expression = matcher.group(1)
            def startPos = matcher.start() + offset
            def endPos = matcher.end() + offset
            
            // 直接使用 groovy 表达式求值
            try {
                def evaluated = evaluateExpression(expression)
                
                // 替换原始文本中的表达式
                result.replace(startPos, endPos, evaluated.toString())
                
                // 调整偏移量以适应替换后的文本长度变化
                offset += (evaluated.toString().length() - fullMatch.length())
            } catch (Exception e) {
                def errorMsg = "Error: ${e.message}"
                result.replace(startPos, endPos, errorMsg)
                offset += (errorMsg.length() - fullMatch.length())
            }
        }
        
        return result.toString()
    }
    
    private Object evaluateExpression(String expression) {
        // 创建包含上下文数据的 binding
        def binding = new Binding(context)
        
        // 使用 GroovyShell 直接评估表达式
        def shell = new GroovyShell(binding)
        return shell.evaluate(expression)
    }
}

// 示例使用
def context = [
    github: [
        repository: 'user/repo',
        ref: 'refs/heads/main',
        event: [
            action: 'push'
        ]
    ],
    env: [
        USERNAME: 'groovy-user',
        API_TOKEN: 'secret-token'
    ],
    vars: [
        MY_VAR: 'custom-value'
    ],
    // 添加一些辅助函数
    contains: { a, b -> a.toString().contains(b) },
    startsWith: { a, b -> a.toString().startsWith(b) },
    endsWith: { a, b -> a.toString().endsWith(b) }
]

def evaluator = new GitHubExpressionEvaluator(context)

// 测试基本表达式
println "基本表达式测试:"
println evaluator.evaluate("Repository: \${{ github.repository }}")
println evaluator.evaluate("Branch: \${{ github.ref }}")
println evaluator.evaluate("用户: \${{ env.USERNAME }}")

// 测试复杂表达式
println "\n复杂表达式测试:"
println evaluator.evaluate("条件判断: \${{ github.repository == 'user/repo' ? 'Yes' : 'No' }}")
println evaluator.evaluate("函数调用: \${{ contains(github.ref, 'main') ? '主分支' : '其他分支' }}")
println evaluator.evaluate("字符串操作: \${{ github.repository.toUpperCase() }}")