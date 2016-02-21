package kth.homeworks.hangmanandroid;


import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Manages connection and communication with server.
 *
 * Created by admin on 12/11/2015.
 */
public class ServerConnectionHandler {

        private int nServerPort;
        private String strHostName;
        private boolean bConnectedToServer;
        private String strConnectionException;

        private Socket clientSocket;
        private GameScreen objGameScreen;

        private ObjectInputStream inputObject;
        private ObjectOutputStream outputObject;

        public ServerConnectionHandler(String strServer, int nPort) {
            strHostName = strServer;
            nServerPort = nPort;
            bConnectedToServer = false;

            this.clientSocket = null;
        }

        public void setGameScreenObject(GameScreen objGameScreen) {
            this.objGameScreen = objGameScreen;
        }

        public boolean isConnectedToServer() {
            return bConnectedToServer;
        }

        public String getConnectionException() {
            return strConnectionException;
        }

        /**
         * Establishes initial connection with the server using
         * the initialized host name and the port number.
         */
        public void EstablishConnection() {

            try {
                clientSocket = new Socket(strHostName, nServerPort);
                outputObject = new ObjectOutputStream(clientSocket.getOutputStream());
                outputObject.flush();
                inputObject = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException ex) {
                bConnectedToServer = false;
                strConnectionException = ex.getMessage();
            }

            if(clientSocket != null)
            {
                // Enable the controls in the client GUI
                bConnectedToServer = true;
            }
        }

        /**
         * Call to communicate with the server and get the reply back and process
         * in case of starting a new game
         */
        public void StartNewGame() {

            try {
                // Send a new message to server
                outputObject.writeObject("Start Game");
                outputObject.flush();

                // Read the new word from the server
                String strServerReply = inputObject.readObject().toString();

                // Split the reply and set the new construct and attempts left
                String[] splitResult = strServerReply.split(" ");
                objGameScreen.setWordConstruct(splitResult[0]);
                objGameScreen.setAttemptsLeftValue(splitResult[1]);
            } catch(ClassNotFoundException | IOException ex) {
                objGameScreen.setStatus(ex.getMessage());
            }
        }

        // Processes the reply from the server and updates the GUI accordingly
        void ProcessReplyFromServer(String strReply) {

            // Split the reply into array of words
            String[] arrWords = strReply.split(" ");

            // Process and set the appropriate status on the client
            if( strReply.contains("Congratulations") ) {
                objGameScreen.setStatus("Awesome... You won");
                objGameScreen.setWordConstruct(arrWords[1]);
                objGameScreen.setScoreValue(arrWords[2]);
            } else if( strReply.contains("GameOver") ) {
                objGameScreen.setStatus("Sorry... You lose...:-(");
                objGameScreen.setScoreValue(arrWords[1]);
            } else {
                objGameScreen.setWordConstruct(arrWords[0]);
                objGameScreen.setAttemptsLeftValue(arrWords[1]);
            }
        }

        /**
         * Call to communicate with the server and get the reply back and process
         */
        public void callServer(String strClientData)
        {
            try {
                // Write the output on to the output stream
                outputObject.writeObject(strClientData);
                outputObject.flush();

                // Read the reply from the server from the input stream
                String strServerReply = inputObject.readObject().toString();
                strServerReply = strServerReply.trim();
                ProcessReplyFromServer(strServerReply);

            } catch (ClassNotFoundException | IOException ex) {
                objGameScreen.setStatus(ex.getMessage());
            }
        }
    }