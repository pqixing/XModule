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
        new Printer(new PrintWriter(outFile)).print(node)
    }

    static class Printer extends XmlNodePrinter {

        Printer(PrintWriter out) {
            super(out)
        }

        @Override
        void print(Node node) {

            namespaceAware = true
            def nameSpaces = XmlNodePrinter.NamespaceContext.class.newInstance(this)
//            nameSpaces.registerNamespacePrefix("android", "http://schemas.android.com/apk/res/android")
//            nameSpaces.registerNamespacePrefix("tools", "http://schemas.android.com/tools")
            print(node, nameSpaces)
        }
    }

}