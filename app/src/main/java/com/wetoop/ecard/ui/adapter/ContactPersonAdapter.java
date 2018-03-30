package com.wetoop.ecard.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.bean.ContactBean;
import com.wetoop.ecard.tools.RoundTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by User on 2017/9/29.
 */

public class ContactPersonAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ContactBean> contactBeanArrayList;
    private Boolean aBoolean = false;
    private Map<String, Integer> alphaIndexer;
    private List<String> sections;
    private boolean flag;//标志用于只执行一次代码
    private OnGetAlphaIndexerAndSectionsListener listener;

    public ContactPersonAdapter(Context context, ArrayList<ContactBean> contactBeanArrayList) {
        this.context = context;
        this.contactBeanArrayList = contactBeanArrayList;
        alphaIndexer = new HashMap<>();
        sections = new ArrayList<>();
        addDataAlphaIndexer(contactBeanArrayList);
    }

    public void addDataAlphaIndexer(ArrayList<ContactBean> list) {
        if (contactBeanArrayList == null) {
            contactBeanArrayList = new ArrayList<>();
        }
        contactBeanArrayList = list;
        for (int i = 0; i < contactBeanArrayList.size(); i++) {
            //当前汉语拼音的首字母
            String currentAlpha = contactBeanArrayList.get(i).getSpellFirst();
            //上一个拼音的首字母，如果不存在则为""
            String previewAlpha = (i - 1) >= 0 ? contactBeanArrayList.get(i - 1).getSpellFirst() : "";
            if (!previewAlpha.equals(currentAlpha)) {
                String firstAlpha = contactBeanArrayList.get(i).getSpellFirst().toUpperCase();
                alphaIndexer.put(firstAlpha, i);
                sections.add(firstAlpha);
            }
        }
    }

    @Override
    public int getCount() {
        if (!flag) {
            if (listener != null) {
                listener.getAlphaIndexerAndSectionsListener(alphaIndexer, sections);
            }
            flag = true;
        }
        return contactBeanArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactBeanArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.contact_person_tab_item, null);
            holder.name = (TextView) convertView.findViewById(R.id.name_text);
            holder.first_alpha = (TextView) convertView.findViewById(R.id.first_alpha);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            //holder.clean();
        }
        Information information = contactBeanArrayList.get(position).getCard().getInformation();
        holder.name.setText(information.getName());
        Glide.with(context)
                .load(App.oss().getURL(information.getCard_id()))
                .placeholder(R.mipmap.default_avatar_icon)
                .transform(new RoundTransformation(context))
                .signature(new StringSignature(String.valueOf(information.getDateUpdated() == null ? System.currentTimeMillis() : information.getDateUpdated().getTime())))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.image);
        if (position >= 1) {
            String currentAlpha = contactBeanArrayList.get(position).getSpellFirst();
            String previewAlpha = contactBeanArrayList.get(position - 1).getSpellFirst();
            if (!previewAlpha.equals(currentAlpha)) {
                holder.first_alpha.setVisibility(View.VISIBLE);
                String inputData = contactBeanArrayList.get(position).getSpellFirst().toUpperCase();
                holder.first_alpha.setText(inputData);
            } else {
                holder.first_alpha.setVisibility(View.GONE);
            }
        } else {
            holder.first_alpha.setVisibility(View.VISIBLE);
            String inputData = contactBeanArrayList.get(position).getSpellFirst().toUpperCase();
            holder.first_alpha.setText(inputData);
        }

        return convertView;
    }

    public final class ViewHolder {
        public TextView name, first_alpha;
        private ImageView image;

        void clean() {
            name.setText(null);
            first_alpha.setText(null);
            image.setImageBitmap(null);
        }
    }

    public void setOnGetAlphaIndeserAndSectionListener(OnGetAlphaIndexerAndSectionsListener listener) {
        this.listener = listener;
    }

    public interface OnGetAlphaIndexerAndSectionsListener {
        public void getAlphaIndexerAndSectionsListener(Map<String, Integer> alphaIndexer, List<String> sections);

    }
}
