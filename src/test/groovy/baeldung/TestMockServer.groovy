package baeldung

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpForward
import org.mockserver.verify.VerificationTimes
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.matchers.Times.exactly
import static org.mockserver.model.HttpClassCallback.callback
import static org.mockserver.model.HttpForward.forward
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response
import static org.mockserver.model.StringBody.exact

class TestMockServer extends Specification{

    @Shared
    ClientAndServer mockServer

    //starts server before the tests
    def setupSpec(){
        mockServer = startClientAndServer(1080)
    }

    //stops server after the tests
    def cleanupSpec(){
        mockServer.stop()
    }


    def "expectation for invalid auth"(){
        given:
            createExpectationForInvalidAuth()

        when:
            def response = hitTheServerWithPostRequest()

        then:
            verifyPostRequest()
        and:
            response.getStatusLine().getStatusCode() == 401
    }

    def "forward when get request"(){
        given:
            createExpectationForForward()

        when:
            def response = hitTheServerWithGetRequest("index.html")

        then:
            verifyGetRequest()
        and:
            println(response)
    }

   def "status code should be 200"(){
        given:
            createExpectationForCallBack()

        when:
            def response= hitTheServerWithGetRequest("/callback")

        then:
            response.getStatusLine().getStatusCode() == 200
    }


    def createExpectationForInvalidAuth(){
        new MockServerClient("127.0.0.1", 1080)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/validate")
                                .withHeader("Content-type", "application/json")
                                .withBody(exact("{username: 'foo', password: 'bar'}")), exactly(1))
                .respond(
                        response()
                                .withStatusCode(401)
                                .withHeaders(
                                        new Header( "Content-Type", "application/json; charset=utf-8"),
                                        new Header ("Cache-Control","public, max-age=86400"))
                                .withBody("{ message: 'incorrect username and password combination' }")
                                .withDelay(TimeUnit.SECONDS, 1))
    }

    def createExpectationForForward(){
        new MockServerClient("127.0.0.1", 1080)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/index.html"),
                        exactly(1))
                .forward(
                        forward()
                                .withHost("www.mock-server.com")
                                .withPort(80)
                                .withScheme(HttpForward.Scheme.HTTP))
    }

    def createExpectationForCallBack() {
        mockServer
                .when(
                        request().withPath("/callback"))
                .respond(
                        callback()
                                .withCallbackClass("com.example.mock.server.ExpectationCallbackHandler")
                )
    }

    def verifyPostRequest() {
        new MockServerClient("localhost", 1080).verify(
                request()
                        .withMethod("POST")
                        .withPath("/validate")
                        .withBody(exact("{username: 'foo', password: 'bar'}")),
                VerificationTimes.exactly(1)
        )
    }
    private void verifyGetRequest() {
        new MockServerClient("localhost", 1080).verify(
                request()
                        .withMethod("GET")
                        .withPath("/index.html"),
                VerificationTimes.exactly(1)
        )
    }


    def hitTheServerWithPostRequest() {
        def url = "http://127.0.0.1:1080/validate"
        def client = HttpClientBuilder.create().build()
        def post = new HttpPost(url)
        post.setHeader("Content-type", "application/json")
        def response=null

        try {
            def stringEntity = new StringEntity("{username: 'foo', password: 'bar'}")
            post.getRequestLine()
            post.setEntity(stringEntity)
            response = client.execute(post)

        } catch (Exception e) {
            throw new RuntimeException(e)
        }
        return response
    }

    def hitTheServerWithGetRequest(String page) {
        def url = "http://127.0.0.1:1080/"+page
        def client = HttpClientBuilder.create().build()
        def response=null
        def get = new HttpGet(url)
        try {
            response=client.execute(get)
        } catch (IOException e) {
            throw new RuntimeException(e)
        }

        return response
    }

}
