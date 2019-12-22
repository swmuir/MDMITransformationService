/*******************************************************************************
 * Copyright (c) 2018 seanmuir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     seanmuir - initial API and implementation
 *     
 *******************************************************************************/
package org.mdmi.rt.service.web;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class CORSFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request,
        ContainerResponse response) {

        response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHttpHeaders().add("Access-Control-Allow-Headers",
            "origin, content-type, accept, authorization");
        response.getHttpHeaders().add("Access-Control-Allow-Credentials","true");
        response.getHttpHeaders().add("Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS, HEAD");

        return response;
    }
}