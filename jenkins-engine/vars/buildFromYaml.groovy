#!/usr/bin/env groovy
import org.ci.YamlPipelineGenerator

/**
 * Jenkins共享库入口点，用于将YAML元数据转换为pipeline并执行
 *
 * @param params 包含工作流元数据和仓库信息的参数
 * @return pipeline执行的结果
 */
def call(Map params = [:]) {
    // 提取参数
    String workflowMetadata = params.workflow_metadata
    String repoName = params.repo_name
    String repoUrl = params.repo_url
    String branch = params.branch
    String commitId = params.commit_id
    
    // 记录接收到的参数
    echo "接收到工作流元数据: ${workflowMetadata}"
    echo "仓库: ${repoName}, 分支: ${branch}, 提交: ${commitId}"
    
    // 创建仓库信息Map
    def repoInfo = [
        repoName: repoName,
        repoUrl: repoUrl,
        branch: branch,
        commitId: commitId
    ]
    
    // 创建YAML pipeline生成器
    def generator = new YamlPipelineGenerator(this, workflowMetadata, repoInfo)
    
    // 生成pipeline数据结构
    def pipelineStructure = generator.generatePipeline()
    
    // 将数据结构转换为Jenkins pipeline
    echo "生成pipeline数据结构..."
    executePipeline(pipelineStructure)
}

/**
 * 根据生成的数据结构执行pipeline
 */
def executePipeline(def pipelineStructure) {
    // 设置agent
    node(getAgentLabel(pipelineStructure.agent)) {
        try {
            // 应用选项
            applyOptions(pipelineStructure.options)
            
            // 设置环境变量
            withEnv(createEnvironmentList(pipelineStructure.environment)) {
                

                // 执行stages
                executeStages(pipelineStructure.stages)
            }
            
            // 成功后执行
            if (pipelineStructure.post?.success) {
                executeSteps(pipelineStructure.post.success)
            }
        } catch (Exception e) {
            // 失败后执行
            if (pipelineStructure.post?.failure) {
                executeSteps(pipelineStructure.post.failure)
            }
            throw e
        } finally {
            // 总是执行
            if (pipelineStructure.post?.always) {
                executeSteps(pipelineStructure.post.always)
            }
        }
    }
}

/**
 * 获取agent标签
 */
def getAgentLabel(def agent) {
    if (agent.label) {
        return agent.label
    } else if (agent.any) {
        return ''  // 任何可用的agent
    } else {
        return ''  // 默认
    }
}

/**
 * 应用pipeline选项
 */
def applyOptions(def options) {
    if (options.timestamps) {
        timestamps {
            echo "启用时间戳"
        }
    }
    
    if (options.timeout) {
        timeout(time: options.timeout.time, unit: options.timeout.unit) {
            echo "设置超时: ${options.timeout.time} ${options.timeout.unit}"
        }
    }
}

/**
 * 创建环境变量列表
 */
def createEnvironmentList(def environment) {
    def envList = []
    environment.each { key, value ->
        envList.add("${key}=${value}")
    }
    return envList
}

/**
 * 执行所有阶段
 */
def executeStages(def stages) {
    stages.each { stageConfig ->
        stage(stageConfig.name) {
            // 处理条件
            if (stageConfig.when) {
                when {
                    expression { 
                        return evaluateCondition(stageConfig.when)
                    }
                }
            }
            
            // 执行步骤
            executeSteps(stageConfig.steps)
        }
    }
}

/**
 * 执行步骤列表
 */
def executeSteps(def steps) {
    steps.each { step ->
        executeStep(step)
    }
}

/**
 * 执行单个步骤
 */
def executeStep(def step) {
    switch (step.type) {
        case 'shell':
            if (step.name) {
                echo "执行步骤: ${step.name}"
            }
            sh script: step.script, returnStatus: false
            break
            
        case 'echo':
            echo step.message
            break
            
        case 'cleanWs':
            cleanWs()
            break
            
        case 'checkout':
            checkout([
                $class: 'GitSCM',
                branches: step.params.branches,
                userRemoteConfigs: step.params.userRemoteConfigs
            ])
            break
            
        case 'action':
            executeAction(step.action, step.params)
            break
            
        default:
            echo "未知步骤类型: ${step.type}"
    }
}

/**
 * 执行action
 */
def executeAction(String actionPath, Map params) {
    // 分解action路径，例如 'checkout@v1' 或 'actions/checkout@v1'
    def parts = actionPath.split('@')
    def actionName = parts[0]
    def actionVersion = parts.length > 1 ? parts[1] : 'latest'
    
    // 执行action
    actionExecutor(actionName: actionName, actionVersion: actionVersion, actionParams: params)
}

/**
 * 评估条件表达式
 */
def evaluateCondition(String condition) {
    // 简化版，实际实现需要更复杂的条件评估
    return eval(condition)
}