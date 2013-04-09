package org.torproject.jtor.config.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.TorConfig.ConfigVar;
import org.torproject.jtor.TorConfig.ConfigVarType;

public class TorConfigProxy implements InvocationHandler {
	
	private final Map<String, Object> configValues;
	private final TorConfigParser parser;
	
	public TorConfigProxy() {
		this.configValues = new HashMap<String, Object>();
		this.parser = new TorConfigParser();
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		if(method.getName().startsWith("set")) {
			invokeSetMethod(method, args);
			return null;
		} else if(method.getName().startsWith("get")) {
			return invokeGetMethod(method);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	void invokeSetMethod(Method method, Object[] args) {
		final String name = getVariableNameForMethod(method);
		configValues.put(name, args[0]);
	}
	
	Object invokeGetMethod(Method method) {
		final String name = getVariableNameForMethod(method);
		if(configValues.containsKey(name)) {
			return configValues.get(name);
		} else {
			return getDefaultValueForMethod(method, name);
		}
	}
	
	private Object getDefaultValueForMethod(Method method, String name) {
		final String defaultValue = getDefaultValueString(method);
		final ConfigVarType type = getVariableType(method);
		return parser.parseValue(defaultValue, type);
	}
	
	private String getDefaultValueString(Method method) {
		final ConfigVar annotation = method.getAnnotation(TorConfig.ConfigVar.class);
		if(annotation == null) {
			return null;
		} else {
			return annotation.defaultValue();
		}
	}
	
	private ConfigVarType getVariableType(Method method) {
		final ConfigVar annotation = method.getAnnotation(TorConfig.ConfigVar.class);
		if(annotation == null) {
			return null;
		} else {
			return annotation.type();
		}
	}
	
	private String getVariableNameForMethod(Method method) {
		final String methodName = method.getName();
		if(methodName.startsWith("get") || methodName.startsWith("set")) {
			return methodName.substring(3);
		}
		throw new IllegalArgumentException();
	}
}
