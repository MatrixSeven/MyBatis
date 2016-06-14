/*
 *    Copyright 2009-2012 The MyBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.io.Resources;

/**
 * @author seven
 * @time:2016年2月29日 上午10:21:36
 *
 * @see
 * 挂载类型的类。
 * 负责各个class的注册，别名引用，加载。
 *
 */
public class TypeAliasRegistry {

	/**
	 * TODO 挂载的类集合 挂载类容器<br>
	 * Seven 2016.1.21
	 */
	private final HashMap<String, Class<?>> TYPE_ALIASES = new HashMap<String, Class<?>>();

	public TypeAliasRegistry() {
		registerAlias("string", String.class);
		registerAlias("byte", Byte.class);
		registerAlias("long", Long.class);
		registerAlias("short", Short.class);
		registerAlias("int", Integer.class);
		registerAlias("integer", Integer.class);
		registerAlias("double", Double.class);
		registerAlias("float", Float.class);
		registerAlias("boolean", Boolean.class);
		registerAlias("byte[]", Byte[].class);
		registerAlias("long[]", Long[].class);
		registerAlias("short[]", Short[].class);
		registerAlias("int[]", Integer[].class);
		registerAlias("integer[]", Integer[].class);
		registerAlias("double[]", Double[].class);
		registerAlias("float[]", Float[].class);
		registerAlias("boolean[]", Boolean[].class);
		registerAlias("_byte", byte.class);
		registerAlias("_long", long.class);
		registerAlias("_short", short.class);
		registerAlias("_int", int.class);
		registerAlias("_integer", int.class);
		registerAlias("_double", double.class);
		registerAlias("_float", float.class);
		registerAlias("_boolean", boolean.class);
		registerAlias("_byte[]", byte[].class);
		registerAlias("_long[]", long[].class);
		registerAlias("_short[]", short[].class);
		registerAlias("_int[]", int[].class);
		registerAlias("_integer[]", int[].class);
		registerAlias("_double[]", double[].class);
		registerAlias("_float[]", float[].class);
		registerAlias("_boolean[]", boolean[].class);
		registerAlias("date", Date.class);
		registerAlias("decimal", BigDecimal.class);
		registerAlias("bigdecimal", BigDecimal.class);
		registerAlias("biginteger", BigInteger.class);
		registerAlias("object", Object.class);
		registerAlias("date[]", Date[].class);
		registerAlias("decimal[]", BigDecimal[].class);
		registerAlias("bigdecimal[]", BigDecimal[].class);
		registerAlias("biginteger[]", BigInteger[].class);
		registerAlias("object[]", Object[].class);
		registerAlias("map", Map.class);
		registerAlias("hashmap", HashMap.class);
		registerAlias("list", List.class);
		registerAlias("arraylist", ArrayList.class);
		registerAlias("collection", Collection.class);
		registerAlias("iterator", Iterator.class);
		registerAlias("ResultSet", ResultSet.class);
	}

	/**
	 * @author seven<br>
	 *         2016.2.27
	 *         <p>
	 *         负责解析类型引用别名<br>
	 *         根据别名挂载实际类型<br>
	 *         如果在引用已经注册的引用池中找不到<br>
	 *         则尝试调用 Resources 类进行类全名进行反射加载<br>
	 * @exception ClassNotFoundException
	 *                抛出类型未找到异常
	 * @param string
	 * @return Class<T>
	 */
	@SuppressWarnings("unchecked")
	public <T> Class<T> resolveAlias(String string) {
		try {
			if (string == null)
				return null;
			String key = string.toLowerCase(Locale.ENGLISH); // issue #748
			Class<T> value;
			if (TYPE_ALIASES.containsKey(key)) {
				value = (Class<T>) TYPE_ALIASES.get(key);
			} else {
				value = (Class<T>) Resources.classForName(string);
			}
			return value;
		} catch (ClassNotFoundException e) {
			throw new TypeException("Could not resolve type alias '" + string + "'.  Cause: " + e, e);
		}
	}

	/**
	 * 配置文件里为挂载包的的时候调用
	 * 
	 * @param packageName
	 *            包名
	 */
	public void registerAliases(String packageName) {
		registerAliases(packageName, Object.class);
	}

	/**
	 * @see 1. 配置为挂在包的的时候调用
	 * @param packageName
	 *            包名
	 * @param superType
	 *            超类 默认为Object
	 */
	public void registerAliases(String packageName, Class<?> superType) {
		// 构造类型引用
		ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
		resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
		Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();
		for (Class<?> type : typeSet) {
			// Ignore inner classes and interfaces (including package-info.java)
			// 判断是否是匿名类或者接口
			if (!type.isAnonymousClass() && !type.isInterface()) {
				registerAlias(type);
			}
		}
	}

	/**
	 * 注册挂在的类型<br>
	 * 现获取简单类名，然后判断是否有Alias注解<br>
	 * 如果有Alias注解，则挂在名称为Alias注解<br>
	 * 内容，否则为简单类名
	 * 
	 * @param type
	 *            类型
	 * 
	 */
	public void registerAlias(Class<?> type) {
		String alias = type.getSimpleName();
		// 拿到注解
		Alias aliasAnnotation = type.getAnnotation(Alias.class);
		// 替换挂载类型默认引用别名，自定义引用名
		if (aliasAnnotation != null) {
			alias = aliasAnnotation.value();
		}
		registerAlias(alias, type);
	}

	/***
	 * TODO 挂载注册类型别名
	 * 
	 * @param alias
	 *            挂载名字
	 * @param value
	 *            挂载类型
	 */
	public void registerAlias(String alias, Class<?> value) {
		if (alias == null)
			throw new TypeException("The parameter alias cannot be null");
		// 转换小写
		String key = alias.toLowerCase(Locale.ENGLISH); // issue #748
		// 判断类型是否重复挂载
		if (TYPE_ALIASES.containsKey(key) && TYPE_ALIASES.get(key) != null && !TYPE_ALIASES.get(key).equals(value)) {
			throw new TypeException("The alias '" + alias + "' is already mapped to the value '"
					+ TYPE_ALIASES.get(key).getName() + "'.");
		}
		TYPE_ALIASES.put(key, value);
	}

	/***
	 * TODO 挂载类型，根据类路径
	 * 
	 * @exception ClassNotFoundException
	 * @param alias
	 *            引用别名
	 * @param value
	 *            类路径
	 */
	public void registerAlias(String alias, String value) {
		try {
			registerAlias(alias, Resources.classForName(value));
		} catch (ClassNotFoundException e) {
			throw new TypeException("Error registering type alias " + alias + " for " + value + ". Cause: " + e, e);
		}
	}

}
