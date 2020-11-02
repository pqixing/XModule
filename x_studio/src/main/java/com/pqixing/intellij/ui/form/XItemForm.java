package com.pqixing.intellij.ui.form;

import javax.swing.*;

public class XItemForm {

    public JPanel rootPanal;
    private JTextField ffffffffffffffTextField;
    private JTextField dTextField;
    private JTextField a123TextField;


    public static void main(String[] args) {
        JFrame frame = new JFrame("XItemForm");
        frame.setContentPane(new XItemForm().rootPanal);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
