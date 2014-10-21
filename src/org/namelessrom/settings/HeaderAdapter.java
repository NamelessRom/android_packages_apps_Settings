package org.namelessrom.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.accounts.AuthenticatorHelper;
import com.android.settings.accounts.ManageAccountsSettings;

import java.util.List;

/**
 * Created by alex on 21.10.14.
 */
public abstract class HeaderAdapter extends ArrayAdapter<Preference> {
    protected static final int HEADER_TYPE_NORMAL = 1;
    protected static final int HEADER_TYPE_SWITCH = 2;
    protected static final int HEADER_TYPE_BUTTON = 3;
    protected static final int HEADER_TYPE_COUNT = HEADER_TYPE_BUTTON + 1;

    private AuthenticatorHelper mAuthHelper;

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    protected static class HeaderViewHolder {
        public ImageView icon;
        public TextView title;
        public TextView summary;
        public Switch switch_;
        public ImageButton button_;
        public View divider_;
    }

    private LayoutInflater mInflater;

    @Override
    public int getItemViewType(int position) {
        Preference preference = getItem(position);
        return getPreferenceType(preference);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false; // because of categories
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != 0;
    }

    @Override
    public int getViewTypeCount() {
        return HEADER_TYPE_COUNT;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public HeaderAdapter(Context context, List<Preference> objects,
            AuthenticatorHelper authenticatorHelper) {
        super(context, 0, objects);

        mAuthHelper = authenticatorHelper;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        Preference preference = getItem(position);
        int headerType = getPreferenceType(preference);

        if (convertView == null || headerType == HEADER_TYPE_SWITCH) {
            holder = new HeaderViewHolder();
            switch (headerType) {
                case HEADER_TYPE_SWITCH:
                    convertView =
                            mInflater.inflate(R.layout.preference_header_switch_item, parent,
                                    false);
                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.title = (TextView)
                            convertView.findViewById(com.android.internal.R.id.title);
                    holder.summary = (TextView)
                            convertView.findViewById(com.android.internal.R.id.summary);
                    holder.switch_ = (Switch) convertView.findViewById(R.id.switchWidget);
                    break;

                case HEADER_TYPE_BUTTON:
                    convertView =
                            mInflater.inflate(R.layout.preference_header_button_item, parent,
                                    false);
                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.title = (TextView)
                            convertView.findViewById(com.android.internal.R.id.title);
                    holder.summary = (TextView)
                            convertView.findViewById(com.android.internal.R.id.summary);
                    holder.button_ = (ImageButton) convertView.findViewById(R.id.buttonWidget);
                    holder.divider_ = convertView.findViewById(R.id.divider);
                    break;

                default:
                case HEADER_TYPE_NORMAL:
                    convertView = mInflater.inflate(
                            R.layout.preference_header_item, parent,
                            false);
                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.title = (TextView)
                            convertView.findViewById(com.android.internal.R.id.title);
                    holder.summary = (TextView)
                            convertView.findViewById(com.android.internal.R.id.summary);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        // All view fields must be updated every time, because the view may be recycled
        switch (headerType) {
            case HEADER_TYPE_SWITCH:
                // Would need a different treatment if the main menu had more switches
                setSwitch(preference, holder);
                updateCommonHeaderView(preference, holder);
                break;

            case HEADER_TYPE_BUTTON:
                setButton(preference, holder);
                updateCommonHeaderView(preference, holder);
                break;

            case HEADER_TYPE_NORMAL:
                updateCommonHeaderView(preference, holder);
                break;
        }

        return convertView;
    }

    private void updateCommonHeaderView(Preference preference, HeaderViewHolder holder) {
        if (preference.getExtras() != null &&
                preference.getExtras().containsKey(ManageAccountsSettings.KEY_ACCOUNT_TYPE)) {
            final String accType = preference.getExtras().getString(
                    ManageAccountsSettings.KEY_ACCOUNT_TYPE);
            final Drawable icon = mAuthHelper.getDrawableForType(getContext(), accType);
            updateIconLayout(holder, true);
            holder.icon.setImageDrawable(icon);
        } else {
            updateIconLayout(holder, false);
            final Drawable icon = preference.getIcon();
            if (icon != null) {
                holder.icon.setImageDrawable(icon);
            }
        }
        holder.title.setText(preference.getTitle());
        CharSequence summary = preference.getSummary();
        if (!TextUtils.isEmpty(summary)) {
            holder.summary.setVisibility(View.VISIBLE);
            holder.summary.setText(summary);
        } else {
            holder.summary.setVisibility(View.GONE);
        }
    }

    private void updateIconLayout(HeaderViewHolder holder, boolean forceDefaultSize) {
        ViewGroup.LayoutParams lp = holder.icon.getLayoutParams();
        if (forceDefaultSize) {
            lp.width = getContext().getResources().getDimensionPixelSize(
                    R.dimen.header_icon_width);
        } else {
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        lp.height = lp.width;
        holder.icon.setLayoutParams(lp);
    }

    public abstract int getPreferenceType(final Preference preference);

    public abstract void setSwitch(final Preference preference, final HeaderViewHolder holder);

    public abstract void setButton(final Preference preference, final HeaderViewHolder holder);

    public abstract void resume();

    public abstract void pause();
}
