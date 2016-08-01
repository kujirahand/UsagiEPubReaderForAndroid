package com.kujirahand.KXml;

import com.kujirahand.utils.KFile;
import com.kujirahand.utils.StringMap;
import com.kujirahand.utils.KStrTokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by kujira on 2016/04/03.
 */

public class KXmlParser {

    public KXmlNode root = null;

    private int bufsize = 1024 * 32;
    private String _tag;
    private StringMap _attr;
    private String _text;
    private KStrTokenizer tok;
    private String path;

    final public static int TYPE_END = 0;
    final public static int TYPE_TAG_BEGIN = 1; // <tag>
    final public static int TYPE_TAG_END = 2;   // </tag>
    final public static int TYPE_TAG_ONE = 3;   // <tag />
    final public static int TYPE_TEXT = 4;
    final public static int TYPE_DOCTYPE = 5;
    final public static int TYPE_COMMENT = 6;

    public static KXmlNode loadAndParse(String path) {
        KXmlParser parser = new KXmlParser();
        if (parser.load(path) == false) return null;
        KXmlNode node = parser.parse();
        return node;
    }

    public static KXmlNode parseString(String src) {
        KXmlParser parser = new KXmlParser();
        parser.tok = new KStrTokenizer(src);
        KXmlNode node = parser.parse();
        return node;
    }


    public KXmlParser() {
    }

