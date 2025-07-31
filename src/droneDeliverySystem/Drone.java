package droneDeliverySystem;

import java.util.concurrent.locks.*;

class Drone {
    private String id;
    private double maxPayloadCapacity;
    private String currentLocation;
    private String targetLocation;
    private boolean available;
    private final Lock lock = new ReentrantLock();
    private Package currentPackage;

    public Drone(String id, double maxPayloadCapacity, String initialLocation) {
        this.id = id;
        this.maxPayloadCapacity = maxPayloadCapacity;
        this.currentLocation = initialLocation;
        this.targetLocation = initialLocation;
        this.available = true;
    }

    public String getId() { return id; }
    public double getMaxPayloadCapacity() { return maxPayloadCapacity; }
    public String getCurrentLocation() { return currentLocation; }
    public String getTargetLocation() { return targetLocation; }
    public boolean isAvailable() { return available; }
    public Package getCurrentPackage() { return currentPackage; }

    public void setCurrentLocation(String location) { this.currentLocation = location; }
    public void setTargetLocation(String location) { this.targetLocation = location; }

    public boolean assignPackage(Package pkg) {
        lock.lock();
        try {
            if (available && pkg.getWeight() <= maxPayloadCapacity) {
                available = false;
                targetLocation = pkg.getDropOffLocation();
                currentPackage = pkg;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void completeDelivery() {
        lock.lock();
        try {
            currentLocation = targetLocation;
            available = true;
            currentPackage = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return id + " (" + maxPayloadCapacity + "kg cap) at " + currentLocation + 
               (available ? " - Available" : " - Delivering " + currentPackage.getId() + " to " + targetLocation);
    }
}