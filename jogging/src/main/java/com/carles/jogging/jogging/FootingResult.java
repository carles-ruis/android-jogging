package com.carles.jogging.jogging;

/**
 * Created by carles1 on 26/04/14.
 */
public enum FootingResult {

    SUCCESS("footing_result_success"),
    CANCELLED_BY_USER("footing_result_cancelled"),
    GPS_DISABLED("footing_result_gps_disabled"),
    GOOGLE_SERVICES_DISCONNECTED("footing_result_gs_disconnected"),
    NO_LOCATION_UPDATES("footing_result_no_updates"),
    GOOGLE_SERVICES_FAILURE("footing_result_gs_failure"),
    UNKNOWN_ERROR("footing_result_unknown_error");

    private String resourceId;
    FootingResult(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

};

