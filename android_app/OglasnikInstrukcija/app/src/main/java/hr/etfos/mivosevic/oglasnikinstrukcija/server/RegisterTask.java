package hr.etfos.mivosevic.oglasnikinstrukcija.server;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import hr.etfos.mivosevic.oglasnikinstrukcija.activities.MyProfileActivity;
import hr.etfos.mivosevic.oglasnikinstrukcija.data.User;
import hr.etfos.mivosevic.oglasnikinstrukcija.utilities.Constants;
import hr.etfos.mivosevic.oglasnikinstrukcija.utilities.Utility;

/**
 * Created by admin on 22.6.2016..
 */
public class RegisterTask extends AsyncTask<User, Void, Boolean> {
    private Context context;
    private User logged;

    public RegisterTask(Context c) {
        this.context = c;
    }

    @Override
    protected Boolean doInBackground(User... params) {
        this.logged = params[0];

        String[] pathArray = this.logged.getImgUrl().split("/");
        String serverFilePath = Constants.SERVER_ADDRESS
                + Constants.SERVER_UPLOAD_DIR
                + pathArray[pathArray.length - 1];

        try {
            //POST user logged and write it to database
            URL dataLink = new URL(Constants.SERVER_ADDRESS + Constants.INSERT_USER_SCRIPT);
            HttpURLConnection dataConn = (HttpURLConnection) dataLink.openConnection();
            dataConn.setDoOutput(true);

            String postData = URLEncoder.encode(Constants.USERNAME_DB_TAG, "UTF-8")
                    + "=" + URLEncoder.encode(this.logged.getUsername(), "UTF-8")
                    + "&" + URLEncoder.encode(Constants.PASSWORD_DB_TAG, "UTF-8")
                    + "=" + URLEncoder.encode(this.logged.getPassword(), "UTF-8")
                    + "&" + URLEncoder.encode(Constants.NAME_DB_TAG, "UTF-8")
                    + "=" + URLEncoder.encode(this.logged.getName(), "UTF-8")
                    + "&" + URLEncoder.encode(Constants.EMAIL_DB_TAG, "UTF-8")
                    + "=" + URLEncoder.encode(this.logged.getEmail(), "UTF-8")
                    + "&" + URLEncoder.encode(Constants.PHONE_DB_TAG, "UTF-8")
                    + "=" + URLEncoder.encode(this.logged.getPhoneNumber(), "UTF-8")
                    + "&" + URLEncoder.encode(Constants.LOCATION_DB_TAG, "UTF-8")
                    + "=" + URLEncoder.encode(this.logged.getLocation(), "UTF-8")
                    + "&" + URLEncoder.encode(Constants.ABOUT_DB_TAG, "UTF-8")
                    + "=" + URLEncoder.encode(this.logged.getAbout(), "UTF-8")
                    + "&" + URLEncoder.encode(Constants.IMAGEURL_DB_TAG, "UTF-8")
                    + "=" + URLEncoder.encode(serverFilePath, "UTF-8");

            OutputStreamWriter wr = new OutputStreamWriter(dataConn.getOutputStream());
            wr.write(postData);
            wr.flush();

            BufferedReader r = new BufferedReader(new InputStreamReader(dataConn.getInputStream()));
            String result = r.readLine();
            Log.d("MILAN", result);
            String line;
            while ((line = r.readLine()) != null) {
                Log.d("MILAN", line);
            }
            if (!result.equals("Success")) return false;

            //Upload user portrait to server
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(this.logged.getImgUrl());

            FileInputStream fileStream = new FileInputStream(sourceFile);

            URL fileLink = new URL(Constants.SERVER_ADDRESS + Constants.FILE_UPLOAD_SCRIPT);
            HttpURLConnection fileConn = (HttpURLConnection) fileLink.openConnection();
            fileConn.setDoInput(true);
            fileConn.setDoOutput(true);
            fileConn.setUseCaches(false);
            fileConn.setRequestMethod("POST");
            fileConn.setRequestProperty("Connection", "Keep-Alive");
            fileConn.setRequestProperty("ENCTYPE", "multipart/form-data");
            fileConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            fileConn.setRequestProperty("uploaded_file", this.logged.getImgUrl());

            DataOutputStream dos = new DataOutputStream(fileConn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";"
                    + "filename=\"" + this.logged.getImgUrl() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                //Write buffer to output
                dos.write(buffer, 0, bufferSize);

                //Read next buffer from input
                bytesAvailable = fileStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            Log.d("MILAN", "Response code: " + fileConn.getResponseCode()
                    + "\n" + fileConn.getResponseMessage());

            fileStream.close();
            dos.flush();
            dos.close();

            r = new BufferedReader(new InputStreamReader(fileConn.getInputStream()));
            result = r.readLine();
            Log.d("MILAN", result);
            while ((line = r.readLine()) != null) {
                Log.d("MILAN", line);
            }
            if (!result.equals("Success")) return false;
        }
        catch (Exception e) {
            Log.d("MILAN", e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            Utility.displayToast(this.context, "Successful registration.", false);

            Intent i = new Intent(this.context, MyProfileActivity.class);
            i.putExtra(Constants.USER_TAG, this.logged);
            this.context.startActivity(i);
        }
        else {
            Utility.displayToast(this.context, "Failed registration.", false);
        }
    }
}
