package com.patota.patota.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@autorizacaoGerente.podeAdministrar({value}, authentication)")
public @interface RequerGerente {

    /** Expressão SpEL que identifica a patota da operação. */
    String value();
}
