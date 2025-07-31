package droneDeliverySystem;

class Package {
    private String id;
    private double weight;
    private String status;
    private String dropOffLocation;
    private String currentLocation;

    public Package(String id, double weight, String dropOffLocation) {
        this.id = id;
        this.weight = weight;
        this.status = "awaiting dispatch";
        this.dropOffLocation = dropOffLocation;
        this.currentLocation = "warehouse";
    }

    public String getId() { return id; }
    public double getWeight() { return weight; }
    public String getStatus() { return status; }
    public String getDropOffLocation() { return dropOffLocation; }
    public String getCurrentLocation() { return currentLocation; }

    public void setStatus(String status) { this.status = status; }
    public void setCurrentLocation(String location) { this.currentLocation = location; }

    @Override
    public String toString() {
        return id + " (" + weight + "kg) to " + dropOffLocation + " - " + status;
    }
}