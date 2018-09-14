package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpParameter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.RESULT_TYPES;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetSyntaxUtil.cleanTypeName;

public class DotNetMappingsStandardMvcGenerator implements DotNetMappingsGenerator {
    private List<CSharpClass> classes;
    private Map<String, RouteParameterMap> routeParameters;

    private static List<String> CONTROLLER_BASE_TYPES = list(
        "Controller",
        "HubController",
        "HubControllerBase",
        "AsyncController",
        "BaseController"
    );

    public DotNetMappingsStandardMvcGenerator(List<CSharpClass> classes, Map<String, RouteParameterMap> routeParameters) {
        this.classes = classes;
        this.routeParameters = routeParameters;
    }

    public List<DotNetControllerMappings> generate() {
        List<DotNetControllerMappings> controllerMappings = list();

        for (CSharpClass csClass : classes) {
            if (!isControllerClass(csClass) || isApiControllerClass(csClass)) {
                continue;
            }

            DotNetControllerMappings currentMappings = new DotNetControllerMappings(csClass.getFilePath());
            currentMappings.setControllerName(csClass.getName().substring(0, csClass.getName().length() - "Controller".length()));

            CSharpAttribute areaAttribute = csClass.getAttribute("RouteArea");
            if (areaAttribute != null && areaAttribute.getParameterValue(0) != null) {
                currentMappings.setAreaName(areaAttribute.getParameterValue(0).getValue());
            }

            RouteParameterMap fileParameters = routeParameters.get(csClass.getFilePath());
            if (fileParameters == null) {
                fileParameters = new RouteParameterMap();
            }

            for (CSharpMethod method : csClass.getMethods()) {
                List<RouteParameter> methodRouteParameters = fileParameters.findParametersInLines(method.getStartLine(), method.getEndLine());

                addActionFromMethod(currentMappings, method, methodRouteParameters);
            }

            controllerMappings.add(currentMappings);
        }

        return controllerMappings;
    }

    private void addActionFromMethod(DotNetControllerMappings controller, CSharpMethod method, List<RouteParameter> methodRouteParameters) {
        if (method.getAccessLevel() != CSharpMethod.AccessLevel.PUBLIC) {
            return;
        }

        String returnType = cleanTypeName(method.getReturnType());
        if (!RESULT_TYPES.contains(returnType)) {
            return;
        }

        if (method.getAttribute("NonAction") != null) {
            return;
        }

        List<String> attributeNames = list();
        for (CSharpAttribute attribute : method.getAttributes()) {
            attributeNames.add(attribute.getName());
        }

        String explicitPath = null;
        CSharpAttribute routeAttribute = method.getAttribute("Route");
        if (routeAttribute != null) {
            CSharpParameter pathParameter = routeAttribute.getParameterValue("template", 0);
            if (pathParameter == null) {
                pathParameter = routeAttribute.getParameterValue("Name", 0);
            }

            if (pathParameter != null) {
                explicitPath = pathParameter.getValue();
            }
        }

        Map<String, RouteParameter> namedRouteParameters = map();
        for (RouteParameter param : methodRouteParameters) {
            namedRouteParameters.put(param.getName(), param);
        }

        for (CSharpParameter param : method.getParameters()) {
            RouteParameter routeParam = namedRouteParameters.get(param.getName());
            if (routeParam == null) {
                routeParam = new RouteParameter(param.getName());
                namedRouteParameters.put(param.getName(), routeParam);
            }

            routeParam.setDataType(param.getType());
        }

        controller.addAction(
            method.getName(),
            new HashSet<String>(attributeNames),
            method.getStartLine(),
            method.getEndLine(),
            new HashSet<RouteParameter>(namedRouteParameters.values()),
            explicitPath
        );
    }

    private boolean isControllerClass(CSharpClass csClass) {
        if (!csClass.getName().endsWith("Controller")) {
            return false;
        }

        if (!csClass.getTemplateParameterNames().isEmpty()) {
            return false;
        }

        for (String baseType : csClass.getBaseTypes()) {
            if (CONTROLLER_BASE_TYPES.contains(baseType)) {
                return true;
            }
        }

        return false;
    }

    private boolean isApiControllerClass(CSharpClass csClass) {
        return isControllerClass(csClass) && csClass.getBaseTypes().contains("ApiController");
    }
}
