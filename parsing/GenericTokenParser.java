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
 * TODO �����һ��ͨ�ý�����<br>
 * ����XML�����SQL���<br>
 * ����XML�����ļ�<br>
 * handler�ǲ���TokenHandlerʵ����
 * @see
 * 	{@link TokenHandler}�ľ���ʵ����,����ʵ���಻ͬ,ʵ���˲�ͬ�Ĺ���<p>
 * 	���������Ϊһ��ͨ�õ�${} openToken  closeToken��ǩ�����ݵ���ȡ
 * 
 * @author seven
 */
public class GenericTokenParser {

	// �����
	private final String openToken;
	// ��������
	private final String closeToken;
	// /token������
	private final TokenHandler handler;

	public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
		this.openToken = openToken;
		this.closeToken = closeToken;
		this.handler = handler;
	}

	/**
	 * �����ַ���
	 * <p>
	 * ���ķ���
	 * 
	 * @param text
	 * @return String
	 */
	public String parse(String text) {
		StringBuilder builder = new StringBuilder();
		if (text != null && text.length() > 0) {
			// ���ַ���ת�����ַ�����
			char[] src = text.toCharArray();
			//
			int offset = 0;
			// Ĭ�ϴӿ�ͷ�±�Ѱ��openTokenλ��
			int start = text.indexOf(openToken, offset);
			while (start > -1) {
				// �����ת����Ž��д���
				if (start > 0 && src[start - 1] == '\\') {
					// the variable is escaped. remove the backslash.
					// ֱ�����openToken��Ƿ�
					builder.append(src, offset, start - 1).append(openToken);
					// ��ȡ�´ο�ʼѰ�ҵ��±�
					offset = start + openToken.length();
				} else {
					// Ѱ�ҹر��ַ������±�
					int end = text.indexOf(closeToken, start);

					if (end == -1) {
						// ���û���ҵ�,ȫ�������
						builder.append(src, offset, src.length - offset);
						offset = src.length;
					} else {
						//����ַ�
						builder.append(src, offset, start - offset);
						//��ȡ�´ο�ʼ����λ�õ��±�
						//����˵�ǻ�ȡ��ȡ��Ҫ�ĳ���
						offset = start + openToken.length();
						//ƴ������
						String content = new String(src, offset, end - offset);
						//ʹ��handler����
						builder.append(handler.handleToken(content));
						//�����´β��ҿ�ʼ�±�
						offset = end + closeToken.length();
					}
				}
				//����start���
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
