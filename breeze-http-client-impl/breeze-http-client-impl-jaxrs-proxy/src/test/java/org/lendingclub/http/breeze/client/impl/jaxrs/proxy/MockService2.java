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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * 
 * @author salbin
 *
 */
@Consumes({"application/json"})
@Produces({"application/json"})
public interface MockService2 {

	@GET
	@Path("/getbaruf")
	public void getBarfuVoidResponse(MockRequest request);

	@GET
	@Path("/getbaruf2")
	public MockResponse getBarfu2();

	@GET
	@Path("/getbaruf2")
	public List<MockResponse> getBarfuCollectionResponse();

	@POST
	@Path("/postbarfu")
	public MockResponse postBarfu(MockRequest request);

	@POST
	@Path("/postbarfu2")
	public void postBarfuVoidResponse(MockRequest request);

	@POST
	@Path("/postbarfu3")
	public List<MockResponse> postBarfu3(MockRequest request);

	@POST
	@Path("/postbarfu4")
	public MockResponse postBarfu4(Object obj);


	@PUT
	@Path("/putbarfu")
	public MockResponse putBarfu(MockRequest request);

	@PUT
	@Path("/butbarfu2")
	public void putBarfuVoidResponse(MockRequest request);

	@PUT
	@Path("/putbarfu3")
	public List<MockResponse> putBarfu3(MockRequest request);
}
