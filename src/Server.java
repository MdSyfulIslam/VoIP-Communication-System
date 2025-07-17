import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class Server extends javax.swing.JFrame {

    private static final int PORT = 8080;  // Change to port 8080 for listening
    private static final List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>()); // Using synchronized list for thread safety
    private static ServerSocket serverSocket;
    private static int clientCount = 0;  // Track the number of connected clients

    public Server() {
        initComponents();
        this.setLocationRelativeTo(null);  // Center the window
    }

    // Start the server and listen for connections
    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server running on port " + PORT + "...");
                JOptionPane.showMessageDialog(null, "Server is running on port " + PORT, "Server Status", JOptionPane.INFORMATION_MESSAGE);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandlers.add(clientHandler);
                    clientCount++;  // Increment client count
                    System.out.println("New client connected: " + clientSocket);
                    if (clientCount > 5) {
                        JOptionPane.showMessageDialog(null, "Warning: More than 5 clients are connected!", "Client Limit Warning", JOptionPane.WARNING_MESSAGE);
                    }

                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                }
            } catch (IOException e) {
                System.err.println("Error starting server: " + e.getMessage());
            }
        }).start();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new Server().setVisible(true));
    }

    // ClientHandler class to handle client communication
    static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream output;
        private ObjectInputStream input;
        private String clientName;  // Store the client's name

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Error creating streams: " + e.getMessage());
                try {
                    socket.close();
                } catch (IOException ex) {
                    System.err.println("Error closing socket: " + ex.getMessage());
                }
            }
        }

        @Override
        public void run() {
            try {
                // Get the client's name
                clientName = (String) input.readObject();
                System.out.println("Client connected: " + clientName);

                // Broadcast the new client's name to all other clients
                broadcastMessage(clientName + " has joined the chat.");

                while (true) {
                    Object message = input.readObject();
                    if (message instanceof String) {
                        System.out.println("Message received: " + message);
                        broadcastMessage(clientName + ": " + message);  // Prefix with client name
                    } else if (message instanceof byte[]) {
                        System.out.println("Audio data received.");
                        broadcastAudio((byte[]) message);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client disconnected: " + socket);
            } finally {
                clientHandlers.remove(this);
                clientCount--;  // Decrement client count
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        // Broadcast a message to all connected clients except the sender
        private void broadcastMessage(String message) {
            synchronized (clientHandlers) {
                for (ClientHandler client : clientHandlers) {
                    if (client != this) {
                        try {
                            client.output.reset();
                            client.output.writeObject(message);
                            client.output.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // Broadcast audio data to all connected clients except the sender
        private void broadcastAudio(byte[] audioData) {
            synchronized (clientHandlers) {
                for (ClientHandler client : clientHandlers) {
                    if (client != this) {
                        try {
                            client.output.reset();
                            client.output.writeObject(audioData);
                            client.output.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    // Custom painting for gradient background
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        // Create a gradient background from top to bottom (blue to dark blue)
        Color color1 = new Color(0, 153, 255);  // Light blue
        Color color2 = new Color(0, 102, 204);  // Dark blue
        GradientPaint gradient = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    // Initialize GUI components with custom styles
    private void initComponents() {
        setTitle("Server Application");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("Server is running...", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(Color.WHITE);  // Text color

        JButton startServerButton = new JButton("Start Server");
        startServerButton.setFont(new Font("Arial", Font.PLAIN, 18));
        startServerButton.setBackground(new Color(0, 102, 204));  // Blue background
        startServerButton.setForeground(Color.WHITE);  // White text
        startServerButton.setFocusPainted(false);  // Remove button focus outline
        startServerButton.setPreferredSize(new Dimension(200, 40));
        startServerButton.addActionListener(e -> startServer());

        // Use FlowLayout with CENTER alignment to center the button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(startServerButton);

        // Adding components to the frame
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
