package dk.uncas.firstandroidapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class DownloadStringActivity extends Activity {
	private static final String DEBUG_TAG = "HttpExample";
	private TextView textView;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_string);
		textView = (TextView) findViewById(R.id.display_string);
		downloadAndDisplayString();
	}

	// When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    private void downloadAndDisplayString() {
        // Gets the URL from the UI's text field.
        String stringUrl = "http://192.168.0.190/api/pipelines?format=json";
        ConnectivityManager connMgr = (ConnectivityManager) 
            getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            textView.setText("No network connection available.");
        }
    }
    
    // Uses AsyncTask to create a task away from the main UI thread. This task takes a 
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
       @Override
       protected String doInBackground(String... urls) {
    	   // params comes from the execute() call: params[0] is the url.
           try {
               return downloadUrl(urls[0]);
           } catch (IOException e) {
               return "Unable to retrieve web page. URL may be invalid. " + e.toString();
           }
       }
       
       // onPostExecute displays the results of the AsyncTask.
       @Override
       protected void onPostExecute(String result) {
    	   try {
    		   String revisions = "";
    		   JSONArray pipelines = new JSONArray(result);
    		   for(int i=0; i<pipelines.length(); i++)
    		   {
    		        JSONObject pipeline = pipelines.getJSONObject(i);
    		        String projectName = pipeline.getString("projectName");
    		        String branchName = pipeline.getString("branchName");
    		        String revision = pipeline.getString("revision");
    		        revisions += projectName + "/" + branchName + " (" + revision.substring(0, 10) + ") ";
    		   }
    		   textView.setText(revisions);
		   } catch (JSONException e) {
			   e.printStackTrace();
			   String message = "Unable to parse JSON. Exception: " + e.toString() +
					   " JSON: " + result;
			   textView.setText(message);
		   }
       }
       
       // Given a URL, establishes an HttpUrlConnection and retrieves
       // the web page content as a InputStream, which it returns as
       // a string.
       private String downloadUrl(String myurl) throws IOException {
    	   InputStream is = null;
    	   // TODO: Dynamically handle buffer size
    	   int len = 50000;
            
    	   try {
               URL url = new URL(myurl);
               HttpURLConnection conn = (HttpURLConnection) url.openConnection();
               conn.setReadTimeout(10000 /* milliseconds */);
               conn.setConnectTimeout(15000 /* milliseconds */);
               conn.setRequestMethod("GET");
               conn.setDoInput(true);
               // Starts the query
               conn.connect();
               int response = conn.getResponseCode();
               Log.d(DEBUG_TAG, "The response is: " + response);
               is = conn.getInputStream();

               // Convert the InputStream into a string
               String contentAsString = readIt(is, len);
               return contentAsString;
            
               // Makes sure that the InputStream is closed after the app is
               // finished using it.
    	   } finally {
    		   if (is != null) {
    			   is.close();
    		   } 
    	   }
       }
    
       // Reads an InputStream and converts it to a String.
       public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
           Reader reader = null;
           reader = new InputStreamReader(stream, "UTF-8");        
           char[] buffer = new char[len];
           reader.read(buffer);
           return new String(buffer);
        }
    }
}