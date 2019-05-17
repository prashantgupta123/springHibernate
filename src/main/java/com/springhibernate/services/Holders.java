package com.springhibernate.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * The type Holders.
 */
@Component
public final class Holders {

    /**
     *
     */
    private static ApplicationContext applicationContext;

    /**
     * Instantiates a new Holders.
     *
     * @param applicationContext the application context
     */
    @Autowired
    private Holders(ApplicationContext applicationContext) {
        Holders.applicationContext = applicationContext;
    }


    /**
     * Gets the reference to an application-context bean
     *
     * @param <T>   This is the type parameter
     * @param clazz the type of the bean
     * @return the bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

}