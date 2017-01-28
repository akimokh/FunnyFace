package com.example.yonko.funnyface.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.yonko.funnyface.R;
import com.example.yonko.funnyface.managers.EditorManager;
import com.example.yonko.funnyface.models.EditorFab;
import com.example.yonko.funnyface.utils.AssetUtils;
import com.example.yonko.funnyface.utils.BitmapUtils;
import com.example.yonko.funnyface.utils.FileUtils;
import com.example.yonko.funnyface.utils.IntentUtils;
import com.example.yonko.funnyface.utils.async.AsyncDiskCache;
import com.example.yonko.funnyface.utils.async.tasks.LoadCameraImageTask;
import com.example.yonko.funnyface.utils.async.tasks.SaveBitmapToDeviceTask;
import com.example.yonko.funnyface.widgets.CanvasView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CACHE_KEY_DEFAULT = "default";
    private static final String CACHE_KEY_CURRENT = "current";

    public enum EDIT_STATE {NONE, MORPH, PAINT, STICKER, TEXT}

    private EDIT_STATE mEditState = EDIT_STATE.NONE;

    private Uri mImageUri = null;
    private View mLastClickedButton = null;
    private ProgressBar mProgressBar = null;
    private MediaPlayer mp;
    private int mStickersLastPage = -1;
    private int mPrimaryColor, mPrimaryDarkColor;

    private CanvasView mCanvasView;
    private EditorManager mEditorToolsManager;
    private Bitmap mDefaultBitmap;

    // Fab buttons
    private EditorFab mFabSave;
    private EditorFab mFabAdd;
    private EditorFab mFabBefore;

    // Bottom toolbar button icons
    private ImageButton mButtonMorph;
    private ImageButton mButtonDraw;
    private ImageButton mButtonSticker;
    private ImageButton mButtonText;
    private ImageButton mButtonUndo;
    private ImageButton mButtonRedo;

    // Undro/Redo drawables
    private Drawable mUndoNotActiveDrawable;
    private Drawable mUndoActiveDrawable;
    private Drawable mRedoNotActiveDrawable;
    private Drawable mRedoActiveDrawable;

    // Right add fab icon drawables
    private Drawable mAddFabStickerDrawable;
    private Drawable mAddFabTextDrawable;
    private Drawable mAddFabPaintDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        AssetUtils.verifyStoragePermissions(this);

        setContentView(R.layout.activity_main);
        setupWindowAnimations();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ProgressBar
        mProgressBar = (ProgressBar) findViewById(R.id.pbProgess);
        init(savedInstanceState);
        //new InitTask().execute(savedInstanceState); // For test purposes!
    }

    /*
    * Initializing
    * */
    public void init(Bundle savedInstanceState) {
        mCanvasView = (CanvasView) findViewById(R.id.myCanvas);
        mCanvasView.setOnClickListener(this);

        // Check if state is saved
        Bitmap imageInEdit = null;
        if (savedInstanceState == null) {
            mDefaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.brad);
            saveBitmapToDiskCache(CACHE_KEY_DEFAULT, mDefaultBitmap, false);
            imageInEdit = mDefaultBitmap;
        } else {
            mDefaultBitmap = getBitmapFromDiskCache(CACHE_KEY_DEFAULT);
            imageInEdit = getBitmapFromDiskCache(CACHE_KEY_CURRENT);
        }

        mEditorToolsManager = new EditorManager(this, mCanvasView, getSupportFragmentManager(), imageInEdit);

        // Get bottom toolbar buttons
        mButtonMorph = (ImageButton) findViewById(R.id.buttonMorph);
        mButtonMorph.setOnClickListener(this);
        mButtonDraw = (ImageButton) findViewById(R.id.buttonPaint);
        mButtonDraw.setOnClickListener(this);
        mButtonSticker = (ImageButton) findViewById(R.id.buttonSticker);
        mButtonSticker.setOnClickListener(this);
        mButtonText = (ImageButton) findViewById(R.id.buttonTextSticker);
        mButtonText.setOnClickListener(this);
        mButtonUndo = (ImageButton) findViewById(R.id.buttonUndo);
        mButtonUndo.setOnClickListener(this);
        mButtonRedo = (ImageButton) findViewById(R.id.buttonRedo);
        mButtonRedo.setOnClickListener(this);

        // Get fab buttons
        mFabSave = new EditorFab((FloatingActionButton) findViewById(R.id.fabSave));
        mFabSave.setOnClickListener(this);
        mFabAdd = new EditorFab((FloatingActionButton) findViewById(R.id.fabAdd));
        mFabAdd.setOnClickListener(this);
        mFabBefore = new EditorFab((FloatingActionButton) findViewById(R.id.fabBefore));
        mFabBefore.getFloatingActionButton().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mEditorToolsManager.setVisibility(false);
                        break;
                    case MotionEvent.ACTION_UP:
                        mEditorToolsManager.setVisibility(true);
                        break;
                }
                return true;
            }
        });

        // Get colors
        mPrimaryColor = AssetUtils.getColor(this, R.color.colorPrimary);
        mPrimaryDarkColor = AssetUtils.getColor(this, R.color.colorPrimaryDark);

        // Get Drawables for undo/redo buttons
        mUndoNotActiveDrawable = getResources().getDrawable(R.mipmap.ic_content_undo_not);
        mUndoActiveDrawable = getResources().getDrawable(R.mipmap.ic_content_undo);
        mRedoNotActiveDrawable = getResources().getDrawable(R.mipmap.ic_content_redo_not);
        mRedoActiveDrawable = getResources().getDrawable(R.mipmap.ic_content_redo);

        // Get right fab drawables
        mAddFabStickerDrawable = getResources().getDrawable(R.drawable.ic_add);
        mAddFabTextDrawable = mAddFabStickerDrawable;
        mAddFabPaintDrawable = getResources().getDrawable(R.drawable.ic_brush);

        // Load user settings
        loadSoundSettings();
    }

    @Override
    protected void onResume() {
        if(mEditorToolsManager != null) {
            mEditorToolsManager.setSaveCallbackFab(mFabSave);
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.unregisterOnSharedPreferenceChangeListener(this);
        saveBitmapToDiskCache(CACHE_KEY_CURRENT, mCanvasView.generateBitmap(), false);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("volume")) {
            loadSoundSettings();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_camera: {
                mImageUri = IntentUtils.sendCameraRequest(this);
                return true;
            }
            case R.id.menu_load: {
                IntentUtils.sendLoadImageRequest(this);
                return true;
            }
            case R.id.menu_save: {
                new SaveBitmapToDeviceTask(this, new SaveBitmapToDeviceTask.SaveBitmapToDeviceCallback() {
                    @Override
                    public void onPreExecute() {
                        item.setActionView(R.layout.view_progressbar);
                        item.expandActionView();
                    }

                    @Override
                    public void onPostExecute(String data) {
                        item.collapseActionView();
                        item.setActionView(null);
                    }
                }).execute(mEditorToolsManager.generateBitmap());
                return true;
            }
            case R.id.menu_reset: {
                reset(mDefaultBitmap);
                return true;
            }
            case R.id.menu_settings: {
                IntentUtils.sendSettingsRequest(this);
                return true;
            }
            case R.id.menu_about: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonMorph:
            case R.id.buttonPaint:
            case R.id.buttonSticker:
            case R.id.buttonTextSticker:
            case R.id.buttonUndo:
            case R.id.buttonRedo:
                onToolbarItemClicked(view);
                break;
            case R.id.fabSave: {
                if (mEditState == EDIT_STATE.STICKER) {
                    mEditorToolsManager.setStickerInEdit(false);
                    mCanvasView.save();
                    mEditorToolsManager.removeAllStickers();
                } else if (mEditState == EDIT_STATE.TEXT) {
                    mEditorToolsManager.setBubbleTextInEdit(false);
                    mCanvasView.save();
                    mEditorToolsManager.removeAllTextBubbles();
                } else {
                    mCanvasView.save();
                    mEditorToolsManager.clearUndo();
                    changeUndoButtonState(false);
                    mFabSave.hide();
                }
            }
            break;
            case R.id.fabAdd: {
                if (mEditState == EDIT_STATE.STICKER) {
                    IntentUtils.sendStickerChooserRequest(this, mStickersLastPage);
                } else if (mEditState == EDIT_STATE.PAINT) {
                    mEditorToolsManager.setDrawingBrush();
                } else if (mEditState == EDIT_STATE.TEXT) {
                    mEditorToolsManager.addBubbleTextView();
                }
            }
            break;
            case R.id.myCanvas: {
                if (mEditState == EDIT_STATE.MORPH || mEditState == EDIT_STATE.PAINT) {
                    changeUndoButtonState(true);
                    changeRedoButtonState(false);
                    mFabSave.show();
                } else if (mEditState == EDIT_STATE.STICKER) {
                    mEditorToolsManager.setStickerInEdit(false);
                } else if (mEditState == EDIT_STATE.TEXT) {
                    mEditorToolsManager.setBubbleTextInEdit(false);
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IntentUtils.LOAD_IMAGE_REQUEST && data != null) { // Load Image Request
                String picturePath = FileUtils.getRealPathFromURI(this, data.getData());
                if (picturePath != null) {
                    new AsyncTask<String, Void, Bitmap>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            showProgressBar();
                        }

                        @Override
                        protected Bitmap doInBackground(String... paths) {
                            return BitmapFactory.decodeFile(paths[0]);
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            reset();
                            mDefaultBitmap.recycle();
                            mDefaultBitmap = mCanvasView.scaleToImage(bitmap);
                            saveBitmapToDiskCache(CACHE_KEY_DEFAULT, mDefaultBitmap, false);
                            hideProgressBar();
                        }
                    }.execute(picturePath);
                }
            } else if (requestCode == IntentUtils.CAMERA_REQUEST && mImageUri != null) { // Camera Request
                new LoadCameraImageTask(this, new LoadCameraImageTask.LoadCameraImageCallback() {
                    @Override
                    public void onPreExecute() {
                        showProgressBar();
                    }

                    @Override
                    public void onPostExecute(Bitmap bitmap) {
                        reset();
                        mDefaultBitmap.recycle();
                        mDefaultBitmap = mCanvasView.scaleToImage(bitmap);
                        saveBitmapToDiskCache(CACHE_KEY_DEFAULT, mDefaultBitmap, false);
                        hideProgressBar();
                    }
                }).execute(mImageUri);
            } else if (requestCode == IntentUtils.STICKER_CHOOSER_REQUEST && data != null) { // Sticker Choose Request
                final String stickerAssetsPath = data.getStringExtra(StickerChooserActivity.RESULT_KEY);
                mStickersLastPage = data.getIntExtra(StickerChooserActivity.POSITION, -1);
                if (stickerAssetsPath != null) {
                    new AsyncTask<String, Void, Bitmap>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            showProgressBar();
                        }

                        @Override
                        protected Bitmap doInBackground(String... paths) {
                            InputStream inputstream = null;
                            Bitmap bitmap = null;
                            try {
                                inputstream = getAssets().open(paths[0]);
                                bitmap = BitmapUtils.decodeSampledBitmapFromResource(inputstream, 100, 100);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (inputstream != null) {
                                    try {
                                        inputstream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            return bitmap;
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            super.onPostExecute(bitmap);
                            hideProgressBar();
                            mEditorToolsManager.addStickerView(bitmap);
                        }
                    }.execute(stickerAssetsPath);
                }
            } else if (requestCode == IntentUtils.SETTINGS_REQUEST) {
                loadSoundSettings();
            }
        }
    }

    // !!! Only if they are sertainly different - this is for small optimization
    public void changeButtonBackground(View imageButton) {
        if (mLastClickedButton != null) {
            mLastClickedButton.setBackgroundColor(mPrimaryColor);
        }
        if (imageButton != null) {
            imageButton.setBackgroundColor(mPrimaryDarkColor);
            mLastClickedButton = imageButton;
        } else {
            mLastClickedButton = null;
        }
    }

    /*
    * Handling Undo button state change
    * */
    public void changeUndoButtonState(boolean active) {
        if (active) {
            mButtonUndo.setImageDrawable(mUndoActiveDrawable);
            mFabSave.show();
        } else {
            mButtonUndo.setImageDrawable(mUndoNotActiveDrawable);
            mFabSave.hide();
        }
    }

    /*
    * Handling Redo button state change
    * */
    public void changeRedoButtonState(boolean active) {
        if (active) {
            mButtonRedo.setImageDrawable(mRedoActiveDrawable);
        } else {
            mButtonRedo.setImageDrawable(mRedoNotActiveDrawable);
        }
    }

    /*
    * Reset all buttons
    * */
    public void reset() {
        changeButtonBackground(null);
        changeUndoButtonState(false);
        changeRedoButtonState(false);
        mEditorToolsManager.reset();
        mEditState = EDIT_STATE.NONE;
    }

    /*
    * Reset all buttons and handle editor image change
    * */
    public void reset(Bitmap image) {
        reset();
        mCanvasView.scaleToImage(image);
    }

    /*
    * Handle a click on bottom toolbar items
    * */
    private void onToolbarItemClicked(View view) {
        switch (view.getId()) {
            case R.id.buttonMorph: {
                if (mLastClickedButton != view) {
                    playSound(mp);
                    handleStateChange(EDIT_STATE.MORPH);
                    mEditorToolsManager.startMorphing();
                    changeButtonBackground(view);
                }
            }
            break;
            case R.id.buttonPaint: {
                if (mLastClickedButton != view) {
                    playSound(mp);
                    handleStateChange(EDIT_STATE.PAINT);
                    mEditorToolsManager.startDrawing();
                    mFabAdd.show(mAddFabPaintDrawable);
                    changeButtonBackground(view);
                }
            }
            break;
            case R.id.buttonSticker: {
                if (mLastClickedButton != view) {
                    playSound(mp);
                    handleStateChange(EDIT_STATE.STICKER);
                    mFabAdd.show(mAddFabStickerDrawable);
                    changeButtonBackground(view);
                    IntentUtils.sendStickerChooserRequest(this, mStickersLastPage);
                }
            }
            break;
            case R.id.buttonTextSticker: {
                if (mLastClickedButton != view) {
                    playSound(mp);
                    handleStateChange(EDIT_STATE.TEXT);
                    mFabAdd.show(mAddFabTextDrawable);
                    mEditorToolsManager.addBubbleTextView();
                    changeButtonBackground(view);
                }
            }
            case R.id.buttonUndo: {
                if (mEditState == EDIT_STATE.MORPH) {
                    changeUndoButtonState(mEditorToolsManager.undoMorph());
                    changeRedoButtonState(mEditorToolsManager.isMorpherRedoActive());
                } else if (mEditState == EDIT_STATE.PAINT) {
                    changeUndoButtonState(mEditorToolsManager.undoPaint());
                    changeRedoButtonState(mEditorToolsManager.isDrawerRedoActive());
                }
            }
            break;
            case R.id.buttonRedo: {
                if (mEditState == EDIT_STATE.MORPH) {
                    changeRedoButtonState(mEditorToolsManager.redoMorph());
                    changeUndoButtonState(mEditorToolsManager.isMorpherUndoActive());
                } else if (mEditState == EDIT_STATE.PAINT) {
                    changeRedoButtonState(mEditorToolsManager.redoPaint());
                    changeUndoButtonState(mEditorToolsManager.isDrawerUndoActive());
                }
            }
            break;
        }
    }

    /*
    * Handle editor EDIT_STATE change
    * */
    private void handleStateChange(EDIT_STATE newState) {
        if (mEditState == EDIT_STATE.STICKER) {
            mEditorToolsManager.removeAllStickers();
        } else if (mEditState == EDIT_STATE.TEXT) {
            mEditorToolsManager.removeAllTextBubbles();
        } else if (mEditState != EDIT_STATE.NONE) {
            mEditorToolsManager.stopMorphing();
            mEditorToolsManager.stopDrawing();
        }

        if (mEditState != EDIT_STATE.NONE && newState == EDIT_STATE.MORPH || newState == EDIT_STATE.NONE) {
            mFabAdd.hide();
        }

        mCanvasView.invalidate();
        changeUndoButtonState(false);
        changeRedoButtonState(false);
        mEditState = newState;
    }

    private void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /*
    * Setup transition animations*/
    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition exitTrans = new Explode();
            getWindow().setExitTransition(exitTrans);

            Transition reenterTrans = new Slide();
            getWindow().setExitTransition(reenterTrans);
        }
    }

    private void playSound(MediaPlayer mp) {
        if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.start();
        }
    }

    /*
    * Prepare and update the player
    * */
    private void loadSoundSettings() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean haveSound = settings.getBoolean("sound", true);
        if (haveSound && mp == null) {
            mp = MediaPlayer.create(this, R.raw.wav_click_on);
        } else if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    /*
    * Save bitmap on disk cache
    * */
    private void saveBitmapToDiskCache(String key, Bitmap bitmap, boolean forRecycle) {
        AsyncDiskCache.getInstance(this).put(key, bitmap, forRecycle);
    }

    /*
    * Get bitmap from disk cache
    * */
    private Bitmap getBitmapFromDiskCache(String key) {
        return AsyncDiskCache.getInstance(this).getBitmap(key);
    }

    // This is for test purposes
    private class InitTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
        }

        @Override
        protected Void doInBackground(Bundle... bundles) {
            init(bundles[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressBar();
        }
    }
}
