package de.domschmidt.koku.dav.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = {})
public @interface DavRequestMapping {

    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};
}
