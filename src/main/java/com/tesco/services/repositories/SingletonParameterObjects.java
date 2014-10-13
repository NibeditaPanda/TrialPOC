package com.tesco.services.repositories;

import com.tesco.services.Configuration;
import org.picocontainer.MutablePicoContainer;

/**
 * This will return the initialized Configuration object so that it can be used anywhere in the project without having to create new objects everytime.
 */
public class SingletonParameterObjects {
    private static Configuration configurationSingleton;
    private static MutablePicoContainer containerSingleton;

    private SingletonParameterObjects(){

    }
    public SingletonParameterObjects(Configuration configuration, MutablePicoContainer container){
        if(configurationSingleton==null && containerSingleton==null){
            synchronized (SingletonParameterObjects.class){
                if(configurationSingleton==null && containerSingleton==null){
                    configurationSingleton = configuration;
                    containerSingleton = container;
                }
            }
        }
    }
    public static Configuration getConfigurationInstance(){
        return configurationSingleton;
    }
    public static MutablePicoContainer getContainerInstance(){
        return containerSingleton;
    }
}
