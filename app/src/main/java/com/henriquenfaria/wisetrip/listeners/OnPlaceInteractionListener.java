package com.henriquenfaria.wisetrip.listeners;

import com.henriquenfaria.wisetrip.models.PlaceModel;

/**
 * Listener for communication between PlaceListFragment and its host Activity
 */
public interface OnPlaceInteractionListener {
    void onPlaceClicked(PlaceModel place);
}