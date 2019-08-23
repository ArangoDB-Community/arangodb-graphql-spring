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

import com.arangodb.graphql.ArangoDataFetcher;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArangoGraphControllerTest {

    @Mock
    private GraphQL graphQL;

    @Mock
    private ExecutionResult executionResult;

    @Mock
    private HttpServletRequest raw;

    private ArangoGraphController arangoGraphController;

    private Map<String, Object> expectedResult;

    @Before
    public void setUp() throws Exception {

        when(graphQL.execute(any(ExecutionInput.class))).thenReturn(executionResult);

        expectedResult = new HashMap<>();
        expectedResult.put("myField", 42);
        when(executionResult.toSpecification()).thenReturn(expectedResult);

        arangoGraphController = new ArangoGraphController(graphQL);

    }

    @Test
    public void graphql() {

        Object context = raw;
        String query = "{ myField }";

        ArgumentCaptor<ExecutionInput> arg = ArgumentCaptor.forClass(ExecutionInput.class);

        Map<String, Object> request = new HashMap<>();
        request.put("query", query);

        Map<String, Object> result = arangoGraphController.graphql(request, raw);

        verify(graphQL).execute(arg.capture());

        ExecutionInput executionInput = arg.getValue();

        assertThat(executionInput.getContext(), equalTo(context));

        assertThat(executionInput.getQuery(), equalTo(query));

        assertThat(result, equalTo(expectedResult));
    }

    @Test
    public void graphqlOptions() {


        Object context = raw;
        String query = "{ myField }";

        ArgumentCaptor<ExecutionInput> arg = ArgumentCaptor.forClass(ExecutionInput.class);

        Map<String, String> request = new HashMap<>();
        request.put("query", query);

        Map<String, Object> result = arangoGraphController.graphqlOptions(request, raw);

        verify(graphQL).execute(arg.capture());

        ExecutionInput executionInput = arg.getValue();

        assertThat(executionInput.getContext(), equalTo(context));

        assertThat(executionInput.getQuery(), equalTo(query));

        assertThat(result, equalTo(expectedResult));
    }
}