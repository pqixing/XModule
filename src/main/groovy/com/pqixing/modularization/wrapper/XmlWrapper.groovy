package com.pqixing.modularization.wrapper

import com.pqixing.modularization.utils.CheckUtils

/**
 * 拓展类Utils
 */
class XmlWrapper {
    public Node node

    static Node parse(String xmlString) {
        if (CheckUtils.isEmpty(xmlString)) xmlString = '''<empty></empty>'''
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