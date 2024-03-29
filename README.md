# sb-sampler

Dummy springboot application which shows usage of some my libraries. (Mostly logging libraries.)
Main purpose is to provide documentation samples for that libraries. 

Application provides 
 - simple jdbc [database](https://github.com/antonsjava/sb-sampler/blob/main/src/main/resources/db/books.sql)  (H2) configured to ./target/db/books.h2 
 - implements a jdbc [api](https://github.com/antonsjava/sb-sampler/blob/main/src/main/java/sk/antons/sbsampler/repo/BookRepo.java) for manipulating data 
 - [configure](https://github.com/antonsjava/sb-sampler/blob/main/src/main/java/sk/antons/sbsampler/repo/BookRepo.java#L51) that api to log sql statements 
 - implements a rest [api](https://github.com/antonsjava/sb-sampler/blob/main/src/main/java/sk/antons/sbsampler/rest/book/BookController.java) for providing that jdbc api
 - [configure](https://github.com/antonsjava/sb-sampler/blob/main/src/main/java/sk/antons/sbsampler/rest/RestConf.java#L40) logging for rest api (also for ws api)
 - [configure](https://github.com/antonsjava/sb-sampler/blob/main/src/main/java/sk/antons/sbsampler/rest/JsonExceptionAdvice.java) rest api to log exceptions and convert them to json
 - implements a rest [client](https://github.com/antonsjava/sb-sampler/blob/main/src/main/java/sk/antons/sbsampler/rest/book/BookRestClient.java) using RestTemplate and configure to [log](https://github.com/antonsjava/sb-sampler/blob/main/src/main/java/sk/antons/sbsampler/rest/book/BookClientConf.java#L69) req/res
 - implements a [soap web service](https://github.com/antonsjava/sb-sampler/blob/main/src/main/resources/META-INF/book/BookService.wsdl) which receive requests and storees them as xml files in ./target/fs/input
 - implements a soap web service [client](https://github.com/antonsjava/sb-sampler/blob/main/src/main/java/sk/antons/sbsampler/ws/book/BookServiceClient.java) using spring web service which logs requests and responses
 - implements integration [flow](https://github.com/antonsjava/sb-sampler/blob/main/src/main/java/sk/antons/sbsampler/flow/XmlFlowConfigurator.java#L75) which reads ./target/fs/input dir and process soap requests by calling rest api and moving files to backup od file folder

## sb-sampler
to test rest 
~~~
curl 'http://localhost:8080/rest/book?title=Potter'
curl 'http://localhost:8080/rest/book?authorId=a1'
curl 'http://localhost:8080/rest/book/jkr1'
curl -X POST -H "Content-Type: application/json" -d '{"title": "The Big Show", "abstractInfo": "THE BIG SHOW is as close as you''ll ever get to fighting for your life from the cockpit of a Spitfire or Typhoon. Perhaps the most viscerally exciting book ever written by a fighter pilot.", "author": {"name":"Pierre Clostermann"}}' 'http://localhost:8080/rest/book'

curl 'http://localhost:8080/rest/author?name=o'
curl 'http://localhost:8080/rest/author/a1'
curl -X POST -H "Content-Type: application/json" -d '{"name":"Self Made Writer"}' 'http://localhost:8080/rest/author'
~~~

to test ws just start implemented ws [client](https://github.com/antonsjava/sb-sampler/blob/main/src/main/java/sk/antons/sbsampler/ws/book/BookServiceClient.java#L226) or start 
~~~
curl -X POST -H "Content-Type: text/xml" -d '<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"><SOAP-ENV:Header/><SOAP-ENV:Body><ns3:updateBookRequest xmlns:ns3="http://sample.antons.sk/book/1.0"><book><title>new book</title><abstract>something long</abstract><author>t1</author></book></ns3:updateBookRequest></SOAP-ENV:Body></SOAP-ENV:Envelope>' 'http://localhost:8080/ws/book'
~~~

This is example of [log](https://github.com/antonsjava/sb-sampler/blob/main/src/main/other-resources/application.log) which is produced by calling implemented soap web service client 
 - ws is called - file is stored to input dir
 - file is processed by flow - rest service is called using rest client
 - rest service is called and book is stored to db

## rest doc

if you want to see usage of io.github.antonsjava:sb-rest-doclet you can start 
~~~
mvn javadoc::javadoc
~~~
and look to ./target/site/apidocs/index-rest.html (most of code is not documented) 


## mimic servlet

if you want to mock some static data you can look to RestConf how to configure servlet to provide some dummy data.  

~~~
curl -X POST -d '
<env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
  <env:Body>
    <Person>
      <Name>Peter</Name>
    </Person>
  </env:Body>
</env:Envelope>
' http://localhost:8080/mock/testsoap
~~~

~~~
curl -X POST -d '
<env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
  <env:Body>
    <Person>
      <Name>Petex</Name>
    </Person>
  </env:Body>
</env:Envelope>
' http://localhost:8080/mock/testsoap
~~~
