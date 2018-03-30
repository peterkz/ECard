package com.wetoop.ecard.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.MessageAPI;
import com.wetoop.ecard.api.SearchAccountAPI;
import com.wetoop.ecard.api.model.Account;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.ContactBean;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.BitmapTool;
import com.wetoop.ecard.tools.CircleTransform;
import com.wetoop.ecard.tools.MD5;
import com.wetoop.ecard.ui.dialog.ChoiceCardDialog;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by User on 2017/10/13.
 */

public class MyContactsAdapter extends BaseAdapter {

    private Activity context;
    private int size;
    private int scrollStauts = 0;
    private List<ContactBean> contactsList = new ArrayList<>();
    private Map<String, Integer> alphaIndexer;
    private List<String> sections;
    private boolean flag;//标志用于只执行一次代码
    private CardBean cardBean;
    private ListView listView;
    private OnGetAlphaIndexerAndSectionsListener listener;

    public MyContactsAdapter(Activity context, List<ContactBean> contactsList, CardBean cardBean,ListView listView) {
        this.context = context;
        this.cardBean = cardBean;
        this.contactsList = contactsList;
        alphaIndexer = new HashMap<>();
        sections = new ArrayList<>();
        for (int i = 0; i < contactsList.size(); i++) {
            //当前汉语拼音的首字母
            String currentAlpha = contactsList.get(i).getSpellFirst();
            //上一个拼音的首字母，如果不存在则为""
            String previewAlpha = (i - 1) >= 0 ? contactsList.get(i - 1).getSpellFirst() : "";
            if (!previewAlpha.equals(currentAlpha)) {
                String firstAlpha = contactsList.get(i).getSpellFirst().toUpperCase();
                alphaIndexer.put(firstAlpha, i);
                sections.add(firstAlpha);
            }
        }
        this.listView = listView;
        this.listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub  
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE://停止  
                        scrollStauts = 0;
                        updateUI();
                        //System.out.println("停止");  
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://触摸滑动  
                        scrollStauts = 1;
                        //System.out.println("触摸滑动");  
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING://快速滑动 
                        scrollStauts = 2;
                        //System.out.println("快速滑动");  
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    public void updateUI() {
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (!flag) {
            if (listener != null) {
                listener.getAlphaIndexerAndSectionsListener(alphaIndexer, sections);
            }
            flag = true;
        }
        return contactsList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.my_contact_list_item, null);
            holder.name = (TextView) convertView.findViewById(R.id.name_text);
            holder.first_alpha = (TextView) convertView.findViewById(R.id.first_alpha);
            holder.sendECard = (TextView) convertView.findViewById(R.id.sendECard);
            holder.photo = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(contactsList.get(position).getCard().getInformation().getName());
        if (!TextUtils.isEmpty(contactsList.get(position).getCard().getInformation().getAvatar()) && scrollStauts == 0) {
            Bitmap bitmap = BitmapTool.base642Bitmap(contactsList.get(position).getCard().getInformation().getAvatar());
            if (bitmap != null) {
                Bitmap transform = new CircleTransform().transform(bitmap);
                if (transform != null) holder.photo.setImageBitmap(transform);
                else holder.photo.setImageResource(R.mipmap.norm_avatar_icon);
            } else holder.photo.setImageResource(R.mipmap.norm_avatar_icon);
        } else holder.photo.setImageResource(R.mipmap.norm_avatar_icon);
        if (position >= 1) {
            String currentAlpha = contactsList.get(position).getSpellFirst();
            String previewAlpha = contactsList.get(position - 1).getSpellFirst();
            if (!previewAlpha.equals(currentAlpha)) {
                holder.first_alpha.setVisibility(View.VISIBLE);
                String inputData = contactsList.get(position).getSpellFirst().toUpperCase();
                holder.first_alpha.setText(inputData);
            } else {
                holder.first_alpha.setVisibility(View.GONE);
            }
        } else {
            holder.first_alpha.setVisibility(View.VISIBLE);
            String inputData = contactsList.get(position).getSpellFirst().toUpperCase();
            holder.first_alpha.setText(inputData);
        }
        holder.sendECard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingDialog.show(context);
                APIProvider.get(SearchAccountAPI.class).searchContactGET(MD5.encoding(contactsList.get(position).getCard().getPhones().get(0).getPhone()), new OnESubscriber<Account>() {
                    @Override
                    protected void onComplete(boolean success, Account o, Throwable e) {
                        if (success) {
                            if (o == null) {
                                Uri uri = Uri.parse("smsto:" + contactsList.get(position).getCard().getPhones().get(0).getPhone()); //要发送短信的电话号码
                                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                                intent.putExtra("向他发送一张e卡", "这是跳转后发送短信界面的消息编辑框显示内容");
                                context.startActivity(intent);
                            } else {
                                LoadingDialog.show(context);
                                APIProvider.get(MessageAPI.class).sendExchangeMessage(cardBean, o.getUser_id(), "", new SyncReference.CompletionListener() {
                                    @Override
                                    public void onComplete(SyncError syncError, SyncReference syncReference) {
                                        if (syncError != null) {
                                            Log.e("MyContactsAdapter", "syncError : " + syncError.getMessage());
                                            Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "发送成功", Toast.LENGTH_SHORT).show();
                                            new ChoiceCardDialog(context, new ChoiceCardDialog.OnChoiceListener() {
                                                @Override
                                                public void onChoice(CardBean target) {
                                                }
                                            }).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
        return convertView;
    }

    public final class ViewHolder {
        public TextView name, first_alpha, sendECard;
        public ImageView photo;
    }

    public void setOnGetAlphaIndexerAndSectionListener(OnGetAlphaIndexerAndSectionsListener listener) {
        this.listener = listener;
    }

    public interface OnGetAlphaIndexerAndSectionsListener {
        void getAlphaIndexerAndSectionsListener(Map<String, Integer> alphaIndexer, List<String> sections);
    }
}