    public boolean load(String path) {
        this.path = path;
        File f = new File(path);
        byte[] buf = new byte[bufsize];
        StringBuilder sb = new StringBuilder();
        if (!f.exists()) return false;
        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            for (;;) {
                String line = br.readLine();
                if (line == null) break;
                sb.append(line + "\n");
            }
            fr.close();
            tok = new KStrTokenizer(sb.toString());
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
    public static void save(KXmlNode root, String path) throws IOException {
    	String str = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" + root.toString();
    	KFile.save(path, str);
    }

    public int readOne() {
        if (tok.isEOS()) return TYPE_END;

        // read tag
        char c = tok.peek();
        if (c == '<') {
            return readTag();
        }

        // read text
        // --- 複数のスペースはスペース一つにまとめる
        boolean hasSpace = (c == ' ' || c == '\t' || c == '\r' || c == '\n');
        tok.skipSpaceReturn();
        _text = tok.getToken('<', false);
        if (hasSpace) {
            if (_text.length() > 0) _text = " " + _text;
        }

        return TYPE_TEXT;
    }

    public int readTag() {
        if (tok.peek() == '<') tok.next();
        tok.skipSpaceReturn();
        char c = tok.peek();
        if (c == '/') {
            tok.next(); // skip /
            _tag = tok.getToken('>', true);
            return TYPE_TAG_END;
        }
        if (c == '?') { // DOCTYPE
            tok.getToken('>', true);
            return TYPE_DOCTYPE;
        }
        if (c == '!') { // Comment?
        	if (tok.compareStr("!--")) {
        		tok.getTokenStr("-->", true);
        		return TYPE_COMMENT;
        	}
        	// <!DOCTYPE ... >
        	tok.getToken('>', true);
        	return TYPE_DOCTYPE;
        }
        // tag
        _attr = new StringMap();
        _tag = readTag_name();
        int tag_type = TYPE_TAG_BEGIN;
        
        // attributes
        while (!tok.isEOS()) {
            tok.skipSpaceReturn();
            char e = tok.peek();
            if (e == '>') {
                tok.next();
                break;
            }
            if (e == '/') {
                tok.next();
                tok.skipSpaceReturn();
                if (tok.peek() == '>') {
                    tok.next();
                    tag_type = TYPE_TAG_ONE;
                    break;
                }
            }
            // key
            String key = readTag_name();
            tok.skipSpaceReturn();
            if (tok.peek() != '=') {
                // only key
                _attr.put(key, "1");
                continue;
            }
            tok.next(); // skip =
            tok.skipSpaceReturn();
            String value;
            char q = tok.peek();
            if (q == '"') {
                tok.next();
                value = tok.getToken('"', true);
                _attr.put(key, value);
                continue;
            }
            if (q == '\'') {
                tok.next();
                value = tok.getToken('\'', true);
                _attr.put(key, value);
                continue;
            }
            int tmp = tok.getPosition();
            value = readTag_name(); // fuzzy
            _attr.put(key, value);
            if (tmp == tok.getPosition()) tok.next();
        }
        if (_attr.isEmpty()) _attr = null;

        // special rule
        if (_tag.equals("br") || _tag.equals("img") || _tag.equals("hr")) {
            tag_type = TYPE_TAG_ONE;
        }

        return tag_type;
    }


    private String readTag_name() {
        StringBuilder sb = new StringBuilder();
        while (!tok.isEOS()) {
            char c = tok.peek();
            if (c == ' ') break;
            if (c == '>') break;
            if (c == '=') break;
            if (c == '/') break;
            sb.append(c);
            tok.next();
        }
        return sb.toString();
    }

    public KXmlNode parse() {
        int etype;
        KXmlNode cur = this.root = new KXmlNode();
        //
        etype = readOne();
        while (etype != TYPE_END) {
            if (etype == TYPE_TEXT) {
            	if (_text.length() > 0) {
                    _text = KXmlParser.decodeSpecialChars(_text);
                    KXmlNode n = new KXmlNode();
                    n.nodeType = KXmlNode.TYPE_TEXT;
                    n.text = _text;
                    cur.appendChild(n);
                    n.parent = cur;
                }
            }
            else if (etype == TYPE_TAG_BEGIN) {
                // append child
                KXmlNode n = new KXmlNode();
                n.nodeType = KXmlNode.TYPE_ELEMENT;
                n.tag = _tag.toLowerCase();
                n.setAttr(_attr);
                n.parent = cur;
                cur.appendChild(n);
                cur = n;
            }
            else if (etype == TYPE_TAG_END) {
                _tag = _tag.toLowerCase();
                if (cur.tag.equals(_tag)) {
                    cur = cur.parent;
                } else {
                	if (cur.parent != null) {
                		if (cur.parent.tag.equals(_tag)) {
                			cur = cur.parent.parent;
                		}
                	}
                }
                if (cur == null) cur = root;
            }
            else if (etype == TYPE_TAG_ONE) {
                // append child
                _tag = _tag.toLowerCase();
                KXmlNode n = new KXmlNode();
                n.nodeType = KXmlNode.TYPE_ELEMENT;
                n.tag = _tag;
                n.setAttr(_attr);
                cur.appendChild(n);
            }
            else {
                // others
            }
            etype = readOne();
        }
        return this.root;
    }

    public static String encodeSpecialChars(String text) {
        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        for (char c : chars) {
            sb.append(encodeSpecialChars(c));
        }
        return sb.toString();
    }

    public static String encodeSpecialChars(char c) {
        switch (c) {
            case '&': return "&amp;";
            case '>': return "&gt;";
            case '<': return "&lt;";
            case '"': return "&quot;";
            case '\'': return "&apos;";
            default: return c + "";
        }
    }
    private static String[] decStrKey = new String[]{ "&apos;", "&quot;", "&lt;", "&gt;", "&amp;"};
    private static String[] decStrRep = new String[]{ "'",      "\"",     "<",    ">",    "&"};
    public static String decodeSpecialChars(String text) {
        // 高速化のため
        StringBuilder sb = new StringBuilder();
        char cs[] = text.toCharArray();
        int i = 0;
        while(i < cs.length) {
            char c = cs[i];
            if (c != '&') {
                sb.append(c);
                i++;
                continue;
            }
            // compare
            boolean match = false;
            for (int j = 0; j < decStrKey.length; j++) {
                boolean b = true;
                char[]key = decStrKey[j].toCharArray();
                for (int k = 1; k < key.length; k++) {
                    if (cs[i+k] != key[k]) {
                        b = false; break;
                    }
                }
                if (b) {
                    sb.append(decStrRep[j]);
                    i += key.length;
                    match = true;
                    break;
                }
            }
            if (!match) {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }
    public static String decodeSpecialCharsEasy(String text) {
        text = text.replace("&apos;", "'");
        text = text.replace("&quot;", "\"");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&amp;", "&");
        return text;
    }
}
