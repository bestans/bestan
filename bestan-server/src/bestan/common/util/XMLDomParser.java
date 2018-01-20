package bestan.common.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 解析XML工具类
 * @author lixiwen
 *
 */
public class XMLDomParser {
	
	private Document document = null;
	
    
	/**
     * Create DOM Document from the given path.
     * 
     * @param xmlPath 
     * 			the path of the xml file.
     * @throws IOException
     **/
	public XMLDomParser(String xmlPath) throws IOException{
		
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			// Parse the xml document.
			document = builder.parse(xmlPath);
			document.normalize();
			
		} catch(Exception e) {
			e.printStackTrace();			
		}		
	}
	
	
	/**
     * Recursively get the children Node by name from the given Node.
     * @param node 
     * 			the the given Node.
     * @param name
     * 			the name of the children Node
     * @return Node the matching child or null if none is found
     **/	
	public static Node getChildNodeByName(Node node, String name) {
		
		if (node.hasChildNodes()) {
			// Get the children node list from the given Node.
			NodeList children = node.getChildNodes();
			
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeName().equalsIgnoreCase(name)) {
					return children.item(i);
				}
	
				Node found = getChildNodeByName(children.item(i), name);
				if (found != null) {
					return found;
				}
				
			}
		} 
		
		return null;
	}
	/**
     * Get the children Node by name from the given Node.
     * @param node 
     * 			the the given Node.
     * @param name
     * 			the name of the children Node
     * @return Node List the matching child or null if none is found
     **/	
	public static List<Node> getChildNodeListByName(Node node, String name) {
		if (node.hasChildNodes()) {
			
			// Get the children node list from the given node.
			NodeList children = node.getChildNodes();
			List<Node> nodeList = new ArrayList<Node>();
			
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeName().equalsIgnoreCase(name)) {
					nodeList.add(children.item(i));
				} 
			}	
			return nodeList;
		} else {
			return null;
		}	
	}
	
	/**
     * Get the children Node by name from the document.
     * @param name
     * 			the name of the children Node
     * @return Node the matching child or null if none is found
     */	
	public Node getChildNodeByName(String name) {
		return getChildNodeByName(document, name);
		
	}
    /**
     * Get the Node at the same level as the given Node.
     * @param node		Node to find siblings for
     * @param name		name property
     * @return Node	with matching name, or null if no matching sibling found
     **/
	public static Node getSiblingNodeByName(Node node, String name) {
		if(null == node) {
			return null;
		}
		
		Node nextNode = node.getNextSibling();
		while (nextNode != null) {
			if (nextNode.getNodeName().equalsIgnoreCase(name)) {
					return nextNode;
			}
			nextNode = node.getNextSibling();
		}
		return null;
	}
	
	/**
     * Recursively looks at the node that are matched the given attribute and value.
     * 
     * @param node
     *            the parent node whose descendants will be compared
     * @param attribute
     *            the name of the attribute
     * @param name
     *            the value of the attribute
     * @return Node the matching child or null if none is found
     */
	public static Node getChildNodeByAttributeValue(Node node, String attribute, String name){
		
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++){
			NamedNodeMap map = children.item(i).getAttributes();
			if (map != null){
				for (int j = 0; j < map.getLength(); j++) {
					if (map.item(j).getNodeValue().equalsIgnoreCase(name)) {
						traceChildElementNodes(children.item(i));
						return children.item(i);
					}
				}
			}
		}
		return null;
		
	}
    /**
     * prints the names of all child nodes of a given node. No recursion, only direct descendants.
     * 
     * @param node
     *           print children for
     **/
	public static void traceChildElementNodes(Node node) {
		
		if (node.hasChildNodes()) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
                    System.out.println("Child number " + (i + 1) + " named: "
                            + children.item(i).getNodeName() + " value: " + children.item(i).getNodeValue());
                    System.out.println("Type: " + children.item(i).getNodeType());
			}
			
		}
	}
	
    /**
     * Get a Hash table of name-value pairs for a given node.
     * 
     * @param node
     *            Node to get the attributes of
     * @return Hash table attribute name-value pairs for the given node
     */
	public static Hashtable<String, String> getNodeAttributes(Node node) {
		
		Hashtable<String, String> attribute = new Hashtable<String, String>();
		NamedNodeMap map = node.getAttributes();
		if (map != null) {
			for (int i = 0; i < map.getLength(); i++){
				attribute.put(map.item(i).getNodeName(), map.item(i).getNodeValue());
			}		
		}

		return attribute;
	}
	
    /**
     * Get a attribute value of name-value pairs for a given node.
     * 
     * @param node
     *            Node to get the attributes of
     * @return attribute value  for the given node
     */
	public static String getNodeAttribute(Node node, String attribute) {
		
		NamedNodeMap map = node.getAttributes();
		if (map != null) {
			for (int i = 0; i < map.getLength(); i++) {
				if (map.item(i).getNodeName().equals(attribute)) {
					return map.item(i).getNodeValue();
				}
			}
		}
		return null;
	}
	
}
