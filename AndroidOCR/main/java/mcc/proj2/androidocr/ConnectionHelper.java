package mcc.proj2.androidocr;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * Created by Rakesh on 10/14/2016
 */

class VncServerInformation {

    String vncURL;
    String vncPassword;
}

class ApplicationInformation {

    String applicationID;
    String applicationName;
    String app_status;
    String app_image_url;
}

public class ConnectionHelper extends Application {

    // Application-wide cookies manager
    static CookieManager m_cookieManager = null;
    static String post_login_url, get_all_apps, post_first_app, gen_api_apps_url, get_logout;

    /**
     *  This is package-local constructor
     */
    public ConnectionHelper() {

        m_cookieManager = new CookieManager();
        CookieHandler.setDefault(m_cookieManager);
    }

    private void addCookiesToCookieMgr(List<String> cookiesList) {

        if (cookiesList != null) {
            for (String cookie : cookiesList) {

                m_cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
            }
        }
    }

    private String getCookiesFromCookieMgr() {

        String strCookie = "";
        if (m_cookieManager.getCookieStore().getCookies().size() > 0) {

            // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
            strCookie = TextUtils.join(";", m_cookieManager.getCookieStore().getCookies());
        }

        return strCookie;
    }

    private String encodeParameters (HashMap<String, String> params) {

        final char PARAM_DELIMITER = '&';
        final char ASSIGN_VALUE_CHAR = '=';
        StringBuilder buffer = new StringBuilder();

        try {
            if (params != null) {

                boolean bFirstParam = true;
                for (String key : params.keySet()) {

                    // If not first parameter then concatenate with the delimiter character
                    if (!bFirstParam) {

                        buffer.append(PARAM_DELIMITER);
                    }

                    // Appends key=value for each parameter
                    buffer.append(key);
                    buffer.append(ASSIGN_VALUE_CHAR);
                    buffer.append(URLEncoder.encode(params.get(key), "UTF-8"));

                    bFirstParam = false;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }

    private String getJSONResponseString(HttpURLConnection connection) {

        String respString = null;

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {

                buffer.append(line);
            }

            respString = buffer.toString();

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return respString;
    }

    // Parse the backend_ip file and form the URL strings
    public boolean formURLsToConnect() {

        boolean bStatus = false;

        try {
            InputStream file = getResources().getAssets().open("backend_ip.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            if (line != null) {

                String[] arrStrings = line.split(":");

                // URLs to be invoked for different functionalities
                post_login_url = "http://" + arrStrings[1] + ":" + arrStrings[2] + "/api/login";
                get_all_apps = "http://" + arrStrings[1] + ":" + arrStrings[2] + "/api/apps";
                post_first_app = "http://" + arrStrings[1] + ":" + arrStrings[2] + "/api/apps/top";

                // General URL for app-related queries - needs to be concatenated
                // with request-specific strings (start, stop, app_id)
                gen_api_apps_url = "http://" + arrStrings[1] + ":" + arrStrings[2] + "/api/apps/";
                get_logout = "http://" + arrStrings[1] + ":" + arrStrings[2] + "/api/logout";

                bStatus = true;
            }
        } catch (IOException ignored) {}

        return bStatus;
    }

    // Check if network connection is available
    public boolean isNetworkAvailable () {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        // In case of no network available, networkInfo will be null
        return networkInfo != null && networkInfo.isConnected();
    }

    // Computes the hash of the supplied string
    public String computeMD5Hash(final String strToHash) {

        String ret_hash = "";
        try {

            // Create MD5 Hash of the passed string parameter
            MessageDigest msg_digest = MessageDigest.getInstance("MD5");
            msg_digest.update(strToHash.getBytes());
            byte messageDigest[] = msg_digest.digest();

            // Create the Hex string from the Integer Hash
            StringBuilder hexStringHash = new StringBuilder();
            for (byte md_byte : messageDigest) {

                String hash_byte = toHexString(0xFF & md_byte);
                while (hash_byte.length() < 2) {
                    hash_byte = "0" + hash_byte;
                }
                hexStringHash.append(hash_byte);
            }

            ret_hash = hexStringHash.toString();

        } catch (NoSuchAlgorithmException e) {

            Toast.makeText(getApplicationContext(), "Failed to hash the password", Toast.LENGTH_SHORT).show();
        }

        return ret_hash;
    }

    public boolean authenticateUser(String strHash) {

        boolean bAuthenticated = false;

        // Establish HTTP connection and post data
        HttpURLConnection urlConnection = null;
        try {

            // Initialize URL object and Connect to the URL
            URL url = new URL(post_login_url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setConnectTimeout(30000);
            urlConnection.setRequestMethod("POST");

            // Post the username and md5Hash to the output stream
            HashMap<String, String> mapParameters = new HashMap<>();
            mapParameters.put("hash", strHash);
            String paramsString = encodeParameters(mapParameters);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
            writer.write(paramsString);
            writer.flush();
            writer.close();

            // Read the response
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Retrieve the cookies from the connection header and store it in the cookies manager
                List<String> cookiesHeader = urlConnection.getHeaderFields().get("Set-Cookie");
                addCookiesToCookieMgr(cookiesHeader);

                // Get JSON response and parse it
                String jsonRespString = getJSONResponseString(urlConnection);
                if(jsonRespString != null) {

                    JSONObject respObject = new JSONObject(jsonRespString);
                    bAuthenticated = respObject.getString("status").toLowerCase().equals("success");
                }
            }
        } catch (IOException | JSONException ex) {

            ex.printStackTrace();
        } finally {

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return bAuthenticated;
    }

    public ArrayList<ApplicationInformation> getAllApplications() {

        // List of applications to return to the caller
        ArrayList<ApplicationInformation> appList = null;

        // Establish HTTP connection and post data
        HttpURLConnection urlConnection = null;
        try {

            // Initialize URL object and Connect to the URL
            URL url = new URL(get_all_apps);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Cookie", getCookiesFromCookieMgr());  // Set the cookie read during logon connection
            urlConnection.setRequestMethod("GET");

            // Read the response
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Get JSON response and parse it
                String jsonRespString = getJSONResponseString(urlConnection);
                if(jsonRespString != null) {

                    // Initialize application container list
                    appList = new ArrayList<>();

                    // Get the JSON array of objects from the resultant JSON object
                    JSONArray arrApplications =  new JSONArray(jsonRespString);
                    for (int index = 0; index < arrApplications.length();  index++) {

                        ApplicationInformation app = new ApplicationInformation();

                        app.applicationID = arrApplications.getJSONObject(index).getString("appId");
                        app.applicationName = arrApplications.getJSONObject(index).getString("name");
                        app.app_status = arrApplications.getJSONObject(index).getString("status");
                        app.app_image_url = arrApplications.getJSONObject(index).getString("logo");

                        appList.add(app);
                    }
                }
            }
        } catch (IOException | JSONException ex) {

            ex.printStackTrace();
        } finally {

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return appList;
    }

    public String getAppForLocation(Double latitude, Double longitude) {

        String strApplicationID = null;

        // Establish HTTP connection and post data
        HttpURLConnection urlConnection = null;
        try {

            // Initialize URL object and Connect to the URL
            URL url = new URL(post_first_app);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Cookie", getCookiesFromCookieMgr());  // Set the cookie read during logon connection
            urlConnection.setRequestMethod("POST");

            // Send the location latitude and longitude and execute the post method
            HashMap<String, String> mapParameters = new HashMap<>();
            mapParameters.put("ltd", latitude.toString() );
            mapParameters.put("lng", longitude.toString() );
            String paramsString = encodeParameters(mapParameters);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
            writer.write(paramsString);
            writer.flush();
            writer.close();

            // Read the response
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Get JSON response and parse it
                String jsonRespString = getJSONResponseString(urlConnection);
                if(jsonRespString != null) {

                    JSONObject respObject = new JSONObject(jsonRespString);
                    strApplicationID = respObject.getString("appId");
                }
            }
        } catch (IOException | JSONException ex) {

            ex.printStackTrace();
        } finally {

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return strApplicationID;
    }

    public VncServerInformation startVM(String applicationID) {

        VncServerInformation vncInfo = null;

        HttpURLConnection urlConnection = null;
        JSONObject respObject = null;
        try {

            // Initialize URL object and Connect to the URL
            String urlString = gen_api_apps_url + applicationID + "/start";
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Cookie", getCookiesFromCookieMgr());  // Set the cookie read during logon connection
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            // Read the response
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Get JSON response and parse it
                String jsonRespString = getJSONResponseString(urlConnection);
                if(jsonRespString != null) {

                    respObject = new JSONObject(jsonRespString);
                    vncInfo = new VncServerInformation();
                    vncInfo.vncURL = respObject.getString("vnc_url");
                    vncInfo.vncPassword = respObject.getString("vnc_passwd");
                }
            }
        } catch (IOException | JSONException ex) {

            ex.printStackTrace();
        } finally {

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return vncInfo;
    }

    public boolean stopVM(String applicationID) {

        boolean bStatus = false;

        // Establish HTTP connection and post data
        HttpURLConnection urlConnection = null;
        try {

            // Initialize URL object and Connect to the URL
            String urlString = gen_api_apps_url + applicationID + "/stop";
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Cookie", getCookiesFromCookieMgr());  // Set the cookie read during logon connection
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            // Read the response
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Get JSON response and parse it
                String jsonRespString = getJSONResponseString(urlConnection);
                if(jsonRespString != null) {

                    JSONObject respObject = new JSONObject(jsonRespString);
                    bStatus = respObject.getString("status").equals("success");
                }
            }
        } catch (IOException | JSONException ex) {

            ex.printStackTrace();
        } finally {

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return bStatus;
    }

    public boolean shutdownSystem() {

        boolean bStatus = false;

        // Establish HTTP connection and post data
        HttpURLConnection urlConnection = null;
        try {

            // Initialize URL object and Connect to the URL
            URL url = new URL(get_logout);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Cookie", getCookiesFromCookieMgr());  // Set the cookie read during logon connection
            urlConnection.setRequestMethod("GET");

            // Read the response
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Get JSON response and parse it
                String jsonRespString = getJSONResponseString(urlConnection);
                if(jsonRespString != null) {

                    JSONObject respObject = new JSONObject(jsonRespString);
                    bStatus = respObject.getString("status").toLowerCase().equals("success");
                }
            }
        } catch (IOException | JSONException ex) {

            ex.printStackTrace();
        } finally {

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return bStatus;
    }

    public ApplicationInformation getApplicationInfo(String applicationID) {

        ApplicationInformation app_info = null;

        // Establish HTTP connection and post data
        HttpURLConnection urlConnection = null;
        try {

            // Initialize URL object and Connect to the URL
            URL url = new URL(gen_api_apps_url + applicationID);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Cookie", getCookiesFromCookieMgr());  // Set the cookie read during logon connection
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the response
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Get JSON response and parse it
                String jsonRespString = getJSONResponseString(urlConnection);
                if(jsonRespString != null) {

                    JSONObject respObject = new JSONObject(jsonRespString);
                    app_info = new ApplicationInformation();
                    app_info.applicationID = respObject.getString("appId");
                    app_info.applicationName = respObject.getString("name");
                    app_info.app_status = respObject.getString("status");
                    app_info.app_image_url = respObject.getString("logo");

                }
            }
        } catch (IOException | JSONException ex) {

            ex.printStackTrace();
        } finally {

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return app_info;
    }
}
