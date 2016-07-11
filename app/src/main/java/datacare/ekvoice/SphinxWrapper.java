package datacare.ekvoice;
/**
 * Created by george on 1/30/16.
 * This is the actual interface for the sphinx speech engine, this is the code that will be executed
 * when pocketsphinx detects certain events.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class SphinxWrapper extends AppCompatActivity implements RecognitionListener {
    private TextView sphinxOut;
    private Button stop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sphinx);
        sphinxOut = (TextView) findViewById(R.id.sphinxTextOut);
        stop = (Button) findViewById(R.id.sphinxStop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechWrapper.sphinxRecognizer.stop();
                stop.setEnabled(false);
            }
        });
    }

    public void offlineSpeechRequest(View view){
        SpeechWrapper.sphinxRecognizer.addListener(this);
        stop.setEnabled(true);
        SpeechWrapper.sphinxRecognizer.startListening("engl", 3000);
        //ignore
    }

    @Override
    public void onBeginningOfSpeech(){

    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech(){
        SpeechWrapper.sphinxRecognizer.stop();
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis){
        stop.setEnabled(false);
        if(hypothesis != null){
            String out = hypothesis.getHypstr();
            Intent returnSpeech = new Intent();
            returnSpeech.putExtra("EXTRA_SPHINX", out);
            setResult(1, returnSpeech);
            sphinxOut.setText(out);
        }else{
            setResult(-1);
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis){
        if(hypothesis != null){
            String hyp = hypothesis.getHypstr();
            sphinxOut.setText(hyp);
        }

    }

    @Override
    public void onTimeout(){
        //SpeechWrapper.sphinxRecognizer.stop();
    }

    @Override
    public void onError(Exception e){

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        SpeechWrapper.sphinxRecognizer.cancel();
    }
    public void sphinxDestroy(){
        SpeechWrapper.sphinxRecognizer.cancel();
        SpeechWrapper.sphinxRecognizer.shutdown();
    }

    public void switchBack(View v){
        finish();
    }
}
