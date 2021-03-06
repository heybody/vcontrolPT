package com.vcontrol.vcontroliot.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.vcontrol.vcontroliot.R;
import com.vcontrol.vcontroliot.model.FtpImageModel;
import com.vcontrol.vcontroliot.util.ConfigParams;
import com.vcontrol.vcontroliot.util.PhotoView;
import com.vcontrol.vcontroliot.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FtpImageAdapter extends BaseAdapter
{

    protected static final String TAG = FtpImageAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private List<FtpImageModel> dataList = new ArrayList<>();
    private Context context;
    private ImageLoader imageLoader;


    public FtpImageAdapter(Context context, ImageLoader imageLoader)
    {
        this.context = context;
        this.imageLoader = imageLoader;
        inflater = LayoutInflater.from(context);
    }

    public void updateData(List<FtpImageModel> imageList)
    {
        if (imageList != null)
        {
            this.dataList.clear();
            this.dataList.addAll(imageList);
            this.notifyDataSetChanged();
        }

    }

    //关键方法，更新指定Item
    public void updateItem(int refreshItemPostion, final View view, String imagePath)
    {
        if (refreshItemPostion > dataList.size())
        {
            ToastUtil.showToastLong(context.getString(R.string.Out_of_list_range));
            return;
        }

        if (TextUtils.isEmpty(imagePath))
        {
            return;
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.ftp_imageview);

        File file = new File(imagePath);
        if (file.exists())
        {
            imageLoader.displayImage("file:///" + imagePath, imageView);
        }
        else
        {
            imageView.setImageResource(R.mipmap.pictures_no);
        }
    }

    @Override
    public int getCount()
    {
        if (dataList == null || dataList.size() == 0)
        {
            return 0;
        }
        else
        {
            return dataList.size();
        }
    }

    @Override
    public Object getItem(int position)
    {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.item_ftp_image, null);
            holder = new ViewHolder();
            holder.nameTextView = (TextView) convertView.findViewById(R.id.image_name);
            holder.ftpImage = (PhotoView) convertView.findViewById(R.id.ftp_imageview);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        FtpImageModel ftpImageModel = dataList.get(position);

        if (ftpImageModel.isFile())
        {
            File file = new File(ConfigParams.FtpImagePath + ftpImageModel.getName());
            if (file.exists())
            {
                imageLoader.displayImage("file:///" + ConfigParams.FtpImagePath + ftpImageModel.getName(), holder.ftpImage);
            }
            else
            {
                holder.ftpImage.setImageResource(R.mipmap.pictures_no);
            }
        }
        else
        {
            holder.ftpImage.setImageResource(R.mipmap.folder_icon);
        }

        holder.nameTextView.setText(ftpImageModel.getName());
        return convertView;
    }


    class ViewHolder
    {
        TextView nameTextView;
        PhotoView ftpImage;
    }

}
