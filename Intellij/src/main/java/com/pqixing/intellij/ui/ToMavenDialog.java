package com.pqixing.intellij.ui;

import com.pqixing.git.Components;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;

public class ToMavenDialog extends JDialog {
    private JPanel rootPane;
    private JProgressBar progressBar1;
    private JList list1;
    private JCheckBox checkBox1;
    private JCheckBox checkBox2;
    private JCheckBox checkBox3;
    private JCheckBox checkBox4;
    private JCheckBox checkBox5;
    private JCheckBox checkBox6;
    private JButton button1;
    private JTextArea jkfjklsjdfksjkldfTextArea;
    private JButton btnOk;
    private JList jlist;
    private JCheckBox cbMore;
    private JPanel morePanel;
    private ToMavenDialogListener listener;
    private List<Components> items;
    private Components self;
    //用户状态 0,默认,1,正在上传中,2,上传失败, 3,上传完成
    private int status = 0;

    public ToMavenDialog() {
        this(null, null);
    }

    public ToMavenDialog(List<Components> items, Components self) {
        setContentPane(rootPane);
        setModal(false);
        getRootPane().setDefaultButton(btnOk);
        this.items = items;
        this.self = self;

        btnOk.addActionListener(e -> onOK());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        rootPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        cbMore.addChangeListener(changeEvent -> setPanelSize());
        SelectModel model = new SelectModel();
        jlist.setModel(model);
        jlist.setSelectedIndices(new int[]{1,2,3,4});
        jlist.setComponentPopupMenu(new JPopupMenu());
        jlist.setCellRenderer(model);
    }

    public ToMavenDialog setListener(ToMavenDialogListener listener) {
        this.listener = listener;
        return this;
    }

    private void setPanelSize() {
        boolean selected = cbMore.isSelected();
        morePanel.setVisible(selected);
        this.setSize(selected ? 600 : 250, 400);
    }

    private void onOK() {

    }

    public static void main(String[] args) {
        ToMavenDialog dialog = new ToMavenDialog();
        dialog.pack();
        dialog.setVisible(true);
    }

    public void onExcuteFinsh(Components item, int index, String result, boolean success) {

    }

    public interface ToMavenDialogListener {
        void onNextExcute(ToMavenDialog dialog, Components item, int index);

        void onExcuteFinish(ToMavenDialog dialog);
    }

    private static class SelectModel extends AbstractListModel<String> implements ListCellRenderer<String> {

        @Override
        public int getSize() {
            return 50;
        }

        @Override
        public String getElementAt(int i) {
            return "getElementAt +"+i;
        }

        @Override
        public Component getListCellRendererComponent(JList jList, String o, int i, boolean b, boolean b1) {
            return new JCheckBox("test "+1);
        }
    }
}
