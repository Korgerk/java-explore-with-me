package ru.practicum.ewm.util;

public final class ApiPaths {
    private ApiPaths() {
    }

    public static final String ADMIN = "/admin";
    public static final String USERS = "/users";

    public static final String EVENTS = "/events";
    public static final String CATEGORIES = "/categories";
    public static final String COMPILATIONS = "/compilations";
    public static final String REQUESTS = "/requests";
    public static final String SUMMARY = "/summary";
    public static final String RATINGS = "/rating";

    public static final String USER_ID = "/{userId}";
    public static final String EVENT_ID = "/{eventId}";
    public static final String CAT_ID = "/{catId}";
    public static final String COMP_ID = "/{compId}";
    public static final String REQUEST_ID = "/{requestId}";
    public static final String ID = "/{id}";

    public static final String CANCEL = "/cancel";

    public static final String ADMIN_USERS = ADMIN + USERS;
    public static final String ADMIN_EVENTS = ADMIN + EVENTS;
    public static final String ADMIN_CATEGORIES = ADMIN + CATEGORIES;
    public static final String ADMIN_COMPILATIONS = ADMIN + COMPILATIONS;

    public static final String PRIVATE_USER_EVENTS = USERS + USER_ID + EVENTS;
    public static final String PRIVATE_USER_REQUESTS = USERS + USER_ID + REQUESTS;
    public static final String PRIVATE_EVENT_REQUESTS = USERS + USER_ID + EVENTS + EVENT_ID + REQUESTS;

    public static final String PRIVATE_EVENT_RATINGS = USERS + USER_ID + EVENTS + EVENT_ID + RATINGS;

    public static final String PUBLIC_EVENTS = EVENTS;
    public static final String PUBLIC_CATEGORIES = CATEGORIES;
    public static final String PUBLIC_COMPILATIONS = COMPILATIONS;

    public static final String EVENT_URI_PREFIX = EVENTS + "/";
}
