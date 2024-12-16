import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

public class Simulator extends JFrame {

    private JTextField inputField;
    private JTextField initialHeadField;
    private JTextField maxCylinderField;
    private JTextField minCylinderField;
    private JComboBox<String> algorithmComboBox;
    private JTextArea resultArea;
    private JButton simulateButton;
    private JFrame frame;

    

    private void init() {
        frame = new JFrame();
        this.frame.setTitle("Disk Scheduling Simulator");

        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

       
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(6, 1, 5, 5));

        
        JPanel diskQueuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        diskQueuePanel.add(new JLabel("Disk Queue (comma separated):"));
        inputField = new JTextField(20);
        diskQueuePanel.add(inputField);
        inputPanel.add(diskQueuePanel);

        JPanel initialHeadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        initialHeadPanel.add(new JLabel("Initial Head Position:"));
        initialHeadField = new JTextField(5);
        initialHeadPanel.add(initialHeadField);
        inputPanel.add(initialHeadPanel);

        JPanel maxCylinderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maxCylinderPanel.add(new JLabel("Maximum Cylinder:"));
        maxCylinderField = new JTextField("199", 5);
        maxCylinderPanel.add(maxCylinderField);
        inputPanel.add(maxCylinderPanel);

        JPanel minCylinderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        minCylinderPanel.add(new JLabel("Minimum Cylinder:"));
        minCylinderField = new JTextField("0", 5);
        minCylinderPanel.add(minCylinderField);
        inputPanel.add(minCylinderPanel);

        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        algorithmPanel.add(new JLabel("Select Algorithm:"));
        algorithmComboBox = new JComboBox<>(new String[]{"FCFS", "SSTF", "SCAN"});
        algorithmPanel.add(algorithmComboBox);
        inputPanel.add(algorithmPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center the button
        simulateButton = new JButton("Simulate");
        simulateButton.addActionListener(new SimulateButtonListener());
        buttonPanel.add(simulateButton);
        inputPanel.add(buttonPanel);

        frame.add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true); 
    }

    private class SimulateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = inputField.getText().trim();
            String initialHeadText = initialHeadField.getText().trim();
            String maxCylinderText = maxCylinderField.getText().trim();
            String minCylinderText = minCylinderField.getText().trim();

            if (input.isEmpty() || initialHeadText.isEmpty() || maxCylinderText.isEmpty() || minCylinderText.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int initialHead = Integer.parseInt(initialHeadText);
                int maxCylinder = Integer.parseInt(maxCylinderText);
                int minCylinder = Integer.parseInt(minCylinderText);

                String[] inputs = input.split(",");
                ArrayList<Integer> diskQueue = new ArrayList<>();
                for (String str : inputs) {
                    int request = Integer.parseInt(str.trim());
                    if (request < minCylinder || request > maxCylinder) {
                        throw new NumberFormatException("Request out of bounds");
                    }
                    diskQueue.add(request);
                }

                String algorithm = (String) algorithmComboBox.getSelectedItem();
                if (algorithm != null) {
                    switch (algorithm) {
                        case "FCFS":
                            simulateFCFS(diskQueue, initialHead);
                            break;
                        case "SSTF":
                            simulateSSTF(diskQueue, initialHead);
                            break;
                        case "SCAN":
                            simulateSCAN(diskQueue, initialHead, maxCylinder, minCylinder);
                            break;
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input. Please enter valid integers.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void simulateFCFS(ArrayList<Integer> diskQueue, int initialHead) {
        resultArea.setText("FCFS Disk Scheduling:\n");
        int currentHead = initialHead;
        int totalMovement = 0;

        for (int request : diskQueue) {
            resultArea.append("Servicing request at: " + request + "\n");
            totalMovement += Math.abs(currentHead - request);
            currentHead = request;
        }

        resultArea.append("\nTotal Head Movement: " + totalMovement + " unit\n");
    }

    private void simulateSSTF(ArrayList<Integer> diskQueue, int initialHead) {
        resultArea.setText("SSTF Disk Scheduling:\n");
        int currentHead = initialHead;
        int totalMovement = 0;
        ArrayList<Integer> queue = new ArrayList<>(diskQueue);

        while (!queue.isEmpty()) {
            int closestRequest = getClosestRequest(currentHead, queue);
            resultArea.append("Servicing request at: " + closestRequest + "\n");
            totalMovement += Math.abs(currentHead - closestRequest);
            currentHead = closestRequest;
            queue.remove((Integer) closestRequest);
        }

        resultArea.append("\nTotal Head Movement: " + totalMovement + " cylinders\n");
    }

    private int getClosestRequest(int currentHead, ArrayList<Integer> queue) {
        int closestRequest = queue.get(0);
        int minimumDistance = Math.abs(currentHead - closestRequest);

        for (int request : queue) {
            int distance = Math.abs(currentHead - request);
            if (distance < minimumDistance) {
                closestRequest = request;
                minimumDistance = distance;
            }
        }

        return closestRequest;
    }

    private void simulateSCAN(ArrayList<Integer> diskQueue, int initialHead, int maxCylinder, int minCylinder) {
        resultArea.setText("SCAN Disk Scheduling:\n");
        int currentHead = initialHead;
        int totalMovement = 0;
        ArrayList<Integer> queue = new ArrayList<>(diskQueue);
        Collections.sort(queue);

        int direction = 1; // 1 for moving towards larger, -1 for smaller
        ArrayList<Integer> left = new ArrayList<>();
        ArrayList<Integer> right = new ArrayList<>();

        for (int request : queue) {
            if (request < currentHead) {
                left.add(request);
            } else {
                right.add(request);
            }
        }

        Collections.reverse(left);

        while (!right.isEmpty() || !left.isEmpty()) {
            if (direction == 1) {
                if (!right.isEmpty()) {
                    int nextRequest = right.remove(0);
                    resultArea.append("Servicing request at: " + nextRequest + "\n");
                    totalMovement += Math.abs(currentHead - nextRequest);
                    currentHead = nextRequest;
                } else {
                    resultArea.append("Moving to max cylinder: " + maxCylinder + "\n");
                    totalMovement += Math.abs(currentHead - maxCylinder);
                    currentHead = maxCylinder;
                    direction = -1;
                }
            } else {
                if (!left.isEmpty()) {
                    int nextRequest = left.remove(0);
                    resultArea.append("Servicing request at: " + nextRequest + "\n");
                    totalMovement += Math.abs(currentHead - nextRequest);
                    currentHead = nextRequest;
                } else {
                    resultArea.append("Moving to min cylinder: " + minCylinder + "\n");
                    totalMovement += Math.abs(currentHead - minCylinder);
                    currentHead = minCylinder;
                    direction = 1;
                }
            }
        }

        resultArea.append("\nTotal Head Movement: " + totalMovement + " cylinders\n");
    }

    public static void main(String[] args) {
        
            Simulator simulator = new Simulator();
            simulator.init();
        
    }
}
