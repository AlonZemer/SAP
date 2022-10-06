package com.test;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class CryptoCurrencyApp extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(CryptoCurrencyService.class);
		return classes;
	}

}
