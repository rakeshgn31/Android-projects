package kth.homeworks.hangmanandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ServerConnectionScreen extends Activity {

    private EditText tfRemoteHost;
    private EditText tfRemotePort;
    private TextView tvStatusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection_screen);

        tfRemoteHost = (EditText) findViewById(R.id.hostName);
        tfRemotePort = (EditText) findViewById(R.id.portNumber);
        tvStatusBar = (TextView) findViewById(R.id.status_bar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server_connection_screen, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // Check the result code passed from ChildActivity
        if(resultCode == 1) // Connection_Failure
        {
            tvStatusBar.setText("Connection failed. Re-connect");
        }
    }

    public void connectToServer(View view) {

        try {
            // Read the remote hostname, port number
            String strHostName = tfRemoteHost.getText().toString();
            String nPortNumber = tfRemotePort.getText().toString();

            // Establish connection to server
            Intent intent = new Intent(ServerConnectionScreen.this, GameScreen.class);
            intent.putExtra("host", strHostName);
            intent.putExtra("port", nPortNumber);
            startActivityForResult(intent, 1);
        } catch (Exception ex) {

            AlertDialog.Builder builder = new AlertDialog.Builder(ServerConnectionScreen.this);
            builder.setMessage(ex.getMessage());
            builder.setTitle("Exception");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
