/*
 *    Copyright 2009-2013 The MyBatis Team
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
package org.apache.ibatis.binding;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.session.SqlSession;

/**
 * 
 * @author seven 需要代理实现的接口 生产工厂 接口AOP拦截的代理工厂 2016.1.25<br>
 * @param <T><br>
 * 
 *            TODO MapperProxyFactory
 */
public class MapperProxyFactory<T> {
	/**
	 * TODO 需要代理实现的接口
	 */
	private final Class<T> mapperInterface;
	/**
	 * TODO 代理方法 接口方法容器
	 */
	private Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

	public MapperProxyFactory(Class<T> mapperInterface) {
		this.mapperInterface = mapperInterface;
	}

	public Class<T> getMapperInterface() {
		return mapperInterface;
	}

	public Map<Method, MapperMethod> getMethodCache() {
		return methodCache;
	}

	@SuppressWarnings("unchecked")
	/**
	 * TODO DEF 生成真正的接口代理类实体对象---->jdk动态代理完成,面向接口的代理,
	 * 这也是为什么一定要Mybatis操作类一定要写成接口... 因为JDK的代理就是面向接口的
	 * 
	 * 要被代理的类 mapperInterface.getClassLoader() 被代理类实现的接口 new Class[] {
	 * mapperInterface } 代理对象要实现的功能操作包装类,Handle mapperProxy
	 * 
	 */
	protected T newInstance(MapperProxy<T> mapperProxy) {
		return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface },
				mapperProxy);
	}

	/**
	 * TODO DEF 生成真正的接口代理类实体对象
	 * 
	 * @param sqlSession
	 * @return
	 */
	public T newInstance(SqlSession sqlSession) {
		final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
		return newInstance(mapperProxy);
	}

}
