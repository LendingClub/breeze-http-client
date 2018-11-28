package org.lendingclub.http.breeze;

import org.lendingclub.http.breeze.converter.BreezeHttpConverter;
import org.lendingclub.http.breeze.decorator.BreezeHttpDecorator;
import org.lendingclub.http.breeze.error.BreezeHttpErrorHandler;
import org.lendingclub.http.breeze.exception.BreezeHttpException;
import org.lendingclub.http.breeze.filter.BreezeHttpFilter;
import org.lendingclub.http.breeze.logging.BreezeHttpRequestLogger;
import org.lendingclub.http.breeze.request.BreezeHttpRequest;
import org.lendingclub.http.breeze.request.BreezeHttpRequest.Method;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Breeze makes REST cool.
 */
public interface BreezeHttp {
    /** Create new request with client's default rootUrl and remote service name. */
    BreezeHttpRequest request();

    /** Create new request to invoke the given url and no service name. */
    BreezeHttpRequest request(URL url);

    /** Create new request to invoke the given url and no service name. */
    BreezeHttpRequest request(String url);

    /** Create new request that copies all the attributes of the given request; useful for defaults. */
    BreezeHttpRequest request(BreezeHttpRequest request);

    /** Invoke GET request. */
    String get(URL url) throws BreezeHttpException;

    /** Invoke GET request. */
    String get(String url) throws BreezeHttpException;

    /** Invoke GET request. */
    String get(BreezeHttpRequest request) throws BreezeHttpException;

    /** Invoke GET request, returning object of given returnClass. */
    <T> T get(URL url, Class<T> returnClass) throws BreezeHttpException;

    /** Invoke GET request, returning object of given returnClass. */
    <T> T get(String url, Class<T> returnClass) throws BreezeHttpException;

    /** Invoke GET request, returning object of given returnClass. */
    <T> T get(BreezeHttpRequest request, Class<T> returnClass) throws BreezeHttpException;

    /** Invoke GET request, returning object of given generic type. */
    <T> T get(URL url, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke GET request, returning object of given generic type. */
    <T> T get(String url, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke GET request, returning object of given generic type. */
    <T> T get(BreezeHttpRequest request, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke POST with given body. InputStreams are streamed. */
    String post(URL url, Object body) throws BreezeHttpException;

    /** Invoke POST with given body. InputStreams are streamed. */
    String post(String url, Object body) throws BreezeHttpException;

    /** Invoke POST with given body. InputStreams are streamed. */
    String post(BreezeHttpRequest request, Object body) throws BreezeHttpException;

    /** Invoke POST with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T post(URL url, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke POST with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T post(String url, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke POST with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T post(BreezeHttpRequest request, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke POST with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T post(URL url, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke POST with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T post(String url, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke POST with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T post(BreezeHttpRequest request, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke PUT with given body. InputStreams are streamed. */
    String put(URL url, Object body) throws BreezeHttpException;

    /** Invoke PUT with given body. InputStreams are streamed. */
    String put(String url, Object body) throws BreezeHttpException;

    /** Invoke PUT with given body. InputStreams are streamed. */
    String put(BreezeHttpRequest request, Object body) throws BreezeHttpException;

    /** Invoke PUT with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T put(URL url, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke PUT with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T put(String url, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke PUT with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T put(BreezeHttpRequest request, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke PUT with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T put(URL url, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke PUT with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T put(String url, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke PUT with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T put(BreezeHttpRequest request, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke PATCH with given body. InputStreams are streamed. */
    String patch(URL url, Object body) throws BreezeHttpException;

    /** Invoke PATCH with given body. InputStreams are streamed. */
    String patch(String url, Object body) throws BreezeHttpException;

    /** Invoke PATCH with given body. InputStreams are streamed. */
    String patch(BreezeHttpRequest request, Object body) throws BreezeHttpException;

    /** Invoke PATCH with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T patch(URL url, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke PATCH with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T patch(String url, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke PATCH with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T patch(BreezeHttpRequest request, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke PATCH with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T patch(URL url, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke PATCH with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T patch(String url, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke PATCH with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T patch(BreezeHttpRequest request, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke DELETE request. */
    String delete(URL url) throws BreezeHttpException;

    /** Invoke DELETE request. */
    String delete(String url) throws BreezeHttpException;

    /** Invoke DELETE request. */
    String delete(BreezeHttpRequest request) throws BreezeHttpException;

    /** Invoke DELETE with given body. */
    String delete(URL url, Object body) throws BreezeHttpException;

    /** Invoke DELETE with given body. */
    String delete(String url, Object body) throws BreezeHttpException;

    /** Invoke DELETE with given body. */
    String delete(BreezeHttpRequest request, Object body) throws BreezeHttpException;

    /** Invoke DELETE with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T delete(URL url, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke DELETE with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T delete(String url, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke DELETE with given body, returning object of given returnClass. InputStreams are streamed. */
    <T> T delete(BreezeHttpRequest request, Object body, Class<T> returnType) throws BreezeHttpException;

    /** Invoke DELETE with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T delete(URL url, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke DELETE with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T delete(String url, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke DELETE with given body, returning object of given generic type. InputStreams are streamed. */
    <T> T delete(BreezeHttpRequest request, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Invoke HEAD and return the response headers. */
    Map<String, List<String>> head(URL url) throws BreezeHttpException;

    /** Invoke HEAD and return the response headers. */
    Map<String, List<String>> head(String url) throws BreezeHttpException;

    /** Invoke HEAD and return the response headers. */
    Map<String, List<String>> head(BreezeHttpRequest request) throws BreezeHttpException;

    /** Execute an HTTP request based on its method, returning a full response of given returnClass. */
    <T> T execute(URL url, Method method, Object body, Class<T> returnClass) throws BreezeHttpException;

    /** Execute an HTTP request based on its method, returning a full response of given returnClass. */
    <T> T execute(String url, Method method, Object body, Class<T> returnClass) throws BreezeHttpException;

    /** Execute an HTTP request based on its method, returning a full response of given returnClass. */
    <T> T execute(BreezeHttpRequest request, Object body, Class<T> returnClass) throws BreezeHttpException;

    /** Execute an HTTP request based on its method, returning a full response of given response type. */
    <T> T execute(URL url, Method method, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Execute an HTTP request based on its method, returning a full response of given response type. */
    <T> T execute(String url, Method method, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Execute an HTTP request based on its method, returning a full response of given response type. */
    <T> T execute(BreezeHttpRequest request, Object body, BreezeHttpType<T> returnType) throws BreezeHttpException;

    /** Execute an HTTP request based on its method, body and returnType. */
    <T> T execute(BreezeHttpRequest request) throws BreezeHttpException;

    /** Return the request logger. */
    BreezeHttpRequestLogger requestLogger();

    /** The error handler to invoke when an HTTP response has a status in the exception statuses. */
    BreezeHttpErrorHandler errorHandler();

    /** List of body converters configured for this client. */
    List<BreezeHttpConverter> converters();

    /** List of request filters configured for this client. */
    List<BreezeHttpFilter> filters();

    /** Return a decorated instance of this BreezeHttp object with the given decorator. */
    BreezeHttp decorate(BreezeHttpDecorator decorator);
}
