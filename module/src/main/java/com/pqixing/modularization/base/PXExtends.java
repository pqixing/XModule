package com.pqixing.modularization.base;

import com.pqixing.Config;
import com.pqixing.model.ManifestModel;
import com.pqixing.model.Module;
import com.pqixing.modularization.android.DpsManager;
import com.pqixing.modularization.maven.MavenModel;

public class PXExtends {

    public boolean transform;
    public MavenModel maven;
    public ManifestModel manifest;
    public Config config;
    public DpsManager dpsManager;
    public Module module;

}
