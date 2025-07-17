import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.sound.sampled.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VoIPClient {
    private static boolean isCalling = false;
    private static Socket socket;
    private static ObjectOutputStream output;
    private static ObjectInputStream input;
    private static JTextArea chatArea;
    private static String userName;  // Store the user's name

    public static void main(String[] args) {
        // Ask for the user's name
        userName = JOptionPane.showInputDialog(null, "Enter your name:", "User Name", JOptionPane.QUESTION_MESSAGE);
        if (userName == null || userName.trim().isEmpty()) {
            userName = "Anonymous";  // Default to "Anonymous" if no name is provided
        }

        try {
            socket = new Socket("localhost", 8080);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            // Send the client's name to the server
            output.writeObject(userName);
            output.flush();

            JOptionPane.showMessageDialog(null, "Connected to the server!", "Connection Status", JOptionPane.INFORMATION_MESSAGE);

            SwingUtilities.invokeLater(() -> createGUI());

            Thread listener = new Thread(() -> {
                try {
                    while (true) {
                        Object message = input.readObject();
                        if (message instanceof String) {
                            appendToChat(message.toString());  // Display the name with message
                        } else if (message instanceof byte[]) {
                            JOptionPane.showMessageDialog(null, "Audio received! Click 'Listen Audio' to play it.", "Audio Notification", JOptionPane.INFORMATION_MESSAGE);
                            saveAudioFile((byte[]) message);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Disconnected from server.");
                }
            });

            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createGUI() {
        JFrame frame = new JFrame("VoIP Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);

        // Set a custom gradient background
        frame.getContentPane().setBackground(new Color(0, 102, 204));  // Default blue color

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);  // Ensure text wraps correctly
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));

        JTextField messageField = new JTextField();
        messageField.setPreferredSize(new Dimension(500, 40));  // Increase text field size
        JButton sendMessageButton = new JButton("Send Message");
        JButton sendAudioButton = new JButton("Send Audio");
        JButton listenAudioButton = new JButton("Listen Audio");
        JButton startCallButton = new JButton("Start Call");

        // Styling the buttons
        styleButton(sendMessageButton);
        styleButton(sendAudioButton);
        styleButton(listenAudioButton);
        styleButton(startCallButton);

        // Action Listeners for buttons
        sendMessageButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                sendMessage(message);
                appendToChat(userName + ": " + message);  // Show the sent message with the user's name
                messageField.setText("");  // Clear text field after sending
            }
        });

        sendAudioButton.addActionListener(e -> sendAudio());
        listenAudioButton.addActionListener(e -> listenAudio());
        startCallButton.addActionListener(e -> startVoiceCall());

        buttonPanel.add(sendMessageButton);
        buttonPanel.add(sendAudioButton);
        buttonPanel.add(listenAudioButton);
        buttonPanel.add(startCallButton);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Apply medium-level buttons with color
    private static void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setBackground(new Color(0, 153, 255));  // Light blue background
        button.setForeground(Color.WHITE);  // White text
        button.setFocusPainted(false);  // Remove focus outline
        button.setPreferredSize(new Dimension(120, 40));
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));  // Add border to make it pop
    }

    private static void appendToChat(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    private static void sendMessage(String message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendAudio() {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(micInfo);

            microphone.open(format);
            microphone.start();

            File wavFile = new File("recorded_audio.wav");
            ByteArrayOutputStream rawAudio = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            JOptionPane.showMessageDialog(null, "Recording audio. Click OK to stop.", "Recording", JOptionPane.INFORMATION_MESSAGE);

            while (microphone.isActive()) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                rawAudio.write(buffer, 0, bytesRead);
            }

            microphone.stop();
            microphone.close();

            // Save the recorded audio as a WAV file
            byte[] audioData = rawAudio.toByteArray();
            saveAudioFile(audioData);  // Save the file locally before sending

            // Send the WAV file as byte array
            output.writeObject(audioData);
            output.flush();

            JOptionPane.showMessageDialog(null, "Audio sent successfully!", "Audio Status", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error during audio recording: " + e.getMessage(), "Recording Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void saveAudioFile(byte[] audioData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData)) {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bais);

            // Write audio data to a WAV file
            File outputFile = new File("received_audio.wav");
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);
            System.out.println("Audio file saved as 'received_audio.wav'.");
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    private static void listenAudio() {
        try {
            File wavFile = new File("received_audio.wav");
            if (!wavFile.exists()) {
                JOptionPane.showMessageDialog(null, "No WAV file found to play!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavFile);
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);

            speaker.open(format);
            speaker.start();

            byte[] buffer = new byte[1024];
            int bytesRead;

            JOptionPane.showMessageDialog(null, "Playing WAV. Click OK to start.", "Playback", JOptionPane.INFORMATION_MESSAGE);

            while ((bytesRead = audioStream.read(buffer)) > 0) {
                speaker.write(buffer, 0, bytesRead);
            }

            speaker.drain();
            speaker.close();
            audioStream.close();

            JOptionPane.showMessageDialog(null, "WAV playback completed!", "Playback Status", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error during WAV playback: " + e.getMessage(), "Playback Error", JOptionPane.ERROR_MESSAGE);
        }
    }
private static void startVoiceCall() {
    isCalling = true;
    System.out.println("Starting voice call...");

    // Piped streams to simulate local audio transmission
    PipedInputStream pipeIn = new PipedInputStream();
    PipedOutputStream pipeOut = new PipedOutputStream();

    try {
        pipeIn.connect(pipeOut);
    } catch (IOException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Failed to establish pipe connection.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Microphone Thread (Captures and sends audio)
    Thread microphoneThread = new Thread(() -> {
        try {
            // Audio format (simplified for better compatibility)
            AudioFormat format = new AudioFormat(8000, 8, 1, true, true); // 8kHz, 8-bit, mono PCM
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);

            // Ensure the microphone is available
            if (!AudioSystem.isLineSupported(micInfo)) {
                JOptionPane.showMessageDialog(null, "Microphone not supported by the system.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[1024];  // Buffer size for audio data

            while (isCalling) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    // Write the captured audio to the pipe (send it to another thread or server)
                    pipeOut.write(buffer, 0, bytesRead);
                    pipeOut.flush();
                }
            }

            microphone.stop();
            microphone.close();
            pipeOut.close();
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Microphone error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    // Speaker Thread (Receives and plays audio)
    Thread speakerThread = new Thread(() -> {
        try {
            // Audio format (simplified for better compatibility)
            AudioFormat format = new AudioFormat(8000, 8, 1, true, true); // 8kHz, 8-bit, mono PCM
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);

            // Ensure the speaker is available
            if (!AudioSystem.isLineSupported(speakerInfo)) {
                JOptionPane.showMessageDialog(null, "Speaker not supported by the system.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(format);
            speaker.start();

            byte[] buffer = new byte[1024]; // Buffer for audio data

            while (isCalling) {
                // Read from the pipe
                int bytesRead = pipeIn.read(buffer);
                if (bytesRead > 0) {
                    speaker.write(buffer, 0, bytesRead); // Play the received audio
                }
            }

            speaker.drain();
            speaker.close();
            pipeIn.close();
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Speaker error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    // Start the threads for capturing and playing audio
    microphoneThread.start();
    speakerThread.start();

    JOptionPane.showMessageDialog(null, "Voice call started. Click OK to end.", "Voice Call", JOptionPane.INFORMATION_MESSAGE);
}

}
