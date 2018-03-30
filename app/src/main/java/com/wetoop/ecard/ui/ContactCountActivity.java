package com.wetoop.ecard.ui;

import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.Holdable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * @author Parck.
 * @date 2017/10/24.
 * @desc
 */
@Slug(layout = R.layout.activity_contact_count)
public class ContactCountActivity extends TitleBarActivity implements Standardize {

    @FindView(R.id.linkman_count_text)
    private TextView linkmanCountText;
    @FindView(R.id.count_info_text)
    private TextView countInfoText;
    @FindView(R.id.pie_chart_view)
    private PieChartView pieChartView;
    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    @FindView(R.id.linkman_percent_text)
    private TextView linkmanPercentText;
    @FindView(R.id.send_count_text)
    private TextView sendCountText;
    @FindView(R.id.receive_count_text)
    private TextView receiveCountText;
    @FindView(R.id.add_count_text)
    private TextView addCountText;
    private ContactCountBean contactCountBean;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        pieChartView.setInteractive(true);
        contactCountBean = new ContactCountBean();
        contactCountBean.initBean();
    }

    @Override
    public void initView() {
        setCenterTitleContent("统计");
    }

    @Override
    public void setListeners() {
    }

    @Override
    public void onCreateLast() {
        contactCountBean.setupView(this);
    }

    // =============================================================================================
    // inner class
    // =============================================================================================

    public class ContactCountBean implements Serializable {

        private static final long serialVersionUID = -814614751637432687L;

        public String linkmanCount;
        public String countInfo;
        public PieChartData pieChart;
        public List<CityItem> items;
        public String linkmanPercent;
        public String sendCount;
        public String receiveCount;
        public String addCount;
        public RecyclerViewAdapter adapter;

        public void initBean() {
            this.linkmanCount = "共有650位联系人";
            this.countInfo = "其中450位有地址信息，一下统计基于联系人地址";
            this.linkmanPercent = "联系人数量超过<font color='#EB5169'><b>80%</b></font>的用户";
            this.sendCount = "185";
            this.receiveCount = "166";
            this.addCount = "155";
            PieChartData pieChart = new PieChartData();//实例化PieChartData对象
            pieChart.setHasLabelsOutside(false);//设置饼图外面是否显示值
            pieChart.setHasCenterCircle(true);//设置饼图中间是否有第二个圈
            pieChart.setCenterCircleColor(THIS.getResources().getColor(R.color.white));//设置饼图中间圈的颜色
            pieChart.setCenterCircleScale(0.4F);////设置第二个圈的大小比例
            pieChart.setCenterText2Color(THIS.getResources().getColor(R.color.grey));//设置第二个圈文本颜色
            pieChart.setHasLabelsOnlyForSelected(true);//设置当值被选中才显示
            ArrayList<SliceValue> values = new ArrayList<>();
            values.add(new SliceValue(0.2F, Color.parseColor("#35CDFC")).setLabel("广西"));
            values.add(new SliceValue(0.8F, Color.parseColor("#FFB260")).setLabel("广东"));
            pieChart.setValues(values);
            this.pieChart = pieChart;

            ArrayList<CityItem> items = new ArrayList<>();
            CityItem item = new CityItem();
            item.resId = R.drawable.shape_red_circle_solid_r15;
            item.city = "广西";
            items.add(item);
            this.items = items;

            item = new CityItem();
            item.resId = R.drawable.shape_red_circle_solid_r15;
            item.city = "广东";
            items.add(item);
            this.items = items;

            this.adapter = new RecyclerViewAdapter<CityItem>(THIS, R.layout.item_linkman_count_city, items) {
                @Override
                protected void binding(ViewHolder holder, CityItem data, int position) {
                    data.holding(holder);
                }
            };
        }

        public void setupView(ContactCountActivity activity) {
            activity.linkmanCountText.setText(linkmanCount);
            activity.countInfoText.setText(countInfo);
            activity.linkmanPercentText.setText(Html.fromHtml(linkmanPercent));
            activity.sendCountText.setText(sendCount);
            activity.receiveCountText.setText(receiveCount);
            activity.addCountText.setText(addCount);
            activity.pieChartView.setPieChartData(pieChart);
            activity.recyclerView.setAdapter(adapter);
        }

        class CityItem implements Holdable {

            private static final long serialVersionUID = 2759459763883248719L;

            public
            @DrawableRes
            int resId;
            public String city;

            @Override
            public void holding(RecyclerViewAdapter.ViewHolder holder) {
                View tagViw = holder.findViewById(R.id.tag_view);
                if (resId != 0) {
                    tagViw.setBackgroundResource(resId);
                }

                TextView tagText = holder.findViewById(R.id.tag_text);
                if (city != null) {
                    tagText.setText(city);
                } else {
                    tagText.setText("");
                }
            }

            @Override
            public void onClicked(View v) {

            }
        }
    }
}
