package edu.mit.yz;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.provider.MediaStore;
import android.util.Log;
import android.database.Cursor;
import android.net.Uri;
import java.util.HashMap;
import android.os.Environment;
import java.io.File;

public class Reader extends Activity {
  TextToSpeech tts;

  private void d(Object o) { Log.d("qreader", o.toString()); }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    d("mediastore internal content URI " + MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
    d("mediastore external content URI " + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    d("playlists internal content URI " + MediaStore.Audio.Playlists.INTERNAL_CONTENT_URI);
    d("playlists external content URI " + MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI);
    d("externalStoragePublicDir(music) " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));

    String[] STAR = {"*"};
    d("All the titles");
    Cursor ca = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, STAR, null,null,null);
    for (ca.moveToFirst(); !ca.isAfterLast(); ca.moveToNext()) {
      if (ca.isFirst()) {   // print all the fields of the first song
        for (int k = 0; k<ca.getColumnCount(); k++)
          d("  "+ca.getColumnName(k)+"="+ca.getString(k));
      } else {              // but just the titles of the res
        d(ca.getString(ca.getColumnIndex("title")));
      }
    }
    ca.close();

    d("--------------------------");
    d("All the playlists");
    Cursor cursor = managedQuery(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, STAR, null,null,null);
    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
      d("-----");
      d("Playlist " + cursor.getString(cursor.getColumnIndex("name")));
      for (int k = 0; k<cursor.getColumnCount(); k++)           
        d(cursor.getColumnName(k)+"="+cursor.getString(k));

      // the members of this playlist
      int id = cursor.getInt(0);
      Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
      Cursor membersCursor = managedQuery(membersUri, STAR, null, null, null);
      for (membersCursor.moveToFirst(); !membersCursor.isAfterLast(); membersCursor.moveToNext())
        d("  "+membersCursor.getString(membersCursor.getColumnIndex("title")));
      membersCursor.close();
    }
    cursor.close();

    tts = new TextToSpeech(this, new OnInitListener() {

      @Override
      public void onInit(int status) {
        // TODO Auto-generated method stub
        tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
          @Override public void onUtteranceCompleted(String utteranceId) {
            d("done " + utteranceId + " isSpeaking=" + tts.isSpeaking());
            tts.stop();
            //Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", );
          }
        });
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Engine.KEY_PARAM_UTTERANCE_ID, "greeting");
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        dir.mkdirs();
        if (tts.synthesizeToFile("Hi there", params, dir + "/hithere.wav") == TextToSpeech.ERROR) {
          throw new RuntimeException();
        }
      }
    });
  }

  @Override
  public void onDestroy() { tts.shutdown(); super.onDestroy(); }
}

// vim: et sw=2 ts=2
