package cc.hicore.hook.stickerPanel.MainItemImpl;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cc.hicore.Env;
import cc.hicore.Utils.ContextUtils;
import cc.hicore.Utils.HttpUtils;
import cc.hicore.hook.stickerPanel.Hooker.StickerPanelEntryHooker;
import cc.hicore.hook.stickerPanel.ICreator;
import cc.hicore.hook.stickerPanel.LocalDataHelper;
import cc.hicore.hook.stickerPanel.MainPanelAdapter;
import cc.hicore.hook.stickerPanel.RecentStickerHelper;
import cc.hicore.message.chat.SessionUtils;
import cc.hicore.message.common.MsgSender;
import cc.ioctl.util.HostInfo;
import cc.ioctl.util.LayoutHelper;
import com.bumptech.glide.Glide;
import com.lxj.xpopup.XPopup;
import com.tencent.qqnt.kernel.nativeinterface.Contact;
import de.robv.android.xposed.XposedBridge;
import io.github.qauxv.R;
import java.io.File;
import java.util.HashSet;
import java.util.List;

public class RecentStickerImpl implements MainPanelAdapter.IMainPanelItem {
    ViewGroup cacheView;
    Context mContext;
    LinearLayout panelContainer;
    HashSet<ImageView> cacheImageView = new HashSet<>();
    TextView tv_title;

    List<RecentStickerHelper.RecentItemInfo> items;

    public RecentStickerImpl(Context mContext) {
        this.mContext = mContext;
        items = RecentStickerHelper.getAllRecentRecord();

        cacheView = (ViewGroup) View.inflate(mContext, R.layout.sticker_panel_plus_pack_item, null);
        tv_title = cacheView.findViewById(R.id.Sticker_Panel_Item_Name);
        panelContainer = cacheView.findViewById(R.id.Sticker_Item_Container);
        tv_title.setText("最近使用");

        View setButton = cacheView.findViewById(R.id.Sticker_Panel_Set_Item);
        setButton.setOnClickListener(v-> new XPopup.Builder(ContextUtils.getFixContext(mContext))
                .asConfirm("提示", "是否要清除最近的表情记录?", RecentStickerHelper::cleanAllRecentRecord)
                .show());

        try {
            LinearLayout itemLine = null;
            for (int i = 0; i < items.size(); i++) {
                RecentStickerHelper.RecentItemInfo item = items.get(items.size() - i - 1);
                if (i % 5 == 0) {
                    itemLine = new LinearLayout(mContext);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.bottomMargin = LayoutHelper.dip2px(mContext, 16);
                    panelContainer.addView(itemLine, params);
                }
                if (item.type == 2) {
                    itemLine.addView(getItemContainer(mContext, item.url, i % 5, item));
                } else if (item.type == 1) {
                    itemLine.addView(getItemContainer(mContext, LocalDataHelper.getLocalItemPath(item), i % 5, item));
                }

            }
        } catch (Exception e) {
            XposedBridge.log(Log.getStackTraceString(e));
        }
    }

    @Override
    public View getView(ViewGroup parent) {
        onViewDestroy(null);
        notifyDataSetChanged();

        return cacheView;
    }

    private void notifyDataSetChanged() {
        for (ImageView img : cacheImageView) {
            String coverView = (String) img.getTag();
            if (coverView.startsWith("http://") || coverView.startsWith("https://")) {
                Glide.with(HostInfo.getApplication()).load(coverView).into(img);
            } else {
                if (new File(coverView+"_thumb").exists()){
                    Glide.with(HostInfo.getApplication()).load(coverView+"_thumb").into(img);
                }else {
                    Glide.with(HostInfo.getApplication()).load(coverView).into(img);
                }

            }
        }
    }

    private View getItemContainer(Context context, String coverView, int count, RecentStickerHelper.RecentItemInfo item) {
        int item_size = LayoutHelper.getScreenWidth(context) / 6;
        int item_distance = (LayoutHelper.getScreenWidth(context) - item_size * 5) / 4;

        ImageView img = new ImageView(context);
        cacheImageView.add(img);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(item_size, item_size);
        if (count > 0) params.leftMargin = item_distance;
        img.setLayoutParams(params);

        img.setTag(coverView);
        img.setOnClickListener(v -> {
            if (coverView.startsWith("http://") || coverView.startsWith("https://")) {
                HttpUtils.ProgressDownload(coverView, Env.app_save_path + "Cache/" + coverView.substring(coverView.lastIndexOf("/")), () -> {
                    Contact contact = SessionUtils.AIOParam2Contact(StickerPanelEntryHooker.AIOParam);
                    if (contact != null){
                        MsgSender.send_pic_by_contact(contact,
                                Env.app_save_path + "Cache/" + coverView.substring(coverView.lastIndexOf("/")));

                        RecentStickerHelper.addPicItemToRecentRecord(item);
                    }

                }, mContext);
                ICreator.dismissAll();

            } else {

                Contact contact = SessionUtils.AIOParam2Contact(StickerPanelEntryHooker.AIOParam);
                if (contact != null){
                    MsgSender.send_pic_by_contact(contact,
                            coverView);
                    RecentStickerHelper.addPicItemToRecentRecord(item);
                    ICreator.dismissAll();
                }

            }
        });

        return img;

    }

    @Override
    public void onViewDestroy(ViewGroup parent) {
        for (ImageView img : cacheImageView) {
            img.setImageBitmap(null);
            Glide.with(HostInfo.getApplication()).clear(img);
        }
    }

    @Override
    public long getID() {
        return 0;
    }

    @Override
    public void notifyViewUpdate0() {

    }
}
