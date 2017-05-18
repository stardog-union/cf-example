# Stardog Cloud Foundry Example Application

This repository contains a Cloud Foundry java application that uses
the the [Stardog service broker](https://github.com/stardog-union/service-broker)
to connect to a [Stardog Knowledge Graph](http://www.stardog.com).

## Quick Start

### Cloud Foundry Environment
In order to use this application you will need the
[cf tool](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html)
configured and logged into a Cloud Foundry account.  The Cloud Foundry
account must be configured with the
[Stardog service broker](https://network.pivotal.io/products/stardog-service-broker)

First log into Cloud Foundry with the cf tool and select a target space:

```
$ cf login
API endpoint: https://api.sys.pie-21.cfplatformeng.com

Email> admin

Password>
Authenticating...
OK

Select an org (or press enter to skip):
1. AppDynamics-DB-Visibility
...
14. stardog-service-broker-org
15. system
16. tibco

Org> 14
Targeted org stardog-service-broker-org

Targeted space stardog-service-broker-space


API endpoint:   https://api.sys.pie-21.cfplatformeng.com (API version: 2.65.0)
User:           admin
Org:            stardog-service-broker-org
Space:          stardog-service-broker-space
```

Verify that the Stardog service is available in the marketplace:

```
$ cf marketplace
Getting services from marketplace in org stardog-service-broker-org / space stardog-service-broker-space as admin...
OK

service                          plans                            description
Stardog                          perinstance                      Provides access to a Stardog knowledge graph
```

### Create the Service Instance
Before this application can be run a Stardog service instance needs to be
started.  The first step is to create a parameters file like the following:

```
{
    "url": "<URL of a stardog server>",
    "username": "admin",
    "password": "<admin password>"
}
```

Create a Stardog service instance named `stardogservice1` using the above
param.json file.

```
cf create-service Stardog perinstance stardogservice1 -c param.json
Creating service instance stardogservice1 in org stardog-service-broker-org / space stardog-service-broker-space as admin...
OK
```

### Build and Launch the Application

```
$ ./gradlew build
....
BUILD SUCCESSFUL
```

```
$ cf push
Using manifest file /Users/bresnaha/Dev/CF/cf-example/manifest.yml

Creating app stardog-cf-example in org stardog-service-broker-org / space stardog-service-broker-space as admin...
OK

....


Showing health and status for app stardog-cf-example in org stardog-service-broker-org / space stardog-service-broker-space as admin...
OK

requested state: started
instances: 1/1
usage: 512M x 1 instances
urls: stardog-cf-example-unretentive-flong.cfapps.pie-21.cfplatformeng.com
last uploaded: Thu May 18 22:52:30 UTC 2017
stack: cflinuxfs2
buildpack: java-buildpack=v3.10-offline-https://github.com/cloudfoundry/java-buildpack.git#193d6b7 java-main open-jdk-like-jre=1.8.0_111 open-jdk-like-memory-calculator=2.0.2_RELEASE spring-auto-reconfiguration=1.10.0_RELEASE (no decorators apply)

     state     since                    cpu    memory      disk      details
#0   running   2017-05-18 12:53:34 PM   0.0%   0 of 512M   0 of 1G
```

### Use The Application
At this point the sample application is running and we can interact with it.

Add a record:
```
curl -k https://stardog-cf-example-unretentive-flong.cfapps.pie-21.cfplatformeng.com/add
"ip":"130.211.1.19","time":"1495148078836","status":"SUCCESS","message":"Successfully added a new connection"}
```

Read the records:
```
curl -k  https://stardog-cf-example-unretentive-flong.cfapps.pie-21.cfplatformeng.com/select
{"connections":[{"IP":"130.211.1.19","time":"1495148078836"}],"status":"SUCCESS","message":"Got the listing"}
```

Clear the records:
```
curl -k  https://stardog-cf-example-unretentive-flong.cfapps.pie-21.cfplatformeng.com/clear
{"status":"SUCCESS","message":"Successfully deleted all entries"}
```
