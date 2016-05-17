package ro.pub.cs.systems.pdsd.practicaltest02;

import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Andreea on 5/17/2016.
 */
public class CommunicationThread extends Thread{
    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket != null) {
            try {
                BufferedReader bufferedReader = Utilities.getReader(socket);
                PrintWriter printWriter = Utilities.getWriter(socket);
                if (bufferedReader != null && printWriter != null) {
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type)!");
                    String word = bufferedReader.readLine();
                    //String informationType = bufferedReader.readLine();
                    //HashMap<String, WeatherForecastInformation> data = serverThread.getData();
                    //WordInformation weatherForecastInformation = null;
                    if (word != null && !word.isEmpty()) {

                        Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + word);
                        ResponseHandler<String> responseHandler = new BasicResponseHandler();
                        String pageSourceCode = httpClient.execute(httpGet, responseHandler);

                        if (pageSourceCode != null) {
                            String result = new String();

                            /*Document document = Jsoup.parse(pageSourceCode);
                            Element element = document.child(0);
                            Element script = element.getElementById(Constants.SCRIPT_TAG);
                            result = script.toString();*/

                            Document doc = Jsoup.parse(pageSourceCode, "", Parser.xmlParser());

                            for (Element sentence : doc.getElementsByTag("WordDefinition")) {
                                System.out.println(sentence.text());
                                result = sentence.text();
                            }

                            /*JSONObject content = new JSONObject(pageSourceCode);
                            JSONArray resultArray = content.getJSONArray(Constants.ARRAY_NAME);
                            for (int i = 0; i < resultArray.length(); ++i) {
                                JSONObject object = resultArray.getJSONObject(i);
                                String name = object.getString("name");
                                if (result.isEmpty()) {
                                    result += name;
                                } else {
                                    result += ", " + name;
                                }
                            }*/

                            Log.i(Constants.TAG, "[COMMUNICATION THREAD]Get data: " + result);
                            printWriter.println(result);
                            printWriter.flush();


                        } else {
                            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                        }



                    } else {
                        Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type)!");
                    }
                } else {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] BufferedReader / PrintWriter are null!");
                }
                socket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            } /*catch (JSONException jsonException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
                if (Constants.DEBUG) {
                    jsonException.printStackTrace();
                }
            }*/
        } else {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
        }
    }


}
