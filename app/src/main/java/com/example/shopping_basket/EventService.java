package com.example.shopping_basket;

import com.google.firebase.firestore.FirebaseFirestore;

public class EventService {
    private static final String TAG = "EventRepository";
    private static final String PROFILES_COLLECTION = "profiles";
    private static final String EVENTS_COLLECTION = "events";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
}
