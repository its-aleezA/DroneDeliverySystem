package droneDeliverySystem;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import java.util.*;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.jface.viewers.*;

public class DroneDeliverySystemGUI {
    protected Shell shell;
    private DeliverySystem deliverySystem;
    private TableViewer droneViewer;
    private TableViewer packageViewer;
    private Text statusText;
    private int packageCounter = 1;

    public static void main(String[] args) {
        try {
            DroneDeliverySystemGUI window = new DroneDeliverySystemGUI();
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void open() {
        Display display = Display.getDefault();
        createContents();
        
        // Initialise drones
        List<Drone> drones = new ArrayList<>();
        drones.add(new Drone("DR-001", 5.0, "Warehouse"));
        drones.add(new Drone("DR-002", 3.0, "Warehouse"));
        drones.add(new Drone("DR-003", 7.0, "Warehouse"));
        
        // Create delivery system
        deliverySystem = new DeliverySystem(drones, display, droneViewer, packageViewer, statusText);
        
        // Initial viewer updates
        droneViewer.setInput(drones.toArray());
        packageViewer.setInput(new Package[0]);
        
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        deliverySystem.shutdown();
    }

    protected void createContents() {
        shell = new Shell();
        shell.setSize(800, 600);
        shell.setText("Drone Delivery System");
        shell.setLayout(new GridLayout(2, true));
        
        // Left side - Drones
        Group droneGroup = new Group(shell, SWT.NONE);
        droneGroup.setText("Drones");
        droneGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        droneGroup.setLayout(new GridLayout(1, false));
        
        droneViewer = new TableViewer(droneGroup, SWT.BORDER | SWT.FULL_SELECTION);
        Table droneTable = droneViewer.getTable();
        droneTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        droneTable.setHeaderVisible(true);
        
        TableColumn droneIdCol = new TableColumn(droneTable, SWT.NONE);
        droneIdCol.setText("Drone ID");
        droneIdCol.setWidth(100);
        
        TableColumn droneCapCol = new TableColumn(droneTable, SWT.NONE);
        droneCapCol.setText("Capacity");
        droneCapCol.setWidth(80);
        
        TableColumn droneLocCol = new TableColumn(droneTable, SWT.NONE);
        droneLocCol.setText("Location");
        droneLocCol.setWidth(100);
        
        TableColumn droneStatusCol = new TableColumn(droneTable, SWT.NONE);
        droneStatusCol.setText("Status");
        droneStatusCol.setWidth(200);
        
        droneViewer.setContentProvider(new ArrayContentProvider());
        droneViewer.setLabelProvider(new StyledCellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                Drone drone = (Drone) cell.getElement();
                switch (cell.getColumnIndex()) {
                    case 0: cell.setText(drone.getId()); break;
                    case 1: cell.setText(drone.getMaxPayloadCapacity() + " kg"); break;
                    case 2: cell.setText(drone.getCurrentLocation()); break;
                    case 3: 
                        if (drone.isAvailable()) {
                            cell.setText("Available");
                            cell.setForeground(cell.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
                        } else {
                            cell.setText("Delivering " + drone.getCurrentPackage().getId());
                            cell.setForeground(cell.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
                        }
                        break;
                }
            }
        });
        
        // Right side - Packages
        Group packageGroup = new Group(shell, SWT.NONE);
        packageGroup.setText("Packages");
        packageGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        packageGroup.setLayout(new GridLayout(1, false));
        
        packageViewer = new TableViewer(packageGroup, SWT.BORDER | SWT.FULL_SELECTION);
        Table packageTable = packageViewer.getTable();
        packageTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        packageTable.setHeaderVisible(true);
        
        TableColumn pkgIdCol = new TableColumn(packageTable, SWT.NONE);
        pkgIdCol.setText("Package ID");
        pkgIdCol.setWidth(100);
        
        TableColumn pkgWeightCol = new TableColumn(packageTable, SWT.NONE);
        pkgWeightCol.setText("Weight");
        pkgWeightCol.setWidth(80);
        
        TableColumn pkgDestCol = new TableColumn(packageTable, SWT.NONE);
        pkgDestCol.setText("Destination");
        pkgDestCol.setWidth(100);
        
        TableColumn pkgStatusCol = new TableColumn(packageTable, SWT.NONE);
        pkgStatusCol.setText("Status");
        pkgStatusCol.setWidth(200);
        
        packageViewer.setContentProvider(new ArrayContentProvider());
        packageViewer.setLabelProvider(new StyledCellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                Package pkg = (Package) cell.getElement();
                switch (cell.getColumnIndex()) {
                    case 0: cell.setText(pkg.getId()); break;
                    case 1: cell.setText(pkg.getWeight() + " kg"); break;
                    case 2: cell.setText(pkg.getDropOffLocation()); break;
                    case 3: 
                        cell.setText(pkg.getStatus());
                        if ("delivered".equals(pkg.getStatus())) {
                            cell.setForeground(cell.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
                        } else if ("on its way".equals(pkg.getStatus())) {
                            cell.setForeground(cell.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
                        } else {
                            cell.setForeground(cell.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
                        }
                        break;
                }
            }
        });
        
        // Bottom panel - Controls and status
        Composite controlPanel = new Composite(shell, SWT.NONE);
        controlPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        controlPanel.setLayout(new GridLayout(2, false));
        
        // Package creation controls
        Composite createPanel = new Composite(controlPanel, SWT.BORDER);
        createPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createPanel.setLayout(new GridLayout(3, false));
        
        Label weightLabel = new Label(createPanel, SWT.NONE);
        weightLabel.setText("Weight (kg):");
        
        Text weightText = new Text(createPanel, SWT.BORDER);
        weightText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Combo locationCombo = new Combo(createPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
        locationCombo.setItems(new String[] {"Downtown", "Uptown", "Airport"});
        locationCombo.select(0);
        
        Button createButton = new Button(createPanel, SWT.PUSH);
        createButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
        createButton.setText("Create Package");
        createButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    double weight = Double.parseDouble(weightText.getText());
                    if (weight <= 0) {
                        statusText.setText("Weight must be positive\n" + statusText.getText());
                        return;
                    }
                    String destination = locationCombo.getText();
                    Package pkg = new Package("PKG-" + packageCounter++, weight, destination);
                    deliverySystem.placeOrder(pkg);
                    weightText.setText("");
                } catch (NumberFormatException ex) {
                    statusText.setText("Invalid weight format\n" + statusText.getText());
                }
            }
        });
        
        // Status area
        Composite statusPanel = new Composite(controlPanel, SWT.BORDER);
        statusPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        statusPanel.setLayout(new GridLayout(1, false));
        
        Label statusLabel = new Label(statusPanel, SWT.NONE);
        statusLabel.setText("System Status:");
        
        statusText = new Text(statusPanel, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY);
        statusText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }
}