# Groovy Server Pages support for Spring Boot

This repository contains a prototype of [Groovy Server Pages (GSP)]
(http://grails.org/doc/latest/guide/theWebLayer.html#gsp) support in [Spring Boot]
(https://github.com/spring-projects/spring-boot).

## Running the prototype

The prototype can be built and run using Maven:

`mvn spring-boot:run`

Then it can be accessed at [http://localhost:8080](http://localhost:8080).

## Using GSPs

The prototype uses the classpath when performing view resolution, looking in a `templates`
directory. `.gsp` is appended to the name of the view that is being resolved. For example, if the
view name that is being resolved is `index` the prototype will look for `templates/index.gsp` on
the classpath.

## Tag libraries

The prototype supports tag libraries. The pattern `classpath:taglib/**/*TagLib.groovy` is used to
locate them.

## Limitations

This is only a prototype. There are a number of areas where GSPs are fairly tightly-coupled to
Grails, for example its dependance upon a bean named grailsApplication being present in the
application context. It is highly likely that anything vaguely Grails-related will break or
will exhibit unexpected behaviour.