package com.example.yonko.funnyface.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yonko.funnyface.R;
import com.example.yonko.funnyface.widgets.BubbleTextView;

public class BubbleInputDialog extends Dialog {
    private static final String TAG = BubbleInputDialog.class.getSimpleName();
    private static final int MAX_COUNT = 33; // Limit of 33 characters
    private final String DEFAULT_TEXT;

    private EditText mEditTextBubbleInput;
    private TextView mTextViewShowCount;
    private TextView mTextViewActionDone;
    private Context mContext;
    private BubbleTextView mBubbleTextView;

    public BubbleInputDialog(Context context) {
        super(context);
        mContext = context;
        DEFAULT_TEXT = context.getString(R.string.double_click_input_text);
        initView();
    }

    public BubbleInputDialog(Context context, BubbleTextView view) {
        super(context);
        mContext = context;
        DEFAULT_TEXT = context.getString(R.string.double_click_input_text);
        mBubbleTextView = view;
        initView();
    }

    @Override
    public void show() {
        super.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager m = (InputMethodManager) mEditTextBubbleInput.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                m.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        }, 500);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        InputMethodManager m = (InputMethodManager) mEditTextBubbleInput.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        m.hideSoftInputFromWindow(mEditTextBubbleInput.getWindowToken(), 0);
    }

    public void setBubbleTextView(BubbleTextView bubbleTextView) {
        mBubbleTextView = bubbleTextView;
        if (DEFAULT_TEXT.equals(bubbleTextView.getStr())) {
            mEditTextBubbleInput.setText("");
        } else {
            mEditTextBubbleInput.setText(bubbleTextView.getStr());
            mEditTextBubbleInput.setSelection(bubbleTextView.getStr().length());
        }
    }

    private void initView() {
        setContentView(R.layout.dialog_view_input);
        mTextViewActionDone = (TextView) findViewById(R.id.tv_action_done);
        mTextViewShowCount = (TextView) findViewById(R.id.tv_show_count);
        mEditTextBubbleInput = (EditText) findViewById(R.id.et_bubble_input);
        mEditTextBubbleInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                long textLength = s.length();
                mTextViewShowCount.setText(String.valueOf(MAX_COUNT - textLength));
                if (textLength > MAX_COUNT) {
                    mTextViewShowCount.setTextColor(mContext.getResources().getColor(R.color.red_e73a3d));
                } else {
                    mTextViewShowCount.setTextColor(mContext.getResources().getColor(R.color.grey_8b8b8b));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEditTextBubbleInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    done();
                    return true;
                }
                return false;
            }
        });
        mTextViewActionDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                done();
            }
        });
    }

    private void done() {
        if (Integer.valueOf(mTextViewShowCount.getText().toString()) < 0) {
            Toast.makeText(mContext, mContext.getString(R.string.over_text_limit), Toast.LENGTH_SHORT).show();
            return;
        }
        dismiss();
        if (mCompleteCallBack != null) {
            String str;
            if (TextUtils.isEmpty(mEditTextBubbleInput.getText())) {
                str = "";
            } else {
                str = mEditTextBubbleInput.getText().toString();
            }
            mCompleteCallBack.onComplete(mBubbleTextView, str);
        }
    }

    public void setCompleteCallBack(CompleteCallBack completeCallBack) {
        this.mCompleteCallBack = completeCallBack;
    }

    public interface CompleteCallBack {
        void onComplete(View mBubbleTextView, String str);
    }

    private CompleteCallBack mCompleteCallBack;
}
