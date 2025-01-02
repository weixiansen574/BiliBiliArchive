package top.weixiansen574.bilibiliArchive.bean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import top.weixiansen574.bilibiliArchive.core.biliApis.protobuf.danmaku.DanmakuElem;
import top.weixiansen574.bilibiliArchive.core.biliApis.protobuf.danmaku.DanmakuEvent;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Inflater;

public class Danmaku {
    public long id;
    public int progress;
    public int mode;
    public int fontsize;
    public int color;
    public String midHash;
    public String content;
    public long ctime;
    public int weight;
    public String action;
    public int pool;
    public String idStr;
    public int attr;
    public String animation;

    public static List<Danmaku>
    parseFromProtobuf(byte[] bytes) throws IOException {
        DanmakuEvent danmakuEvent = DanmakuEvent.parseFrom(bytes);
        List<DanmakuElem> elemsList = danmakuEvent.getElemsList();
        List<Danmaku> danmakuList = new ArrayList<>();
        for (DanmakuElem danmakuElem : elemsList) {
            Danmaku danmaku = new Danmaku();
            danmaku.id = danmakuElem.getId();
            danmaku.progress = danmakuElem.getProgress();
            danmaku.mode = danmakuElem.getMode();
            danmaku.fontsize = danmakuElem.getFontsize();
            danmaku.color = danmakuElem.getColor();
            danmaku.midHash = danmakuElem.getMidHash();
            danmaku.content = danmakuElem.getContent();
            danmaku.ctime = danmakuElem.getCtime();
            danmaku.weight = danmakuElem.getWeight();
            danmaku.action = danmakuElem.getAction();
            danmaku.pool = danmakuElem.getPool();
            danmaku.idStr = danmakuElem.getIdStr();
            danmaku.attr = danmakuElem.getAttr();
            danmaku.animation = danmakuElem.getAnimation();
            danmakuList.add(danmaku);
        }
        return danmakuList;
    }

    public long getId() {
        return id;
    }

    public static List<Danmaku> paresFromXML(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        List<Danmaku> danmakuList = new ArrayList<>();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(inputStream);
        NodeList nodeList = doc.getElementsByTagName("d");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String p = element.getAttribute("p");
                String[] split = p.split(",");
                Danmaku d = new Danmaku();
                d.progress = (int) (Double.parseDouble(split[0]) * 1000);
                d.mode = Integer.parseInt(split[1]);
                d.fontsize = Integer.parseInt(split[2]);
                d.color = Integer.parseInt(split[3]);
                d.ctime = Long.parseLong(split[4]);
                d.pool = Integer.parseInt(split[5]);
                d.midHash = split[6];
                d.id = Long.parseLong(split[7]);
                d.idStr = split[7];
                if (split.length > 8) {
                    d.weight = Integer.parseInt(split[8]);
                }
                d.content = element.getTextContent();
                danmakuList.add(d);
            }
        }
        inputStream.close();
        return danmakuList;
    }

    public static byte[] decompressDM(byte[] data) throws IOException {
        byte[] decompressData = null;
        Inflater decompressor = new Inflater(true);
        decompressor.reset();
        decompressor.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int i = decompressor.inflate(buf);
                outputStream.write(buf, 0, i);
            }
            decompressData = outputStream.toByteArray();
        } catch (Exception e) {
        } finally {
            outputStream.close();
        }
        decompressor.end();
        return decompressData;
    }

    public static void toXML(List<Danmaku> danmakusList, long cid, OutputStream outputStream) throws ParserConfigurationException, TransformerException, IOException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element iElement = document.createElement("i");
        document.appendChild(iElement);

        createElementAndAppend(document, iElement, "chatserver", "chat.bilibili.com");
        createElementAndAppend(document, iElement, "chatid", String.valueOf(cid));
        createElementAndAppend(document, iElement, "mission", "0");
        createElementAndAppend(document, iElement, "maxlimit", "1000");
        createElementAndAppend(document, iElement, "state", "0");
        createElementAndAppend(document, iElement, "real_name", "0");
        createElementAndAppend(document, iElement, "source", "k-v");

        for (Danmaku danmaku : danmakusList) {
            Element d = document.createElement("d");
            d.setAttribute("p", danmaku.toXMLAttribute());
            d.appendChild(document.createTextNode(danmaku.content));
            iElement.appendChild(d);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        // 设置输出格式为XML，并去掉空白文本节点
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(outputStream);
        // 将DOMSource的内容写入到streamResult中，也就是输出流中
        transformer.transform(domSource, streamResult);
        outputStream.close();
    }

    private static void createElementAndAppend(Document document, Element iElement, String elementName, String textContent) {
        Element element = document.createElement(elementName);
        element.appendChild(document.createTextNode(textContent));
        iElement.appendChild(element);
    }

    public static List<Danmaku> merge(List<Danmaku> dm1, List<Danmaku> dm2) {
        return new ArrayList<>(Stream.concat(dm1.stream(), dm2.stream())
                .collect(Collectors.toMap(Danmaku::getId, danmaku -> danmaku, (existing, replacement) -> existing))
                .values());
    }

    public String toXMLAttribute() {
        return ((double) progress) / 1000 + "," +
                mode + "," +
                fontsize + "," +
                color + "," +
                ctime + "," +
                pool + "," +
                midHash + "," +
                id + "," +
                weight;
    }

    @Override
    public String toString() {
        return "Danmaku{" +
                "id=" + id +
                ", progress=" + progress +
                ", mode=" + mode +
                ", fontsize=" + fontsize +
                ", color=" + color +
                ", midHash='" + midHash + '\'' +
                ", content='" + content + '\'' +
                ", ctime=" + ctime +
                ", weight=" + weight +
                ", action='" + action + '\'' +
                ", pool=" + pool +
                ", idStr='" + idStr + '\'' +
                ", attr=" + attr +
                ", animation='" + animation + '\'' +
                '}';
    }
}
