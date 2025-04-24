#!/usr/bin/env python3
import os
import sys
import json
import subprocess
import yaml

def load_action_definition():
    """加载action.yml定义"""
    action_yml_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'action.yml')
    
    if not os.path.exists(action_yml_path):
        print(f"错误: 找不到action.yml文件: {action_yml_path}")
        return None
        
    with open(action_yml_path, 'r') as file:
        try:
            return yaml.safe_load(file)
        except yaml.YAMLError as e:
            print(f"错误: 解析action.yml失败: {e}")
            return None

def get_input_params(params, action_def):
    """从输入参数中获取值，应用默认值"""
    result = {}
    
    # 如果action定义中包含inputs
    if 'inputs' in action_def:
        for input_name, input_def in action_def['inputs'].items():
            # 如果参数中存在，使用参数值
            if input_name in params:
                result[input_name] = params[input_name]
            # 否则使用默认值（如果有）
            elif 'default' in input_def:
                result[input_name] = input_def['default']
    
    return result

def checkout_repository(params):
    """
    检出git仓库，使用提供的参数
    """
    # 解析输入参数
    repository = params.get('repository')
    ref = params.get('ref', 'main')
    path = params.get('path', '.')
    depth = params.get('depth', 1)
    token = params.get('token', os.environ.get('GITHUB_TOKEN', ''))
    
    # 验证必需参数
    if not repository:
        print("错误: 缺少repository参数")
        return 1
    
    # 格式化仓库URL（如果有token）
    if token and 'github.com' in repository:
        # 使用token进行认证
        repo_url = f"https://x-access-token:{token}@{repository.split('//')[1]}"
    else:
        repo_url = repository
    
    try:
        # 创建目录（如果需要）
        if not os.path.exists(path):
            os.makedirs(path)
        
        # 执行git检出
        git_cmd = [
            "git", "clone",
            "--depth", str(depth),
            "--branch", ref,
            repo_url, path
        ]
        
        print(f"执行: git clone --depth {depth} --branch {ref} {repository} {path}")
        result = subprocess.run(git_cmd, capture_output=True, text=True)
        
        if result.returncode != 0:
            print(f"检出时出错: {result.stderr}")
            return result.returncode
            
        print(f"成功检出 {repository} 在 {ref}")
        
        # 准备输出
        outputs = {}
        
        # 如果action定义了outputs，可以在这里设置输出值
        action_def = load_action_definition()
        if action_def and 'outputs' in action_def:
            # 例如，可以添加commit哈希作为输出
            try:
                get_commit_cmd = ["git", "-C", path, "rev-parse", "HEAD"]
                commit_result = subprocess.run(get_commit_cmd, capture_output=True, text=True)
                if commit_result.returncode == 0:
                    outputs["commit_hash"] = commit_result.stdout.strip()
            except Exception as e:
                print(f"获取提交哈希出错: {str(e)}")
        
        # 写入输出到文件
        if outputs:
            with open('outputs.json', 'w') as f:
                json.dump(outputs, f)
        
        return 0
        
    except Exception as e:
        print(f"检出失败: {str(e)}")
        return 1

if __name__ == "__main__":
    # 获取action定义
    action_def = load_action_definition()
    if not action_def:
        sys.exit(1)
    
    # 从命令行读取参数文件路径
    if len(sys.argv) > 1:
        params_file = sys.argv[1]
        try:
            with open(params_file, 'r') as f:
                params = json.load(f)
        except Exception as e:
            print(f"读取参数文件出错: {str(e)}")
            sys.exit(1)
    else:
        params = {}
    
    # 处理输入参数
    input_params = get_input_params(params, action_def)
    
    # 执行检出
    sys.exit(checkout_repository(input_params))