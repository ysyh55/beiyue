/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.volley.DoCommentRequest;
import me.zheteng.cbreader.utils.volley.StringRequest;

/**
 * TODO 记得添加注释
 */
public class PublishCommentFragment extends DialogFragment {
    private static final String KEY_TOKEN = "key_token";
    private static final String KEY_CAPTCHAURL = "key_captchaurl";
    private static final String KEY_SID = "key_sid";
    private static final String KEY_PID = "key_pid";
    ImageView mSeccodeView;
    Button mSend;
    Button mDismiss;
    ReadActivity mActivity;
    String mToken;
    String mCaptchaUrl;
    EditText mCaptcha;
    EditText mContent;
    int mSid;
    String mPid;
    private boolean mIsSendingComment;
    private PublishCommentDialogListener mListener;

    public interface PublishCommentDialogListener {
        public void onCommentSuccess(DialogFragment dialog);
    }

    public static PublishCommentFragment newInstance(String mToken, int mSid, String pid) {
        PublishCommentFragment fragment = new PublishCommentFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TOKEN, mToken);
        args.putInt(KEY_SID, mSid);
        args.putString(KEY_PID, pid);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPublishCommentDialogListener(PublishCommentDialogListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mToken = args.getString(KEY_TOKEN);
        mSid = args.getInt(KEY_SID);
        mPid = args.getString(KEY_PID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_comment, container, false);
        // Inflate the layout to use as dialog or embedded fragment
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        findViews(view);
        return view;
    }

    private void findViews(View view) {
        mSeccodeView = ((ImageView) view.findViewById(R.id.seccode));
        mSend = ((Button) view.findViewById(R.id.btn_send));
        mDismiss = ((Button) view.findViewById(R.id.btn_dismiss));
        mCaptcha = (EditText) view.findViewById(R.id.captcha);
        mContent = (EditText) view.findViewById(R.id.comment_content);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (ReadActivity) getActivity();

        initViews();
        refreshCaptcha();

    }

    private void initViews() {
        mSeccodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCaptcha();
            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        mDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void sendComment() {
        mSend.setClickable(false);
        if (TextUtils.isEmpty(mCaptcha.getText()) || TextUtils.isEmpty(mContent.getText())) {
            Toast.makeText(mActivity, R.string.empty_content_or_captcha, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mIsSendingComment) {
            return;
        }

        mIsSendingComment = true;
        Map<String, String> params = new HashMap<>();
        params.put("op", "publish");
        params.put("sid", String.valueOf(mSid));
        params.put("seccode", mCaptcha.getText().toString());
        params.put("pid", mPid);
        params.put("csrf_token", mToken);
        params.put("content", mContent.getText().toString());
        MainApplication.requestQueue.add(new DoCommentRequest<String>(Request.Method.POST,
                APIUtils.DO_CMT_URL, String.class,
                APIUtils.ajaxHeaders, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                mIsSendingComment = false;
                if (s.equals("comment_ok") || s.equals("reply_ok")) {
                    Toast.makeText(mActivity, R.string.send_comment_ok, Toast.LENGTH_SHORT).show();
                    if (mListener != null) {
                        mListener.onCommentSuccess(PublishCommentFragment.this);
                    }
                    dismiss();
                } else {
                    Toast.makeText(mActivity, R.string.send_comment_fail, Toast.LENGTH_SHORT).show();
                    refreshCaptcha();
                }
                mSend.setClickable(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(mActivity, R.string.send_comment_fail, Toast.LENGTH_SHORT).show();
                mIsSendingComment = false;
                refreshCaptcha();
                System.out.println(volleyError.networkResponse.toString());
                mSend.setClickable(false);
            }
        }));
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    public void refreshCaptcha() {
        String url = APIUtils.getCaptchaDataUrl(mToken);
        Request request = new StringRequest(url, APIUtils.ajaxHeaders, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonObject) {
                if (TextUtils.isEmpty(jsonObject)) {
                    Toast.makeText(mActivity, R.string.err_captcha, Toast.LENGTH_SHORT).show();
                    return;
                }
                JsonObject object = new JsonParser().parse(jsonObject).getAsJsonObject();

                mCaptchaUrl = APIUtils.HOST_NAME + object.get("url").getAsString();

                requestCaptchaImage();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println(volleyError.toString());
            }
        });
        MainApplication.requestQueue.add(request);

    }

    private void requestCaptchaImage() {
        MainApplication.requestQueue.add(new ImageRequest(mCaptchaUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                mSeccodeView.setImageBitmap(bitmap);
            }
        }, 100, 100, ImageView.ScaleType.FIT_XY, Bitmap.Config.ALPHA_8, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println(volleyError.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return APIUtils.ajaxHeaders;
            }
        });
    }
}
