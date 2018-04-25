/*
 * Copyright (C) 2018 Lending Club, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lendingclub.http.breeze.client;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Test REST controller invoked by the clients under test.
 *
 * @author Raul Acevedo
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {
    /**
     * Append extra field to TestModel to make sure clients ignore new JSON fields,
     * rather than throw an exception during parsing.
     */
    private static class ServerTestModel extends TestModel {
        private String extraField = "hi there";

        public ServerTestModel(String message) {
            super(message);
        }

        public String getExtraField() {
            return extraField;
        }

        public void setExtraField(String extraField) {
            this.extraField = extraField;
        }
    }

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/get/{pathVariable}", method = GET)
    public TestModel get(
            @PathVariable String pathVariable,
            @RequestParam String queryVariable,
            @RequestParam TreeSet<String> queryMultiple,
            @RequestHeader("TestHeader") String header
    ) {
        return new ServerTestModel("pathVariable=" + pathVariable
                + ", queryVariable=" + queryVariable
                + ", queryMultiple=" + queryMultiple
                + ", header=" + header);
    }

    @RequestMapping(value = "/null", method = GET)
    public TestModel nullGet() {
        return null;
    }

    @RequestMapping(value = "/void", method = GET)
    public void voidGet() {
    }

    @RequestMapping(value = "/getMap/{pathVariable}")
    public Map<String, List<TestModel>> getMap(
            @PathVariable String pathVariable,
            @RequestParam String queryVariable,
            @RequestParam TreeSet<String> queryMultiple,
            @RequestHeader("TestHeader") String header
    ) {
        return singletonMap(
                pathVariable,
                singletonList(new ServerTestModel("queryVariable=" + queryVariable
                        + ", queryMultiple=" + queryMultiple
                        + ", header=" + header))
        );
    }

    @RequestMapping(value = "/put/{pathVariable}", method = PUT)
    public TestModel put(
            @PathVariable String pathVariable,
            @RequestParam String queryVariable,
            @RequestParam TreeSet<String> queryMultiple,
            @RequestBody String body,
            @RequestHeader("TestHeader") String header
    ) {
        return new ServerTestModel("pathVariable=" + pathVariable
                + ", queryVariable=" + queryVariable
                + ", queryMultiple=" + queryMultiple
                + ", header=" + header
                + ", body=" + body);
    }

    @RequestMapping(value = "/putMap/{pathVariable}", method = PUT)
    public Map<String, List<TestModel>> putMap(
            @PathVariable String pathVariable,
            @RequestParam String queryVariable,
            @RequestParam TreeSet<String> queryMultiple,
            @RequestBody String body,
            @RequestHeader("TestHeader") String header
    ) {
        return singletonMap(
                pathVariable,
                singletonList(new ServerTestModel("queryVariable=" + queryVariable
                        + ", queryMultiple=" + queryMultiple
                        + ", header=" + header
                        + ", body=" + body))
        );
    }

    @RequestMapping(value = "/post/{pathVariable}", method = POST)
    public TestModel post(
            @PathVariable String pathVariable,
            @RequestParam String queryVariable,
            @RequestParam TreeSet<String> queryMultiple,
            @RequestBody String body,
            @RequestHeader("TestHeader") String header
    ) {
        return new ServerTestModel("pathVariable=" + pathVariable
                + ", queryVariable=" + queryVariable
                + ", queryMultiple=" + queryMultiple
                + ", header=" + header
                + ", body=" + body);
    }

    @RequestMapping(value = "/postMap/{pathVariable}", method = POST)
    public Map<String, List<TestModel>> postMap(
            @PathVariable String pathVariable,
            @RequestParam String queryVariable,
            @RequestParam TreeSet<String> queryMultiple,
            @RequestBody String body,
            @RequestHeader("TestHeader") String header
    ) {
        return singletonMap(
                pathVariable,
                singletonList(new ServerTestModel("queryVariable=" + queryVariable
                        + ", queryMultiple=" + queryMultiple
                        + ", header=" + header
                        + ", body=" + body))
        );
    }

    @RequestMapping(value = "/putFile/{pathVariable}", method = PUT)
    public void putFile(
            @PathVariable String pathVariable,
            @RequestParam String queryVariable,
            @RequestParam TreeSet<String> queryMultiple,
            @RequestHeader("TestHeader") String header,
            HttpServletRequest request
    ) throws IOException {
        String expected =
                "pathVariable=" + pathVariable
                        + ", queryVariable=" + queryVariable
                        + ", queryMultiple=" + queryMultiple
                        + ", header=" + header;
        String fileContents = IOUtils.toString(request.getInputStream(), "UTF-8");
        if (!fileContents.equals(expected)) {
            throw new IllegalArgumentException("fileContents='" + fileContents + "' does not match '" + expected + "'");
        }
    }

    @RequestMapping(value = "/json/post", method = POST)
    public TestModel jsonPost(@RequestBody TestModel model) {
        return model;
    }

    @RequestMapping(value = "/json/put", method = PUT)
    public TestModel jsonPut(@RequestBody TestModel model) {
        return model;
    }

    @RequestMapping(value = "/form", method = POST)
    public TestModel form(@ModelAttribute TestModel model) {
        if (!"application/x-www-form-urlencoded".equals(request.getContentType())) {
            throw new IllegalArgumentException("incorrect content-type: " + request.getContentType());
        }
        return model;
    }

    @RequestMapping(value = "/patch", method = PATCH)
    public TestModel patch(@RequestBody TestModel model) {
        return model;
    }

    @RequestMapping(value = "/patchMap/{pathVariable}", method = PATCH)
    public Map<String, List<TestModel>> patchMap(
            @PathVariable String pathVariable,
            @RequestParam String queryVariable,
            @RequestParam TreeSet<String> queryMultiple,
            @RequestBody String body,
            @RequestHeader("TestHeader") String header
    ) {
        return singletonMap(
                pathVariable,
                singletonList(new ServerTestModel("queryVariable=" + queryVariable
                        + ", queryMultiple=" + queryMultiple
                        + ", header=" + header
                        + ", body=" + body))
        );
    }

    @RequestMapping(value = "/ping")
    public String ping() {
        return "pong";
    }
}
