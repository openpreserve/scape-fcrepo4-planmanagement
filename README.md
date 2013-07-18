SCAPE Plan Management on Fedora 4 
=================================

This is the implementation of the SCAPE Plan Management API as described by the spec available at 
https://github.com/openplanets/scape-apis


Installation
------------

#### 1. Get Fedora 4

Checkout and build Fedora 4 from Github at https://github.com/futures/fcrepo4
OR download a prepackaged WAR from https://wiki.duraspace.org/display/FF/Downloads

```bash
$ git clone https://github.com/futures/fcrepo4.git
$ cd fcrepo4
$ mvn clean install
```

#### 2. Deploy Fedora 4

Deploy the web application on a servlet container e.g. Apache Tomcat by copying the war file to the servlet container's webapp directory and start Fedora 4 so that the WAR file gets exploded.

```bash
$ cp fcrepo4/fcrepo-webapp/fcrepo-webapp-{VERSION}.war {TOMCAT_HOME}/webapps/fcrepo.war
$ {TOMCAT_HOME}/bin/catalina.sh run
```

#### 3. Create the Datamodel JAR

Checkout and build/install the Scape platform data model from  https://github.com/openplanets/scape-platform-datamodel

```bash
$ git clone https://github.com/openplanets/scape-platform-datamodel.git
$ cd scape-platform-datamodel
$ mvn clean install
```

#### 4. Create the Plan Management JAR

Checkout and build/package the plan Management api from https://github.com/openplanets/scape-fcrepo4-planmanagement

```bash
$ git clone https://github.com/openplanets/scape-fcrepo4-planmanagement.git
$ cd scape-fcrepo4-planmanagement
$ mvn clean compile package
```	

#### 5. Install the JAR files

Copy the required JAR files from platform data model and Plan Management API to the Fedora 4 Webapp

```bash
$ cp scape-fcrepo4-planmanagement/target/scape-fcrepo4-planmanagement-{VERSION}.jar {TOMCAT_HOME}/webapps/fcrepo/WEB-INF/lib/
$ cp scape-platform-datamodel/target/scape-platform-datamodel-{VERSION}.jar {TOMCAT_HOME}/webapps/fcrepo/WEB-INF/lib/
```
	
#### 6. Update the web.xml

Update the configuration of the web application in order to have Fedora 4 discover the new HTTP endpoints at /scape/plans

*  Add "classpath:scape.xml" to the contextConfigLocation:

```xml
<context-param>
	<param-name>contextConfigLocation</param-name>
	<param-value>WEB-INF/classes/spring/.xml classpath:scape.xml</param-value>
</context-param>
```

*  Add "eu.scape_project.resource" to the init parameter in order for Jersey to discover the new Endpoint

```xml
<init-param>
	<param-name>com.sun.jersey.config.property.packages</param-name>
	<param-value>org.fcrepo, eu.scape_project.resource</param-value>
</init-param>
```
#### 7. Start the servlet container

Run the servlet container again and check that you can interact with the Plan Management API

```bash
$ {TOMCAT_HOME}/bin/catalina.sh run
$ curl -X PUT http://localhost:8080/fcrepo/rest/scape/plan/myplan -d "empty-plan"
```

