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
 * @time:2016��2��29�� ����10:21:36
 *
 * @see
 * �������͵��ࡣ
 * �������class��ע�ᣬ�������ã����ء�
 *
 */
public class TypeAliasRegistry {

	/**
	 * TODO ���ص��༯�� ����������<br>
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
	 *         ��������������ñ���<br>
	 *         ���ݱ�������ʵ������<br>
	 *         ����������Ѿ�ע������ó����Ҳ���<br>
	 *         ���Ե��� Resources �������ȫ�����з������<br>
	 * @exception ClassNotFoundException
	 *                �׳�����δ�ҵ��쳣
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
	 * �����ļ���Ϊ���ذ��ĵ�ʱ�����
	 * 
	 * @param packageName
	 *            ����
	 */
	public void registerAliases(String packageName) {
		registerAliases(packageName, Object.class);
	}

	/**
	 * @see 1. ����Ϊ���ڰ��ĵ�ʱ�����
	 * @param packageName
	 *            ����
	 * @param superType
	 *            ���� Ĭ��ΪObject
	 */
	public void registerAliases(String packageName, Class<?> superType) {
		// ������������
		ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
		resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
		Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();
		for (Class<?> type : typeSet) {
			// Ignore inner classes and interfaces (including package-info.java)
			// �ж��Ƿ�����������߽ӿ�
			if (!type.isAnonymousClass() && !type.isInterface()) {
				registerAlias(type);
			}
		}
	}

	/**
	 * ע����ڵ�����<br>
	 * �ֻ�ȡ��������Ȼ���ж��Ƿ���Aliasע��<br>
	 * �����Aliasע�⣬���������ΪAliasע��<br>
	 * ���ݣ�����Ϊ������
	 * 
	 * @param type
	 *            ����
	 * 
	 */
	public void registerAlias(Class<?> type) {
		String alias = type.getSimpleName();
		// �õ�ע��
		Alias aliasAnnotation = type.getAnnotation(Alias.class);
		// �滻��������Ĭ�����ñ������Զ���������
		if (aliasAnnotation != null) {
			alias = aliasAnnotation.value();
		}
		registerAlias(alias, type);
	}

	/***
	 * TODO ����ע�����ͱ���
	 * 
	 * @param alias
	 *            ��������
	 * @param value
	 *            ��������
	 */
	public void registerAlias(String alias, Class<?> value) {
		if (alias == null)
			throw new TypeException("The parameter alias cannot be null");
		// ת��Сд
		String key = alias.toLowerCase(Locale.ENGLISH); // issue #748
		// �ж������Ƿ��ظ�����
		if (TYPE_ALIASES.containsKey(key) && TYPE_ALIASES.get(key) != null && !TYPE_ALIASES.get(key).equals(value)) {
			throw new TypeException("The alias '" + alias + "' is already mapped to the value '"
					+ TYPE_ALIASES.get(key).getName() + "'.");
		}
		TYPE_ALIASES.put(key, value);
	}

	/***
	 * TODO �������ͣ�������·��
	 * 
	 * @exception ClassNotFoundException
	 * @param alias
	 *            ���ñ���
	 * @param value
	 *            ��·��
	 */
	public void registerAlias(String alias, String value) {
		try {
			registerAlias(alias, Resources.classForName(value));
		} catch (ClassNotFoundException e) {
			throw new TypeException("Error registering type alias " + alias + " for " + value + ". Cause: " + e, e);
		}
	}

}
