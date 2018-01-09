package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CompositeActionMapper implements ActionMapper {

    static SanitizedLogger log = new SanitizedLogger(CompositeActionMapper.class.getName());

    Collection<ActionMapper> subMappers;

    public CompositeActionMapper() {
        this.subMappers = list();
    }

    public CompositeActionMapper(Collection<ActionMapper> subMappers) {
        this.subMappers = list();
        this.subMappers.addAll(subMappers);
    }

    public CompositeActionMapper(StrutsProject project) {
        subMappers = list();

        StrutsConfigurationProperties config = project.getConfig();
        String allMappers = config.get("struts.mapper.composite");

        ActionMapperFactory mapperFactory = new ActionMapperFactory(config);

        String[] mapperNames = allMappers.split(",");
        for (String name : mapperNames) {
            ActionMapper mapper = mapperFactory.findMapper(name, project);
            if (mapper == null) {
                log.warn("Couldn't find action mapper with name " + name);
            } else {
                subMappers.add(mapper);
            }
        }
    }

    public void addMapper(ActionMapper mapper) {
        this.subMappers.add(mapper);
    }

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, Collection<StrutsPackage> packages, String namespace) {

        List<StrutsEndpoint> endpoints = list();
        for (ActionMapper mapper : subMappers) {
            Collection<StrutsEndpoint> subEndpoints = mapper.generateEndpoints(project, packages, namespace);
            if (subEndpoints != null) {
                //  Ignore endpoints generated by previous mappers
                for (StrutsEndpoint endpoint : subEndpoints) {
                    boolean isNew = true;
                    for (StrutsEndpoint existingEndpoint : endpoints) {
                        if (endpoint.getUrlPath().equals(existingEndpoint.getUrlPath())) {
                            isNew = false;
                            break;
                        }
                    }
                    if (isNew) {
                        endpoints.add(endpoint);
                    }
                }
            }
        }

        return endpoints;
    }
}
