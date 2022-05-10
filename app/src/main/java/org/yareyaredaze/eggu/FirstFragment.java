package org.yareyaredaze.eggu;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.yareyaredaze.eggu.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private MediaRecorder mediaRecorder;
    private FragmentFirstBinding binding;
    private boolean status = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile("/data/local/tmp"); // TODO: test
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        binding.egguBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !status ) {
                    mediaRecorder.start();
                    binding.egguTxt.setTextColor(Color.rgb(89, 9, 181));
                    binding.egguTxt.setText("kokowai");
                } else {
                    mediaRecorder.stop();
                    binding.egguTxt.setText("");
                }
                status = !status;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}