package com.dachen.creator.ui;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.List;

public class StringModel implements ListModel<String> {

    private List<String> datas;
    public StringModel(List<String> datas){
        this.datas = datas;
    }

    @Override
    public int getSize() {
        return datas==null?0:datas.size();
    }

    @Override
    public String getElementAt(int i) {
        return datas.get(i);
    }

    @Override
    public void addListDataListener(ListDataListener listDataListener) {

    }

    @Override
    public void removeListDataListener(ListDataListener listDataListener) {

    }
}
