package org.vaadin.hashbanglegacysupportexample;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.PushStateNavigation;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

// This annotation enables push state navigation
@PushStateNavigation
public class MyUI extends UI {

    public static class MainView extends VerticalLayout implements View {

        private Label label = new Label("");

        public MainView() {
            label.setCaption("Main View");
            addComponent(label);
        }

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            label.setValue("Parameters: " + event.getParameters());
            addComponent(new Button("Go to SecondView", e->getUI().getNavigator().navigateTo("second")));
            addComponent(new Link("Go to SecondView with legacy link", new ExternalResource("/#!second/with/some/parameters")));
        }

    }

    public static class SecondView extends VerticalLayout implements View {

        private Label label = new Label("");

        public SecondView() {
            label.setCaption("Second View");
            addComponent(label);
            addComponent(new Button("Go to MainView", e->getUI().getNavigator().navigateTo("")));
        }

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            label.setValue("Parameters: " + event.getParameters());
        }

    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setNavigator(new Navigator(this, this));
        getNavigator().addView("second", SecondView.class);
        getNavigator().setErrorView(MainView.class);
        // Most Vaadin apps are inited so that the fragment is already available
        // here and that is enough
        checkForLegacyHashBangUrl();
        // If you might have links in your Vaadin app to hash bangs, like we 
        // have for demo purposes here in the MainView, also do the check in
        getPage().addUriFragmentChangedListener(e-> checkForLegacyHashBangUrl());
    }

    /**
     * This method supports old style #!foo/bar URLs that people might still
     * link if the app has been in use before HTML5 History era.
     */
    protected void checkForLegacyHashBangUrl() {
        String uriFragment = getPage().getUriFragment();
        if(uriFragment != null) {
            if(uriFragment.startsWith("!")) {
                // Assume this is a legacy hashbang URL, assign directly to Navigator and clear
                getNavigator().navigateTo(uriFragment.substring(1));
                getPage().setUriFragment(null);
            } else {
                // Some other fragment, just ignore or do what you have done before
            }
        }
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
