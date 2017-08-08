package com.amirnoel.android.todopile.todopile;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_VOICE_RECOGNITION = 1;

    private CastContext mCastContext;
    private CastSession mCastSession;
    private TodoPileChannel mTodoPileChannel;

    private SessionManagerListener<CastSession> mSessionManagerListener
            = new SessionManagerListenerImpl<CastSession>() {

        @Override
        public void onSessionStarted(CastSession castSession, String s) {
            Log.d(TAG, "Session started");
            mCastSession = castSession;
            invalidateOptionsMenu();
            startCustomMessageChannel();
        }

        @Override
        public void onSessionEnded(CastSession castSession, int i) {
            Log.d(TAG,"Session Ended");
            if(mCastSession == castSession) {
                cleanupSession();
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumed(CastSession castSession, boolean b) {
            Log.d(TAG,"Session resumed");
            mCastSession = castSession;
            invalidateOptionsMenu();
        }
    };

    private TextView mTodoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Button voiceButton = (Button) findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSpeechRecognitionActivity();
            }
        });

        mCastContext = CastContext.getSharedInstance(this);
    }

    private void startSpeechRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.message_to_cast));
        startActivityForResult(intent,REQUEST_CODE_VOICE_RECOGNITION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_VOICE_RECOGNITION && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if(matches.size() > 0) {
                Log.d(TAG, matches.get(0));
                sendMessage(matches.get(0));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void sendMessage(String message) {
        if(mTodoPileChannel != null) {
            mCastSession.sendMessage(mTodoPileChannel.getNamespace(), message);
        } else {
            Toast.makeText(this,"message",Toast.LENGTH_SHORT).show();
        }
    }

    private void populateTodoList() {
        String[] todos = getResources().getStringArray(R.array.todos);

        for(String todo : todos) {
            mTodoList.append(todo + "\n");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        CastButtonFactory.setUpMediaRouteButton(this,menu,R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCastContext.getSessionManager().addSessionManagerListener(mSessionManagerListener,CastSession.class);
        if(mCastSession == null) {
            mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupSession();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCastContext.getSessionManager().removeSessionManagerListener(mSessionManagerListener,CastSession.class);
    }



    private void startCustomMessageChannel() {

        if(mCastContext != null && mTodoPileChannel == null) {
            mTodoPileChannel = new TodoPileChannel(getString(R.string.cast_namespace));

            try {
                mCastSession.setMessageReceivedCallbacks(mTodoPileChannel.getNamespace(),mTodoPileChannel);
                Log.d(TAG,"Message channel started");
            } catch (IOException e) {
                Log.d(TAG,"Error starting message channel",e);
                mTodoPileChannel = null;
            }
        }
    }
    private void cleanupSession() {
        closeCustomMessageChannel();
        mCastSession = null;
    }

    private void closeCustomMessageChannel() {
        if(mCastSession != null && mTodoPileChannel != null) {
            try {
                mCastSession.removeMessageReceivedCallbacks(mTodoPileChannel.getNamespace());
                Log.d(TAG, "Message Channel closed");
            } catch (IOException e) {
                Log.d(TAG,"Error closing message channel", e);
            }
        }
    }

    private class TodoPileChannel implements Cast.MessageReceivedCallback {

        private final String mNamespace;

        TodoPileChannel(String namespace) {
            mNamespace = namespace;
        }

        public String getNamespace() {
            return mNamespace;
        }

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            Log.d(TAG,  "onMessageReceived: " + message);
        }
    }

    private static class SessionManagerListenerImpl<T extends CastSession> implements SessionManagerListener<T> {
        @Override
        public void onSessionStarting(T t) {

        }

        @Override
        public void onSessionStarted(T t, String s) {

        }

        @Override
        public void onSessionStartFailed(T t, int i) {

        }

        @Override
        public void onSessionEnding(T t) {

        }

        @Override
        public void onSessionEnded(T t, int i) {

        }

        @Override
        public void onSessionResuming(T t, String s) {

        }

        @Override
        public void onSessionResumed(T t, boolean b) {

        }

        @Override
        public void onSessionResumeFailed(T t, int i) {

        }

        @Override
        public void onSessionSuspended(T t, int i) {

        }
    }
}
