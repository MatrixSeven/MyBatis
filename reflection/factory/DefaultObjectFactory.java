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
package org.apache.ibatis.reflection.factory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.ibatis.reflection.ReflectionException;

/**
 * -----MyBatis uses an ObjectFactory to create all needed new Objects.
 * <p>
 * 就像英文说的,用来生产需要的对象类型
 * <p>
 * 实现了接口{@link ObjectFactory}
 * 
 * @author Seven
 *         <p>
 * @date 2016年4月13日-下午12:37:50
 */
public class DefaultObjectFactory implements ObjectFactory, Serializable {

	private static final long serialVersionUID = -8855120656740914948L;

	/**
	 * 创建类型
	 */
	public <T> T create(Class<T> type) {
		return create(type, null, null);
	}

	/**
	 * TODO 在这里,创建真正的空白的返回 的结果集合的Bean类型对象
	 */
	public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		Class<?> classToCreate = resolveInterface(type);
		@SuppressWarnings("unchecked")
		// we know types are assignable
		T created = (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
		return created;
	}

	public void setProperties(Properties properties) {
		// no props for default
	}

	/**
	 * TODO 反射生成Bean对象(查询返回类型)
	 * 
	 * @param type				            要生成的类型
	 * @param constructorArgTypes	 构造器参数类型
	 * @param constructorArgs		 构造器参数列表
	 * @return
	 */
	private <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		try {
			Constructor<T> constructor;
			//获取未定义的构造方法,返回实例
			if (constructorArgTypes == null || constructorArgs == null) {
				constructor = type.getDeclaredConstructor();
				if (!constructor.isAccessible()) {
					constructor.setAccessible(true);
				}
				return constructor.newInstance();
			}
			//拿到constructorArgTypes类型的构造器.返回实例
			constructor = type
					.getDeclaredConstructor(constructorArgTypes.toArray(new Class[constructorArgTypes.size()]));
			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);
			}
			return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
		} catch (Exception e) {
			/*
			 * 返回错误信息
			 */
			StringBuilder argTypes = new StringBuilder();
			if (constructorArgTypes != null) {
				for (Class<?> argType : constructorArgTypes) {
					argTypes.append(argType.getSimpleName());
					argTypes.append(",");
				}
			}
			StringBuilder argValues = new StringBuilder();
			if (constructorArgs != null) {
				for (Object argValue : constructorArgs) {
					argValues.append(String.valueOf(argValue));
					argValues.append(",");
				}
			}
			throw new ReflectionException("Error instantiating " + type + " with invalid types (" + argTypes
					+ ") or values (" + argValues + "). Cause: " + e, e);
		}
	}

	/**
	 * 确认数去类型<p>
	 * 根据返回结果类型里面的配置<p>
	 * 过滤集合类... 如果不是集合类
	 * <p>返回Class类型
	 * 
	 * @param type
	 * @return Class<?>
	 */
	protected Class<?> resolveInterface(Class<?> type) {
		Class<?> classToCreate;
		if (type == List.class || type == Collection.class || type == Iterable.class) {
			classToCreate = ArrayList.class;
		} else if (type == Map.class) {
			classToCreate = HashMap.class;
		} else if (type == SortedSet.class) { // issue #510 Collections Support
			classToCreate = TreeSet.class;
		} else if (type == Set.class) {
			classToCreate = HashSet.class;
		} else {
			classToCreate = type;
		}
		return classToCreate;
	}

	public <T> boolean isCollection(Class<T> type) {
		return Collection.class.isAssignableFrom(type);
	}

}
