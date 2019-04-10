package com.example.hydrangea.imaginecup;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessDataActivity extends Activity {

    ListView bookListView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_data);

        String isbnString = getIntent().getStringExtra("data");

        bookListView = (ListView) findViewById(R.id.bookListView);

        DownloadTask task = new DownloadTask();
        task.execute("https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbnString + "&key=AIzaSyAJ4v45nlTctOGLN-HX7-jSRDVPK13sfeQ");

        imageView = (ImageView) findViewById(R.id.imageView);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream is = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);

                int data = reader.read();

                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String bookInfo = jsonObject.getString("items");

                Log.i("items", bookInfo);

                JSONArray bookArray = new JSONArray(bookInfo);

                for (int i = 0; i < bookArray.length(); i++) {

                    JSONObject jsonPart = bookArray.getJSONObject(i);
                    String volumeInfo = "[" + jsonPart.getString("volumeInfo") + "]";
                    JSONArray volumeArray = new JSONArray(volumeInfo);
                    Log.i("Volume Content", volumeInfo);

                    for (i = 0; i < volumeArray.length(); i++) {
                        JSONObject volumeArrayJSONObject = volumeArray.getJSONObject(i);
                        String title = volumeArrayJSONObject.getString("title");
                        String description = volumeArrayJSONObject.getString("description");
                        String averageRating = volumeArrayJSONObject.getString("averageRating");
                        String authors = volumeArrayJSONObject.getString("authors");
                        authors = authors.replace("\"", "");
                        authors = authors.replace("[", "");
                        authors = authors.replace("]", "");
                        String publisher = volumeArrayJSONObject.getString("publisher");
                        String year = volumeArrayJSONObject.getString("publishedDate");
                        String image = volumeArrayJSONObject.getString("imageLinks");

                        //retrieve Image link
                        Pattern pattern = Pattern.compile("http(.*?)api");
                        Matcher matcher = pattern.matcher(image);
                        if (matcher.find()) {
                            image = matcher.group(1);
                            image = "http" + image + "api";
                            image = image.replace("\\", "");
                        } else {
                            image = "Image not found";
                        }

                        ArrayList<String> book = new ArrayList<>();
                        book.add("Title\n" + title);
                        book.add("Authors\n" + authors);
                        book.add("Description\n" + description);
                        book.add("Year\n" + year);
                        book.add("Publisher\n" + publisher);
                        book.add("Average Rating\n" + averageRating);
                        //book.add("Image\n" + image);
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(AccessDataActivity.this, android.R.layout.simple_list_item_1, book);
                        bookListView.setAdapter(arrayAdapter);

                        new DownloadImageTask(imageView).execute(image);

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

   /* public boolean isConnected (){
        boolean connected;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;
        return connected;
    }*/

}
