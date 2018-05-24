package cz.glubo.nfgp;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;


@Slf4j
@RestController
public class ProxyController {

    @Value("${target:null}")
    private String target;

    private static final String SEPARATOR = System.lineSeparator();

    @RequestMapping("/**")
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info(requestToString(request));
        if (target != null && !target.equals("null")) {
            forwardRequest(request);
            ResponseEntity<String> respEntity = forwardRequest(request);
            log.info(responseToString(respEntity));
            respEntity.getHeaders().forEach((name, values) -> response.setHeader(name, values.stream().collect(Collectors.joining(","))));
            response.setStatus(respEntity.getStatusCodeValue());
            response.getOutputStream().print(respEntity.getBody()); //TODO: async?
        }
    }

    private ResponseEntity<String> forwardRequest(HttpServletRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        final HttpClient httpClient = HttpClientBuilder.create()
                                               .setRedirectStrategy(new LaxRedirectStrategy())
                                               .build();
        factory.setHttpClient(httpClient);
        restTemplate.setRequestFactory(factory);

        HttpHeaders headers = new HttpHeaders();
        java.util.Collections.list(request.getHeaderNames()).forEach(
                name -> headers.add(name, request.getHeader(name))
        );

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        ResponseEntity<String> respEntity = restTemplate.exchange(target + request.getRequestURI(), HttpMethod.resolve(request.getMethod()), entity, String.class);
        // TODO: 30x

        return respEntity;
    }

    private String responseToString(ResponseEntity<String> respEntity) {
        StringBuilder sb = new StringBuilder();

        sb.append("STATUS: ").append(respEntity.getStatusCodeValue());

        return sb.toString();
    }

    private String requestToString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("METHOD: ").append(request.getMethod()).append(SEPARATOR);
        sb.append("URL: ").append(request.getRequestURL()).append(SEPARATOR);
        sb.append("HEADERS: ");
        java.util.Collections.list(request.getHeaderNames()).forEach(
                name -> sb.append(name).append(": ")
                        .append(request.getHeader(name))
                        .append(SEPARATOR)
        );
        return sb.toString();
    }
}
