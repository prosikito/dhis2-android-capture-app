package org.dhis2.usescases.main.program;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemProgramModelBinding;
import org.dhis2.utils.Period;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

public class ProgramModelAdapter extends RecyclerView.Adapter<ProgramModelHolder> {
    private final ProgramContract.ProgramPresenter programPresenter;
    private Period currentPeriod;
    private final List<ProgramViewModel> programList;

    ProgramModelAdapter(ProgramContract.ProgramPresenter programPresenter, Period currentPeriod) {
        this.programPresenter = programPresenter;
        this.programList = new ArrayList<>();
        this.currentPeriod = currentPeriod;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ProgramModelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProgramModelBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_program_model, parent, false);
        return new ProgramModelHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramModelHolder holder, int position) {

        holder.bind(programPresenter, programList.get(holder.getAdapterPosition()), currentPeriod);
    }


    @Override
    public long getItemId(int position) {
        return programList.get(position).id().hashCode();
    }

    @Override
    public int getItemCount() {
        return programList.size();
    }

    public void setData(List<ProgramViewModel> data) {
//        Collections.sort(data, (o1, o2) -> o2.count() - o1.count()); //TODO: ADD ORDER TO QUERY (BY NAME OR COUNT)
        this.programList.clear();
        this.programList.addAll(data);
        notifyDataSetChanged();
    }

    public void setCurrentPeriod(Period currentPeriod) {
        this.currentPeriod = currentPeriod;
    }


}
