buildMvn {
  publishModDescriptor = 'yes'
  mvnDeploy = 'yes'
  buildNode = 'jenkins-agent-java11'

  doApiLint = true
  doApiDoc = true
  apiTypes = 'RAML'
  apiDirectories = 'ramls'

  doDocker = {
    buildJavaDocker {
      publishMaster = 'yes'
      healthChk = 'yes'
      healthChkCmd = 'curl -sS --fail -o /dev/null  http://localhost:8081/apidocs/ || exit 1'
    }
  }
}
