package com.kujirahand.KPage;

import com.kujirahand.KCSS.CSSItems;
import com.kujirahand.KCSS.CSSParser;
import com.kujirahand.KXml.KXmlNode;
import com.kujirahand.KXml.KXmlNodeList;
import com.kujirahand.utils.KFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by kujira on 2016/04/05.
 */
public class KPageParser {
	
	private static String KINSOKU_DISALLOW_FOOT =
            "（([｛〔〈《「『【〘〖〝‘“｟«";
	private static String KINSOKU_DISALLOW_HEAD = "。、？）」』．，.,?)-" +
            "ゝゞーァィゥェォッャュョヮヵヶぁぃぅぇぉっゃゅょゎゕゖㇰㇱㇲㇳㇴㇵㇶㇷㇸㇹㇷ゚ㇺㇻㇼㇽㇾㇿ々〻,"+
            ")]｝、〕〉》」』】〙〗〟’”｠»:";
    private static HashMap<String, Boolean> newlineTagListMap = null;

	public static KCharList convertCharList(KCharList clist, KXmlNode node) {
		if (clist == null) clist = new KCharList();
		if (node.isTextNode()) {
			clist.appendStr(node.text);
			return clist;
		}
		String tag = node.tag;
        KTagBegin tagBegin = new KTagBegin(node.tag, node.getAttr());
		if (!tag.equals("")) {
			clist.add(tagBegin);
		}
		for (KXmlNode n : node.getChildren()) {
			convertCharList(clist, n);
		}
		if (!tag.equals("")) {
			clist.add(new KTagEnd(tagBegin));
		}
		return clist;
	}

    public static KCharMeasureIF charMeasure = null;

    public static KPage parse(KCharList src) {
        if (charMeasure == null) {
            throw new NullPointerException("charMeasure Object is null.");
        }
        int width = charMeasure.getFrameWidth();

        String title = src.findTagText(KTagNo.TITLE, 0);
        if (title.equals("")) title = "No Title";

        KPage page = splitLine(src, width);
        page.title = title;

        // TODO: CSS (parseFromXML)を見る
        return page;
    }

