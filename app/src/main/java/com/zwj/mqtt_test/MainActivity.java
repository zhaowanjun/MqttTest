package com.zwj.mqtt_test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public static final int MSG_CONNECT_SUCCESS = 0x01;
    public static final int MSG_CONNECT_FAILD = 0x02;
    public static final int MSG_DISCONNECT_SUCCESS = 0x03;
    public static final int MSG_DISCONNECT_FAIL = 0x04;
    public static final int MSG_SUB_TOPIC_SUCCESS = 0x05;
    public static final int MSG_SUB_TOPIC_FAILD = 0x06;
    final String clientId = UUID.randomUUID().toString();
    @BindView(R.id.et_broker)
    TextInputEditText etBroker;
    @BindView(R.id.et_username)
    TextInputEditText etUsername;
    @BindView(R.id.et_password)
    TextInputEditText etPassword;
    @BindView(R.id.viewFlipper)
    ViewFlipper viewFlipper;
    @BindView(R.id.btn_connect)
    Button btnConnect;
    @BindView(R.id.tv_toolbar_content)
    TextView tvToolbarContent;
    @BindView(R.id.toolbar1)
    Toolbar toolbar1;
    @BindView(R.id.iv_connect_success)
    ImageView ivConnectSuccess;
    @BindView(R.id.iv_connect_fail)
    ImageView ivConnectFail;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.fab_go)
    FloatingActionButton fabGo;
    @BindView(R.id.toolbar2)
    Toolbar toolbar2;
    @BindView(R.id.et_sub_topic)
    EditText etSubTopic;
    @BindView(R.id.et_pub_topic)
    EditText etPubTopic;
    @BindView(R.id.btn_sub_topic_ok)
    Button btnSubTopicOk;
    @BindView(R.id.btn_pub_topic_ok)
    Button btnPubTopicOk;
    @BindView(R.id.pb_sub_topic)
    ProgressBar pbSubTopic;
    @BindView(R.id.pb_pub_topic)
    ProgressBar pbPubTopic;
    private MqttAsyncClient mqttAsyncClient;
    private boolean isConnect = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CONNECT_SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    ivConnectSuccess.setVisibility(View.VISIBLE);
                    fabGo.show();
                    btnConnect.setText(getString(R.string.disconnect));
                    tvToolbarContent.setTextColor(getResources().getColor(R.color.white));
                    tvToolbarContent.setText(getString(R.string.connect_success));
                    toolbar1.setBackgroundColor(getResources().getColor(R.color.success));
                    break;
                case MSG_DISCONNECT_SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    fabGo.hide();
                    btnConnect.setText(getString(R.string.connect));
                    tvToolbarContent.setTextColor(getResources().getColor(R.color.white));
                    tvToolbarContent.setText(getString(R.string.has_disconnect));
                    toolbar1.setBackgroundColor(getResources().getColor(R.color.accent));
                    break;
                case MSG_CONNECT_FAILD:
                    progressBar.setVisibility(View.GONE);
                    ivConnectFail.setVisibility(View.VISIBLE);
                    tvToolbarContent.setTextColor(getResources().getColor(R.color.error));
                    tvToolbarContent.setText(getString(R.string.connect_fail));
                    break;
                case MSG_DISCONNECT_FAIL:
                    progressBar.setVisibility(View.GONE);
                    ivConnectFail.setVisibility(View.VISIBLE);
                    tvToolbarContent.setTextColor(getResources().getColor(R.color.error));
                    tvToolbarContent.setText(getString(R.string.disconnect_fail));
                    break;
                case MSG_SUB_TOPIC_SUCCESS:
                    btnSubTopicOk.setVisibility(View.VISIBLE);
                    pbSubTopic.setVisibility(View.GONE);
                    break;
                case MSG_SUB_TOPIC_FAILD:
                    btnSubTopicOk.setVisibility(View.VISIBLE);
                    pbSubTopic.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolbar();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar2.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.showPrevious();
            }
        });
    }

    /**
     * 连接服务器
     */
    private void connectBroker(String broker, String username, String password) {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            mqttAsyncClient = new MqttAsyncClient(broker, clientId, persistence);
            mqttAsyncClient.connect(connOpts, null, new MqttConnectCallback());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 断开连接
     */
    private void disconnectBroker() {
        if (mqttAsyncClient == null) {
            return;
        }
        try {
            mqttAsyncClient.disconnect(null, new MqttConnectCallback());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布消息
     */
    private void publishMessage(String topic, String content) {
        if (mqttAsyncClient == null) {
            return;
        }
        MqttMessage message = new MqttMessage(content.getBytes());
        try {
            mqttAsyncClient.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.btn_connect, R.id.fab_go, R.id.btn_sub_topic_ok, R.id.btn_pub_topic_ok})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                if (!isConnect) {
                    String broker = etBroker.getText().toString().trim();
                    String username = etUsername.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    if (TextUtils.isEmpty(broker)
                            || TextUtils.isEmpty(username)
                            || TextUtils.isEmpty(password)) {
                        Toast.makeText(this, getText(R.string.cannot_null), Toast.LENGTH_LONG).show();
                        return;
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    ivConnectSuccess.setVisibility(View.GONE);
                    ivConnectFail.setVisibility(View.GONE);
                    connectBroker(broker, username, password);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    ivConnectSuccess.setVisibility(View.GONE);
                    ivConnectFail.setVisibility(View.GONE);
                    disconnectBroker();
                }
                break;
            case R.id.fab_go:
                viewFlipper.showNext();
                break;
            case R.id.btn_sub_topic_ok:
                btnSubTopicOk.setVisibility(View.GONE);
                pbSubTopic.setVisibility(View.VISIBLE);
                setSubscribeTopic();
                break;
            case R.id.btn_pub_topic_ok:
                btnPubTopicOk.setVisibility(View.GONE);
                pbPubTopic.setVisibility(View.VISIBLE);
                setPublishTopic();
                break;
        }

    }

    /**
     * 设置发布主题
     */
    private void setPublishTopic() {

    }

    /**
     * 设置订阅主题
     */
    private void setSubscribeTopic() {
        if (mqttAsyncClient == null) {
            return;
        }
        try {
            String topic = etSubTopic.getText().toString().trim();
            mqttAsyncClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    handler.sendEmptyMessage(MSG_SUB_TOPIC_SUCCESS);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    handler.sendEmptyMessage(MSG_SUB_TOPIC_FAILD);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接服务器回调
     */
    private class MqttConnectCallback implements IMqttActionListener {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            isConnect = !isConnect;
            if (isConnect) {
                Log.i("=====", "连接成功");
                handler.sendEmptyMessage(MSG_CONNECT_SUCCESS);
            } else {
                Log.i("=====", "断开连接成功");
                handler.sendEmptyMessage(MSG_DISCONNECT_SUCCESS);
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (!isConnect) {
                Log.i("=====", "连接失败");
                handler.sendEmptyMessage(MSG_CONNECT_FAILD);
            } else {
                Log.i("=====", "断开连接失败");
                handler.sendEmptyMessage(MSG_DISCONNECT_FAIL);
            }
        }
    }

    /**
     * 消息订阅监听
     */
    public class MqttReceiver implements IMqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage message) {
            System.out.println("=====messageReceiver:" + message);
        }
    }
}
