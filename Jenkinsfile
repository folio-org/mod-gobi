buildMvn {
  publishModDescriptor = 'yes'
  mvnDeploy = 'yes'
  buildNode = 'jenkins-agent-java17'

  doDocker = {
    buildJavaDocker {
      publishMaster = 'yes'
      healthChk = 'yes'
      healthChkCmd = 'wget --no-verbose --tries=1 --spider http://localhost:8081/admin/health || exit 1'
    }
  }
}
