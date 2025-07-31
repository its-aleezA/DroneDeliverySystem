package droneDeliverySystem;

import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;

class DeliverySystem {
    private List<Drone> drones;
    private Queue<Package> packageQueue;
    private ExecutorService executorService;
    private Map<String, Map<String, Integer>> locationGraph;
    private final Lock lock = new ReentrantLock();
    private Display display;
    private TableViewer droneViewer;
    private TableViewer packageViewer;
    private Text statusText;

    public DeliverySystem(List<Drone> drones, Display display, TableViewer droneViewer, TableViewer packageViewer, Text statusText) {
        this.drones = drones;
        this.packageQueue = new LinkedList<>();
        this.executorService = Executors.newFixedThreadPool(10);
        this.display = display;
        this.droneViewer = droneViewer;
        this.packageViewer = packageViewer;
        this.statusText = statusText;
        
        initializeLocationGraph();
    }

    private void initializeLocationGraph() {
        this.locationGraph = new HashMap<>();
        
        Map<String, Integer> warehouseConnections = new HashMap<>();
        warehouseConnections.put("Downtown", 5);
        warehouseConnections.put("Uptown", 3);
        locationGraph.put("Warehouse", warehouseConnections);

        Map<String, Integer> downtownConnections = new HashMap<>();
        downtownConnections.put("Warehouse", 5);
        downtownConnections.put("Airport", 2);
        locationGraph.put("Downtown", downtownConnections);

        Map<String, Integer> uptownConnections = new HashMap<>();
        uptownConnections.put("Warehouse", 3);
        uptownConnections.put("Airport", 6);
        locationGraph.put("Uptown", uptownConnections);

        Map<String, Integer> airportConnections = new HashMap<>();
        airportConnections.put("Downtown", 2);
        airportConnections.put("Uptown", 6);
        locationGraph.put("Airport", airportConnections);
    }

    public void placeOrder(Package pkg) {
        lock.lock();
        try {
            packageQueue.add(pkg);
            updateStatus("Package " + pkg.getId() + " added to queue");
            updatePackageViewer();
        } finally {
            lock.unlock();
        }
        processOrders();
    }

    private void processOrders() {
        executorService.submit(() -> {
            while (!packageQueue.isEmpty()) {
                lock.lock();
                try {
                    Package pkg = packageQueue.poll();
                    if (pkg != null) {
                        Drone assignedDrone = findAvailableDrone(pkg);
                        if (assignedDrone != null) {
                            updateStatus("Assigning package " + pkg.getId() + " to drone " + assignedDrone.getId());
                            pkg.setStatus("on its way");
                            assignedDrone.assignPackage(pkg);
                            updateDroneViewer();
                            updatePackageViewer();
                            simulateDelivery(pkg, assignedDrone);
                        } else {
                            updateStatus("No available drones for package " + pkg.getId() + ". Requeuing.");
                            packageQueue.add(pkg);
                        }
                    }
                } finally {
                    lock.unlock();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private Drone findAvailableDrone(Package pkg) {
        Drone bestDrone = null;
        int shortestDistance = Integer.MAX_VALUE;

        for (Drone drone : drones) {
            if (drone.isAvailable() && drone.getMaxPayloadCapacity() >= pkg.getWeight()) {
                int distance = calculateShortestDistance(drone.getCurrentLocation(), pkg.getDropOffLocation());
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    bestDrone = drone;
                }
            }
        }
        return bestDrone;
    }

    private int calculateShortestDistance(String start, String end) {
        Map<String, Integer> distances = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));
        Set<String> visited = new HashSet<>();

        for (String location : locationGraph.keySet()) {
            distances.put(location, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(end)) {
                return distances.get(current);
            }
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            for (Map.Entry<String, Integer> neighbor : locationGraph.getOrDefault(current, Collections.emptyMap()).entrySet()) {
                String next = neighbor.getKey();
                int newDist = distances.get(current) + neighbor.getValue();
                if (newDist < distances.get(next)) {
                    distances.put(next, newDist);
                    queue.add(next);
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private void simulateDelivery(Package pkg, Drone drone) {
        executorService.submit(() -> {
            try {
                int distance = calculateShortestDistance(drone.getCurrentLocation(), pkg.getDropOffLocation());
                long deliveryTime = distance * 1000L;
                
                updateStatus("Drone " + drone.getId() + " delivering package " + pkg.getId() + 
                            " to " + pkg.getDropOffLocation() + ". ETA: " + deliveryTime/1000 + "s");
                
                // Simulate movement
                for (int i = 1; i <= distance; i++) {
                    Thread.sleep(1000);
                    final int progress = i;
                    display.asyncExec(() -> {
                        updateStatus("Drone " + drone.getId() + " in transit (" + progress + "/" + distance + ")");
                    });
                }
                
                pkg.setStatus("delivered");
                pkg.setCurrentLocation(pkg.getDropOffLocation());
                drone.completeDelivery();
                
                display.asyncExec(() -> {
                    updateStatus("Package " + pkg.getId() + " delivered by drone " + drone.getId());
                    updateDroneViewer();
                    updatePackageViewer();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void updateStatus(final String message) {
        display.asyncExec(() -> {
            statusText.setText(message + "\n" + statusText.getText());
        });
    }

    private void updateDroneViewer() {
        display.asyncExec(() -> {
            droneViewer.setInput(drones.toArray());
            droneViewer.refresh();
        });
    }

    private void updatePackageViewer() {
        List<Package> allPackages = new ArrayList<>();
        for (Drone drone : drones) {
            if (drone.getCurrentPackage() != null) {
                allPackages.add(drone.getCurrentPackage());
            }
        }
        allPackages.addAll(packageQueue);
        
        display.asyncExec(() -> {
            packageViewer.setInput(allPackages.toArray());
            packageViewer.refresh();
        });
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}