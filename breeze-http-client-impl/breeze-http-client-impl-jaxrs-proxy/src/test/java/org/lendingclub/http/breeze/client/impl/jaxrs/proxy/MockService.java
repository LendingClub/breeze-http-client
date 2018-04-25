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

package org.lendingclub.http.breeze.client.impl.jaxrs.proxy;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * 
 * @author salbin
 *
 */
@Path("/fubar")
public interface MockService {

    @GET
    @Path("/status")
    public MockResponse getStatus();

    @GET
    @Path("/barfu/{pathParam}")
    @Produces({ "application/json" })
    public MockResponse getBarfu(@HeaderParam("x-fubar") String xFubar, @PathParam("pathParam") String pathParam,
            @QueryParam("arg1") String arg1, @QueryParam("arg2") String arg2,
            @QueryParam("arg3") @DefaultValue("fubar") String arg3, @FormParam("formParam1") String formParam1,
            @FormParam("formParam2") String formParam2);

    @POST
    @Path("/post")
    public MockResponse fubarPost(@FormParam("formParam") List<String> args);

    @POST
    @Path("/post-weird")
    public MockResponse fubarPostWithIncorrectAnnotation(@DefaultValue("formParam") String arg);

    @POST
    @Path("/barfu")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public MockResponse newBarfu(MockRequest request);

    @POST
    @Path("/barfu-no-consumes")
    @Consumes({})
    @Produces({})
    public MockResponse whatDoIConsumeAndProduce(MockRequest request);

    @Path("noHttpMethod")
    public Response noHttpMethod();

    @GET
    public Response noPath();

    @GET
    @Produces({ "application/octet-stream" })
    public Response getOctetStream();

    @POST
    @Path("/postform")
    public MockResponse postForm(Object obj);

}
