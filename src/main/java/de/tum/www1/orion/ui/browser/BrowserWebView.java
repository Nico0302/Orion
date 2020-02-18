package de.tum.www1.orion.ui.browser;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import de.tum.www1.orion.settings.OrionSettingsProvider;
import de.tum.www1.orion.ui.OrionRouter;
import de.tum.www1.orion.ui.util.UrlAccessForbiddenWarning;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.util.Objects;
import java.util.ResourceBundle;

public class BrowserWebView {
    private WebView browser;
    private WebEngine engine;
    private JFXPanel browserPanel;
    private Project project;

    /**
     * Inits the actual browser panel. We use a JFXPanel in a {@link WebView} gets initialized. This web view only
     * displays the ArTEMiS Angular webapp containing a few adaptions, so that we only show the most important information
     * in the IDE.
     */
    public void init() {
        browserPanel = new JFXPanel();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            browser = new WebView();
            engine = browser.getEngine();
            final var version = ResourceBundle.getBundle("de.tum.www1.orion.Orion").getString("version");
            engine.setUserAgent(ServiceManager.getService(OrionSettingsProvider.class).getSetting(OrionSettingsProvider.KEYS.USER_AGENT) + " Orion/" + version);
            project = Objects.requireNonNull(DataManager.getInstance().getDataContext(browserPanel).getData(CommonDataKeys.PROJECT));

            final OrionRouter orionRouter = ServiceManager.getService(project, OrionRouter.class);
            final String route = orionRouter.routeForCurrentExercise();
            engine.load(Objects.requireNonNullElseGet(route, orionRouter::defaultRoute));

            injectJSBridge();
            preventAccessToExternalWebpages();
        });
    }

    private void injectJSBridge() {
        engine.getLoadWorker().stateProperty().addListener((observableValue, state, t1) -> {
            if (state == Worker.State.SUCCEEDED || t1 == Worker.State.SUCCEEDED) {
                project.getMessageBus().syncPublisher(OrionBrowserNotifier.ORION_BROWSER_TOPIC).artemisLoadedWith(engine);
            }
        });
    }

    private void preventAccessToExternalWebpages() {
        this.engine.locationProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.startsWith(ServiceManager.getService(project, OrionRouter.class).defaultRoute())) {
                engine.load(oldValue);
                ApplicationManager.getApplication().invokeLater(() -> new UrlAccessForbiddenWarning(project).show());
            }
        }));
    }

    /**
     * Get the browser panel in which the ArTEMiS webapp is displayed.
     *
     * @return The {@link JFXPanel} containing the {@link WebView} with ArTEMiS
     */
    public JComponent getBrowser() {
        Platform.runLater(() -> {
            final BorderPane borderPane = new BorderPane();
            borderPane.setCenter(browser);
            final Scene scene = new Scene(borderPane);
            browserPanel.setScene(scene);
        });

        return browserPanel;
    }

    public interface OrionBrowserNotifier {
        Topic<OrionBrowserNotifier> ORION_BROWSER_TOPIC = Topic.create("Orion Browser Init", OrionBrowserNotifier.class);

        void artemisLoadedWith(final WebEngine engine);
    }
}
