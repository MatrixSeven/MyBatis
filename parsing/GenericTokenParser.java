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
package org.apache.ibatis.parsing;

/**
 * TODO 这货是一个通用解析器<br>
 * 解析XML里面的SQL语句<br>
 * 解析XML配置文件<br>
 * handler是参数TokenHandler实现类
 * @see
 * 	{@link TokenHandler}的具体实现类,根据实现类不同,实现了不同的功能<p>
 * 	这里仅仅作为一个通用的${} openToken  closeToken标签中内容的提取
 * 
 * @author seven
 */
public class GenericTokenParser {

	// 起点标符
	private final String openToken;
	// 结束点标服
	private final String closeToken;
	// /token处理器
	private final TokenHandler handler;

	public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
		this.openToken = openToken;
		this.closeToken = closeToken;
		this.handler = handler;
	}

	/**
	 * 处理字符串
	 * <p>
	 * 核心方法
	 * 
	 * @param text
	 * @return String
	 */
	public String parse(String text) {
		StringBuilder builder = new StringBuilder();
		if (text != null && text.length() > 0) {
			// 讲字符串转换成字符数组
			char[] src = text.toCharArray();
			//
			int offset = 0;
			// 默认从开头下表寻找openToken位置
			int start = text.indexOf(openToken, offset);
			while (start > -1) {
				// 如果是转义符号进行处理
				if (start > 0 && src[start - 1] == '\\') {
					// the variable is escaped. remove the backslash.
					// 直接添加openToken标记符
					builder.append(src, offset, start - 1).append(openToken);
					// 获取下次开始寻找的下标
					offset = start + openToken.length();
				} else {
					// 寻找关闭字符所在下表
					int end = text.indexOf(closeToken, start);

					if (end == -1) {
						// 如果没有找到,全部提添加
						builder.append(src, offset, src.length - offset);
						offset = src.length;
					} else {
						//添加字符
						builder.append(src, offset, start - offset);
						//获取下次开始查找位置的下标
						//或者说是获取截取不要的长度
						offset = start + openToken.length();
						//拼接内容
						String content = new String(src, offset, end - offset);
						//使用handler返回
						builder.append(handler.handleToken(content));
						//更新下次查找开始下表
						offset = end + closeToken.length();
					}
				}
				//更新start标记
				start = text.indexOf(openToken, offset);
			}
			//TODO ,,,,
			if (offset < src.length) {
				builder.append(src, offset, src.length - offset);
			}
		}
		return builder.toString();
	}

}
