/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bento.rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.enterprise.context.RequestScoped;

import com.bento.utils.Crawler;
import javax.naming.NamingException;
import javax.ws.rs.QueryParam;
/**
 * REST Web Service
 *
 * @author wing
 */
@Path("paths")
@RequestScoped
public class RESTResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of RESTResource
     */
    public RESTResource() {
    }

    /**
     * Retrieves representation of an instance of com.bento.rest.RESTResource
     * @return an instance of java.lang.String
     */
    @GET
    @Path("")
    @Produces("application/json")
    public String getPath(@QueryParam("url") String url) throws NamingException {
        // FormParam?
        //TODO return proper representation object
        
        Crawler c = new Crawler();
        return c.getPath(url);
//        List<String> a = c.getPath(url);
//        JsonArrayBuilder pathJson = Json.createArrayBuilder();
//        for (String item : a) {
//            pathJson.add(item);
//        }
//        
//        JsonObject response = Json.createObjectBuilder()
//                .add("path", pathJson.build())
//                .build();
//        
//        return response;
    }

//    /**
//     * PUT method for updating or creating an instance of RESTResource
//     * @param content representation for the resource
//     */
//    @PUT
//    @Consumes(MediaType.APPLICATION_XML)
//    public void putXml(String content) {
//    }
}
