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

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.ibatis.builder.BuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * TODO ����һ��ͨ�õ�XML������
 * 
 * @author seven
 *
 */
public class XPathParser {

	private Document document;
	private boolean validation;
	/**
	 * TODO entityResolver
	 * 
	 * @see org.apache.ibatis.builder.xml.XMLMapperEntityResolver
	 *      ��������������ĵ�У�����DTD�������л��� ָ��Ҫʹ�õ�EntityResolver���ʵ�������XML�ĵ�����
	 *      ����Ϊnull�ᵼ�µײ�ʵ��ʹ�����Լ���Ĭ��ʵ�ֺ���Ϊ��
	 * 
	 */
	private EntityResolver entityResolver;
	private Properties variables;
	private XPath xpath;

	public XPathParser(String xml) {
		commonConstructor(false, null, null);
		this.document = createDocument(new InputSource(new StringReader(xml)));
	}

	public XPathParser(Reader reader) {
		commonConstructor(false, null, null);
		this.document = createDocument(new InputSource(reader));
	}

	public XPathParser(InputStream inputStream) {
		commonConstructor(false, null, null);
		this.document = createDocument(new InputSource(inputStream));
	}

	public XPathParser(Document document) {
		commonConstructor(false, null, null);
		this.document = document;
	}

	public XPathParser(String xml, boolean validation) {
		commonConstructor(validation, null, null);
		this.document = createDocument(new InputSource(new StringReader(xml)));
	}

	public XPathParser(Reader reader, boolean validation) {
		commonConstructor(validation, null, null);
		this.document = createDocument(new InputSource(reader));
	}

	public XPathParser(InputStream inputStream, boolean validation) {
		commonConstructor(validation, null, null);
		this.document = createDocument(new InputSource(inputStream));
	}

	public XPathParser(Document document, boolean validation) {
		commonConstructor(validation, null, null);
		this.document = document;
	}

	public XPathParser(String xml, boolean validation, Properties variables) {
		commonConstructor(validation, variables, null);
		this.document = createDocument(new InputSource(new StringReader(xml)));
	}

	public XPathParser(Reader reader, boolean validation, Properties variables) {
		commonConstructor(validation, variables, null);
		this.document = createDocument(new InputSource(reader));
	}

	public XPathParser(InputStream inputStream, boolean validation, Properties variables) {
		commonConstructor(validation, variables, null);
		this.document = createDocument(new InputSource(inputStream));
	}

	public XPathParser(Document document, boolean validation, Properties variables) {
		commonConstructor(validation, variables, null);
		this.document = document;
	}

	public XPathParser(String xml, boolean validation, Properties variables, EntityResolver entityResolver) {
		commonConstructor(validation, variables, entityResolver);
		this.document = createDocument(new InputSource(new StringReader(xml)));
	}

	public XPathParser(Reader reader, boolean validation, Properties variables, EntityResolver entityResolver) {
		commonConstructor(validation, variables, entityResolver);
		this.document = createDocument(new InputSource(reader));
	}
	/**
	 * TODO 4 ��ʼ������ new XMLMapperEntityResolver()����XML��ǩ XPathParserXML����
	 * @param inputStream
	 * @param validation
	 * @param variables
	 * @param entityResolver
	 */
	public XPathParser(InputStream inputStream, boolean validation, Properties variables,
			EntityResolver entityResolver) {
		//ͨ�ù��췽��
		commonConstructor(validation, variables, entityResolver);
		//����DOM��
		this.document = createDocument(new InputSource(inputStream));
	}

	public XPathParser(Document document, boolean validation, Properties variables, EntityResolver entityResolver) {
		commonConstructor(validation, variables, entityResolver);
		this.document = document;
	}

	public void setVariables(Properties variables) {
		this.variables = variables;
	}

	public String evalString(String expression) {
		// �������е�document������Ϊroot
		return evalString(document, expression);
	}

	/**
	 * �������е�document������Ϊroot
	 * 
	 * @param root
	 * @param expression
	 * @return
	 */
	public String evalString(Object root, String expression) {
		String result = (String) evaluate(expression, root, XPathConstants.STRING);
		result = PropertyParser.parse(result, variables);
		return result;
	}

	public Boolean evalBoolean(String expression) {
		return evalBoolean(document, expression);
	}

