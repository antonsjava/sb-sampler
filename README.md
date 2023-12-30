# sb-sampler

Dummy springboot application which shows usage of some my libraries. (Mostly logging libraries.)
Main purpose is to provide documentation samples for that libraries. 



You can build appllication start it and check logging result using folowing commands 
~~~
curl 'http://localhost:8080/rest/book?title=Potter'
curl 'http://localhost:8080/rest/book?authorId=a1'
curl 'http://localhost:8080/rest/book/jkr1'
curl -X POST -H "Content-Type: application/json" -d '{"title": "The Big Show", "abstractInfo": "THE BIG SHOW is as close as you''ll ever get to fighting for your life from the cockpit of a Spitfire or Typhoon. Perhaps the most viscerally exciting book ever written by a fighter pilot.", "author": {"name":"Pierre Clostermann"}}' 'http://localhost:8080/rest/book'

curl 'http://localhost:8080/rest/author?name=o'
curl 'http://localhost:8080/rest/author/a1'
curl -X POST -H "Content-Type: application/json" -d '{"name":"Self Made Writer"}' 'http://localhost:8080/rest/author'
~~~
