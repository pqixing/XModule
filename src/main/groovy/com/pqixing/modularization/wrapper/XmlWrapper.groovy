package com.pqixing.modularization.wrapper
/**
 * 拓展类Utils
 */
class XmlWrapper {
    public Node node

    public XmlWrapper(String xmlString) {
        node = new XmlParser().parseText(xmlString)
    }

    public void save(File outFile){
        outFile.mkdirs()
        new XmlNodePrinter(new PrintWriter(outFile).print(node))
    }
}