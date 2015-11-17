package com.github.mmin18.layoutcast.ide;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;

/**
 * Created by Fabio on 16/11/2015.
 */
public class Settings implements Configurable {
    private JBCheckBox chkDefault;
    private JBTextField txtLocation;
    private boolean isChanged;
    private JBLabel lbError;

    @Nullable
    @Override
    public JComponent createComponent() {
        lbError = new JBLabel("Error, file not found");
        lbError.setBackground(Color.pink);
        lbError.setVisible(false);
        isChanged = false;
        chkDefault = new JBCheckBox("Use default location( inside Plugin.jar in .AndroidStudiox.x\\config\\plugins)");

        JBLabel label = new JBLabel("Specify another location for the cast.py:");
        txtLocation = new JBTextField();
        txtLocation.setEnabled(false);

        final JPanel panel = new JPanel(new VerticalFlowLayout());
        panel.setBorder(IdeBorderFactory.createTitledBorder("LayoutCast Configurations", false));
        panel.add(chkDefault);
        panel.add(new JBLabel("  "));
        panel.add(label);
        panel.add(txtLocation);
        panel.add(new JBLabel("  "));
        panel.add(lbError);

        if (PropertiesComponent.getInstance().isValueSet("com.LayouCast.location")) {
            chkDefault.setSelected(false);
            txtLocation.setEnabled(true);
            txtLocation.setText(PropertiesComponent.getInstance().getValue("com.LayouCast.location"));
        }

        chkDefault.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (chkDefault.isSelected()) {
                    txtLocation.setEnabled(false);
                    txtLocation.setText("");
                }
                else {
                    txtLocation.setEnabled(true);
                }
                isChanged = true;
            }
        });

        return panel;
    }

    @Override
    public boolean isModified() {
        return isChanged;
    }

    @Override
    public void apply() throws ConfigurationException {
        if (chkDefault.isSelected()) {
            PropertiesComponent.getInstance().unsetValue("com.LayouCast.location");
            isChanged = false;
        }
        else if (new File(txtLocation.getText()).exists()) {
            PropertiesComponent.getInstance().setValue("com.LayouCast.location", txtLocation.getText());
            isChanged = false;
            lbError.setVisible(false);
        }
        else {
            lbError.setVisible(true);
        }
    }

    @Override
    public void reset() {
        if (PropertiesComponent.getInstance().isValueSet("com.LayouCast.location")) {
            chkDefault.setSelected(false);
            txtLocation.setEnabled(true);
        }
        else {
            chkDefault.setSelected(true);
            txtLocation.setText("");
            lbError.setVisible(false);
        }
    }

    @Override
    public void disposeUIResources() {

    }

    @Nls
    @Override
    public String getDisplayName() {
        return "LayoutCast";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }
}
