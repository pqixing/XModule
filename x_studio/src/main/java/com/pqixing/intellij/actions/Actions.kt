package com.pqixing.intellij.actions

class XGitAction : XAnAction() {
    XGitDialog(e.project ?: return, e).show()
}