package service.http;

import static org.mockito.Mockito.*;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.jupiter.api.Test;
import com.dedalus.xraycucumber.service.http.HttpService;
import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;

import org.apache.http.auth.AuthenticationException;

public class HttpServiceTest {
    @Test
    public void addAuthentication_shouldAddAuthenticationHeader() throws AuthenticationException, javax.naming.AuthenticationException {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpUriRequest mockRequest = mock(HttpUriRequest.class);
        JiraServiceParameters mockServiceParameters = mock(JiraServiceParameters.class);
        BasicScheme mockBasicScheme = mock(BasicScheme.class);

        when(mockServiceParameters.getUsername()).thenReturn("user");
        when(mockServiceParameters.getPassword()).thenReturn("pass");

        HttpService testService = new HttpService(mockHttpClient, mockServiceParameters) {
            @Override
            protected BasicScheme createBasicScheme() {
                return mockBasicScheme;
            }
        };

        testService.addAuthentication(mockRequest, mockServiceParameters);

        verify(mockServiceParameters).getUsername();
        verify(mockServiceParameters).getPassword();
        verify(mockBasicScheme).authenticate(any(UsernamePasswordCredentials.class), eq(mockRequest), eq(null));
    }
}
