package kth.homeworks.hangmanandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GameScreen extends Activity {

    private TextView tvAttemptsLeft;
    private TextView tvScore;
    private EditText tfGuess;
    private EditText tfResultantWord;
    private EditText tfStatus;

    private ServerConnectionHandler objServerConnectionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        tvAttemptsLeft = (TextView) findViewById(R.id.nAttemptsLeft);
        tvScore = (TextView) findViewById(R.id.nTotalScore);
        tfGuess = (EditText) findViewById(R.id.etGuess);
        tfResultantWord = (EditText) findViewById(R.id.etResultant);
        tfStatus = (EditText) findViewById(R.id.etGameStatus);

        Intent intent = getIntent();
        ServerConnection connectServer = new ServerConnection(intent.getStringExtra("host"), Integer.valueOf(intent.getStringExtra("port")));
        connectServer.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setStatus(final String strStatus) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tfStatus.setText(strStatus);
            }
        });
    }

    public void setWordConstruct(final String strResultantWord) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tfResultantWord.setText(strResultantWord);
            }
        });
    }

    public void setAttemptsLeftValue(final String strAttemptsLeft) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvAttemptsLeft.setText(strAttemptsLeft);
            }
        });
    }

    public void setScoreValue(final String strScoreValue) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvScore.setText(strScoreValue);
            }
        });
    }

    public void btnNewGameClicked(View view) {

        tfStatus.setText("");

        if(objServerConnectionHandler != null) {

            new Thread(new Runnable() {
                public void run() {
                    objServerConnectionHandler.StartNewGame();
                }
            }).start();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Connection object is null");
            builder.setTitle("Connection Failure");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void btnSendClicked(View view) {

        final String strGuess = tfGuess.getText().toString();

        if(objServerConnectionHandler != null) {

            new Thread(new Runnable() {
                public void run() {
                    objServerConnectionHandler.callServer(strGuess);
                }
            }).start();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Connection object is null");
            builder.setTitle("Connection Failure");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private class ServerConnection extends AsyncTask<Void, Void, ServerConnectionHandler> {

        private String strHostName;
        private Integer nPortNumber;

        ServerConnection(String sHostName, Integer nPort) {
            strHostName = sHostName;
            nPortNumber = nPort;
        }

        @Override
        protected ServerConnectionHandler doInBackground(Void... _) {
            ServerConnectionHandler srvConnectionHandler = new ServerConnectionHandler(strHostName, nPortNumber);
            srvConnectionHandler.EstablishConnection(); // blocking call
            return srvConnectionHandler;
        }

        @Override
        protected void onPostExecute(ServerConnectionHandler objServerConnHandler) {
            if (objServerConnHandler.isConnectedToServer()) {

                objServerConnectionHandler = objServerConnHandler;
                objServerConnectionHandler.setGameScreenObject(GameScreen.this);
            } else {

                Intent connFailureIntent = new Intent();
                connFailureIntent.putExtra("Exception_Message", objServerConnHandler.getConnectionException());
                GameScreen.this.setResult(1, connFailureIntent);
            }
        }
    }
}
