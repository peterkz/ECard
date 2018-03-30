package com.wetoop.ecard.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.model.Feedback;
import com.wetoop.ecard.tools.BitmapTool;
import com.wetoop.ecard.tools.GetImagePath;
import com.wetoop.ecard.tools.PermissionUtil;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wetoop.ecard.ui.dialog.PopupSelectorDialog;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Gradable;
import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.slug.annotation.BindView;
import cn.edots.slug.annotation.ClickView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import cn.edots.slug.core.EditSlugger;

import static com.wetoop.ecard.Constants.URI_FEEDBACK;
import static com.wetoop.ecard.Constants.URI_REPORT;

/**
 * @author Parck.
 * @date 2017/11/23.
 * @desc 投诉、反馈界面
 */
@Slug(layout = R.layout.activity_feedback)
public class FeedbackActivity extends TitleBarActivity implements Standardize {
    public static final String TITLE_TEXT = "TITLE_TEXT";

    @BindView(R.id.feedback_text)
    private EditSlugger feedbackSlugger;
    @FindView(R.id.photo_count_text)
    private TextView countView;
    @FindView(R.id.recycle_view)
    private RecyclerView recyclerView;

    private PopupSelectorDialog selectorDialog;
    private String title;
    private Uri imageUri;
    private File imageFile;
    private final int TAKE_PHOTO = 1000;
    private final int CHOOSE_PHOTO = 1001;
    private final int CROP_PHOTO = 1002;
    private List<Item> items = new ArrayList<>();
    private RecyclerViewAdapter adapter;

    @ClickView({R.id.submit_button})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.submit_button:
                upload(new SyncReference.CompletionListener() {
                    @Override
                    public void onComplete(SyncError syncError, SyncReference syncReference) {
                        LoadingDialog.hide();
                        if (syncError == null) {
                            TOAST("上传成功");
                            items.clear();
                            Item item = new Item();
                            item.setType(1);
                            items.add(item);
                            feedbackSlugger.clearFocus(THIS);
                            feedbackSlugger.getView().setText("");
                            adapter.notifyDataSetChanged();
                        } else {
                            TOAST("操作失败");
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        Item item = new Item();
        item.setType(1);
        items.add(item);
        if (map != null) {
            title = (String) map.get(TITLE_TEXT);
            if (TextUtils.isEmpty(title)) finish();
        } else finish();
        selectorDialog = new PopupSelectorDialog(THIS, new String[]{"打开相机", "选择照片"}, new PopupSelectorDialog.OnClickListener() {
            @Override
            public void onSure(int position, CharSequence text) {
                switch (position) {
                    case 0:
                        if (ActivityCompat.checkSelfPermission(THIS, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(THIS, new String[]{Manifest.permission.CAMERA}, 1);
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                imageFile = BitmapTool.open_v2(THIS, TAKE_PHOTO);
                            } else imageUri = BitmapTool.open(THIS, TAKE_PHOTO);
                        }
                        break;
                    case 1:
                        BitmapTool.choose_v2(THIS, CHOOSE_PHOTO);
                        break;
                }
            }
        });
        adapter = new RecyclerViewAdapter<Item>(THIS, new int[]{R.layout.item_image, R.layout.item_choose_button}, items) {

            @Override
            protected void binding(ViewHolder holder, final Item o, int i) {
                if (o.getType() == 0) {
                    ImageView itemImage = holder.findViewById(R.id.item_image);
                    ImageView deleteButton = holder.findViewById(R.id.delete_button);
                    try {
                        itemImage.setImageBitmap(o.getBitmap());
                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                items.remove(o);
                                notifyDataSetChanged();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    countView.setText(Html.fromHtml("<span>添加图片<font color=\"#529CEB\">(" + (items.size() - 1) + "/3)</font></span>"));
                    holder.setOnItemClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (items.size() > 3) {
                                TOAST("最多上传3张图片");
                                return;
                            }
                            selectorDialog.show();
                        }
                    });
                }
            }

            @Override
            public int getItemViewType(int position) {
                return items.get(position).getType();
            }
        };
    }

    @Override
    public void initView() {
        setCenterTitleContent(title);
        LinearLayoutManager layoutManager = new LinearLayoutManager(THIS);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void setListeners() {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateLast() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                imageFile = BitmapTool.open_v2(THIS, TAKE_PHOTO);
            } else imageUri = BitmapTool.open(THIS, TAKE_PHOTO);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                // 从拍照界面返回
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        imageUri = FileProvider.getUriForFile(THIS, THIS.getPackageName() + ".file_provider", imageFile);
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    intent.putExtra("noFaceDetection", false);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, CROP_PHOTO);
                }

                break;
            case CROP_PHOTO:
                // 从裁剪界面返回
                if (resultCode == RESULT_OK) {
                    Item item = new Item();
                    item.setType(0);
                    item.setBitmap(BitmapTool.adjustImage(THIS, imageUri.getEncodedPath()));
                    items.add(0, item);
                    adapter.notifyDataSetChanged();
                }

                break;
            case CHOOSE_PHOTO:
                // 选择图片返回
                if (resultCode == RESULT_OK) {
                    Item item = new Item();
                    item.setType(0);
                    Bitmap bitmap = BitmapTool.adjustImage(THIS, GetImagePath.get(THIS, data.getData()));
                    item.setBitmap(bitmap);
                    items.add(0, item);
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 上传信息
     *
     * @param listener
     */
    private void upload(SyncReference.CompletionListener listener) {
        if (feedbackSlugger.isEmpty()) {
            TOAST("请输入内容");
            return;
        }
        Feedback feedback = new Feedback();
        feedback.setDescribe(feedbackSlugger.getText());
        List<String> images = new ArrayList<>();
        for (Item item : items) {
            images.add(BitmapTool.bitmap2Base64(item.getBitmap()));
        }
        feedback.setImage(images);
        LoadingDialog.show(THIS);
        SyncReference reference;
        if ("投诉与举报".equals(title)) {
            reference = App.getReference(URI_REPORT + App.getCurrentUser().getUid());
        } else {
            reference = App.getReference(URI_FEEDBACK + App.getCurrentUser().getUid());
        }
        reference = reference.push();
        feedback.setId(reference.getKey());
        reference.setValue(feedback, listener);
    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public class Item implements Gradable {

        private static final long serialVersionUID = -3668974534034032003L;

        private Bitmap bitmap;
        private int type;

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

}
