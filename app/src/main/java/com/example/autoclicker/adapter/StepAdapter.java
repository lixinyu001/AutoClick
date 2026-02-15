package com.example.autoclicker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.autoclicker.model.ClickScript;

import java.util.List;

/**
 * 步骤列表适配器
 */
public class StepAdapter extends BaseAdapter {
    private Context context;
    private List<ClickScript.ClickStep> steps;

    public StepAdapter(Context context, List<ClickScript.ClickStep> steps) {
        this.context = context;
        this.steps = steps;
    }

    @Override
    public int getCount() {
        return steps.size();
    }

    @Override
    public Object getItem(int position) {
        return steps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                R.layout.item_step, parent, false);
            holder = new ViewHolder();
            holder.tvStepNumber = convertView.findViewById(R.id.tv_step_number);
            holder.tvStepType = convertView.findViewById(R.id.tv_step_type);
            holder.tvCoordinates = convertView.findViewById(R.id.tv_coordinates);
            holder.tvDescription = convertView.findViewById(R.id.tv_description);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        ClickScript.ClickStep step = steps.get(position);
        
        holder.tvStepNumber.setText(String.valueOf(position + 1));
        holder.tvStepType.setText(step.getType().getDisplayName());
        holder.tvCoordinates.setText(String.format("(%.0f, %.0f)", step.getX(), step.getY()));
        holder.tvDescription.setText(step.getDescription());
        
        return convertView;
    }

    static class ViewHolder {
        TextView tvStepNumber;
        TextView tvStepType;
        TextView tvCoordinates;
        TextView tvDescription;
    }
}