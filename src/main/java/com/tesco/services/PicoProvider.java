package com.tesco.services;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import org.picocontainer.MutablePicoContainer;

import java.util.Set;

public class PicoProvider implements IoCComponentProviderFactory {

    private MutablePicoContainer container;
    private Set resources;

    public PicoProvider(MutablePicoContainer container, Set resources) {
        this.container = container;
        this.resources = resources;
    }

    @Override
    public IoCComponentProvider getComponentProvider(Class<?> aClass) {
        return getComponentProvider(null, aClass);
    }

    @Override
    public IoCComponentProvider getComponentProvider(ComponentContext componentContext, Class<?> aClass) {
        if(resources.contains(aClass)) {
            return new PicoComponentProvider(aClass, container);
        }
        return null;
    }
}
