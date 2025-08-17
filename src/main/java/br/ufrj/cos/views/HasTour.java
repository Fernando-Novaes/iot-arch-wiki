package br.ufrj.cos.views;

import com.vaadin.componentfactory.onboarding.Onboarding;

public interface HasTour {

    /**
     * Creates and configures the guided tour for this specific view.
     * The implementation of this method should define all the steps
     * of the tour, attaching them to the components within the view.
     *
     * @return A Component representing the configured tour (e.g., a ShepherdTour instance).
     */
    Onboarding createTour();

    Boolean startDemoTour();
}
