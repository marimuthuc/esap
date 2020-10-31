/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2017  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 *
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.fm.core.io.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * TODO description
 *
 * @author Vinith
 */
public class XmlAdaptationModel {

	private final List<String> inputFileAddresses;
	private final String outputFileAddress;

	public XmlAdaptationModel(List<String> inputFileAddresses, String outputFileAddress) {
		this.inputFileAddresses = inputFileAddresses;
		this.outputFileAddress = outputFileAddress;
	}

	public void createXML() {
		final List<String> confiles = inputFileAddresses;
		try {
			// java.lang.System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.xsltc.trax.TransformerFactoryImpl");
			final int numconfiles = confiles.size();
			DocumentBuilderFactory dbFactory;
			DocumentBuilder dBuilder;
			final Document[] originals = new Document[numconfiles];

			try {
				dbFactory = DocumentBuilderFactory.newInstance();
				dBuilder = dbFactory.newDocumentBuilder();
				for (int j = 0; j < numconfiles; j++) {
					originals[j] = dBuilder.parse(new InputSource(new InputStreamReader(new FileInputStream(confiles.get(j)))));
				}
				// duplicate = dBuilder.parse(new InputSource(new InputStreamReader(new FileInputStream("config6.xml"))));
			} catch (SAXException | IOException | ParserConfigurationException e) {
				e.printStackTrace();
			}

			final XPath xPath = XPathFactory.newInstance().newXPath();
			final String expression = "/configuration/feature";
			final NodeList[] nodelist = new NodeList[numconfiles];
			for (int k = 0; k < numconfiles; k++) {
				nodelist[k] = (NodeList) xPath.compile(expression).evaluate(originals[k], XPathConstants.NODESET);
			}

			try {
				final DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
				final DocumentBuilder dBui = dbF.newDocumentBuilder();
				final Document doc = dBui.newDocument();

				// root element
				final Element rootElement = doc.createElement("AdaptationModel");
				doc.appendChild(rootElement);

				final TransformerFactory transformerFactory = TransformerFactory.newInstance();
				final Transformer transformer = transformerFactory.newTransformer();
				final DOMSource source = new DOMSource(doc);
				final StreamResult result = new StreamResult(new File(outputFileAddress));
				transformer.transform(source, result);

			} catch (final Exception e6) {

			}

			final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			final Document document = documentBuilder.parse(new InputSource(new InputStreamReader(new FileInputStream(outputFileAddress))));
			final Element root = document.getDocumentElement();

			final Element[] on = new Element[numconfiles];
			final Element[] config = new Element[numconfiles];
			final Attr[] attr = new Attr[numconfiles];
			for (int l = 0; l < numconfiles; l++) {

				if ((l % 2) == 0) {
					on[l] = document.createElement("On");

					config[l] = document.createElement("configuration");
					on[l].appendChild(config[l]);

					attr[l] = document.createAttribute("VTS");
					attr[l].setValue(String.valueOf((l + 2) / 2));
					on[l].setAttributeNode(attr[l]);
				}

				else {
					on[l] = document.createElement("Execute");

					config[l] = document.createElement("configuration");

					attr[l] = document.createAttribute("EAAC");
					attr[l].setValue(String.valueOf((l + 2) / 2));
					on[l].setAttributeNode(attr[l]);

				}
				for (int i = 0; i < nodelist[l].getLength(); i++) {
					final Node nNode = nodelist[l].item(i);
					// System.out.println("\nCurrent Element :" + nNode.getNodeName());
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						final Element eElement = (Element) nNode;
						final Element feature = document.createElement("feature");
						if (eElement.hasAttribute("automatic")) {
							final Attr attr2 = document.createAttribute("automatic");
							attr2.setValue(eElement.getAttribute("automatic"));
							feature.setAttributeNode(attr2);
						}
						if (eElement.hasAttribute("manual")) {
							final Attr attr2 = document.createAttribute("manual");
							attr2.setValue(eElement.getAttribute("manual"));
							feature.setAttributeNode(attr2);
						}
						if (eElement.hasAttribute("name")) {
							final Attr attr2 = document.createAttribute("name");
							attr2.setValue(eElement.getAttribute("name"));
							feature.setAttributeNode(attr2);
						}
						config[l].appendChild(feature);

					}

				}
				on[l].appendChild(config[l]);
				root.appendChild(on[l]);

			}
			document.getDocumentElement().normalize();
			final XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//text()[normalize-space(.) = '']");
			final NodeList blankTextNodes = (NodeList) xpath.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < blankTextNodes.getLength(); i++) {
				blankTextNodes.item(i).getParentNode().removeChild(blankTextNodes.item(i));
			}

			final StringWriter stringWriter = new StringWriter();
			final StreamResult xmlOutput = new StreamResult(outputFileAddress);
			final TransformerFactory tf = TransformerFactory.newInstance();
			// tf.setAttribute("indent-number", 2);
			final Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(new DOMSource(document), xmlOutput);
			// System.out.println(xmlOutput.getWriter().toString());

			// java.lang.System.out.println(xmlOutput.getWriter().toString());

			// write the content into xml file
			/*
			 * TransformerFactory transformerFactory = TransformerFactory.newInstance();
			 * Transformer transformer = transformerFactory.newTransformer();
			 * DOMSource source = new DOMSource(doc);
			 * StreamResult result = new StreamResult(new File("C:\\cars.xml"));
			 * transformer.transform(source, result);
			 * // Output to console for testing
			 * StreamResult consoleResult = new StreamResult(System.out);
			 * transformer.transform(source, consoleResult);
			 */

		} catch (final Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}

}
