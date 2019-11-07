package com.zwj.mqtt_debug;

import android.os.Bundle;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    final String broker = "ssl://z9bk6im.mqtt.iot.gz.baidubce.com:1884";
    final String clientId = UUID.randomUUID().toString();
    final String username = "z9bk6im/write_story";
    final String password = "rrro0OZVO2yrAX7F";

    final String topic = "theme_1";
    final String content = "Message from Baidu IoT demo";
    @BindView(R.id.et_broker)
    TextInputEditText etBroker;
    @BindView(R.id.et_username)
    TextInputEditText etUsername;
    @BindView(R.id.et_password)
    TextInputEditText etPassword;
    @BindView(R.id.viewFlipper)
    ViewFlipper viewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initMqttProperty();
    }

    private void initMqttProperty() {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());

            System.out.println("Connecting to broker: " + broker);
            MqttClient mqttClient = new MqttClient(broker, clientId, persistence);
            mqttClient.connect(connOpts);
            System.out.println("Connected. Client id is " + clientId);

            mqttClient.subscribe(topic, new MqttReceiver());
            System.out.println("Subscribed to topic: " + topic);

            MqttMessage message = new MqttMessage(content.getBytes());
            mqttClient.publish(topic, message);
            System.out.println("Published message: " + content);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnClick(R.id.btn_connect)
    public void onViewClicked() {
        viewFlipper.showNext();
    }

    public class MqttReceiver implements IMqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            System.out.println("=====messageReceiver:" + message);
        }
    }
}
