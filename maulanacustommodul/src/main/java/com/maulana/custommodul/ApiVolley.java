package com.maulana.custommodul;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Shin on 2/24/2017.
 */

public class ApiVolley {

    public static RequestQueue requestQueue;

    public ApiVolley(final Context context, JSONObject jsonBody, String requestMethod, String REST_URL, final String successDialog, final String failDialog, final int showDialogFlag, final String user, final String pass , final VolleyCallback callback){

        /*
        NOTE: you have to customize this class before you use it (haeder, etc)
        context : Application context
        jsonBody : jsonBody (usually be used for POST and PUT)
        requestMethod : GET, POST, PUT, DELETE
        REST_URL : Rest API URL
        successDialog : custom Dialog when success call API
        failDialog : custom Dialog when failed call API
        showDialogFlag : 1 = show successDialog / failDialog with filter
        callback : return of the response
        */

        final String requestBody = jsonBody.toString();

        int method = 0;

        switch(requestMethod.toUpperCase()){

            case "GET" :
                method = Request.Method.GET;
                break;
            case "POST" :
                method = Request.Method.POST;
                break;
            case "PUT" :
                method = Request.Method.PUT;
                break;
            case "DELETE" :
                method = Request.Method.DELETE;
                break;
            default: method = Request.Method.GET;
                break;
        }

        //region initial of stringRequest
        StringRequest stringRequest = new StringRequest(method, REST_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                // Important Note : need to use try catch when parsing JSONObject, no need when parsing string

                if(response == null || response.equals("null")) {

                    Toast.makeText(context, context.getResources().getString(R.string.api_unauthorized), Toast.LENGTH_LONG).show();
                    callback.onError(context.getResources().getString(R.string.api_unauthorized));
                }

                try {

                    callback.onSuccess(response);
                    ShowCustomDialog(context, showDialogFlag, successDialog);

                } catch (Exception e) {

                    e.printStackTrace();
//                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(context, context.getResources().getString(R.string.api_error), Toast.LENGTH_LONG).show();
                    callback.onError(e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override

            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.d("Log", "onErrorResponse: timeout server " + error.toString());
                }

                callback.onError(error.toString());
                ShowCustomDialog(context,showDialogFlag,failDialog);
                callback.onError(error.toString());
                return;
            }
        }) {

            // Request Header
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Client-Service", "frontend-client");
                params.put("Auth-Key", "gmedia_resto");
                params.put("user", user);
                params.put("pass", pass);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return String.format("application/json; charset=utf-8");
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                            requestBody, "utf-8");
                    return null;
                }
            }
        };
        //endregion

        trustAllCertivicate();

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        // retry when timeout
        /*stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30*60*1000, *//*DefaultRetryPolicy.DEFAULT_MAX_RETRIES*//*0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));*/

        /*// Never retry when slow response
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0, -1,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));*/

        // set timeout after 6 second && never retry
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                12*1000,-1,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
        requestQueue.getCache().clear();

    }

    // interface for call callback from response API
    public interface VolleyCallback{
        void onSuccess(String result);
        void onError(String result);
    }

    public void ShowCustomDialog(Context context, int flag, String message){
        if(flag == 1){
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    private void trustAllCertivicate() {

        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    if (hostname.equalsIgnoreCase("semargres.gmedia.id") ||
                            hostname.equalsIgnoreCase("api.crashlytics.com") ||
                            hostname.equalsIgnoreCase("pullens.gmedia.bz") ||
                            hostname.equalsIgnoreCase("103.255.240.2") ||
                            hostname.equalsIgnoreCase("119.2.53.2") ||
                            hostname.equalsIgnoreCase("settings.crashlytics.com") ||
                            hostname.equalsIgnoreCase("clients4.google.com") ||
                            hostname.equalsIgnoreCase("www.facebook.com") ||
                            hostname.equalsIgnoreCase("www.instagram.com") ||
                            hostname.equalsIgnoreCase("lh1.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh2.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh3.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh4.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh5.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh6.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh7.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh8.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh9.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("googleusercontent.com") ||
                            hostname.equalsIgnoreCase("scontent.xx.fbcdn.net") ||
                            hostname.equalsIgnoreCase("lookaside.facebook.com")) {
                        return true;
                    } else {
                        return false;
                    }
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
