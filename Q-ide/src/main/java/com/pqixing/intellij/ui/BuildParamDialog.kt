package com.pqixing.intellij.ui

import com.pqixing.intellij.utils.UiUtils
import javax.swing.*
import java.awt.event.*
import kotlin.math.max

class BuildParamDialog : BaseJDialog() {
    private var contentPane: JPanel? = null
    private var buttonOK: JButton? = null
    private var jLVersion: JLabel? = null
    private var cbBuildType: JComboBox<*>? = null
    private var cbDpModel: JComboBox<*>? = null
    private var btnCheckoutTemplet: JButton? = null
    private var tfTemplet: JTextField? = null
    private var btnCheckoutModule: JButton? = null
    private var btnClear: JButton? = null
    private var tfModule: JTextField? = null
    private var jpModule: JPanel? = null
    private var jpTemplet: JPanel? = null
    private var jpContent: JPanel? = null

    var versionPath: String? = null
    var buildType: String? = null
    var dpModel: String? = null

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        title = "Build Params"
        buttonOK!!.addActionListener { onOK() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })

        // call onCancel() on ESCAPE
        contentPane!!.registerKeyboardAction({ onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        initView()
    }

    private fun initView() {
        UiUtils.setTransfer(contentPane!!) { list ->
            updateStatusByVersionFile(list.find { it.name.endsWith(".version") }?.absolutePath)
        }
    }

    var defHeight = -1
    fun updateStatusByVersionFile(path: String?) {
        if(defHeight<0) defHeight = height
        versionPath = path
        jLVersion?.text = versionPath?.let {
            if(it.length<=68)  it  else ("..."+it.substring(Math.max(it!!.length-65, 0)))
        } ?: "Drag version file into panel"
        jpContent?.isVisible = versionPath != null
        setSize(width,if(versionPath==null) defHeight else 210)
    }

    private fun onOK() {
        // add your code here
        dispose()
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }
}
