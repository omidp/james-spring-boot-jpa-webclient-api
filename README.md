== James Spring boot JPA webclient api

It is a spring boot project with Apache JAMES(Java Mail Server) configuration which helps you to interact with JAMES functionalities via REST APIs.

It is implemented only for openJPA and it is using postgresql, rabbitmq, JAMES 3.5.0 or later. 

APIs authentication only allowed via the use of JWT.

=== Install requirments

* JDK 11
* Maven 3.6 
* Git
* Postgresql
* RabbitMQ 
* libc6 (linux)
* JAMES 3.5.0 or later


=== Functionalities

Currently it is supporting following functionalities

* Login (to get a jwt token)
* Add a domain
* Add a User
* Send an email
* List user's mailbox
* Load mailbox messages
* List and Load attachments
* Download an attachment


=== Install JAMES

* Unzip apache-james-3.5.0-app.zip.
* Copy postgresql jdbc driver jar into conf/lib
* Create a jamesdb 
* Modify conf/james-database.properties

```
database.driverClassName=org.postgresql.Driver
database.url=jdbc:postgresql://127.0.0.1:5432/jamesdb
database.username=james
database.password=james
vendorAdapter.database=POSTGRESQL
```
* Choose jpa as provider for all xml files (mailbox.xml,...) in conf folder
* Execute run.sh from bin folder

```
sudo ./run.sh
```

=== Test JAMES Setup

* From bin folder execute 

```
-> james-cli -h localhost -p 9999 adddomain mydomain.tld
-> james-cli -h localhost -p 9999 adduser admin@mydomain.tld adminpassword
-> james-cli -h localhost -p 9999 adduser user@mydomain.tld userpassword
```

Send Email

```
-> telnet 127.0.0.1 25
ehlo test
auth login
YWRtaW5AbXlkb21haW4udGxk
YWRtaW5wYXNzd29yZA==
mail from:<admin@mydomain.tld>
rcpt to:<user@mydomain.tld>
data
subject: test

this is a test
.
```

PS: You can also use thunderbird or any mail client to check it is working.

=== Run the project


```
-> mvn verify
-> java -jar target/
```


it is up and running on

```
http://localhost:8668/swagger-ui.html
```


