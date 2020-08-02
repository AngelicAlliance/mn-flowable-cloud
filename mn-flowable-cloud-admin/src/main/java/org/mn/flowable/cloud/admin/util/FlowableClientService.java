package org.mn.flowable.cloud.admin.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mn.flowable.cloud.admin.domain.ServerConfig;
import org.mn.flowable.cloud.admin.model.AttachmentResponseInfo;
import org.mn.flowable.cloud.admin.model.ResponseInfo;
import org.mn.flowable.cloud.admin.service.exception.FlowableServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Service for invoking Flowable REST services.
 */
@Service
@Qualifier("FlowableClientService")
@Primary
public class FlowableClientService {

    public static final String DEFAULT_FLOWABLE_CONTEXT_ROOT = "";
    public static final String DEFAULT_FLOWABLE_REST_ROOT = "api";
    protected static final String[] PAGING_AND_SORTING_PARAMETER_NAMES = new String[]{"sort", "order", "size"};
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowableClientService.class);
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    RestTemplate restTemplate;
    @Value("${flowable.admin.app.security.preemptive-basic-authentication:false}")
    boolean preemptiveBasicAuthentication;

    public ResponseEntity<JsonNode> exe(HttpUriRequest request) {
        URI uri = request.getURI();
        ResponseEntity<JsonNode> responseEntity = null;
        RequestEntity requestEntity = null;
        if (request instanceof HttpGet) {
            requestEntity = RequestEntity.get(uri).build();
        } else if (request instanceof HttpPost) {
            HttpPost postReq = (HttpPost) request;
            HttpEntity entity = postReq.getEntity();
            String body = null;
            try {
                body = IOUtils.toString(entity.getContent(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
                throw wrapException(e, request);
            }
            requestEntity = RequestEntity.post(uri)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(body);
        } else if (request instanceof HttpPut) {
            HttpPut putReq = (HttpPut) request;
            HttpEntity entity = putReq.getEntity();
            String body = null;
            try {
                body = IOUtils.toString(entity.getContent(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
                throw wrapException(e, request);
            }
            requestEntity = RequestEntity
                    .put(uri)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(body);
        } else if (request instanceof HttpDelete) {
            requestEntity = RequestEntity
                    .delete(uri)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
        }
        responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);
        //restTemplate.exchange(requestEntity,byte[].class);
        return responseEntity;
    }

    public ResponseEntity<byte[]> exe2(HttpUriRequest request) {
        URI uri = request.getURI();
        ResponseEntity<byte[]> responseEntity = null;
        RequestEntity requestEntity = null;
        if (request instanceof HttpGet) {
            requestEntity = RequestEntity.get(uri).build();
        } else if (request instanceof HttpPost) {
            HttpPost postReq = (HttpPost) request;
            HttpEntity entity = postReq.getEntity();
            String body = null;
            try {
                body = IOUtils.toString(entity.getContent(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
                throw wrapException(e, request);
            }
            requestEntity = RequestEntity.post(uri)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(body);
        } else if (request instanceof HttpPut) {
            HttpPut putReq = (HttpPut) request;
            HttpEntity entity = putReq.getEntity();
            String body = null;
            try {
                body = IOUtils.toString(entity.getContent(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
                throw wrapException(e, request);
            }
            requestEntity = RequestEntity.put(uri)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(body);
        } else if (request instanceof HttpDelete) {
            requestEntity = RequestEntity
                    .delete(uri)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
        }
        responseEntity = restTemplate.exchange(requestEntity, byte[].class);
        //restTemplate.exchange(requestEntity,byte[].class);
        return responseEntity;
    }

    public CloseableHttpClient getHttpClient(ServerConfig serverConfig) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        return httpClientBuilder.build();
    }

    public JsonNode executeRequest(HttpUriRequest request, ServerConfig serverConfig) {
        return executeRequest(request, serverConfig, HttpStatus.SC_OK);
    }

    public JsonNode executeRequest(HttpUriRequest request, ServerConfig serverConfig, int expectedStatusCode) {
        ResponseEntity<JsonNode> responseEntity = exe(request);
        boolean success = responseEntity.getStatusCode().value() == expectedStatusCode;
        if (success) {
            JsonNode bodyNode = responseEntity.getBody();
            return bodyNode;
        } else {
            JsonNode bodyNode = null;
            bodyNode = responseEntity.getBody();
            throw new FlowableServiceException(extractError(bodyNode, "An error occurred while calling Flowable: " + responseEntity.getStatusCode()));
        }
    }

    public JsonNode executeDownloadRequest(HttpUriRequest request, HttpServletResponse httpResponse, ServerConfig serverConfig) {
        try {
            ResponseEntity<byte[]> responseEntity = exe2(request);
            boolean success = responseEntity.getStatusCodeValue() == HttpStatus.SC_OK;
            if (success) {
                httpResponse.setHeader("Content-Disposition", responseEntity.getHeaders().get("Content-Disposition").get(0));
                httpResponse.getOutputStream().write(responseEntity.getBody());
                return null;
            } else {
                JsonNode bodyNode = null;
                String strResponse = IOUtils.toString(responseEntity.getBody(), "utf-8");
                try {
                    bodyNode = objectMapper.readTree(strResponse);
                } catch (Exception e) {
                    LOGGER.debug("Error parsing error message", e);
                }
                throw new FlowableServiceException(extractError(bodyNode, "An error occurred while calling Flowable: " + responseEntity.getStatusCode()));
            }
        } catch (Exception e) {
            LOGGER.error("Error executing request to uri {}", request.getURI(), e);
            throw wrapException(e, request);
        }
    }

    public AttachmentResponseInfo executeDownloadRequest(HttpUriRequest request, ServerConfig serverConfig, Integer... expectedStatusCodes) {
        try {
            ResponseEntity<byte[]> response = exe2(request);
            Integer statusCode = response.getStatusCodeValue();
            boolean success = Arrays.asList(expectedStatusCodes).contains(statusCode);
            JsonNode bodyNode = null;
            String strResponse = IOUtils.toString(response.getBody(), "utf-8");
            bodyNode = objectMapper.readTree(strResponse);
            if (success) {
                if (statusCode == HttpStatus.SC_OK) {
                    String contentDispositionFileName[] = response.getHeaders().get("Content-Disposition").get(0).split("=");
                    String fileName = contentDispositionFileName[contentDispositionFileName.length - 1];
                    return new AttachmentResponseInfo(fileName, response.getBody());
                } else {
                    return new AttachmentResponseInfo(statusCode, bodyNode);
                }
            } else {
                throw new FlowableServiceException(extractError(bodyNode, "An error occurred while calling Flowable: " + statusCode));
            }
        } catch (Exception e) {
            LOGGER.error("Error executing request to uri {}", request.getURI(), e);
            throw wrapException(e, request);
        }
    }

    public ResponseInfo execute(HttpUriRequest request, String userName, String password, int... expectedStatusCodes) {

        FlowableServiceException exception = null;
        /*CloseableHttpClient client = getHttpClient(userName, password);
        try {
            try (CloseableHttpResponse response = client.execute(request)) {
                JsonNode bodyNode = readJsonContent(response.getEntity().getContent());
*/
        ResponseEntity<JsonNode> responseEntity = exe(request);
        int statusCode = -1;
                /*if (response.getStatusLine() != null) {
                    statusCode = response.getStatusLine().getStatusCode();
                }*/
        statusCode = responseEntity.getStatusCodeValue();
        boolean success = Arrays.asList(expectedStatusCodes).contains(statusCode);

        if (success) {
            return new ResponseInfo(statusCode, responseEntity.getBody());

        } else {
            exception = new FlowableServiceException(extractError(responseEntity.getBody(), "An error occurred while calling Flowable: " + responseEntity.getStatusCode()));
        }
            /*} catch (Exception e) {
                LOGGER.warn("Error consuming response from uri {}", request.getURI(), e);
                exception = wrapException(e, request);
            }
        } catch (Exception e) {
            LOGGER.error("Error executing request to uri {}", request.getURI(), e);
            exception = wrapException(e, request);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                LOGGER.warn("Error closing http client instance", e);
            }
        }*/

        if (exception != null) {
            throw exception;
        }

        return null;
    }

    public void execute(HttpUriRequest request, HttpServletResponse httpResponse, ServerConfig serverConfig) {
        ResponseEntity<byte[]> responseEntity = exe2(request);
        if (responseEntity.getStatusCodeValue() != HttpStatus.SC_UNAUTHORIZED) {
            httpResponse.setStatus(responseEntity.getStatusCodeValue());
            if (responseEntity.getHeaders().getContentType() != null)
                httpResponse.setContentType(responseEntity.getHeaders().getContentType().getType());
            try {
                httpResponse.getOutputStream().write(responseEntity.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //}
        } else {
            JsonNode bodyNode = null;
            String strResponse = null;
            try {
                strResponse = IOUtils.toString(responseEntity.getBody(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bodyNode = objectMapper.readTree(strResponse);
            } catch (Exception e) {
                LOGGER.debug("Error parsing error message", e);
            }
            throw new FlowableServiceException(extractError(bodyNode, "An error occurred while calling Flowable: " + responseEntity.getStatusCode()));
        }
    }

    public String executeRequestAsString(HttpUriRequest request, ServerConfig serverConfig, int expectedStatusCode) {
        return exe(request).getBody().asText();
    }

    public void executeRequestNoResponseBody(HttpUriRequest request, ServerConfig serverConfig, int expectedStatusCode) {
        ResponseEntity<JsonNode> responseEntity = exe(request);
        boolean success = responseEntity.getStatusCode().value() == expectedStatusCode;
        if (success) {
        } else {
            JsonNode bodyNode = null;
            bodyNode = responseEntity.getBody();
            throw new FlowableServiceException(extractError(bodyNode, "An error occurred while calling Flowable: " + responseEntity.getStatusCode()));
        }
    }

    public FlowableServiceException wrapException(Exception e, HttpUriRequest request) {
        if (e instanceof HttpHostConnectException) {
            return new FlowableServiceException("Unable to connect to the Flowable server.");
        } else if (e instanceof ConnectTimeoutException) {
            return new FlowableServiceException("Connection to the Flowable server timed out.");
        } else {
            // Use the raw exception message
            return new FlowableServiceException(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public String extractError(JsonNode errorBody, String defaultValue) {
        if (errorBody != null && errorBody.isObject() && errorBody.has("exception")) {
            return errorBody.get("exception").asText();
        }
        return defaultValue;
    }

    public HttpPost createPost(String uri, ServerConfig serverConfig) {
        HttpPost post = new HttpPost(getServerUrl(serverConfig, uri));
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");
        return post;
    }

    public HttpPost createPost(URIBuilder builder, ServerConfig serverConfig) {
        HttpPost post = new HttpPost(getServerUrl(serverConfig, builder));
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");
        return post;
    }

    public HttpPut createPut(String uri, ServerConfig serverConfig) {
        HttpPut put = new HttpPut(getServerUrl(serverConfig, uri));
        put.setHeader("Content-Type", "application/json");
        put.setHeader("Accept", "application/json");
        return put;
    }

    public HttpPut createPut(URIBuilder builder, ServerConfig serverConfig) {
        HttpPut put = new HttpPut(getServerUrl(serverConfig, builder));
        put.setHeader("Content-Type", "application/json");
        put.setHeader("Accept", "application/json");
        return put;
    }

    public HttpDelete createDelete(URIBuilder builder, ServerConfig serverConfig) {
        HttpDelete delete = new HttpDelete(getServerUrl(serverConfig, builder));
        delete.setHeader("Content-Type", "application/json");
        delete.setHeader("Accept", "application/json");
        return delete;
    }

    public StringEntity createStringEntity(JsonNode json) {

        // add

        try {
            return new StringEntity(json.toString());
        } catch (Exception e) {
            LOGGER.warn("Error translation json to http client entity {}", json, e);
        }
        return null;
    }

    public StringEntity createStringEntity(String json) {
        try {
            return new StringEntity(json);
        } catch (Exception e) {
            LOGGER.warn("Error translation json to http client entity {}", json, e);
        }
        return null;
    }

    public String getServerUrl(ServerConfig serverConfig, String uri) {
        return getServerUrl(serverConfig.getContextRoot(), serverConfig.getRestRoot(), serverConfig.getServerAddress(), serverConfig.getPort(), uri);
    }

    public String getServerUrl(String contextRoot, String restRoot, String serverAddress, Integer port, String uri) {
        String actualContextRoot = null;
        if (contextRoot != null) {
            actualContextRoot = stripSlashes(contextRoot);
        } else {
            actualContextRoot = DEFAULT_FLOWABLE_CONTEXT_ROOT;
        }

        String actualRestRoot = null;
        if (restRoot != null) {
            actualRestRoot = stripSlashes(restRoot);
        } else {
            actualRestRoot = DEFAULT_FLOWABLE_REST_ROOT;
        }

        String finalUrl = serverAddress;
        if (port != null) {
            finalUrl = serverAddress + ":" + port;
        }
        if (StringUtils.isNotEmpty(actualContextRoot)) {
            finalUrl += "/" + actualContextRoot;
        }

        if (StringUtils.isNotEmpty(actualRestRoot)) {
            finalUrl += "/" + actualRestRoot;
        }

        if (StringUtils.isNotEmpty(uri) && !uri.startsWith("/")) {
            uri = "/" + uri;
        }

        URIBuilder builder = createUriBuilder(finalUrl + uri);

        return builder.toString();
    }

    public URIBuilder createUriBuilder(String url) {
        try {
            return new URIBuilder(url);
        } catch (URISyntaxException e) {
            throw new FlowableServiceException("Error while creating Flowable endpoint URL: " + e.getMessage());
        }
    }

    public String getServerUrl(ServerConfig serverConfig, URIBuilder builder) {
        try {
            return getServerUrl(serverConfig, builder.build().toString());
        } catch (URISyntaxException e) {
            throw new FlowableServiceException("Error while creating Flowable endpoint URL: " + e.getMessage());
        }
    }

    public String getUriWithPagingAndOrderParameters(URIBuilder builder, JsonNode bodyNode) throws URISyntaxException {
        addParameterToBuilder("size", bodyNode, builder);
        addParameterToBuilder("sort", bodyNode, builder);
        addParameterToBuilder("order", bodyNode, builder);
        return builder.build().toString();
    }

    public void addParameterToBuilder(String name, JsonNode bodyNode, URIBuilder builder) {
        JsonNode nameNode = bodyNode.get(name);
        if (nameNode != null && !nameNode.isNull()) {
            builder.addParameter(name, nameNode.asText());
            ((ObjectNode) bodyNode).remove(name);
        }
    }

    protected String stripSlashes(String url) {
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

}
