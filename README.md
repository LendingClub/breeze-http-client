# breeze-http-client

BreezeHttpClient is a fluent HTTP/REST client interface with plugglable implementions. It is designed to be super easy to use, extensible, and easy to configure.

BreezeHttpClient itself is simply a Java interface for all the common HTTP verbs: GET, PUT, POST, PATCH, and a generic execute method; think `java.util.logging` but for HTTP. You write your client code around the interface and you can change implementations without touching a line of code. Breeze doesn't introduce any new dependencies on your code, as the core module is pure Java without additional libraries; in fact dependency madness (the tangled mess of conflicting dependencies created when you import multiple large, complex libraries) is the reason it was created. If your implementation doesn't suit you, just import a new implementation, and use a client instance constructed from that implementation. The rest of your code stays the same.

## Using BreezeHttpClient

### Construction

First, pick an implementation: Spring RestTemplate or JAX-RS Jersey. (You can write your own for say Apache `HttpClient` in a couple hours.) We'll use RestTemplate; the constructor is the only thing that changes throughout these examples. Note that the RestTemplate implementation comes with a very handy `BreezeHttpRestTemplateClientBuilder` with lots of flexibility in building your client instance.

```java
BreezeHttpClient client = new BreezeHttpRestTemplateClient();
```

### Basic HTTP commands

Here's how you do a GET; note that by default all requests have JSON content type unless you specify otherwise:

```java
Person person = client.request(url).get(Person.class);
```

Or POST/PUT:

```java
person = client.request(url).post(Person.class, person);
person.setName("Fred");
client.request(url).put(person);
```

Headers, path parameters and query string:

```java
Person person = client.request("https://api.persons.com/persons/get/{id}")
    .pathVariable("id", person.getId())
    .queryVariable("foo", "bar")
    .queryVariable("other", "value")
    .header("X-MAGIC-HEADER", magic)
    .get(Person.class);
```

Generic types such as Lists or Maps, using `BreezeHttpType`, the equivalent of Gson's `TypeToken` or Spring's `ParameterizedTypeReference`:

```java
List<Person> persons = client.request("https://api.persons.com/persons/find/firstName/{firstName}")
    .pathVariable("firstName", person.getFirstName())
    .get(new BreezeHttpType<List<Person>>() {});
```

Here's how to get a response class that has headers, status code, and response entity:

```java
BreezeHttpResponse<Person> response = client.request("https://api.persons.com/persons/get/{id}")
    .pathVariable("id", person.getId())
    .method(BreezeHttpRequest.GET)
    .execute(Person.class, null);
int httpStatusCode = response.getHttpStatusCode();
Map<String, List<String>> headers = response.getHeaders();
Person person = response.getBody();
```

How to submit a web form; you can use the fluent style or create an explict BreezeHttpForm object (which is also fluent) and post it directly if your parameter list is dynamic:

```java
Person person = client.request("https://api.persons.com/persons/create")
    .form()
    .param("name", "Fred")
    .param("city", "San Francisco")
    .post(Person.class);
```

By default, HTTP 4xx/5xx responses result in an exception that includes the response, with the body available as a string; but this is configurable and you can specify the error response body Java class it should try to convert it to:

```java
try {
    Person person = client.request(url).get(Person.class);
} catch (BreezeHttpResponseException e) {
    logger.severe("error getting person, response body: " + e.getResponse().getBody(), e);
}
```

Breeze logs all requests by default, though you can configure it to use a given Logger class, or to not log at all. It's relatively smart about logging; it includes the timing, response code, whether it was a network error (true if there was an `IOException` somewhere inside the stack trace), and the request object. The logged request includes the names, but not the values, of all the path/query variables and the HTTP headers.

Breeze is easy to extend. It's just an interface with an abstract implementation that lets you worry about implementing only the generic execute method. It also has the concept of filters and decorators.

Filters are executed before the request executes and allow you to configure request defaults or do anything else; see `BreezeHttpFilter` and `UserAgentRequestFilter` as an example.

Decorators are powerful; they are a full decorator pattern that allow you to decorate your client instance any way you want. You can nest decorators; at Lending Club we use nested decorators to provide Graphite metrics, automatic retries, and Hystrix integration. Check out `RetryDecorator` and `EndpointDecorator`.

### License

BreezeHttpClient is released under the [Apache 2.0 license](LICENSE).

```
Copyright (C) 2018 Lending Club, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
