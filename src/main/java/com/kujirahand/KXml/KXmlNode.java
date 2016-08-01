package com.kujirahand.KXml;

import com.kujirahand.utils.StringList;
import com.kujirahand.utils.StringMap;

/**
 * Created by kujira on 2016/04/03.
 */
public class KXmlNode {
	final public static int TYPE_ELEMENT = 0;
	final public static int TYPE_TEXT = 1;
	
	public int nodeType = TYPE_ELEMENT;
	
    public KXmlNode parent = null;
    public String text = "";
    public String tag = "";

    private KXmlNodeList children = null;
    private StringMap attr = null;
    
    // format for toString() 
    private static int indentLevel = 2;
    private static String _indent_spc = "  ";
    
    public KXmlNode firstChild() {
    	if (children == null) return null;
    	return children.get(0);
    }
    
    public KXmlNodeList getChildren() {
        if (children == null) {
            children = new KXmlNodeList();
        }
        return this.children;
    }

    public void appendChild(KXmlNode node) {
        getChildren().add(node);
    }

    public void removeChild(KXmlNode node) {
        if (children == null) return;
        for (int i = 0; i < children.size(); i++) {
            KXmlNode n = children.get(i);
            if (n == node) {
                children.remove(i);
                break;
            }
        }
    }
    //
    public boolean removeChildRec(KXmlNode removeNode) {
        if (children == null) return false;
        int i = 0;
        while (i < children.size()) {
            KXmlNode n = children.get(i);
            if (n == removeNode) {
                children.remove(i);
                return true;
            }
            if (n.removeChildRec(removeNode)) return true;
            i++;
        }
        return false;
    }

    public StringMap getAttr() {
        if (attr == null) {
            attr = new StringMap();
        }
        return attr;
    }

    public void setAttr(StringMap attr) {
        this.attr = attr;
    }

    public String getAttrValue(String key) {
        StringMap att = getAttr();
        String value = att.get(key);
        return value;
    }

    public KXmlNodeList findChildren(String tag) {
        KXmlNodeList list = new KXmlNodeList();
        if (children == null) return null;
        for (int i = 0; i < children.size(); i++) {
            KXmlNode n = children.get(i);
            if (n.tag.equals(tag)) {
                list.add(n);
            }
        }
        return list;
    }

    public void getTagNames(String tag, KXmlNodeList result) {
        for (KXmlNode n : getChildren()) {
            if (n.tag.equals(tag)) {
                result.add(n);
                continue;
            }
            n.getTagNames(tag, result);
        }
    }

    public String getTagAttrValue(String tag, String attrName) {
        KXmlNode node = getByTag(tag);
        if (node == null) return null;
        String r = node.getAttrValue(attrName);
        return r;
    }

    public KXmlNode getByTag(String tag) {
        // self
        if (tag.equals(this.tag)) return this;
        // children
        for (KXmlNode n : getChildren()) {
            KXmlNode nn = n.getByTag(tag);
            if (nn != null) return nn;
        }
        return null;
    }

    public KXmlNode getById(String id) {
        // self
        String value = getAttrValue("id");
        if (id.equals(value)) return this;
        // children
        for (KXmlNode n : getChildren()) {
            KXmlNode nn = n.getById(id);
            if (nn != null) return nn;
        }
        return null;
    }

    public KXmlNode getByTagAndAttr(String tag, String attr_key, String attr_value) {
        // self
        if (this.tag.equals(tag)) {
            String value = getAttrValue(attr_key);
            if (attr_value.equals(value)) return this;
        }
        // children
        for (KXmlNode n : getChildren()) {
            KXmlNode nn = n.getByTagAndAttr(tag, attr_key, attr_value);
            if (nn != null) return nn;
        }
        return null;
    }

    public boolean isTextNode() {
    	return (nodeType == TYPE_TEXT);
    }

    public static void setIndentLevel(int level) {
    	indentLevel = level;
    	_indent_spc = "";
    	for (int j = 0; j < indentLevel; j++) {
    		_indent_spc += " ";
    	}
    	
    }
    
    public String indent(int level) {
    	String n = "";
    	for (int i = 0; i < level; i++) {
    		n += _indent_spc;
    	}
    	return n;
    }
    
    public String toStringEx(int level) {
    	// text node
    	if (this.nodeType == TYPE_TEXT) {
    		return indent(level) + this.text;
    	}
    	// element
        String result = indent(level);
        KXmlNodeList children = this.getChildren();
        
        // EOL
        String EOL = "\n";
        if (indentLevel == 0) EOL = "";
        
        // tag name
        result += "<" + this.tag;
        // attr
        for (String key : this.getAttr().keySet()) {
            String value = attr.get(key);
            result += " " + key + "=" + '"' + value + '"';
        }
        //
        if (children.size() == 0) {
        	result += " />";
        	return indent(level) + result;
        }
        
        // with child element
        result += ">" + EOL;
        for (int i = 0; i < children.size(); i++) {
            KXmlNode node = children.get(i);
            String line = node.toStringEx(level + 1);
            if (indentLevel > 0) line = line.trim();
            result += indent(level + 1) + line + EOL;
        }
        result += indent(level) + "</" + this.tag + ">" + EOL;
        return result;
    }
    
    public KXmlNodeList getSerialList(KXmlNodeList list) {
    	if (list == null) list = new KXmlNodeList();
    	list.add(this);
    	for (KXmlNode n : getChildren()) {
    		n.getSerialList(list);
    	}
    	return list;
    }
    
    public KXmlNode getLinkInParent() {
    	KXmlNode p = this;
    	while (p != null) {
    		if (p.nodeType != TYPE_ELEMENT) continue;
    		if (p.tag.equals("a")) {
    			return p;
    		}
    		p = p.parent;
    	}
    	return null;
    }

    private String _getText(KXmlNode n, String text) {
        if (n.isTextNode()) {
            text += n.text;
            return text;
        }
        for (KXmlNode nn : getChildren()) {
            text += nn._getText(nn, text);
        }
        return text;
    }

    public String getText() {
        return _getText(this, "");
    }

    public String toString() {
    	return this.toStringEx(0);
    }
}
