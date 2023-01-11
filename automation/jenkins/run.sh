docker run --detach --restart=on-failure -p 3000:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts 
