package com.carles.jogging.util;

/**
 * Created by carles1 on 21/04/14.
 */
public class LocationUtil {

    public static boolean hasReachedKilometer(float distance, float previousDistance) {
        if ((int) (distance / 1000.0f) > (int) (previousDistance / 1000.0f)) {
            return true;
        } else {
            return false;
        }
    }
}
//    private boolean servicesConnected() {
//        // Check that Google Play services is available
//        int resultCode = GooglePlayServicesUtil.
//                isGooglePlayServicesAvailable(this);
//        // If Google Play services is available
//        if (ConnectionResult.SUCCESS == resultCode) {
//            // In debug mode, log the status
//            Log.d("Location Updates", "Google Play services is available.");
//            // Continue
//            return true;
//            // Google Play services was not available for some reason
//        } else {
//            // Get the error code
//            int errorCode = connectionResult.getErrorCode();
//            // Get the error dialog from Google Play services
//            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
//
//            // If Google Play services can provide an error dialog
//            if (errorDialog != null) {
//                // Create a new DialogFragment for the error dialog
//                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
//                // Set the dialog in the DialogFragment
//                errorFragment.setDialog(errorDialog);
//                // Show the error dialog in the DialogFragment
//                errorFragment.show(getSupportFragmentManager(), "Location Updates");
//            }
//        }
//    }
