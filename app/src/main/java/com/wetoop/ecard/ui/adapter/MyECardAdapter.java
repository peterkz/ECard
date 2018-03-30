package com.wetoop.ecard.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.ContactBean;
import com.wetoop.ecard.ui.dialog.QRCodeDialog;

import java.util.ArrayList;

/**
 * Created by User on 2017/9/29.
 */

public class MyECardAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ContactBean> contactBeanArrayList = new ArrayList<>();

    public MyECardAdapter(Context context, ArrayList<ContactBean> contactBeanArrayList) {
        this.context = context;
        this.contactBeanArrayList = contactBeanArrayList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return contactBeanArrayList.size()+1;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.my_card_tab_item, null);
            holder.name = (TextView) convertView.findViewById(R.id.name_text);
            holder.position = (TextView)convertView.findViewById(R.id.position_text);
            holder.qrCodeIcon = (ImageView)convertView.findViewById(R.id.qrcode_button);
            holder.company = (TextView)convertView.findViewById(R.id.company_text);
            holder.phone = (TextView)convertView.findViewById(R.id.phone_text);
            holder.email = (TextView)convertView.findViewById(R.id.email_text);
            holder.address = (TextView)convertView.findViewById(R.id.address_text);
            holder.noData = (RelativeLayout)convertView.findViewById(R.id.noData);
            holder.hasData = (RelativeLayout)convertView.findViewById(R.id.hasData);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            //holder.clean();
        }
        if (position < contactBeanArrayList.size()) {
            holder.name.setText(contactBeanArrayList.get(position).getCard().getInformation().getName());
            if(contactBeanArrayList.get(position).getCard().getInformation().getCompany()!=null){
                holder.company.setText(contactBeanArrayList.get(position).getCard().getInformation().getCompany());
            }
            if(contactBeanArrayList.get(position).getCard().getInformation().getPosition()!=null){
                holder.position.setText(contactBeanArrayList.get(position).getCard().getInformation().getPosition());
            }
            if(contactBeanArrayList.get(position).getCard().getPhones() != null) {
                for (int i = 0; i < contactBeanArrayList.get(position).getCard().getPhones().size(); i++) {
                    String phoneType = contactBeanArrayList.get(position).getCard().getPhones().get(i).getType();
                    if ("手机".equals(phoneType)) {
                        String phone = contactBeanArrayList.get(position).getCard().getPhones().get(i).getPhone();
                        holder.phone.setText(phone);
                    } else if ("工作号码".equals(phoneType)) {
                        String phone = contactBeanArrayList.get(position).getCard().getPhones().get(i).getPhone();
                    }
                }
            }
            if(contactBeanArrayList.get(position).getCard().getEmails() != null){
                for (int i = 0; i < contactBeanArrayList.get(position).getCard().getPhones().size(); i++) {
                    holder.email.setText(contactBeanArrayList.get(position).getCard().getEmails().get(i).getEmail());
                }
            }
            if(contactBeanArrayList.get(position).getCard().getAddresses()!=null){
                for (int i = 0; i < contactBeanArrayList.get(position).getCard().getAddresses().size(); i++) {
                    holder.address.setText(contactBeanArrayList.get(position).getCard().getAddresses().get(i).getAddress());
                }
            }
            holder.qrCodeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //new QRCodeDialog(context,contactBeanArrayList.get(position));
                }
            });
            holder.noData.setVisibility(View.GONE);
            holder.hasData.setVisibility(View.VISIBLE);
        }else{
            holder.noData.setVisibility(View.VISIBLE);
            holder.hasData.setVisibility(View.GONE);
        }
        return convertView;
    }
    public final class ViewHolder{
        public TextView name,position,company,phone,email,address;
        private ImageView qrCodeIcon;
        private RelativeLayout noData,hasData;
        void clean(){
            name.setText(null);
            position.setText(null);
            company.setText(null);
            phone.setText(null);
            email.setText(null);
            address.setText(null);
            qrCodeIcon.setImageBitmap(null);
        }
    }
}
