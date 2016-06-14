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
package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * MethodInvoker
 * <p>
 * 实现 {@link Invoker} 方法反射类
 * 
 * @author Seven
 *         <p>
 * @date 2016年4月13日-下午12:56:06
 */
public class MethodInvoker implements Invoker {
	/**
	 * 返回类型?参数类型?
	 */
	private Class<?> type;
	private Method method;

	/**
	 * 这里为什么是getParameterTypes?
	 * <p>
	 * 为什么如果有参数,就返回参数类型
	 * <p>
	 * 没有参数在获取getReturnType? 难道是BeanGet方法?
	 * 
	 * @param method
	 */
	public MethodInvoker(Method method) {
		this.method = method;
		if (method.getParameterTypes().length == 1) {
			type = method.getParameterTypes()[0];
		} else {
			type = method.getReturnType();
		}
	}

	/**
	 * 反射调用方法
	 */
	public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
		return method.invoke(target, args);
	}

	/**
	 * 获得方法返回类型
	 */
	public Class<?> getType() {
		return type;
	}
}
