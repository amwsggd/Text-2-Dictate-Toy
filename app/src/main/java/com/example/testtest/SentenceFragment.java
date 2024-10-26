package com.example.testtest;

import static java.lang.Math.max;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.flexbox.FlexboxLayout;

public class SentenceFragment extends Fragment {
    private SentenceViewModel viewModel;
    public static boolean showUnderline;
    private static final String ARG_SENTENCE = "sentence";
    private static final String ARG_INPUTS = "inputs";
    private String sentence;
    private EditText[] wordInputs;
    private String[] words;
    private int currentWordIndex = 0;
    private Boolean[] vis;

    public static SentenceFragment newInstance(String sentence){
        SentenceFragment fragment = new SentenceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SENTENCE, sentence);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SentenceViewModel.class);
    }

    public void requestFocus(){
//        wordInputs[currentWordIndex].requestFocus();
        for(int i = 0; i < wordInputs.length; i ++)
            if(!vis[i]) {
                MainActivity.focusIndex = currentWordIndex = i;
                wordInputs[i].requestFocus();
                return;
            }
    }
    @Override
    public void onResume() {
        super.onResume();
//        wordInputs[currentWordIndex].requestFocus();
        requestFocus();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_sentence, container, false);
        FlexboxLayout inputContainer = view.findViewById(R.id.inputContainer);

        if(words == null) {
            sentence = getArguments().getString(ARG_SENTENCE);
            String[] words1 = sentence.split(" ");
            String tmp1 = new String();
            for (int i = 0; i < words1.length; i++) {
                String tmp = words1[i];
                tmp = tmp.replaceAll("^[^a-zA-Z0-9.]+", "");
                tmp = tmp.replaceAll("[^a-zA-Z0-9.]+$", "");
                if (tmp.length() >= 1 && tmp.matches(".*[a-zA-Z0-9].*"))
                    tmp1 += tmp + " ";

            }
            words = tmp1.split(" ");
            wordInputs = new EditText[words.length];

            vis = new Boolean[words.length];
            for (int i = 0; i < words.length; i++)
                vis[i] = false;
        }

        final String[] savedInputs1 = viewModel.getInputs().getValue();
        if (savedInputs1 == null || savedInputs1.length != words.length) {
            final String[] newInputs = new String[words.length];
            viewModel.setInputs(newInputs.clone());
        }
        final String[] savedInputs = viewModel.getInputs().getValue();
        for (int i = 0; i < words.length; i++) {
            EditText wordInput = new EditText(getContext());
            FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            if(showUnderline)
                layoutParams.setMargins(1, 8, 1, 8);
            else
                layoutParams.setMargins(5, 8, 5, 8);
            wordInput.setLayoutParams(layoutParams);
            if(showUnderline)
                wordInput.setHint("_".repeat(words[i].length()));
            else
                wordInput.setHint(" ".repeat(words[i].length()));
            wordInput.setHintTextColor(getResources().getColor(android.R.color.transparent));
            if(!showUnderline)
                wordInput.setBackgroundColor(getResources().getColor(android.R.color.transparent));
//            wordInput.setSingleLine(true);
//            wordInput.setInputType(InputType.TYPE_CLASS_TEXT);
            wordInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(words[i].length())});
            wordInput.setTag(i);

            if (savedInputs[i] != null) {
                wordInput.setText(savedInputs[i]);
            }
            wordInput.setOnFocusChangeListener((v, hasFocus) -> {
                if(hasFocus)
                    MainActivity.focusIndex = currentWordIndex = (int)wordInput.getTag();
            });
            wordInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int i = (int) wordInput.getTag();
                    String correctWord = words[i];
                    String input = s.toString();
                    SpannableString spannable = new SpannableString(input);
                    MainActivity.focusIndex = currentWordIndex = (int)wordInput.getTag();
                    for (int j = 0; j < input.length(); j++) {
                        if (j < correctWord.length()) {
                            if (input.charAt(j) == correctWord.charAt(j)) {
                                spannable.setSpan(new ForegroundColorSpan(Color.BLACK), j, j + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if (Character.toLowerCase(input.charAt(j)) == Character.toLowerCase(correctWord.charAt(j))) {
                                spannable.setSpan(new ForegroundColorSpan(Color.RED), j, j + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else {
                                spannable.setSpan(new ForegroundColorSpan(Color.RED), j, j + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        } else {
                            spannable.setSpan(new ForegroundColorSpan(Color.RED), j, j + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    wordInput.removeTextChangedListener(this);
                    wordInput.setText(spannable);
                    wordInput.setSelection(input.length());
                    wordInput.addTextChangedListener(this);

                    if(!input.isEmpty() && input.charAt((int)input.length() - 1) == ' ') {
                        wordInput.setText(input.substring(0, max(input.length() - 1, 0)));
                        moveToNextWord((int) wordInput.getTag());
                    }
                    if (input.equals(correctWord)) {
                        wordInput.setFocusable(false);
                        vis[i] = true;
                        moveToNextWord((int)wordInput.getTag());
                    }

                    savedInputs[i] = s.toString();
                    viewModel.setInputs(savedInputs.clone());
                    checkIfComplete();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            wordInput.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_DEL) {
                            handleDeleteKey((EditText) v);
//                            return true;
                        }
                    }
                    return false;
                }
            });

            if(vis[i])
                wordInput.setFocusable(false);
            wordInputs[i] = wordInput;

            inputContainer.addView(wordInput);
        }

        if (wordInputs.length > 0) {
//            currentWordIndex = 0;
//            if(wordInputs[currentWordIndex].isFocusable())
//                wordInputs[currentWordIndex].requestFocus();
            requestFocus();
        }
        return view;
    }

    private void moveToNextWord(int index) {
        MainActivity.focusIndex = currentWordIndex = index;
        for(int i = currentWordIndex + 1; i < wordInputs.length; i ++)
            if(!vis[i]) {
                MainActivity.focusIndex = currentWordIndex = i;
                wordInputs[currentWordIndex].requestFocus();
                return;
            }

        for(int i = 0; i < currentWordIndex; i ++) {
            if(!vis[i]) {
                MainActivity.focusIndex = currentWordIndex = i;
                wordInputs[currentWordIndex].requestFocus();
                return;
            }
        }
    }

    private void handleDeleteKey(EditText currentEditText) {
        MainActivity.focusIndex = currentWordIndex = (int) currentEditText.getTag();
        if(currentEditText.getText().length() == 0) {
            for (int i = currentWordIndex - 1; i >= 0; i--) {
                if (!vis[i]) {
                    MainActivity.focusIndex = currentWordIndex = i;
                    wordInputs[currentWordIndex].requestFocus();
                    wordInputs[currentWordIndex].setSelection(wordInputs[currentWordIndex].getText().length());
                    return;
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String[] inputs = new String[wordInputs.length];
        for (int i = 0; i < wordInputs.length; i++) {
            inputs[i] = wordInputs[i].getText().toString();
        }
        outState.putStringArray(ARG_INPUTS, inputs);
    }

        private void checkIfComplete() {

        for (int i = 0; i < words.length; i++) {
            if(vis[i]) continue;
            if (!wordInputs[i].getText().toString().equals(words[i])) {
                return;
            }
        }
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).moveToNextPage();
        }
    }
}
