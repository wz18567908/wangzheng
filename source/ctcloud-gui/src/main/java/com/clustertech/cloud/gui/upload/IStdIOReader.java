package com.clustertech.cloud.gui.upload;

import java.io.IOException;
import java.io.InputStream;

public interface IStdIOReader {
    public void read(InputStream input) throws IOException;
}
