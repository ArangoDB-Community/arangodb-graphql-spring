/*
 * DISCLAIMER
 * Copyright 2019 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 *
 */

package com.arangodb.graphql.spring;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * GraphQL HTTP Endpoint.
 *
 * This class represents an HTTP Endpoint that will support GraphQL queries and introspection.
 *
 * GraphQL Queries will be dispatched to the ArangoDataFetcher instead of the usual GraphQL resolver mechanism
 * allowing the incoming queries to be mapped to a single Arango AQL Queries.
 *
 * @author Colin Findlay
 */
@RestController
public class ArangoGraphController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private GraphQL graphQL;

    /**
     * Default Constructor
     * @param graphQL The GraphQL Entry Point
     * @throws IOException
     */
    public ArangoGraphController(GraphQL graphQL) throws IOException {
        this.graphQL = graphQL;
    }

    private Instant logEnter(String request, String method){
        Instant now = Instant.now();
        logger.info("Arango GraphQL Controller received {} request", method);
        logger.trace(request);
        return now;
    }

    private void logExit(Instant start){
        Instant end = Instant.now();
        long requestTime = Duration.between(start, end).toMillis();
        logger.info("Requested completed in {}ms", requestTime);
    }

    /**
     * Request handler for GraphQL Queries
     * @param request Inbound request
     * @param raw Raw HTTP Servlet Request
     * @return The query response
     */
    @PostMapping(value = "/graphql", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Map<String, Object> graphql(@RequestBody Map<String, Object> request, HttpServletRequest raw) {
        String query = String.valueOf(request.get("query"));
        String method = raw.getMethod();
        Instant start = logEnter(query, method);
        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(query)
                .context(raw)
                .build());
        Map<String, Object> specResult = executionResult.toSpecification();
        logExit(start);
        return specResult;
    }

    /**
     * Request handler for GraphQL Introspection Queries
     * @param request Inbound request
     * @param raw Raw HTTP Servlet Request
     * @return The query response
     */
    @RequestMapping(
            value = "/graphql",
            method = RequestMethod.OPTIONS
    )
    public Map<String, Object> graphqlOptions(@RequestBody Map<String, String> request, HttpServletRequest raw) {
        String query = String.valueOf(request.get("query"));
        String method = raw.getMethod();
        Instant start = logEnter(query, method);
        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(request.get("query"))
                .context(raw)
                .build());
        Map<String, Object> specResult = executionResult.toSpecification();
        logExit(start);
        return specResult;
    }

}
