package com.yovenny.stickview.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yovenny.stickview.R;
import com.yovenny.stickview.model.WaterMarkItem;
import com.yovenny.stickview.util.BitmapUtil;
import com.yovenny.stickview.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;


public class WaterAdapter extends RecyclerView.Adapter<WaterAdapter.ViewHolder> implements View.OnClickListener {
    private Context mContext;
    private List<WaterMarkItem> mItems;

    private List<CheckProperty> checkPropertyList;

    public void addItemCheck(int lastCategroyId, int lastCheck) {
        int check = checkContain(lastCategroyId, lastCheck);
        if (check < 0) {
            checkPropertyList.add(new CheckProperty(lastCategroyId, lastCheck));
            notifyDataSetChanged();
        }
    }

    public void removeItemCheck(int lastCategroyId, int lastCheck) {
        int check = checkContain(lastCategroyId, lastCheck);
        if (check > -1) {
            checkPropertyList.remove(check);
            notifyDataSetChanged();
        }
    }

    public WaterAdapter(Context context, List<WaterMarkItem> items) {
        mContext = context;
        mItems = items;
        checkPropertyList = new ArrayList<>();
    }

    //创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_water, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        return vh;
    }

    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(WaterAdapter.ViewHolder holder, int position) {
        //        holder.filterText.setText((CharSequence) getItem(position));
        final WaterMarkItem item = mItems.get(position);
        //将数据保存在itemView的Tag中，以便点击时进行获取
        holder.itemView.setTag(position);
//      holder.filterImage.setImageResource(PhotoProcessing.FILTERS_DRAWABLES[position]);
        //循环判断是否存在，选中。
        int check = checkContain(item.getCategoryId(), position);
        if (check > -1) {
            holder.filterBorderImage.setVisibility(View.VISIBLE);
            holder.filterCoverImage.setVisibility(View.VISIBLE);
//            holder.filterImage.setBackgroundResource(R.color.process_item_check_bg_color);
        } else {
            holder.filterBorderImage.setVisibility(View.GONE);
            holder.filterCoverImage.setVisibility(View.GONE);
//            holder.filterImage.setBackgroundResource(R.color.process_item_bg_color);
        }
        holder.filterText.setText(item.getName() + "");
        Bitmap tempBitamp = BitmapUtil.getSampledBitmap(item.getSavePath(), DensityUtil.dip2px(mContext, 150), DensityUtil.dip2px(mContext, 150));
        if (holder.filterRelative.getVisibility() != View.VISIBLE) {
            holder.filterRelative.setVisibility(View.VISIBLE);
        }
        holder.filterImage.setImageBitmap(tempBitamp);
    }


    //获取数据的数量
    @Override
    public int getItemCount() {
        if (mItems == null || mItems.size() == 0) {
            return 0;
        }
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }



    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView filterText;
        ImageView filterImage;
        ImageView filterBorderImage;
        ImageView filterCoverImage;
        ImageView filterTipImage;
        RelativeLayout filterLayout;
        RelativeLayout filterRelative;

        public ViewHolder(View convertView) {
            super(convertView);
            filterText = (TextView) convertView.findViewById(R.id.txtFilter);
            filterImage = (ImageView) convertView.findViewById(R.id.imgFilter);
            filterBorderImage = (ImageView) convertView.findViewById(R.id.imgfilterBorder);
            filterCoverImage = (ImageView) convertView.findViewById(R.id.imgfilterCover);
            filterTipImage = (ImageView) convertView.findViewById(R.id.imgFilter_new_tip);
            filterLayout = (RelativeLayout) convertView.findViewById(R.id.imgFilter_layout);
            filterRelative = (RelativeLayout) convertView.findViewById(R.id.imgFilter_relative);
        }
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public void notifyDataSetChange(List<WaterMarkItem> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    public int checkContain(int lastCategroyId, int lastCheck) {
        for (int i = 0; i < checkPropertyList.size(); i++) {
            CheckProperty property = checkPropertyList.get(i);
            if (property.lastCategroyId == lastCategroyId && property.lastCheck == lastCheck) {
                return i;
            }
        }
        return -1;
    }

    public class CheckProperty implements Comparable<CheckProperty> {
        int lastCheck;
        int lastCategroyId;

        public CheckProperty(int lastCategroyId, int lastCheck) {
            this.lastCheck = lastCheck;
            this.lastCategroyId = lastCategroyId;
        }

        @Override
        public int compareTo(CheckProperty another) {
            if (lastCheck == another.lastCheck && lastCategroyId == another.lastCategroyId) {
                return 0;
            } else {
                return -1;
            }

        }
    }

    //构建recycleView 的点击事件
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , int position);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v,(int)v.getTag());
        }
    }

    public void addItem(WaterMarkItem data, int position) {
        mItems.add(position, data);
        notifyItemInserted(position); //Attention!
    }

    public void removeItem(WaterMarkItem data) {
        int position = mItems.indexOf(data);
        mItems.remove(position);
        notifyItemRemoved(position);//Attention!
    }

}
