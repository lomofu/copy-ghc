#!/usr/bin/env groovy

/**
 * Action执行器 - 负责调用Python-based actions并处理结果
 *
 * @param actionName action名称
 * @param actionVersion action版本
 * @param actionParams action参数
 * @return 执行结果
 */
def call(Map params = [:]) {
    String actionName = params.actionName
    String actionVersion = params.actionVersion ?: 'latest'
    Map actionParams = params.actionParams ?: [:]
    
    echo "执行action: ${actionName}@${actionVersion}"
    
    // 解析action路径
    def (namespace, name) = parseActionPath(actionName)
    
    // 获取action定义
    def actionDef = getActionDefinition(namespace, name, actionVersion)
    
    // 验证必填参数
    validateRequiredParams(actionDef, actionParams)
    
    // 准备action的输入参数
    def preparedParams = prepareActionInputs(actionDef, actionParams)
    
    // 执行action
    def result = executeAction(actionDef, preparedParams)
    
    return result
}

/**
 * 解析action路径（例如: 'actions/checkout' 或 'checkout'）
 */
def parseActionPath(String actionName) {
    if (actionName.contains('/')) {
        def parts = actionName.split('/')
        return [parts[0], parts[1]]
    } else {
        return ['actions', actionName]
    }
}

/**
 * 获取action的定义（来自action.yml文件）
 */
def getActionDefinition(String namespace, String name, String version) {
    // 构建action目录路径
    def actionPath = "${WORKSPACE}/actions/${name}"
    if (namespace != 'actions') {
        actionPath = "${WORKSPACE}/${namespace}/${name}"
    }
    
    // 检查action.yml文件是否存在
    if (!fileExists("${actionPath}/action.yml")) {
        error "无法找到action.yml文件: ${actionPath}/action.yml"
    }
    
    // 读取并解析action.yml
    def actionYaml = readYaml file: "${actionPath}/action.yml"
    
    // 添加路径信息
    actionYaml.path = actionPath
    
    return actionYaml
}

/**
 * 验证所有必填参数是否存在
 */
def validateRequiredParams(def actionDef, Map params) {
    if (!actionDef.inputs) {
        return
    }
    
    actionDef.inputs.each { inputName, inputDef ->
        if (inputDef.required && !params.containsKey(inputName) && !inputDef.default) {
            error "Action ${actionDef.name} 缺少必填参数: ${inputName}"
        }
    }
}

/**
 * 准备action的输入参数，包括应用默认值
 */
def prepareActionInputs(def actionDef, Map params) {
    def preparedParams = [:]
    
    // 复制原始参数
    params.each { key, value ->
        preparedParams[key] = value
    }
    
    // 应用默认值（如果存在）
    if (actionDef.inputs) {
        actionDef.inputs.each { inputName, inputDef ->
            if (!preparedParams.containsKey(inputName) && inputDef.default) {
                preparedParams[inputName] = inputDef.default
            }
        }
    }
    
    return preparedParams
}

/**
 * 执行action（根据runs.using类型）
 */
def executeAction(def actionDef, Map params) {
    def using = actionDef.runs?.using
    def main = actionDef.runs?.main
    
    // 处理不同类型的action
    switch (using) {
        case 'python':
            return executePythonAction(actionDef, params)
            break
            
        // 可以添加其他类型的action支持（如composite, node, docker等）
        
        default:
            error "不支持的action类型: ${using}"
    }
}

/**
 * 执行Python-based action
 */
def executePythonAction(def actionDef, Map params) {
    def actionPath = actionDef.path
    def main = actionDef.runs.main
    def mainScript = "${actionPath}/${main}"
    
    // 将参数转换为JSON
    def paramsJson = groovy.json.JsonOutput.toJson(params)
    
    // 使用临时文件存储参数
    def tempParamsFile = "${actionPath}/params.json"
    writeFile file: tempParamsFile, text: paramsJson
    
    // 执行Python脚本
    echo "执行Python action: ${mainScript}"
    def exitCode = sh(
        script: "cd ${actionPath} && python ${mainScript} ${tempParamsFile}",
        returnStatus: true
    )
    
    // 清理临时文件
    sh "rm -f ${tempParamsFile}"
    
    if (exitCode != 0) {
        error "Action执行失败: ${actionDef.name} (退出码: ${exitCode})"
    }
    
    // 处理输出（如果有）
    def result = [:]
    if (fileExists("${actionPath}/outputs.json")) {
        def outputsText = readFile "${actionPath}/outputs.json"
        if (outputsText?.trim()) {
            result = readJSON text: outputsText
        }
        sh "rm -f ${actionPath}/outputs.json"
    }
    
    return result
}