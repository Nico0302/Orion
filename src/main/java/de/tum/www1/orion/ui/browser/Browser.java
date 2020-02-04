package de.tum.www1.orion.ui.browser;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;

public class Browser extends JPanel {
    private BrowserWebView browserView;

    public static Browser instance(Project project) {
        return ServiceManager.getService(project, Browser.class);
    }

    /**
     * Inits the web browser UI panel. It only contains the actual browser panel, which fills out the whole
     * tool window.
     */
    public void init() {
        browserView = new BrowserWebView();
        SwingUtilities.invokeLater(() -> {
            removeAll();
            final GridBagLayout layout = new GridBagLayout();
            setLayout(layout);

            browserView.init();
            final JComponent webPanel = browserView.getBrowser();
            add(webPanel);

            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridwidth = 0;
            constraints.weightx = 1;
            constraints.weighty = 1;
            layout.setConstraints(webPanel, constraints);

            validate();
            repaint();
        });
    }
}