	public static KPage parseFromXMl(KXmlNode node) {
        if (charMeasure == null) {
			throw new NullPointerException("charMeasure Object is null.");
        }
		int width = charMeasure.getFrameWidth();

        String title = "no title";
        CSSParser cssParser = new CSSParser();
        // head
        KXmlNode head_n = node.getByTag("head");
        if (head_n != null) {
            // title
            KXmlNode title_n = head_n.getByTag("title");
            if (title_n != null) {
                title = title_n.getText();
            }
            // link
            KXmlNodeList links = head_n.findChildren("link");
            if (links != null) {
                for (KXmlNode lt : links) {
                    String href = lt.getAttrValue("href");
                    String type = lt.getAttrValue("type");
                    if (type != null) {
                        if (!type.equals("text/css")) href = null;
                    }
                    // cssパスの解決
                    String path = charMeasure.getAbsolutePath(href);
                    try {
                        // TODO: 将来的にCSSのパース
                        String src = KFile.load(path);
                        //cssParser.parse(src);
                        // 今はとりあえず読み込むだけ
                        CSSItems css = cssParser.getCSS();
                        css.setRawString(src);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // style
            KXmlNode style_n = head_n.getByTag("style");
            if (style_n != null) {
                String style_s = style_n.getText();
                cssParser.parse(style_s);
            }
            if (head_n.parent != null) {
                head_n.parent.removeChild(head_n);
            }
        }

        // remove style
        KXmlNodeList styles = new KXmlNodeList();
        node.getTagNames("style", styles);
        for (KXmlNode el: styles) {
            el.getChildren().clear();
        }
        KXmlNode body_node = node.getByTag("body");
        if (body_node == null) {
            body_node = node;
        } else {
            body_node.tag = "";
        }

		KCharList all = convertCharList(null, body_node);
		KPage page = splitLine(all, width);
        page.title = title;
        page.css = cssParser.getCSS();

		return page;
	}

    public static KPage splitLine(KCharList clist, int width) {
        KPage page = new KPage();
        page.source = clist;
        page.lines.clear();

        // set variable
        float clen = 0;
        int index = 0;
        int size = clist.size();
        Stack<KTagBegin> stack = new Stack<KTagBegin>();

        // line info
        KLineInfo info = new KLineInfo(0, 0);
        page.lines.add(info);
        KLineInfo lastInfo = null;

        // Unvisible tag
        boolean isHeader = false;
        boolean isStyle = false;

        // check
        while (index < size) {
            Object o = clist.get(index);
            if (o instanceof KTagBegin) {
                KTagBegin tagBegin = (KTagBegin)o;
                if (tagBegin.tagNo == KTagNo.HEAD) isHeader = true;
                if (tagBegin.tagNo == KTagNo.STYLE) isStyle = true;
                if (stack.size() > 0) {
                    tagBegin.parent = stack.peek();
                }
                stack.push((KTagBegin)o);
                int tagNo = tagBegin.tagNo;
                if (tagNo == KTagNo.IMG) {
                    info.end = index;
                    clen = 0;
                    for (int j = 0; j <= 6; j++) {
                        info = new KLineInfo(index, index + 1);
                        page.lines.add(info);
                        info.pocketNo = j;
                    }
                    info = new KLineInfo(index + 1, index + 1);
                }
                index++;
                continue;
            }
            if (o instanceof KTagEnd) {
                KTagEnd tagEnd = (KTagEnd)o;
                KTagBegin top = stack.peek();
                if (tagEnd.tagBegin == top) {
                    stack.pop();
                }
                //
                int tagNo = tagEnd.getTagNo();
                if (tagNo == KTagNo.HEAD) isHeader = false;
                if (tagNo == KTagNo.STYLE) isStyle = false;
                if (KTagBegin.isLineTag(tagNo) || KTagBegin.isParagraphTag(tagNo)) {
                    index++;
                    //TODO: ここでパラグラフの連続で閉じタグがあればindexを進める
                    while (index < clist.size()) {
                        Object o2 = clist.get(index);
                        if (o2 instanceof KTagEnd) {
                            int tagNo2 = ((KTagEnd)o2).getTagNo();
                            if (KTagBegin.isLineTag(tagNo2) || KTagBegin.isParagraphTag(tagNo2)) {
                                if (stack.peek().tagNo == tagNo2) {
                                    stack.pop();
                                    index++;
                                    continue;
                                }
                            }
                        }
                        break;
                    }
                    info.end = index;
                    lastInfo = info;
                    info = new KLineInfo(index, index);
                    page.lines.add(info);
                    clen = 0;
                    // todo: 連続でパラグラフがあれば、それは一つと見なす
                    if (KTagBegin.isParagraphTag(tagNo)) {
                        info = new KLineInfo(index, index);
                        page.lines.add(info);
                    }
                    continue;
                }
                index++;
                continue;
            }
            if (isHeader || isStyle) {
                index++;
                continue;
            }
            // if (o instaceof Character) {
            // --- 文字を数える ---
            float w;
            int cur_i = index;
            // 英単語
            if (isWord((char)o)) {
                StringBuilder wb = new StringBuilder();
                while (index < size) {
                    Object ec = clist.get(index);
                    if (ec instanceof Character && isWord((char)ec)) {
                        wb.append((char)ec);
                        index++;
                        continue;
                    }
                    break;
                }
                w = getStrWidthPx(wb.toString());
                clen += w;
                if (clen > width) {
                    info.end = cur_i;
                    lastInfo = info;
                    info = new KLineInfo(cur_i, index);
                    page.lines.add(info);
                    clen = w;
                }
                continue;
            }
            // マルチバイトの場合
            else {
                char c = (char)o;
                // 行頭のチェック
                if (clen == 0) {
                    if (KINSOKU_DISALLOW_HEAD.indexOf("" + c) >= 0) {
                        if (lastInfo != null) {
                            lastInfo.end--;
                            info.start--;
                            KCharList sub = clist.substring(info.start, index);
                            clen = getStrWidthPx(sub.getOnlyText());
                        }
                    }
                }

                w = getCharWidthPx(c);
                clen += w;
                boolean flag_over = (clen > width);
                if (flag_over) {
                    if (KINSOKU_DISALLOW_FOOT.indexOf("" + w) >= 0) {
                        flag_over = false;
                    }
                }

                if (flag_over) {
                    info.end = index;
                    lastInfo = info;
                    // debug: String s = clist.substring(info).toString();
                    // System.out.println("["+s+"]");
                    info = new KLineInfo(index, index);
                    page.lines.add(info);
                    clen = 0;
                    continue;
                }
                index++;
                continue;
            }
        }
        info.end = index;
        // 行の最適化
        slimLine(page);
        return page;
    }

    private static void slimLine(KPage page) {
        int k = 0;
        KLineInfo info, lastInfo = null;
        while (k < page.lines.size()) {
            info = page.lines.get(k);
            // System.out.println(info.start + "-" + info.end + ":" + page.source.substringOnlyStr(info.start, info.end));
            if (lastInfo != null) {
                if (info.width() == 0 && lastInfo.width() == 0) {
                    if (info.pocketNo == 0) { // for <img>tag
                        page.lines.remove(k);
                        // System.out.println("remove=" + k);
                        continue;
                    }
                }
            }
            lastInfo = info;
            k++;
        }
    }

    public static boolean isWord(char c) {
        return ('a' <= c && c <= 'z' ||
                'A' <= c && c <= 'Z' ||
                '0' <= c && c <= '9');
    }

    public static float getCharWidthPx(Character c) {
        String t = c.toString();
        return charMeasure.getStrWidth(t);
    }
    public static float getStrWidthPx(String t) {
        return charMeasure.getStrWidth(t);
    }
}
