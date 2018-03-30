package com.wetoop.ecard.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.ContactBean;

import java.util.ArrayList;

/**
 * Created by User on 2017/10/18.
 */

public class NewContactsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ContactBean> contactBeanArrayList;
    private Boolean aBoolean = false;
    private Button saveECard;
    private TextView recordsState,contactsName,contactsMessage;
    private ImageView eCardPhone;
    /**
     * recordsStateType用于记录发送过来的名片状态类型：
     * 1、“noSave”为“未保存”状态，显示“保存”按钮
     * 2、“hasSave”为“已保存”状态，“保存”按钮消失，TextView显示“已保存”
     * 3、“commutation”为“已互加”状态，“保存”按钮消失，TextView显示“已互加”
     * 4、“hasSend”为“已发送”状态，“保存”按钮消失，TextView显示“已发送”（这个只在“发送记录”里显示）
     * */
    private String recordsStateType;

    public NewContactsAdapter(Context context,ArrayList<ContactBean> contactBeanArrayList) {
        this.context = context;
        this.contactBeanArrayList = contactBeanArrayList;
    }

    public void addData(ArrayList<ContactBean> list){
        if(contactBeanArrayList == null){
            contactBeanArrayList = new ArrayList<>();
        }
        contactBeanArrayList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //return list.size();
        return contactBeanArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
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
        if (convertView == null){
            holder= new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.new_contacts_tab_item, null);
            holder.contactsName = (TextView) convertView.findViewById(R.id.contactsName);
            //holder.recordsState = (TextView) convertView.findViewById(R.id.recordsState);
            holder.contactsMessage = (TextView) convertView.findViewById(R.id.contactsMessage);
            holder.eCardPhone = (ImageView)convertView.findViewById(R.id.eCardPhoto);
            holder.saveECard = (TextView)convertView.findViewById(R.id.saveECard);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.clean();
        }
        holder.saveECard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        if(contactBeanArrayList.get(position).getRecordsStateType() != null) {
            recordsStateType = contactBeanArrayList.get(position).getRecordsStateType();
            holder.saveECard.setText(recordsStateType);
            if ("保存".equals(recordsStateType)) {
                holder.saveECard.setBackgroundResource(R.drawable.save_card_background);
            } else{
                holder.saveECard.setBackgroundResource(R.color.white);
            }
        }else{
            holder.saveECard.setVisibility(View.GONE);
        }
        if(contactBeanArrayList.get(position).getUserName() != null){
            holder.contactsName.setText(contactBeanArrayList.get(position).getUserName());
        }
        if(contactBeanArrayList.get(position).getContactsMessage() != null){
            holder.contactsMessage.setText(contactBeanArrayList.get(position).getContactsMessage());
        }
        return convertView;
    }
    public final class ViewHolder{
        public TextView contactsName,contactsMessage;
        public ImageView eCardPhone;
        public TextView saveECard;
        void clean(){
            //recordsState.setText(null);
            contactsName.setText(null);
            contactsMessage.setText(null);
            saveECard.setText(null);
        }
    }
}
