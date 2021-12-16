def call(String registryCred = 'a', String registryin = 'a', String docTag = 'a', String contName = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a') {

pipeline {
environment { 
		registryCredential = "${registryCred}"
		registry = "$registryin" 	
		dockerTag = "${docTag}_$BUILD_NUMBER"
		containerName = "${contName}"
		gitRepo = "${grepo}"
		gitBranch = "${gbranch}"
		gitCredId = "${gitcred}"
	}
		
	agent { label 'docker' }
	
	triggers {
		pollSCM '* * * * *'
	}

	stages {
		stage("POLL SCM"){
			steps {
				 checkout([$class: 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], userRemoteConfigs: [[credentialsId: "$gitCredId", url: "$gitRepo"]]])
			}
		}	
					
		stage('BUILD IMAGE') { 
			 steps { 
				 script { 
					 dockerImage = docker.build('"$registry:$dockerTag"') 
				 }
			} 
		}
					
		stage('PUSH HUB') { 
			 steps { 
				 script { 
					 docker.withRegistry( '', "$registryCredential" ) { 
						 dockerImage.push() 
					}
				}		
			} 
		}
					
		stage('DEPLOY IMAGE') {
			steps {
				script { 
					 docker.withRegistry( '', registryCredential ) { 
						 dockerImage.run('-it --name "$containerName-$dockerTag"') 
					}
				} 
			}
		}
	}
			  
}

}
