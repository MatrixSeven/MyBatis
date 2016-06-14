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
package org.apache.ibatis.reflection.property;

import java.lang.reflect.Field;

/**
 * 属性复制,类似Js里的深度克隆
 * 
 * @author Seven
 *         <p>
 * @date 2016年4月13日-下午12:59:55
 */
public class PropertyCopier {
	/**
	 * 复制属性
	 * 
	 * @param type
	 * @param sourceBean
	 * @param destinationBean
	 */
	public static void copyBeanProperties(Class<?> type, Object sourceBean, Object destinationBean) {
		Class<?> parent = type;
		while (parent != null) {
			final Field[] fields = parent.getDeclaredFields();
			for (Field field : fields) {
				try {
					/*
					 * 因为getDeclaredFields函数返回的这个类中各种限定符的属性，
					 * 如果不设置accessible为true,在调用限定符是private的属性时会报错
					 */
					field.setAccessible(true);
					field.set(destinationBean, field.get(sourceBean));
				} catch (Exception e) {
					// Nothing useful to do, will only fail on final fields,
					// which will be ignored.
				}
			}
			parent = parent.getSuperclass();
		}
	}

}
