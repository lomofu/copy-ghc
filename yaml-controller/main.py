import json
import os
import yaml
import requests
from typing import Dict, Any, List, Optional
import logging
from aws_lambda_powertools import Logger

logger = Logger()

class WorkflowController:
    def __init__(self):
        self.jenkins_url = os.environ.get('JENKINS_URL')
        self.jenkins_api_token = os.environ.get('JENKINS_API_TOKEN')
        self.jenkins_username = os.environ.get('JENKINS_USERNAME')
        self.jenkins_shared_library_endpoint = f"{self.jenkins_url}/job/shared-library/buildWithParameters"

    def should_trigger_build(self, event_type: str, workflow_config: Dict[str, Any]) -> bool:
        """
        Determine if a build should be triggered based on the event type and workflow config
        """
        if 'on' not in workflow_config:
            return False

        triggers = workflow_config['on']
        
        # Handle string event type
        if isinstance(triggers, str):
            return triggers == event_type
            
        # Handle list of event types
        if isinstance(triggers, list):
            return event_type in triggers
            
        # Handle dictionary with detailed event configuration
        if isinstance(triggers, dict):
            return event_type in triggers
            
        return False

    def parse_workflow_file(self, repo_path: str, workflow_file: str) -> Optional[Dict[str, Any]]:
        """
        Parse a workflow YAML file
        """
        try:
            file_path = os.path.join(repo_path, '.workflow', workflow_file)
            if not os.path.exists(file_path):
                logger.warning(f"Workflow file not found: {file_path}")
                return None
                
            with open(file_path, 'r') as file:
                workflow_config = yaml.safe_load(file)
                
            return workflow_config
        except Exception as e:
            logger.error(f"Error parsing workflow file: {e}")
            return None

    def get_workflow_files(self, repo_path: str) -> List[str]:
        """
        Get all workflow files in the .workflow directory
        """
        workflow_dir = os.path.join(repo_path, '.workflow')
        if not os.path.exists(workflow_dir):
            return []
            
        return [f for f in os.listdir(workflow_dir) if f.endswith('.yaml') or f.endswith('.yml')]

    def trigger_jenkins_build(self, workflow_metadata: Dict[str, Any], repo_info: Dict[str, Any]) -> bool:
        """
        Trigger a Jenkins build with the workflow metadata
        """
        try:
            # Combine workflow metadata with repository information
            payload = {
                "workflow_metadata": json.dumps(workflow_metadata),
                "repo_name": repo_info.get("name"),
                "repo_url": repo_info.get("url"),
                "branch": repo_info.get("branch"),
                "commit_id": repo_info.get("commit_id")
            }
            
            response = requests.post(
                self.jenkins_shared_library_endpoint,
                auth=(self.jenkins_username, self.jenkins_api_token),
                data=payload
            )
            
            if response.status_code == 201:
                logger.info(f"Successfully triggered Jenkins build: {response.text}")
                return True
            else:
                logger.error(f"Failed to trigger Jenkins build: {response.status_code}, {response.text}")
                return False
                
        except Exception as e:
            logger.error(f"Error triggering Jenkins build: {e}")
            return False

@logger.inject_lambda_context
def lambda_handler(event, context):
    """
    AWS Lambda function to handle webhook events
    """
    try:
        # Parse webhook payload
        webhook_body = json.loads(event.get('body', '{}'))
        
        # Extract repository information
        repo_info = {
            "name": webhook_body.get("repository", {}).get("name"),
            "url": webhook_body.get("repository", {}).get("clone_url"),
            "branch": webhook_body.get("ref", "").replace("refs/heads/", ""),
            "commit_id": webhook_body.get("after")
        }
        
        # Extract event type
        event_type = event.get('headers', {}).get('X-GitHub-Event')
        if not event_type:
            logger.warning("No event type found in webhook")
            return {"statusCode": 400, "body": "No event type found"}
        
        # Create temporary directory for repo
        import tempfile
        import subprocess
        
        with tempfile.TemporaryDirectory() as repo_path:
            # Clone repository
            clone_cmd = f"git clone {repo_info['url']} {repo_path}"
            subprocess.run(clone_cmd, shell=True, check=True)
            
            # Checkout specific commit
            checkout_cmd = f"cd {repo_path} && git checkout {repo_info['commit_id']}"
            subprocess.run(checkout_cmd, shell=True, check=True)
            
            # Process workflow files
            controller = WorkflowController()
            workflow_files = controller.get_workflow_files(repo_path)
            
            triggered = False
            for workflow_file in workflow_files:
                workflow_config = controller.parse_workflow_file(repo_path, workflow_file)
                
                if workflow_config and controller.should_trigger_build(event_type, workflow_config):
                    if controller.trigger_jenkins_build(workflow_config, repo_info):
                        triggered = True
            
            if triggered:
                return {"statusCode": 200, "body": "Build triggered successfully"}
            else:
                return {"statusCode": 200, "body": "No builds were triggered"}
                
    except Exception as e:
        logger.exception(f"Error processing webhook: {e}")
        return {"statusCode": 500, "body": f"Error: {str(e)}"}