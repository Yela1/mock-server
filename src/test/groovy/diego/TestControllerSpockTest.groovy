package diego

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.mockserver.client.MockServerClient
import org.mockserver.verify.VerificationTimes
import spock.lang.Specification


import static org.mockserver.matchers.Times.exactly
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response

class TestControllerSpockTest extends Specification{

    def serverPort = 8082

    def "test for testController"(){
        given:
            createViewControllerExpectation()

        when:
            def response = sendGetRequest()

        then:
            verifyControllerGetRequest()
        and:
            response.getStatusLine().getStatusCode() == 200

    }


    def createViewControllerExpectation(){
        new MockServerClient("127.0.0.1", serverPort).
                when(
                        request()
                                .withMethod("GET")
                                .withPath("/view/controller")
                                .withQueryStringParameter("someId", "test123"),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("some_response_body")
                )
    }


    def verifyControllerGetRequest(){
        new MockServerClient("localhost", serverPort).verify(
                request()
                        .withMethod("GET")
                        .withPath("/view/controller")
                        .withQueryStringParameter("someId", "test123"),
                VerificationTimes.atLeast(0)
        )
    }

    def sendGetRequest() {
        def uri = "http://127.0.0.1:${serverPort}/view/controller?someId=test123"
        def client = HttpClientBuilder.create().build()
        def response=null
        def get = new HttpGet(uri)
        try {
            response=client.execute(get)
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
        return response
    }


}
