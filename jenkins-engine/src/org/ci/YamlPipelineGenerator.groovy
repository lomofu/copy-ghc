package org.ci

import groovy.json.JsonSlurper

/**
 * YamlPipelineGenerator - 将YAML元数据转换为Jenkins pipeline代码
 * 这个类专注于分析YAML工作流并生成对应的Jenkins Pipeline定义
 */
class YamlPipelineGenerator implements Serializable {
    private def script
    private def yamlMetadata
    private def repoInfo
    
    // 映射器，用于存储可用的action处理器
    private Map<String, Closure> actionHandlers = [:]

    YamlPipelineGenerator(script, String yamlMetadataJson, Map repoInfo) {
        this.script = script
        def jsonSlurper = new JsonSlurper()
        this.yamlMetadata = jsonSlurper.parseText(yamlMetadataJson)
        this.repoInfo = repoInfo
        
        // 注册默认的action处理器
        registerDefaultActionHandlers()
    }
    
    /**
     * 注册默认的action处理器
     */
    private void registerDefaultActionHandlers() {
        // 注册checkout action处理器
        actionHandlers['checkout'] = { params ->
            return {
                // 使用actionExecutor来执行action
                script.actionExecutor(actionName: 'checkout', actionParams: params)
            }
        }
        
        // 可以在这里注册其他默认action处理器
    }
    
    /**
     * 注册一个自定义action处理器
     */
    void registerActionHandler(String actionName, Closure handler) {
        actionHandlers[actionName] = handler
    }

    /**
     * 生成并返回完整的pipeline脚本
     * 这里不使用字符串拼接，而是返回一个代表pipeline的数据结构
     */
    def generatePipeline() {
        def pipeline = [
            agent: generateAgent(),
            options: generateOptions(),
            environment: generateEnvironment(),
            stages: generateStages(),
            post: generatePost()
        ]
        
        return pipeline
    }

    /**
     * 生成agent部分
     */
    private def generateAgent() {
        if (yamlMetadata.containsKey('runs-on')) {
            return [label: yamlMetadata['runs-on']]
        } else {
            return [any: true]
        }
    }

    /**
     * 生成options部分
     */
    private def generateOptions() {
        def options = [
            timestamps: true,
            timeout: [time: 60, unit: 'MINUTES']
        ]
        
        // 添加自定义选项
        if (yamlMetadata.containsKey('options')) {
            def yamlOptions = yamlMetadata['options']
            if (yamlOptions.containsKey('timeout')) {
                options.timeout = [
                    time: yamlOptions.timeout.time,
                    unit: yamlOptions.timeout.unit.toUpperCase()
                ]
            }
        }
        
        return options
    }

    /**
     * 生成environment部分
     */
    private def generateEnvironment() {
        def env = [
            REPO_NAME: repoInfo.repoName,
            BRANCH_NAME: repoInfo.branch,
            COMMIT_ID: repoInfo.commitId
        ]
        
        // 添加自定义环境变量
        if (yamlMetadata.containsKey('env')) {
            yamlMetadata.env.each { key, value ->
                env[key] = value
            }
        }
        
        return env
    }

    /**
     * 生成stages部分
     */
    private def generateStages() {
        def stages = []
        
        if (yamlMetadata.containsKey('jobs')) {
            yamlMetadata.jobs.each { jobName, jobConfig ->
                def stage = [
                    name: jobName,
                    steps: generateJobSteps(jobConfig),
                ]
                
                // 添加条件
                if (jobConfig.containsKey('if')) {
                    stage.when = parseCondition(jobConfig.if)
                }
                
                stages << stage
            }
        }
        
        return stages
    }

    /**
     * 为job生成steps
     */
    private def generateJobSteps(def jobConfig) {
        def steps = []
        
        // 添加checkout步骤
        steps << [
            type: 'checkout',
            params: [
                branches: [[name: repoInfo.branch]],
                userRemoteConfigs: [[url: repoInfo.repoUrl]]
            ]
        ]
        
        // 添加其他步骤
        if (jobConfig.containsKey('steps')) {
            jobConfig.steps.each { step ->
                steps << parseStep(step)
            }
        }
        
        return steps
    }

    /**
     * 解析单个step
     */
    private def parseStep(def step) {
        if (step.containsKey('run')) {
            // Shell命令
            return [
                type: 'shell',
                script: step.run
            ]
        } else if (step.containsKey('uses')) {
            // 自定义action
            def actionPath = step.uses
            def actionParams = step.with ?: [:]
            
            return [
                type: 'action',
                action: actionPath,
                params: actionParams
            ]
        } else if (step.containsKey('name') && step.containsKey('script')) {
            // 命名脚本步骤
            return [
                type: 'shell',
                name: step.name,
                script: step.script
            ]
        }
        
        return null
    }
    
    /**
     * 解析条件表达式
     */
    private def parseCondition(String condition) {
        // 这是一个简化版的实现 - 实际实现中需要更复杂的条件解析
        return condition
    }

    /**
     * 生成post部分
     */
    private def generatePost() {
        def post = [
            always: [
                [type: 'echo', message: 'Cleaning up workspace'],
                [type: 'cleanWs']
            ],
            success: [
                [type: 'echo', message: 'Build completed successfully']
            ],
            failure: [
                [type: 'echo', message: 'Build failed']
            ]
        ]
        
        return post
    }
    
    /**
     * 执行action
     */
    def executeAction(String actionName, Map params) {
        if (actionHandlers.containsKey(actionName)) {
            def handler = actionHandlers[actionName]
            return handler(params)
        } else {
            script.echo "未找到action处理器: ${actionName}"
            return { script.error "无法执行action: ${actionName}" }
        }
    }
}