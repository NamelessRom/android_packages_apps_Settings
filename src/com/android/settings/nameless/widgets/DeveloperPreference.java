package com.android.settings.nameless.widgets;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DeveloperPreference extends LinearLayout {
    public static final String GRAVATAR_API       = "http://www.gravatar.com/avatar/";
    public static       int    mDefaultAvatarSize = 400;

    private String nameDev;
    private String githubLink;
    private String crowdinLink;
    private String devEmail;

    @Override
    public boolean isInEditMode() {
        return true;
    }

    public DeveloperPreference(Context context) {
        this(context, null);
    }

    public DeveloperPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeveloperPreference(Context context, String name, String github, String email) {
        this(context, name, github, null, email);
    }

    public DeveloperPreference(Context context, String name, String crowdin) {
        this(context, name, null, crowdin, null);
    }

    public DeveloperPreference(Context context, String name, String github, String crowdin,
            String email) {
        super(context);

        nameDev = name;
        githubLink = github;
        crowdinLink = crowdin;
        devEmail = email;

        setupView(context);
    }

    public DeveloperPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.DeveloperPreference);
            nameDev = typedArray.getString(R.styleable.DeveloperPreference_nameDev);
            githubLink = typedArray.getString(R.styleable.DeveloperPreference_githubLink);
            crowdinLink = typedArray.getString(R.styleable.DeveloperPreference_crowdinLink);
            devEmail = typedArray.getString(R.styleable.DeveloperPreference_emailDev);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }

        setupView(context);
    }

    private void setupView(final Context context) {
        /**
         * Inflate views
         */

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dev_card, this, true);

        final TextView devName = (TextView) layout.findViewById(R.id.name);
        final ImageView githubButton = (ImageView) layout.findViewById(R.id.github_button);
        final ImageView photoView = (ImageView) layout.findViewById(R.id.photo);
        final View photoTextBar = layout.findViewById(R.id.photo_text_bar);

        /**
         * Initialize buttons
         */

        if (githubLink != null) {
            final OnClickListener openGithub = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri githubURL = Uri.parse(githubLink);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, githubURL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(intent);
                }
            };
            githubButton.setOnClickListener(openGithub);
        } else {
            githubButton.setVisibility(View.GONE);
        }

        if (crowdinLink != null) {
            final OnClickListener openCrowdin = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri crowdinURL = Uri.parse(crowdinLink);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, crowdinURL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(intent);
                }
            };
            devName.setOnClickListener(openCrowdin);
            photoTextBar.setVisibility(View.GONE);
        }

        if (devEmail != null) {
            UrlImageViewHelper.setUrlDrawable(photoView,
                    getGravatarUrl(devEmail),
                    R.drawable.ic_null,
                    UrlImageViewHelper.CACHE_DURATION_ONE_WEEK);
        } else {
            photoView.setVisibility(View.GONE);
        }

        devName.setText(nameDev);
    }

    public String getGravatarUrl(String email) {
        try {
            String emailMd5 = getMd5(email.trim().toLowerCase());
            return String.format("%s%s?s=%d&d=mm",
                    GRAVATAR_API,
                    emailMd5,
                    mDefaultAvatarSize);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String getMd5(String devEmail) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(devEmail.getBytes());
        byte byteData[] = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
