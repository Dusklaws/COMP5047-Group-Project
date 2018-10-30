// The following code is based on https://wildanmsyah.wordpress.com/2017/05/11/mqtt-android-client-tutorial/
package comp5047.exmaster.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper {

    public MqttAndroidClient mqttAndroidClient;

    String m_serverUri = "";
    String m_clientId = "";
    String m_subbedTopic = "";

    //String username = "xxxxxxx";
    //String password = "yyyyyyyyyy";

    public MqttHelper(Context context, String serverUri, String clientId, String subbedTopic) {
        this.m_serverUri = serverUri;
        this.m_clientId = clientId;
        this.m_subbedTopic = subbedTopic;
        Log.w("Mqtt", "MqttHelper constructor called");
        initMqttClient(context, this.m_serverUri, this.m_clientId, this.m_subbedTopic);
    }

    /**
     * Create a new mqttAndroidClient instance.
     * @param context The Android app's context
     * @param serverUri Server URI
     * @param clientId Client ID
     * @param subbedTopic MQTT topic to publish and subscribe to
     */
    private void initMqttClient(Context context, String serverUri, String clientId, String subbedTopic) {
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        Log.w("Mqtt", "initMqttClient() called");
        try {
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /**
     * Set the callback for the client
     */
    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    /**
     * Connect to the MQTT broker
     */
    private void connect() throws MqttException {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        //mqttConnectOptions.setUserName(username);
        //mqttConnectOptions.setPassword(password.toCharArray());

        Log.w("Mqtt", "connect() - gonna try connect");

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + m_serverUri + exception.toString());
                }
            });

        Log.w("Mqtt", "connect() - idk...!");

        } catch (MqttException ex){

            Log.w("Mqtt", "connect() - AN EXCEPTION OCCURED");
            ex.printStackTrace();
        }
    }

    /**
     * Subscribe to a topic
     */
    private void subscribeToTopic() {
        Log.w("Mqtt", String.format("Server URI: %s\n", m_serverUri));
        Log.w("Mqtt", String.format("Client ID:  %s\n", m_clientId));
        Log.w("Mqtt", String.format("Topic:      %s\n", m_subbedTopic));
        Log.w("Mqtt", "subscribeToTopic() - gonna try subscribe");
        try {
            mqttAndroidClient.subscribe(m_subbedTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "DID NOT SUBSCRIBE!");
                }
            });

            Log.w("Mqtt", "subscribeToTopic() - success!");

        } catch (MqttException ex) {
            Log.w("Mqtt", "subscribeToTopic() - AN EXCEPTION OCCURED");
            ex.printStackTrace();
        }
    }

    /**
     * Publish to a topic
     */
    public void publishToTopic(final String payload) {
        Log.w("Mqtt", String.format("pub Server URI: '%s'\n", m_serverUri));
        Log.w("Mqtt", String.format("pub Client ID:  '%s'\n", m_clientId));
        Log.w("Mqtt", String.format("pub Topic:      '%s'\n", m_subbedTopic));
        try {
            mqttAndroidClient.publish(m_subbedTopic, new MqttMessage(payload.getBytes()), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Published! " + payload);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Published fail! " + payload);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}