	public Boolean evalBoolean(Object root, String expression) {
		return (Boolean) evaluate(expression, root, XPathConstants.BOOLEAN);
	}

	public Short evalShort(String expression) {
		return evalShort(document, expression);
	}

	public Short evalShort(Object root, String expression) {
		return Short.valueOf(evalString(root, expression));
	}

	public Integer evalInteger(String expression) {
		return evalInteger(document, expression);
	}

	public Integer evalInteger(Object root, String expression) {
		return Integer.valueOf(evalString(root, expression));
	}

	public Long evalLong(String expression) {
		return evalLong(document, expression);
	}

	public Long evalLong(Object root, String expression) {
		return Long.valueOf(evalString(root, expression));
	}

	public Float evalFloat(String expression) {
		return evalFloat(document, expression);
	}

	public Float evalFloat(Object root, String expression) {
		return Float.valueOf(evalString(root, expression));
	}

	public Double evalDouble(String expression) {
		return evalDouble(document, expression);
	}

	public Double evalDouble(Object root, String expression) {
		return (Double) evaluate(expression, root, XPathConstants.NUMBER);
	}

	public List<XNode> evalNodes(String expression) {
		return evalNodes(document, expression);
	}

	public List<XNode> evalNodes(Object root, String expression) {
		List<XNode> xnodes = new ArrayList<XNode>();
		NodeList nodes = (NodeList) evaluate(expression, root, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			xnodes.add(new XNode(this, nodes.item(i), variables));
		}
		return xnodes;
	}

	public XNode evalNode(String expression) {
		return evalNode(document, expression);
	}

	public XNode evalNode(Object root, String expression) {
		Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
		if (node == null) {
			return null;
		}
		return new XNode(this, node, variables);
	}

	private Object evaluate(String expression, Object root, QName returnType) {
		try {
			return xpath.evaluate(expression, root, returnType);
		} catch (Exception e) {
			throw new BuilderException("Error evaluating XPath.  Cause: " + e, e);
		}
	}

	private Document createDocument(InputSource inputSource) {
		// important: this must only be called AFTER common constructor
		// ��Ϊʲô�����ڵ���commonConstructor��������ܵ�����������أ���Ϊ������������õ����������ԣ�validation��entityResolver
		// ���������������û������ǰ�͵�������������Ϳ��ܻᵼ��������ڲ����Գ�ͻ
		try {
			// ����documentʱ�õ��������ࣺDocumentBuilderFactory��DocumentBuilder��
			// Ϊʲô���������������Щ���ԣ���Щ������ʲô���á�Ҫ��ȫ���������Ҫ����ƪ����������Ͳ��������ˣ�
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(validation);

			factory.setNamespaceAware(false);
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(false);
			factory.setCoalescing(false);
			factory.setExpandEntityReferences(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			/**
			 * TODO ָ��Ҫʹ�õ�EntityResolver���ʵ�������XML�ĵ�������
			 * ����Ϊnull�ᵼ�µײ�ʵ��ʹ�����Լ���Ĭ��ʵ�ֺ���Ϊ��
			 */
			builder.setEntityResolver(entityResolver);
			/**
			 * TODO ָ��ErrorHandler��Ҫʹ�õĽ������� ��������Ϊnull���ᵼ�µײ�ʵ��ʹ����������Ĭ��ʵ�ֺ���Ϊ��
			 */
			builder.setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException exception) throws SAXException {
					throw exception;
				}

				public void fatalError(SAXParseException exception) throws SAXException {
					throw exception;
				}

				public void warning(SAXParseException exception) throws SAXException {
				}
			});
			/**
			 * TODO 6��ʼ������ ���������ļ�,��ʼ����xml
			 */
			return builder.parse(inputSource);
		} catch (Exception e) {
			throw new BuilderException("Error creating document instance.  Cause: " + e, e);
		}
	}
	/**
	 * ͨ�ù��췽��
	 * @param validation
	 * @param variables
	 * @param entityResolver
	 */
	private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) {
		this.validation = validation;
		this.entityResolver = entityResolver;
		this.variables = variables;
		/**
		 * TODO 5��ʼ������ �õ�XPathFactory��������
		 */
		XPathFactory factory = XPathFactory.newInstance();
		this.xpath = factory.newXPath();
	}

}