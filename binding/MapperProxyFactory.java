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
 * @author seven ��Ҫ����ʵ�ֵĽӿ� �������� �ӿ�AOP���صĴ����� 2016.1.25<br>
 * @param <T><br>
 * 
 *            TODO MapperProxyFactory
 */
public class MapperProxyFactory<T> {
	/**
	 * TODO ��Ҫ����ʵ�ֵĽӿ�
	 */
	private final Class<T> mapperInterface;
	/**
	 * TODO ������ �ӿڷ�������
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
	 * TODO DEF ���������Ľӿڴ�����ʵ�����---->jdk��̬�������,����ӿڵĴ���,
	 * ��Ҳ��Ϊʲôһ��ҪMybatis������һ��Ҫд�ɽӿ�... ��ΪJDK�Ĵ����������ӿڵ�
	 * 
	 * Ҫ��������� mapperInterface.getClassLoader() ��������ʵ�ֵĽӿ� new Class[] {
	 * mapperInterface } �������Ҫʵ�ֵĹ��ܲ�����װ��,Handle mapperProxy
	 * 
	 */
	protected T newInstance(MapperProxy<T> mapperProxy) {
		return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface },
				mapperProxy);
	}

	/**
	 * TODO DEF ���������Ľӿڴ�����ʵ�����
	 * 
	 * @param sqlSession
	 * @return
	 */
	public T newInstance(SqlSession sqlSession) {
		final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
		return newInstance(mapperProxy);
	}

}
