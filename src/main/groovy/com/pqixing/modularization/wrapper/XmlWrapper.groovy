package com.pqixing.modularization.wrapper
/**
 * 拓展类Utils
 */
class XmlWrapper {
    public Node node

    static Node parse(String xmlString) {
        return new XmlParser().parseText(xmlString)
    }

    public XmlWrapper(String xmlString) {
        node = parse(xmlString)
    }

    public void writeTo(File outFile) {
        outFile.parentFile.mkdirs()
        new XmlNodePrinter(new PrintWriter(outFile)).print(node)
    }

}