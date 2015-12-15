package org.tango.web.server.providers;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.12.2015
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@NameBinding
public @interface StaticValue {
}
