package com.example.testtest;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private SentencePagerAdapter adapter;
    List<String> sentences = new ArrayList<>();
    List<String> ansSentences = new ArrayList<>();

    private Handler handler = new Handler();
    private Runnable runnable;
    Boolean flag = false;

    public static int focusIndex = 0;
    private Button buttonAns;
    private Button buttonWord;
    TextView tv;

    private void processInput(String text, Boolean option) {
        text = text.replaceAll("\n", " ");
        text = text.trim();
        text = text.replaceAll("(?i)" + Pattern.quote("P.M."), " --------PM-------- ");
        text = text.replaceAll("(?i)" + Pattern.quote("A.M."), " --------AM-------- ");
        text = text.replaceAll("(?i)" + Pattern.quote("P.M"), " --------PMM-------- ");
        text = text.replaceAll("(?i)" + Pattern.quote("A.M"), " --------AMM-------- ");
        text = text.replaceAll("(?i)" + Pattern.quote("B.C."), " --------BC-------- ");
        text = text.replaceAll("(?i)" + Pattern.quote("B.C"), " --------BCC-------- ");
        text = text.replaceAll("(?i)" + Pattern.quote("A.D."), " --------AD-------- ");
        text = text.replaceAll("(?i)" + Pattern.quote("A.D"), " --------ADD-------- ");
        text = text.replaceAll(Pattern.quote("Mrs."), " --------MRS-------- ");
        text = text.replaceAll(Pattern.quote("Mr."), " --------MR-------- ");
        if(!text.matches(".*[.?!]$"))
            text += ".";
        Pattern pattern;
        if(option)
            pattern = Pattern.compile("([^.!?;]*[.!?;](\"?)('?))");
        else
            pattern = Pattern.compile("([^.!?,;]*[.!?,;](\"?))('?)");
        Matcher matcher = pattern.matcher(text);

        while(matcher.find()) {
            String tmp = matcher.group();
            tmp = tmp.replaceAll(Pattern.quote("--------PM--------"), "P.M.");
            tmp = tmp.replaceAll(Pattern.quote("--------AM--------"), "A.M.");
            tmp = tmp.replaceAll(Pattern.quote("--------PMM--------"), "P.M");
            tmp = tmp.replaceAll(Pattern.quote("--------AMM--------"), "A.M");
            tmp = tmp.replaceAll(Pattern.quote("--------BC--------"), "B.C.");
            tmp = tmp.replaceAll(Pattern.quote("--------BCC--------"), "B.C");
            tmp = tmp.replaceAll(Pattern.quote("--------AD--------"), "A.D.");
            tmp = tmp.replaceAll(Pattern.quote("--------ADD--------"), "A.D");
            tmp = tmp.replaceAll(Pattern.quote("--------MRS--------"), "Mrs.");
            tmp = tmp.replaceAll(Pattern.quote("--------MR--------"), "Mr.");
            tmp = tmp.trim().replaceAll("\\s+", " ");
            ansSentences.add(tmp);
        }
        // 去除所有无关字符
        text = text.replaceAll("([.,;!?])", "$1 ");
        text = text.replaceAll("[^-a-zA-Z0-9.,?!\"' ]", " ");
        // 用句号分隔句子
        String[] ttt;
        if(option)
            ttt = text.split("[.?!;]\\s*");
        else
            ttt = text.split("[.?!,;]\\s*");
        for(String s : ttt)
            if(s.length() >= 1 && s.matches(".*[a-zA-Z0-9].*")) {
                s = s.replaceAll(Pattern.quote("--------PM--------"), "P.M.");
                s = s.replaceAll(Pattern.quote("--------AM--------"), "A.M.");
                s = s.replaceAll(Pattern.quote("--------PMM--------"), "P.M");
                s = s.replaceAll(Pattern.quote("--------AMM--------"), "A.M");
                s = s.replaceAll(Pattern.quote("--------BC--------"), "B.C.");
                s = s.replaceAll(Pattern.quote("--------BCC--------"), "B.C");
                s = s.replaceAll(Pattern.quote("--------AD--------"), "A.D.");
                s = s.replaceAll(Pattern.quote("--------ADD--------"), "A.D");
                s = s.replaceAll(Pattern.quote("--------MRS--------"), "Mrs.");
                s = s.replaceAll(Pattern.quote("--------MR--------"), "Mr.");
                sentences.add(s);
            }
    }

    private void showInputDialog1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("是否显示单词下划线？");
        builder.setPositiveButton("显示", (dialog, which) -> {
            SentenceFragment.showUnderline = true;
            process();
        });

        builder.setNegativeButton("不显示", (dialog, which) -> {
            SentenceFragment.showUnderline = false;
            process();
        });
        builder.setCancelable(false);
        builder.show();
    }
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入句子");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);

        builder.setPositiveButton("标准句划分", (dialog, which) -> {
            String text = input.getText().toString();
            processInput(text, true);
//            process();
            showInputDialog1();
        });
        builder.setNegativeButton("短句划分", (dialog, which) -> {
            String text = input.getText().toString();
            processInput(text, false);
//            process();
            showInputDialog1();
        });
