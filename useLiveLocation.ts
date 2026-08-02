import { useState, useEffect, useCallback, useRef } from 'react';
import * as Location from 'expo-location';

export interface LiveLocation {
  latitude: number;
  longitude: number;
  altitude: number | null;
  accuracy: number | null;
  altitudeAccuracy: number | null;
  heading: number | null;
  speed: number | null;
  timestamp: number;
}

export interface UseLiveLocationResult {
  location: LiveLocation | null;
  errorMsg: string | null;
  isLoading: boolean;
  isTrackingActive: boolean;
  permissionStatus: Location.PermissionStatus | null;
  requestPermission: () => Promise<boolean>;
  startTracking: () => Promise<void>;
  stopTracking: () => void;
}

interface UseLiveLocationOptions {
  accuracy?: Location.Accuracy;
  timeInterval?: number; // Minimum time to wait between updates (in ms)
  distanceInterval?: number; // Minimum distance between updates (in meters)
  autoStart?: boolean; // Automatically start tracking on mount
}

/**
 * Custom React Native hook to track the user's live position using expo-location.
 * Ideal for "discover what's close to you" mode (proximity-based discovery).
 *
 * Usage:
 * const { location, errorMsg, isLoading, startTracking, stopTracking } = useLiveLocation({
 *   accuracy: Location.Accuracy.Balanced,
 *   timeInterval: 5000,
 *   distanceInterval: 10,
 *   autoStart: true
 * });
 */
export function useLiveLocation({
  accuracy = Location.Accuracy.Balanced,
  timeInterval = 5000,
  distanceInterval = 10,
  autoStart = true,
}: UseLiveLocationOptions = {}): UseLiveLocationResult {
  const [location, setLocation] = useState<LiveLocation | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isTrackingActive, setIsTrackingActive] = useState<boolean>(false);
  const [permissionStatus, setPermissionStatus] = useState<Location.PermissionStatus | null>(null);

  // Use ref to keep track of the subscription so it can be cleared on cleanup or when stopped
  const subscriptionRef = useRef<Location.LocationSubscription | null>(null);

  /**
   * Request location permissions from the user
   */
  const requestPermission = useCallback(async (): Promise<boolean> => {
    try {
      const { status } = await Location.requestForegroundPermissionsAsync();
      setPermissionStatus(status);
      if (status !== Location.PermissionStatus.GRANTED) {
        setErrorMsg('Permission to access location was denied');
        setIsLoading(false);
        return false;
      }
      setErrorMsg(null);
      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'An unknown error occurred';
      setErrorMsg(`Failed to request location permissions: ${message}`);
      setIsLoading(false);
      return false;
    }
  }, []);

  /**
   * Stop active location tracking
   */
  const stopTracking = useCallback(() => {
    if (subscriptionRef.current) {
      subscriptionRef.current.remove();
      subscriptionRef.current = null;
    }
    setIsTrackingActive(false);
  }, []);

  /**
   * Start active location tracking
   */
  const startTracking = useCallback(async () => {
    setIsLoading(true);
    
    // 1. Ensure we have permission
    const hasPermission = await requestPermission();
    if (!hasPermission) {
      return;
    }

    // 2. Clean up any existing tracking subscription first
    if (subscriptionRef.current) {
      subscriptionRef.current.remove();
    }

    try {
      // Get an initial single fix so the UI gets data instantly
      const initialLocation = await Location.getCurrentPositionAsync({
        accuracy,
      });
      
      setLocation({
        latitude: initialLocation.coords.latitude,
        longitude: initialLocation.coords.longitude,
        altitude: initialLocation.coords.altitude,
        accuracy: initialLocation.coords.accuracy,
        altitudeAccuracy: initialLocation.coords.altitudeAccuracy,
        heading: initialLocation.coords.heading,
        speed: initialLocation.coords.speed,
        timestamp: initialLocation.timestamp,
      });
      
      setIsLoading(false);

      // 3. Establish watching subscription for live updates
      const subscription = await Location.watchPositionAsync(
        {
          accuracy,
          timeInterval,
          distanceInterval,
        },
        (newLocation) => {
          setLocation({
            latitude: newLocation.coords.latitude,
            longitude: newLocation.coords.longitude,
            altitude: newLocation.coords.altitude,
            accuracy: newLocation.coords.accuracy,
            altitudeAccuracy: newLocation.coords.altitudeAccuracy,
            heading: newLocation.coords.heading,
            speed: newLocation.coords.speed,
            timestamp: newLocation.timestamp,
          });
        }
      );

      subscriptionRef.current = subscription;
      setIsTrackingActive(true);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'An unknown error occurred';
      setErrorMsg(`Failed to start live location tracking: ${message}`);
      setIsLoading(false);
    }
  }, [accuracy, timeInterval, distanceInterval, requestPermission]);

  // Handle auto-start and cleanup on unmount
  useEffect(() => {
    if (autoStart) {
      startTracking();
    }

    return () => {
      if (subscriptionRef.current) {
        subscriptionRef.current.remove();
      }
    };
  }, [autoStart, startTracking]);

  return {
    location,
    errorMsg,
    isLoading,
    isTrackingActive,
    permissionStatus,
    requestPermission,
    startTracking,
    stopTracking,
  };
}
