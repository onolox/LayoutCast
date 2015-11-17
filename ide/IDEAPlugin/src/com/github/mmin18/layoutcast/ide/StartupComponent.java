package com.github.mmin18.layoutcast.ide;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by mmin18 on 7/29/15.
 */
public class StartupComponent implements ApplicationComponent {
    private static File UPDATE_FILE;
    private static byte[] UPDATE_DATA;
    private static double UPDATE_VER;
    private static File STROKE_FILE;
    private static double STROKE_VER;

    public StartupComponent() {
        STROKE_FILE = null;
    }

    public void initComponent() {

    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "StartupComponent";
    }


    public static double getCastVersion() {
        if (STROKE_VER == 0) {
            try {
                File cp = new File(PathUtil.getJarPathForClass(StartupComponent.class));
                if (cp.isDirectory()) {
                    File file = new File(cp, "cast.py");
                    InputStream ins = new FileInputStream(file);
                    STROKE_VER = CastAction.readVersion(ins);
                    ins.close();
                }
                else {
                    ZipFile zf = new ZipFile(cp);
                    ZipEntry ze = zf.getEntry("cast.py");
                    InputStream ins = zf.getInputStream(ze);
                    STROKE_VER = CastAction.readVersion(ins);
                    ins.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Math.max(STROKE_VER, UPDATE_VER);
    }

    public static File getCastFile() {
        try {
            if (PropertiesComponent.getInstance().isValueSet("com.LayouCast.location")) {
                STROKE_FILE = new File(PropertiesComponent.getInstance().getValue("com.LayouCast.location"));
            }
            else {
                File cp = new File(PathUtil.getJarPathForClass(StartupComponent.class));
                if (cp.isDirectory()) {
                    STROKE_FILE = new File(cp, "cast.py");
                }
                else {
                    ZipFile zf = new ZipFile(cp);
                    ZipEntry ze = zf.getEntry("cast.py");
                    InputStream ins = zf.getInputStream(ze);
                    File tmp = File.createTempFile("lcast", "" + STROKE_VER);
                    FileOutputStream fos = new FileOutputStream(tmp);
                    ins = zf.getInputStream(ze);
                    byte[] buf = new byte[4096];
                    int l;
                    while ((l = ins.read(buf)) != -1) {
                        fos.write(buf, 0, l);
                    }
                    fos.close();
                    ins.close();
                    STROKE_FILE = tmp;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return STROKE_FILE;
    }
}
