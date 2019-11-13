package com.zwj.mqtt_test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public static final int MSG_CONNECT_SUCCESS = 0x01;
    public static final int MSG_CONNECT_FAILED = 0x02;
    public static final int MSG_DISCONNECT_SUCCESS = 0x03;
    public static final int MSG_DISCONNECT_FAILED = 0x04;
    public static final int MSG_SUB_TOPIC_SUCCESS = 0x05;
    public static final int MSG_SUB_TOPIC_FAILED = 0x06;
    public static final int MSG_PUBLISH_SUCCESS = 0x07;
    public static final int MSG_PUBLISH_FAILED = 0x08;
    public static final int MSG_RECEIVE_MESSAGE = 0x09;

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
    @BindView(R.id.iv_sub_status)
    ImageView ivSubStatus;
    @BindView(R.id.iv_pub_status)
    ImageView ivPubStatus;
    @BindView(R.id.pb_set_sub_topic)
    ProgressBar pbSetSubTopic;
    @BindView(R.id.pb_set_pub_topic)
    ProgressBar pbSetPubTopic;
    @BindView(R.id.et_publish_msg)
    EditText etPublishMsg;
    @BindView(R.id.rv_subscriber)
    RecyclerView rvSubscriber;
    @BindView(R.id.rv_publisher)
    RecyclerView rvPublisher;
    @BindView(R.id.layout_test_page)
    LinearLayout layoutTestPage;
    private MqttAsyncClient mqttAsyncClient;
    private boolean isConnect = false;

    @SuppressLint("HandlerLeak")
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
                    clearTopicAndMessage();
                    break;
                case MSG_CONNECT_FAILED:
                    progressBar.setVisibility(View.GONE);
                    ivConnectFail.setVisibility(View.VISIBLE);
                    tvToolbarContent.setTextColor(getResources().getColor(R.color.error));
                    tvToolbarContent.setText(getString(R.string.connect_fail));
                    break;
                case MSG_DISCONNECT_FAILED:
                    progressBar.setVisibility(View.GONE);
                    ivConnectFail.setVisibility(View.VISIBLE);
                    tvToolbarContent.setTextColor(getResources().getColor(R.color.error));
                    tvToolbarContent.setText(getString(R.string.disconnect_fail));
                    break;
                case MSG_SUB_TOPIC_SUCCESS:
                    pbSetSubTopic.setVisibility(View.GONE);
                    ivSubStatus.setImageResource(R.drawable.ok);
                    ivSubStatus.setVisibility(View.VISIBLE);
                    etPubTopic.setTextColor(getResources().getColor(R.color.success_dark));
                    break;
                case MSG_SUB_TOPIC_FAILED:
                    pbSetSubTopic.setVisibility(View.GONE);
                    ivSubStatus.setImageResource(R.drawable.alert);
                    ivSubStatus.setVisibility(View.VISIBLE);
                    etPubTopic.setTextColor(getResources().getColor(R.color.error));
                    etSubTopic.setText(getString(R.string.set_failed));
                    break;
                case MSG_PUBLISH_SUCCESS:
                    break;
                case MSG_PUBLISH_FAILED:
                    break;
                case MSG_RECEIVE_MESSAGE:
                    String content = (String) msg.obj;
                    MessageBean messageBean = new MessageBean();
                    messageBean.setDateTime(Utils.getDateTime());
                    messageBean.setContent(content);
                    setSubscribeWindow(messageBean);
                    break;
                default:
                    break;
            }
        }
    };

    private void clearTopicAndMessage() {
        etPubTopic.setText("");
        etSubTopic.setText("");
        subscribeAdapter.clearData();
        publishAdapter.clearData();
    }

    private SubscribeAdapter subscribeAdapter;
    private PublishAdapter publishAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolbar();
        initAdapter();
        addTopicChangeListener();
    }

    private void addTopicChangeListener() {
        etSubTopic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ivSubStatus.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etPubTopic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ivPubStatus.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initAdapter() {
        //下面两行代码固定页面高度，避免NestedScrollView嵌套recyclerview导致的高度不固定问题
        int activityHeight = Utils.getScreenHeight(this) - Utils.getStatusBarHeight(this);
        layoutTestPage.getLayoutParams().height = activityHeight;
        subscribeAdapter = new SubscribeAdapter(this, new ArrayList<>());
        rvSubscriber.setLayoutManager(new LinearLayoutManager(this));
        rvSubscriber.setAdapter(subscribeAdapter);
        publishAdapter = new PublishAdapter(this, new ArrayList<>());
        rvPublisher.setLayoutManager(new LinearLayoutManager(this));
        rvPublisher.setAdapter(publishAdapter);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar2.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setInAnimation(MainActivity.this, R.anim.slide_in_right);
                viewFlipper.setOutAnimation(MainActivity.this, R.anim.slide_out_right);
                viewFlipper.showPrevious();
                //收回键盘
                InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null)
                    manager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
            mqttAsyncClient.publish(topic, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    handler.sendEmptyMessage(MSG_PUBLISH_SUCCESS);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    handler.sendEmptyMessage(MSG_PUBLISH_FAILED);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.btn_connect, R.id.fab_go, R.id.btn_sub_topic_ok, R.id.btn_pub_topic_ok, R.id.btn_publish})
    public void onViewClicked(View view) {
        if (!NetWorkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_LONG).show();
            return;
        }
        executeClick(view.getId());
    }

    private void executeClick(int id) {
        switch (id) {
            case R.id.btn_connect:
                if (!isConnect) {
                    String broker = etBroker.getText().toString().trim();
                    String username = etUsername.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    if (TextUtils.isEmpty(broker)
                            || TextUtils.isEmpty(username)
                            || TextUtils.isEmpty(password)) {
                        Toast.makeText(this, getText(R.string.login_msg_cant_null), Toast.LENGTH_LONG).show();
                        return;
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    ivConnectSuccess.setVisibility(View.GONE);
                    ivConnectFail.setVisibility(View.GONE);
                    tvToolbarContent.setText(getString(R.string.connecting));
                    tvToolbarContent.setTextColor(getResources().getColor(R.color.white));
                    connectBroker(broker, username, password);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    ivConnectSuccess.setVisibility(View.GONE);
                    ivConnectFail.setVisibility(View.GONE);
                    disconnectBroker();
                }
                break;
            case R.id.fab_go:
                viewFlipper.setInAnimation(this, R.anim.slide_in_left);
                viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
                viewFlipper.showNext();
                break;
            case R.id.btn_sub_topic_ok:
                setSubscribeTopic();
                break;
            case R.id.btn_pub_topic_ok:
                setPublishTopic();
                break;
            case R.id.btn_publish:
                String topic = etPubTopic.getText().toString().trim();
                if (TextUtils.isEmpty(topic)) {
                    Toast.makeText(this, getString(R.string.topic_cant_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                String content = etPublishMsg.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, getString(R.string.content_cant_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                publishMessage(topic, content);
                MessageBean messageBean = new MessageBean();
                messageBean.setDateTime(Utils.getDateTime());
                messageBean.setContent(content);
                setPublishWindow(messageBean);
                break;
        }
    }

    private void setPublishWindow(MessageBean messageBean) {
        etPublishMsg.setText("");
        publishAdapter.addData(messageBean);
        rvPublisher.smoothScrollToPosition(publishAdapter.getItemCount());
    }

    /**
     * 设置发布主题
     */
    private void setPublishTopic() {
        String topic = etPubTopic.getText().toString().trim();
        if (TextUtils.isEmpty(topic)) {
            Toast.makeText(this, getString(R.string.topic_cant_null), Toast.LENGTH_SHORT).show();
            return;
        }
        etPubTopic.clearFocus();
        ivPubStatus.setVisibility(View.VISIBLE);
    }

    /**
     * 设置订阅主题
     */
    private void setSubscribeTopic() {
        etSubTopic.clearFocus();
        if (mqttAsyncClient == null) {
            return;
        }
        try {
            pbSetSubTopic.setVisibility(View.VISIBLE);
            String topic = etSubTopic.getText().toString().trim();
            if (TextUtils.isEmpty(topic)) {
                Toast.makeText(this, getString(R.string.topic_cant_null), Toast.LENGTH_SHORT).show();
                return;
            }
            mqttAsyncClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    handler.sendEmptyMessage(MSG_SUB_TOPIC_SUCCESS);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    handler.sendEmptyMessage(MSG_SUB_TOPIC_FAILED);
                }
            }, new MqttReceiver());
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
                handler.sendEmptyMessage(MSG_CONNECT_FAILED);
            } else {
                Log.i("=====", "断开连接失败");
                handler.sendEmptyMessage(MSG_DISCONNECT_FAILED);
            }
        }
    }

    /**
     * 消息订阅监听
     */
    public class MqttReceiver implements IMqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            System.out.println("=====messageReceiver:" + mqttMessage);
            Message message = handler.obtainMessage(MSG_RECEIVE_MESSAGE, mqttMessage.toString());
            handler.sendMessage(message);
        }
    }

    private void setSubscribeWindow(MessageBean messageBean) {
        subscribeAdapter.addData(messageBean);
        rvSubscriber.smoothScrollToPosition(subscribeAdapter.getItemCount());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (viewFlipper.getDisplayedChild() != 0) {
                viewFlipper.setInAnimation(this, R.anim.slide_in_right);
                viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
                viewFlipper.showPrevious();
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
