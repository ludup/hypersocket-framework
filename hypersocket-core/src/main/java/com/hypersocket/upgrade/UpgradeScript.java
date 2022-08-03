package com.hypersocket.upgrade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UpgradeScript {

	String module();
	
	String version();
	
	String lang() default "java";
	
	boolean pre() default false;
}
