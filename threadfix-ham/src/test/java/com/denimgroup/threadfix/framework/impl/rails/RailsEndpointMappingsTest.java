////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.TestConstants;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by sgerick on 5/5/2015.
 */
public class RailsEndpointMappingsTest {

    @Test
    public void testRailsGoatControllerParser() {
        File f = new File (TestConstants.RAILSGOAT_SOURCE_LOCATION);

        assert f.exists() : "Source code location does not exist. " + TestConstants.RAILSGOAT_SOURCE_LOCATION;
        assert f.isDirectory() : "Source code location is not folder. " + TestConstants.RAILSGOAT_SOURCE_LOCATION;

        // System.err.println("parsing "+f.getAbsolutePath() );
        RailsEndpointMappings mappings = new RailsEndpointMappings(f);
        //  System.err.println(System.lineSeparator() + "Parse done." + System.lineSeparator());

        List<Endpoint> endpoints = mappings.generateEndpoints();

        assert !endpoints.isEmpty() : "Got empty endpoints for " + TestConstants.RAILSGOAT_SOURCE_LOCATION;

        Endpoint testEndpoint = new RailsEndpoint(
                "/app/controllers/password_resets_controller.rb",   // filePath
                "/password_resets",                                 // urlPath
                "POST",
                map("confirm_password", RouteParameter.fromDataType("confirm_password", ParameterDataType.STRING),
                        "password", RouteParameter.fromDataType("password", ParameterDataType.STRING),
                        "user", RouteParameter.fromDataType("user", ParameterDataType.STRING))
            );

        confirmEndpointExistsIn(testEndpoint, endpoints);

    }

    private void confirmEndpointExistsIn(Endpoint testEndpoint, List<Endpoint> endpoints) {
        boolean endpointFound = false;
        String filePath = testEndpoint.getFilePath();
        String urlPath = testEndpoint.getUrlPath();
        String httpMethods = testEndpoint.getHttpMethod();
        Map<String, RouteParameter> parameters = testEndpoint.getParameters();

        for (Endpoint endpoint : endpoints) {
            if (filePath.equals(endpoint.getFilePath())
                    && urlPath.equals(endpoint.getUrlPath())
                    && endpoint.getHttpMethod().equals(httpMethods)
                    && endpoint.getParameters().keySet().containsAll(parameters.keySet())) {
                endpointFound = true;
                break;
            }
        }

        assert endpointFound : "Endpoint " + testEndpoint + " not found in endpoints.";
    }

}

