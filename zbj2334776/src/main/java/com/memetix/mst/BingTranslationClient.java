/*
 * microsoft-translator-java-api
 * 
 * Copyright 2012 Jonathan Griggs <jonathan.griggs at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.memetix.mst;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.memetix.mst.language.Language;

/**
 *
 * MicrosoftAPI
 * 
 * Makes the generic Microsoft Translator API calls. Different service classes then
 * extend this to make the specific service calls.
 * 
 * Uses the AJAX Interface V2 - see: http://msdn.microsoft.com/en-us/library/ff512404.aspx
 * 
 * @author Jonathan Griggs
 */
public class BingTranslationClient {
    //Encoding type
    protected static final String ENCODING                   = "UTF-8";

    private static final String   PARAM_TO                   = "to";

    private static final String   PARAM_FROM                 = "from";

    private static final String   PARAM_TEXT                 = "text";

    private static final String   PARAM_TEXTS                = "texts";

    private static final String   PARAM_LANGUAGE             = "language";

    private static final String   PARAM_LOCALE               = "locale";

    private static final String   PARAM_LANGUAGE_CODES       = "languageCodes";

    private static final String   SERVICE_URL                = "http://api.microsofttranslator.com/V2/Ajax.svc/Translate?";

    private static final String   ARRAY_SERVICE_URL          = "http://api.microsofttranslator.com/V2/Ajax.svc/TranslateArray?";

    private static final String   ARRAY_JSON_OBJECT_PROPERTY = "TranslatedText";

    private static String         DatamarketAccessUri        = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";

    private static String         contentType                = "text/plain";

    private String                clientId;

    private String                clientSecret;

    private String                token;

    private long                  tokenExpiration            = 0;

    private HttpClient            client;

    public BingTranslationClient() {
        client = new DefaultHttpClient();
    }

    /**
     * Sets the Client ID.
     * All new applications should obtain a ClientId and Client Secret by following 
     * the guide at: http://msdn.microsoft.com/en-us/library/hh454950.aspx
     * @param pKey The Client Id.
     */
    public void setClientId(final String pClientId) {
        clientId = pClientId;
    }

    /**
     * Sets the Client Secret.
     * All new applications should obtain a ClientId and Client Secret by following 
     * the guide at: http://msdn.microsoft.com/en-us/library/hh454950.aspx
     * @param pKey The Client Secret.
     */
    public void setClientSecret(final String pClientSecret) {
        clientSecret = pClientSecret;
    }

    /**
     * Gets the OAuth access token.
     * @param clientId The Client key.
     * @param clientSecret The Client Secret
     */
    private String refreshToken(String clientId, String clientSecret) throws Exception {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("grant_type", "client_credentials"));
        paramList.add(new BasicNameValuePair("scope", "http://api.microsofttranslator.com"));
        paramList.add(new BasicNameValuePair("client_id", clientId));
        paramList.add(new BasicNameValuePair("client_secret", clientSecret));

        String params = URLEncodedUtils.format(paramList, ENCODING);

        final URL url = new URL(DatamarketAccessUri);
        final HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + ENCODING);
        uc.setRequestProperty("Accept-Charset", ENCODING);
        uc.setRequestMethod("POST");
        uc.setDoOutput(true);

        OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
        wr.write(params);
        wr.flush();

        try {
            final int responseCode = uc.getResponseCode();
            final String result = inputStreamToString(uc.getInputStream());
            if (responseCode != 200) {
                throw new Exception("Error from Microsoft Translator API: " + result);
            }
            return result;
        } finally {
            if (uc != null) {
                uc.disconnect();
            }
        }
    }

    /**
     * Forms an HTTP request, sends it using GET method and returns the result of the request as a String.
     * 
     * @param url The URL to query for a String response.
     * @return The translated String.
     * @throws Exception on error.
     */
    private String retrieveResponse(String url) throws Exception {
        if (clientId != null && clientSecret != null && System.currentTimeMillis() > tokenExpiration) {
            String tokenJson = refreshToken(clientId, clientSecret);
            Integer expiresIn = Integer.parseInt((String) ((JSONObject) JSONValue.parse(tokenJson)).get("expires_in"));
            tokenExpiration = System.currentTimeMillis() + ((expiresIn * 1000) - 1);
            token = "Bearer " + (String) ((JSONObject) JSONValue.parse(tokenJson)).get("access_token");
        }

        HttpGet getMethod = new HttpGet(url);
        getMethod.setHeader("Content-Type", contentType + "; charset=" + ENCODING);
        getMethod.setHeader("Accept-Charset", ENCODING);
        if (token != null) {
            getMethod.setHeader("Authorization", token);
        }

        HttpResponse response = client.execute(getMethod);
        int statusCode = response.getStatusLine().getStatusCode();

        String result = inputStreamToString(response.getEntity().getContent());
        if (statusCode != 200) {
            throw new Exception("Error from Microsoft Translator API: " + result);
        }
        return result;
    }

    /**
     * Fetches the JSON response, parses the JSON Response, returns the result of the request as a String.
     * 
     * @param url The URL to query for a String response.
     * @return The translated String.
     * @throws Exception on error.
     */
    private String retrieveString(String url) throws Exception {
        try {
            final String response = retrieveResponse(url);
            return jsonToString(response);
        } catch (Exception ex) {
            throw new Exception("[microsoft-translator-api] Error retrieving translation : " + ex.getMessage(), ex);
        }
    }

    private String jsonToString(final String inputString) throws Exception {
        String json = (String) JSONValue.parse(inputString);
        return json.toString();
    }

    /**
     * Reads an InputStream and returns its contents as a String.
     * Also effects rate control.
     * @param inputStream The InputStream to read from.
     * @return The contents of the InputStream as a String.
     * @throws Exception on error.
     */
    private String inputStreamToString(final InputStream inputStream) throws Exception {
        final StringBuilder outputBuilder = new StringBuilder();

        try {
            String string;
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
                while (null != (string = reader.readLine())) {
                    // Need to strip the Unicode Zero-width Non-breaking Space. For some reason, the Microsoft AJAX
                    // services prepend this to every response
                    outputBuilder.append(string.replaceAll("\uFEFF", ""));
                }
            }
        } catch (Exception ex) {
            throw new Exception("[microsoft-translator-api] Error reading translation stream.", ex);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return outputBuilder.toString();
    }

    public String translate(String text, Language from, Language to) throws Exception {
        //Run the basic service validations first
        if (clientId == null || clientSecret == null) {
            throw new RuntimeException(
                    "Must provide a Windows Azure Marketplace Client Id and Client Secret - Please see http://msdn.microsoft.com/en-us/library/hh454950.aspx for further documentation");
        }

        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(PARAM_FROM, from.toString()));
        paramList.add(new BasicNameValuePair(PARAM_TO, to.toString()));
        paramList.add(new BasicNameValuePair(PARAM_TEXT, text));

        String params = URLEncodedUtils.format(paramList, ENCODING);

        String response = retrieveString(SERVICE_URL + params);
        return response;
    }
}
