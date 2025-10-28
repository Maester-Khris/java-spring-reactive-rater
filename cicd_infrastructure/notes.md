#### Deploy a java springboot on an ec2 amazon linux instance

- install java
- set java_home
- install git
- set git credentials: ssh keygen
- retrieve the code in a home folder /home/applications
- make necessary update (application.yml logs)
- build the app
- copy the jar to a neutral folder: /opts
- create necessary folder: logs
- create a sudo user make it own the application folder and each subfolder and files
- create a systemd file, make sure to set the execstart and working directory
- reload systemd daemon
- run the application from the service
- optional: check the journalctl logs for application logs

jenkins update jenkis url from the command line
/var/lib/jenkins/jenkins.model.JenkinsLocationConfiguration.xml
systenctk restart jenkins

sonarqube create service to automate launch
/opt/sonarqube/bin/linux-x86-64/sonar,sh
sudo systemctl daemon-reload
sudo systemctl enable --now sonarqube
[Unit]
Description=SonarQube service
After=syslog.target network.target

[Service]
ExecStart=/opt/sonarqube/bin/linux-x86-64/sonar.sh start # Adjust path and architecture
ExecStop=/opt/sonarqube/bin/linux-x86-64/sonar.sh stop  # Adjust path and architecture
LimitNOFILE=65536
LimitNPROC=4096
User=sonar
Group=sonar
Type=forking
Restart=on-failure

[Install]
WantedBy=multi-user.target