//        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.setCancelable(false);
        builder.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showInputDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void process() {
        buttonWord = findViewById(R.id.btd1);
        buttonAns = findViewById(R.id.btd);
        tv = findViewById(R.id.tv);

        viewPager = findViewById(R.id.viewPager);
        adapter = new SentencePagerAdapter(getSupportFragmentManager());

        buttonWord.setOnClickListener(v -> {

            String sentence = sentences.get(viewPager.getCurrentItem());
            String[] words1 = sentence.split(" ");
            String tmp1 = new String();
            for(int i = 0; i < words1.length; i ++) {
                String tmp = words1[i];
                tmp = tmp.replaceAll("^[^a-zA-Z0-9]+", "");
                tmp = tmp.replaceAll("[^a-zA-Z0-9]+$", "");
                if (tmp.length() >= 1 && tmp.matches(".*[a-zA-Z0-9].*"))
                    tmp1 += tmp + " ";
            }
            String[] words = tmp1.split(" ");
            updateTextView((String)words[focusIndex], 5000);
        });
        buttonAns.setOnClickListener(v->{
            updateTextView((String)ansSentences.get(viewPager.getCurrentItem()), 10000);
        });

        for (String sentence : sentences) {
            adapter.addFragment(SentenceFragment.newInstance(sentence.trim()));
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {      }

            @Override
            public void onPageSelected(int position) {
                Fragment fragment = adapter.getItem(position);
                if (fragment.getView() != null) {
                    if (fragment instanceof SentenceFragment) {
                        ((SentenceFragment) fragment).requestFocus();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {      }
        });

        viewPager.setAdapter(adapter);
    }

    private void auto_focus() {
        int position = viewPager.getCurrentItem();
        Fragment fragment = adapter.getItem(position);
        if (fragment.getView() != null) {
            if (fragment instanceof SentenceFragment) {
                ((SentenceFragment) fragment).requestFocus();
            }
        }
    }

    private void updateTextView(String s, int delayTime) {
        if(runnable != null) {
            handler.removeCallbacks(runnable);
        }
        tv.setText(s);

        runnable = new Runnable() {
            @Override
            public void run() {
                tv.setText("answer is going to be here");
            }
        };

        handler.postDelayed(runnable, delayTime);
    }

    public void moveToNextPage() {
        int nextItem = viewPager.getCurrentItem() + 1;
        if (nextItem < adapter.getCount()) {
            viewPager.setCurrentItem(nextItem);
        }
    }
}

class SentencePagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();

    public SentencePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    public int getCount() {
        return fragmentList.size();
    }

    public void addFragment(Fragment fragment) {
        fragmentList.add(fragment);
    }
}
