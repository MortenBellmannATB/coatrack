## Tutorial for system-integration-testing in CoatRack

### Requirements

* Linux Commandline, for Windows use WSL
* Docker installation
* Credentials for GitHub account and CoatRack WEb Application URL - please, insert them in the ```config.properties``` file.



### Scripts

```run.sh```: Executes the tests. The scripts cares for all details in the background like pulling/building images etc.

```stop.sh```: If run.sh is interrupted (e.g. by CTRL + 'c') before terminating, the containers could still run in the detached-mode in the background. Execute stop.sh to terminate them.

```cleanup.sh```: Deletes all containers, images and networks related to this feature.



### Testing localhost

* ```init_variables.sh```: Change the value of ```NETWORK``` to "host". 
* ```config.properties```: Change the value of ```host``` to "host.docker.internal".
* Ensure the OAuth2 Application settings ```Homepage URL``` and ```Authorization callback URL``` on GitHub to have the value ```http://host.docker.internal:8080```.