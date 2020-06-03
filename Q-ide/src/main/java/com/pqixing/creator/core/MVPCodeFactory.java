package com.pqixing.creator.core;

import com.pqixing.creator.utils.AndroidUtils;
import com.pqixing.creator.utils.StringUtils;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class MVPCodeFactory {
    public static String generatContract(VirtualFile dir, String moduleName){
        return StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackageName(dir) + ";") +
                "\n" +
                StringUtils.formatSingleLine(0, "import  com.pqixing.common.constract.BaseContract;") +
//                StringUtils.formatSingleLine(0, "import  com.example.suzhan.plugintest.BaseContract;") +
                "\n" +
                StringUtils.formatSingleLine(0, "public interface "+moduleName+"Contract {") +
                "\n" +
                "}";
    }

    public static String getMvpModelString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.formatSingleLine(0, "/**\n" +
                " * @author \n" +
                " * @Description \n" +
                " * @date\n" +
                " */"));
        sb.append(StringUtils.formatSingleLine(0, "interface IModel extends BaseContract.IModel {"));
        sb.append("}");
        return sb.toString().trim();
    }

    public static String getMvpPrecenterString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.formatSingleLine(0, "/**\n" +
                " * @author \n" +
                " * @Description \n" +
                " * @date\n" +
                " */"));
        sb.append(StringUtils.formatSingleLine(0, "interface IPresenter extends BaseContract.IPresenter {"));
        sb.append("}");
        return sb.toString().trim();
    }

    public static String getMvpViewString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.formatSingleLine(0, "/**\n" +
                " * @author \n" +
                " * @Description \n" +
                " * @date\n" +
                " */"));
        sb.append(StringUtils.formatSingleLine(0, "interface IView extends BaseContract.IView {"));
        sb.append("}");
        return sb.toString().trim();
    }


    public static String generatModelImpl(VirtualFile dir, String moduleName){
        String parentPkg = getParentPkg(dir);
        return StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackageName(dir) + ";") +
                "\n" +
                StringUtils.formatSingleLine(0, "import  com.pqixing.common.model.BaseModel;") +
//                StringUtils.formatSingleLine(0, "import  com.example.suzhan.plugintest.BaseModel;") +
                StringUtils.formatSingleLine(0, "import  "+ parentPkg +".contract."+moduleName+"Contract;") +
                "\n" +
                StringUtils.formatSingleLine(0, "/**\n" +
                        " * @author \n" +
                        " * @Description \n" +
                        " * @date\n" +
                        " */")+
                StringUtils.formatSingleLine(0, "public class "+moduleName+"Model extends BaseModel implements "+moduleName+"Contract.IModel{") +
                "\n" +
                "}";
    }

    @NotNull
    private static String getParentPkg(VirtualFile dir) {
        String pkg = AndroidUtils.getFilePackageName(dir);
        return pkg.substring(0, pkg.lastIndexOf("."));
    }

    public static String generatPrensterImpl(VirtualFile dir, String moduleName){
        return StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackageName(dir) + ";") +
                "\n" +
//                StringUtils.formatSingleLine(0, "import  com.example.suzhan.plugintest.BaseContract;") +
//                StringUtils.formatSingleLine(0, "import  com.example.suzhan.plugintest.BasePresenter;") +
                StringUtils.formatSingleLine(0, "import  com.pqixing.common.constract.BaseContract;") +
                StringUtils.formatSingleLine(0, "import  com.pqixing.common.presenter.BasePresenter;") +
                StringUtils.formatSingleLine(0, "import  "+ getParentPkg(dir) +".contract."+moduleName+"Contract;") +
                StringUtils.formatSingleLine(0, "import  "+ getParentPkg(dir) +".model."+moduleName+"Model;") +
                "\n" +
                StringUtils.formatSingleLine(0, "/**\n" +
                        " * @author \n" +
                        " * @Description \n" +
                        " * @date\n" +
                        " */")+
                StringUtils.formatSingleLine(0, "public class "+moduleName+"Presenter " + "extends BasePresenter<"+moduleName+"Contract.IView, "+moduleName+"Contract.IModel> implements "+moduleName+"Contract.IPresenter{") +
                "\n" +
                StringUtils.formatSingleLine(0, "       @Override") +
                StringUtils.formatSingleLine(0, "       public Class<? extends BaseContract.IModel> getRealModel() {") +
                StringUtils.formatSingleLine(0, "               return "+moduleName+"Model.class;") +
                StringUtils.formatSingleLine(0, "       }") +
                "\n" +
                "}";
    }

    public static String generatActivity(VirtualFile dir, String moduleName){
        return StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackageName(dir) + ";") +
                "\n" +
                StringUtils.formatSingleLine(0, "import  com.pqixing.common.constract.BaseContract;") +
                StringUtils.formatSingleLine(0, "import  com.pqixing.common.views.activity.MVPBaseActivity;") +
//                StringUtils.formatSingleLine(0, "import  com.example.suzhan.plugintest.BaseContract;") +
//                StringUtils.formatSingleLine(0, "import  com.example.suzhan.plugintest.MVPBaseActivity;") +
                StringUtils.formatSingleLine(0, " import android.os.Bundle;") +
                StringUtils.formatSingleLine(0, "import  "+ getParentPkg(dir) +".contract."+moduleName+"Contract;") +
                StringUtils.formatSingleLine(0, "import  "+ getParentPkg(dir) +".presenter."+moduleName+"Presenter;") +
                "\n" +
                StringUtils.formatSingleLine(0, "/**\n" +
                        " * @author \n" +
                        " * @Description \n" +
                        " * @date\n" +
                        " */")+
                StringUtils.formatSingleLine(0, "public class "+moduleName+"Activity extends MVPBaseActivity<"+moduleName+"Contract.IPresenter> \n" +
                        "       implements "+moduleName+"Contract.IView {") +
                "\n" +
                StringUtils.formatSingleLine(0, "       @Override") +
                StringUtils.formatSingleLine(0, "       public Class<? extends BaseContract.IPresenter> getRealPresenter() {") +
                StringUtils.formatSingleLine(0, "               return "+moduleName+"Presenter.class;") +
                StringUtils.formatSingleLine(0, "       }") +
                "\n" +
                StringUtils.formatSingleLine(0, "       @Override") +
                StringUtils.formatSingleLine(0, "       protected void onCreate(Bundle savedInstanceState) {") +
                StringUtils.formatSingleLine(0, "               super.onCreate(savedInstanceState);") +
                StringUtils.formatSingleLine(0, "       }") +
                "\n" +
                "}";
    }

    public static String generatFragment(VirtualFile dir, String moduleName){
        return StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackageName(dir) + ";") +
                "\n" +
                StringUtils.formatSingleLine(0, " import android.os.Bundle;") +
                StringUtils.formatSingleLine(0, "import android.support.regester.Nullable;") +
                StringUtils.formatSingleLine(0, "import android.view.LayoutInflater;") +
                StringUtils.formatSingleLine(0, "import android.view.View;") +
                StringUtils.formatSingleLine(0, "import android.view.ViewGroup;") +
                StringUtils.formatSingleLine(0, "import  com.pqixing.common.constract.BaseContract;") +
                StringUtils.formatSingleLine(0, "import  com.pqixing.common.views.fragment.MVPBaseFragment;") +
//                StringUtils.formatSingleLine(0, "import  com.example.suzhan.plugintest.BaseContract;") +
//                StringUtils.formatSingleLine(0, "import  com.example.suzhan.plugintest.MVPBaseFragment;") +
                StringUtils.formatSingleLine(0, "import  "+ getParentPkg(dir) +".contract."+moduleName+"Contract;") +
                StringUtils.formatSingleLine(0, "import  "+ getParentPkg(dir) +".presenter."+moduleName+"Presenter;") +
                "\n" +
                StringUtils.formatSingleLine(0, "/**\n" +
                        " * @author \n" +
                        " * @Description \n" +
                        " * @date\n" +
                        " */")+
                StringUtils.formatSingleLine(0, "public class "+moduleName+"Fragment extends MVPBaseFragment<"+moduleName+"Contract.IPresenter> \n" +
                        "       implements "+moduleName+"Contract.IView {") +
                "\n" +
                StringUtils.formatSingleLine(0, "       @Override") +
                StringUtils.formatSingleLine(0, "       public Class<? extends BaseContract.IPresenter> getRealPresenter() {") +
                StringUtils.formatSingleLine(0, "               return "+moduleName+"Presenter.class;") +
                StringUtils.formatSingleLine(0, "       }") +
                "\n" +
                StringUtils.formatSingleLine(0, "       @Override") +
                StringUtils.formatSingleLine(0, "       public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {") +
                StringUtils.formatSingleLine(0, "               return super.onCreateView(inflater, container, savedInstanceState);") +
                StringUtils.formatSingleLine(0, "       }") +
                "\n" +
                "}";
    }
}
