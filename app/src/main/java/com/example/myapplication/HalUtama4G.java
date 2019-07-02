package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
//import android.support.v4.app.ActivityCompat;
import android.telephony.*;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.*;

import au.com.bytecode.opencsv.CSVWriter;
import org.json.JSONException;
import org.json.JSONObject;

public class HalUtama4G extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;


    private static final String KEY_TIMECAPTURED = "timecature";
    private static final String KEY_RSRP = "srsrp";
    private static final String KEY_RSRQ = "srsrq";
    private static final String KEY_CELLPCI = "scellPci";
    private static final String KEY_CQI = "scqi";
    private static final String KEY_USERNAME = "username";
    private String data4g_url = "https://cellin-test1.000webhostapp.com/koneksi/data4g.php";


    protected SignalStrengthListener signalStrengthListener;
    protected TelephonyManager tm;
    protected List<CellInfo> cellInfoList;

    protected String[] parts;
    protected String ltestr;
    protected int cellPci = 0, mcc = 0, mnc = 0;

    protected Timer timerCapture;
    protected int numDataPoints = 0;
    protected List<String[]> data;
    protected String csvFilename;
//    String publicFilePath = null;
//    String publicFileName = null;

    protected String srsrp, srsrq, scqi, scellPci, smcc, smnc, snetworkOperator, usernameIntent4G;

    protected boolean isRecording = false;
    protected boolean animateRecording = true;
    protected TextView tvRecPau;
    protected ImageView recImage;
    protected Animation recAnimation;

    Context context = this;

    protected PopupWindow popShim;
    protected LineChart mChart;

    protected Button btnStartRecording, btnPauseResumeRecording, btnStopRecording;
    protected TextView tvSignalStrength, tvRSRP, tvRSRQ, tvPCI, tvCQI, tvNetworkOperator, tvDataPoints;
    protected String stringNetworkOperator, stringMccMnc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hal_utama_4g);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Intent intent = new Intent();
        usernameIntent4G = intent.getStringExtra("usernameIntent4G");
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        final int min = 1;
        final int max = 100;
        final int Id = new Random().nextInt((max - min) + 1) + min;

        Button btn;
        GradientDrawable gd;

        btnStartRecording = (Button) findViewById(R.id.start_recording);
        gd = (GradientDrawable) (btnStartRecording.getBackground());
        gd.setColor(getResources().getColor(R.color.button_start_recording));

        btnPauseResumeRecording = (Button) findViewById(R.id.pause_resume_recording);
        gd = (GradientDrawable) (btnPauseResumeRecording.getBackground());
        gd.setColor(getResources().getColor(R.color.button_pause_resume_recording));

        btnStopRecording = (Button) findViewById(R.id.stop_recording);
        gd = (GradientDrawable) (btnStopRecording.getBackground());
        gd.setColor(getResources().getColor(R.color.button_stop_recording));

        recImage = (ImageView) findViewById(R.id.imgRecording);




        animateRecording = true;

        // The user can tap the blinking REC image to make
        // it stop blinking, if they find it annoying.
        recImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    return;
                }
                animateRecording = !animateRecording;
                if (animateRecording) {
                    recImage.startAnimation(recAnimation);
                } else {
                    recImage.clearAnimation();
                }
            }
        });

        recAnimation = new AlphaAnimation(1, 0);
        recAnimation.setDuration(750);
        recAnimation.setInterpolator(new LinearInterpolator());
        recAnimation.setRepeatCount(Animation.INFINITE);
        recAnimation.setRepeatMode(Animation.REVERSE);

        tvRSRP = (TextView) findViewById(R.id.numRsrp);
        tvRSRQ = (TextView) findViewById(R.id.numRsrq);
        tvPCI = (TextView) findViewById(R.id.numPCI);
        tvCQI = (TextView) findViewById(R.id.numCQI);
        tvNetworkOperator = (TextView) findViewById(R.id.numNetworkOperator);

        tvRecPau = (TextView) findViewById(R.id.rec_pau);
        tvDataPoints = (TextView) findViewById(R.id.numDataPoints);
        tvSignalStrength = (TextView) findViewById(R.id.signalValue);

        updateSignalStrengthText(-141); // initialize with 'Poor'

        // Start the signal strength listener
        signalStrengthListener = new SignalStrengthListener();
        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public void onStartRecording(View v) {

        btnStartRecording.setVisibility(View.GONE);
        btnPauseResumeRecording.setVisibility(View.VISIBLE);
        btnStopRecording.setVisibility(View.VISIBLE);

        btnPauseResumeRecording.setText("Pause");

        tvRecPau.setVisibility(View.VISIBLE);
        tvRecPau.setText("REC");

        recImage.setVisibility(View.VISIBLE);
        recImage.setImageResource(R.drawable.recording);

        if (animateRecording) {
            recImage.startAnimation(recAnimation);
        }

        ((TextView) findViewById(R.id.filenameLabel)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.filenameValue)).setVisibility(View.VISIBLE);

        String startDate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        csvFilename = startDate;
        csvFilename = csvFilename.replace(' ', '_').replace(",", "");
        ((TextView) findViewById(R.id.filenameValue)).setText(csvFilename);

        data = new ArrayList<String[]>();

        isRecording = true;

        createDataCaptureTimer();
    }

    protected void createDataCaptureTimer() {

        numDataPoints = 0;

        timerCapture = new Timer();
        timerCapture.scheduleAtFixedRate(new TimerTask() {

            int rsrp, rsrq, cqi;

            @Override
            public void run() {

                if (parts == null || parts.length < 13) {
                    return;
                }

                rsrp = Integer.parseInt(parts[9]);
                rsrq = Integer.parseInt(parts[10]);
                cqi = Integer.parseInt(parts[12]);



                if (rsrp == 2147483647) {
                    rsrp = -141;
                }
                if (rsrq == 2147483647) {
                    rsrq = -30;
                }
                if (cqi == 2147483647) {
                    cqi = -20;
                }

                srsrp = String.valueOf(rsrp);
                srsrq = String.valueOf(rsrq);
                scqi = String.valueOf(cqi);
                scellPci = String.valueOf(cellPci);
                smcc = String.valueOf(mcc);
                smnc = String.valueOf(mnc);
                snetworkOperator = smcc + smnc;

                switch (snetworkOperator) {
                    case "51010":
                        stringNetworkOperator = "Telkomsel";
                        break;
                    case "51011":
                        stringNetworkOperator = "XL";
                        break;
                    case "51001":
                        stringNetworkOperator = "Indosat";
                        break;
                    case "51089":
                        stringNetworkOperator = "Tri";
                        break;
                    case "51009":
                        stringNetworkOperator = "SmartFren";
                        break;
                    default:
                        stringNetworkOperator = "Reading";
                        break;
                }


                if (isRecording) {
                    String timeCapture = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                    data.add(new String[]{timeCapture, srsrp, srsrq, scellPci, scqi});
                    ++numDataPoints;
                    JSONObject request = new JSONObject();
                    try {
                        //Populate the request parameters
                        request.put(KEY_USERNAME,usernameIntent4G);
                        request.put(KEY_TIMECAPTURED, timeCapture);
                        request.put(KEY_RSRP, srsrp);
                        request.put(KEY_RSRQ, srsrq);
                        request.put(KEY_CELLPCI,scellPci);
                        request.put(KEY_CQI,scqi);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                            (Request.Method.POST, data4g_url, request, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String key = response.getString("status");
                                        //Check if user got registered successfully
                                        if (key.equals("0")) {
                                            //Set the user session

                                        }else if(key.equals("1")){
                                            //Display error message if username is already existsing


                                        }else{


                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();

                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    //Display error message whenever an error occurs
                                    Toast.makeText(getApplicationContext(),
                                            error.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });


                    // Access the RequestQueue through your singleton class.
                    VolleyController.getInstance(HalUtama4G.this).addToRequestQueue(jsArrayRequest);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        updateSignalStrengthText(rsrp);

                        tvRSRP.setText(srsrp);
                        tvRSRQ.setText(srsrq);
                        tvPCI.setText(scellPci);
                        tvCQI.setText(scqi);
                        tvNetworkOperator.setText(stringNetworkOperator);
                        tvDataPoints.setText(String.valueOf(numDataPoints));


                    }
                });
            }
        }, 0, 1000);
    }

    protected void updateSignalStrengthText(int rsrp) {
        if (rsrp >= -70) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_excellent));
        } else if (-71 > rsrp && rsrp >= -80) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_good));
        } else if (-81 > rsrp && rsrp >= -115) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_fair));
        } else {
            tvSignalStrength.setText(getResources().getString(R.string.signal_poor));
        }
    }

    public void onPauseResumeRecording(View v) {

        if (isRecording) {
            btnPauseResumeRecording.setText("Resume");
            tvRecPau.setText("PAU");
            recImage.setImageResource(R.drawable.recording_paused);
            recImage.clearAnimation();

            isRecording = false;
        } else {
            btnPauseResumeRecording.setText("Pause");
            tvRecPau.setText("REC");
            recImage.setImageResource(R.drawable.recording);

            if (animateRecording) {
                recImage.startAnimation(recAnimation);
            }

            isRecording = true;
        }
    }

    public void onStopRecording(View view) {

        isRecording = false;

        timerCapture.cancel();
        timerCapture.purge();
        timerCapture = null;


        // When switching between the Display Line Graph popup and the
        // Display Grade popup, there is a very brief flash of the
        // Start Recording page, as one popup is dismissed and the other
        // one is constructed.
        // So, to prevent the flashing, we create a shim popup that
        // simply covers the entire screen in the background color
        // and obscures the Start Recording page; and then the
        // Display Grade/Line Graph popus are displayed on top
        // of the shim.
        // Then, when the user clicks New Recording, we dismiss the
        // shim so that the Start Recording view is once again
        // visible.

        View viewShim = view.inflate(HalUtama4G.this, R.layout.layout_shim, null);


        popShim = new PopupWindow(
                viewShim,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                true);

        popShim.showAtLocation(viewShim,
                Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL,
                0, 0);

        onDisplayGrade(view);

        // We want the Display Grade popup window to get fully rendered
        // before we change the display states for the buttons on the
        // Start Recording view; otherwise, the user can see
        // the updates to the buttons momentarily before the popup
        // window is finished rendering.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnStartRecording.setVisibility(View.VISIBLE);
                btnPauseResumeRecording.setVisibility(View.GONE);
                btnStopRecording.setVisibility(View.GONE);

                tvRecPau.setVisibility(View.INVISIBLE);
                recImage.setVisibility(View.INVISIBLE);
                recImage.clearAnimation();
            }
        }, 1000);

        writeCSV();
    }

    protected void writeCSV() {

        // Write CSV file in a background thread.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                CSVWriter writer;
                String[] headers = "Time, RSRP, RSRQ, PCI, CQI".split(",");
                try {
                    File file = new File(getExternalFilesDir(null), csvFilename + ".csv");
                    writer = new CSVWriter(new FileWriter(file, true), ',');
                    writer.writeNext(headers);

                    writer.writeAll(data);
                    writer.flush();
                    writer.close();
                    Toast.makeText(HalUtama4G.this, "CSV file written", Toast.LENGTH_SHORT).show();
//                    Uri path = Uri.fromFile(file);
//                    publicFilePath= path.getAbsolutePath();
//                    publicFileName = file.getPath()
//                    dialog = ProgressDialog.show(HalUtama4G.this,"","Uploading file...", true);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            uploadFile();
//                        }
//                    })


                } catch (IOException e) {
                    Log.d("CSV Writer", "Error writing CSV file : " + e);
                    Toast.makeText(HalUtama4G.this, "Error writing CSV file", Toast.LENGTH_SHORT).show();
                }

            }
        }, 0);
    }

//    public void uploadFile(String sourceFileUri){
//        String filename = publicFilePath;
//        HttpURLConnection conn = null;
//        DataOutputStream dataOutputStream = null;
//        String lineEnd ="\r\n";
//        String twoHyphens = "--";
//        String boundary = "*****";
//        int bytesRead, bytesAvailable, bufferSize;
//        byte[] buffer;
//        int maxBufferSize = 1*1024*1024;
//        File sourceFile = new File(publicFilePath);
//
//        if (!sourceFile.isFile()){
//            if (dialog )
//        }
//    }

    protected void onDisplayGrade(final View view) {

        final View viewGrade = view.inflate(HalUtama4G.this, R.layout.layout_grade, null);


        final PopupWindow pop = new PopupWindow(
                viewGrade,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                true);

        pop.showAtLocation(viewGrade,
                Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL,
                0, 0);

        ((TextView) viewGrade.findViewById(R.id.filenameValue)).setText(csvFilename);

        Button btn;
        GradientDrawable gd;

        btn = (Button) viewGrade.findViewById(R.id.newRecording);
        gd = (GradientDrawable) (btn.getBackground());
        gd.setColor(getResources().getColor(R.color.button_start_recording));

        btn = (Button) viewGrade.findViewById(R.id.displayGraph);
        gd = (GradientDrawable) (btn.getBackground());
        gd.setColor(getResources().getColor(R.color.button_start_recording));

        ((Button) viewGrade.findViewById(R.id.displayGraph)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                onDisplayGraph(view);
            }
        });
        ((Button) viewGrade.findViewById(R.id.newRecording)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                onNewRecording(view);
            }
        });

        // Process collected data ---------------------------------------------

        double top, mid, low;
        double topProb, midProb, lowProb, grade;

        top = mid = low = 0;

        for (String[] dataVals : data) {
            int rsrp = Integer.valueOf(dataVals[1]);

            if (rsrp >= -70) {
                top++;
            } else if (rsrp <= -71 && rsrp >= -80) {
                mid++;
            } else {
                low++;
            }
        }

        topProb = (top / numDataPoints);
        midProb = (mid / numDataPoints);
        lowProb = (low / numDataPoints);

        grade = 1 * lowProb + 5.5 * midProb + 10 * topProb;

        TextView gradeLabel = (TextView) viewGrade.findViewById(R.id.gradeLabel);
        TextView gradeView = (TextView) viewGrade.findViewById(R.id.gradeView);

        if (top >= mid && top > low) {
            gradeView.setTextColor(getResources().getColor(R.color.grade_pie_top));
            gradeLabel.setTextColor(getResources().getColor(R.color.grade_pie_top));
        } else if (mid > low) {
            gradeView.setTextColor(getResources().getColor(R.color.grade_pie_mid));
            gradeLabel.setTextColor(getResources().getColor(R.color.grade_pie_low));
        } else {
            gradeView.setTextColor(getResources().getColor(R.color.grade_pie_low));
            gradeLabel.setTextColor(getResources().getColor(R.color.grade_pie_low));
        }

        DecimalFormat df4 = new DecimalFormat("#.###");
        String sf4 = df4.format(grade);
        gradeView.setText(sf4);

        // Configure pie chart ------------------------------------------------

        PieChart pieChart = (PieChart) viewGrade.findViewById(R.id.chart);
        pieChart.setUsePercentValues(true);
        pieChart.setDescription("");
        pieChart.setDrawHoleEnabled(false);

        ArrayList<Entry> entries = new ArrayList<>();

        // creating labels
        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Integer> color = new ArrayList<>();

        if (top != 0) {
            entries.add(new Entry((float) topProb, 1));
            labels.add(getResources().getString(R.string.grade_label_top));
            color.add(getResources().getColor(R.color.grade_pie_top));
        }
        if (mid != 0) {
            entries.add(new Entry((float) midProb, 2));
            labels.add(getResources().getString(R.string.grade_label_mid));
            color.add(getResources().getColor(R.color.grade_pie_mid));
        }
        if (low != 0) {
            entries.add(new Entry((float) lowProb, 3));
            labels.add(getResources().getString(R.string.grade_label_low));
            color.add(getResources().getColor(R.color.grade_pie_low));
        }


// Used for testing ++++++++++++++++++++++++++++++++++++++++++++++++++++
        //entries.add(new Entry((float) 13 / 30, 1));
        //labels.add(getResources().getString(R.string.grade_label_top));
        //color.add(getResources().getColor(R.color.grade_pie_top));

        //entries.add(new Entry((float) 15 / 30, 2));
        //labels.add(getResources().getString(R.string.grade_label_mid));
        //color.add(getResources().getColor(R.color.grade_pie_mid));

        //entries.add(new Entry((float) 2 / 30, 3));
        //labels.add(getResources().getString(R.string.grade_label_low));
        //color.add(getResources().getColor(R.color.grade_pie_low));
// Used for testing ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


        // Put data into pie chart --------------------------------------------

        PieDataSet dataset = new PieDataSet(entries, "");

        PieData pieData = new PieData(labels, dataset); // initialize Piedata
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(16f);
        pieData.setValueTextColor(getResources().getColor(R.color.grade_pie_text));

        pieChart.setData(pieData);

        // Configure chart legend ---------------------------------------------

        Legend legend = pieChart.getLegend();
        legend.setFormSize(24f);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        legend.setTextSize(20f);
        legend.setTextColor(getResources().getColor(R.color.grade_pie_text_legend));
        legend.setXEntrySpace(20f);
        dataset.setColors(color);
    }

    protected void onNewRecording(View view) {

        ((TextView) findViewById(R.id.filenameLabel)).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.filenameValue)).setVisibility(View.INVISIBLE);

        tvRSRP.setText("");
        tvRSRQ.setText("");
        tvPCI.setText("");
        tvCQI.setText("");
        tvDataPoints.setText("");

        popShim.dismiss();
        popShim = null;
    }

    protected void onDisplayGraph(final View view) {

        final View viewGraph = view.inflate(HalUtama4G.this, R.layout.layout_graph, null);

        final PopupWindow pop = new PopupWindow(
                viewGraph,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                true);

        pop.showAtLocation(viewGraph,
                Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL,
                0, 0);

        ((TextView) viewGraph.findViewById(R.id.filenameValue)).setText(csvFilename);

        Button btn, btnUploadIntent;
        GradientDrawable gd;

        btn = (Button) viewGraph.findViewById(R.id.newRecording);
        gd = (GradientDrawable) (btn.getBackground());
        gd.setColor(getResources().getColor(R.color.button_start_recording));

        btn = (Button) viewGraph.findViewById(R.id.displayGrade);
        gd = (GradientDrawable) (btn.getBackground());
        gd.setColor(getResources().getColor(R.color.button_start_recording));

        btnUploadIntent = (Button)viewGraph.findViewById(R.id.displayGoToUpload);
        btnUploadIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),Upload.class);
                startActivity(i);
            }
        });

        ((Button) viewGraph.findViewById(R.id.displayGrade)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                onDisplayGrade(view);
            }
        });
        ((Button) viewGraph.findViewById(R.id.newRecording)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                onNewRecording(view);
            }
        });

        setupChart(viewGraph);

        int numSamples = data.size();

        for (int ndx = 0; ndx < numSamples; ndx++) {
            addEntry(ndx);
        }

        mChart.notifyDataSetChanged();
        mChart.setVisibleXRange(0, 7);
        if (numSamples > 8) {
            mChart.moveViewToX(numSamples - 8);
        }
    }

    protected void setupChart(View view) {

        mChart = new LineChart(this);
        mChart = (LineChart) view.findViewById(R.id.chart2);

        mChart.setDescription("");
        mChart.setNoDataTextDescription("No data");
        mChart.setHighlightPerTapEnabled(true);
        mChart.setTouchEnabled(true);

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);

        LineData data = new LineData();
        data.setValueTextColor(getResources().getColor(R.color.line_chart_text));

        mChart.setData(data);

        // Setup legend
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setTextColor(getResources().getColor(R.color.line_chart_text_legend));

        XAxis x1 = mChart.getXAxis();
        x1.setTextColor(getResources().getColor(R.color.line_chart_x_axis_text));
        x1.setDrawGridLines(false);
        x1.setAvoidFirstLastClipping(true);

        YAxis y1 = mChart.getAxisLeft();
        y1.setTextColor(getResources().getColor(R.color.line_chart_y_axis_text));

        // Can't set explicit range if we want the initial
        // view to be zoomed into only the range of values
        // within the data set.
        //y1.setAxisMaxValue(-40f);
        //y1.setAxisMinValue(-140f);

        // This causes the axis to be limited
        // to the range of the dataset, i.e.
        // the default appearance is that of being
        // zoomed in to see only the data range
        // in the data set.  NOTE: you have to
        // set both values to false for this to work.
        mChart.getAxisLeft().setStartAtZero(false);
        mChart.getAxisRight().setStartAtZero(false);

        y1.setDrawGridLines(true);
        y1.setLabelCount(6, true);

        YAxis y2 = mChart.getAxisRight();
        y2.setEnabled(false);

    }

    protected LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "RSRP dBm");
        // Enable this for interpreted cubic lines between data points;
        // otherwise, the lines are straight
        //set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(getResources().getColor(R.color.line_chart_line_color));
        set.setCircleColor(getResources().getColor(R.color.line_chart_circle_color));
        set.setCircleColorHole(getResources().getColor(R.color.line_chart_circle_hole_color));
        set.setLineWidth(2f);
        set.setCircleRadius(6f);
        //set.setFillColor(Color.YELLOW);
        set.setHighLightColor(getResources().getColor(R.color.line_chart_highlight_color));
        set.setValueTextColor(getResources().getColor(R.color.line_chart_value_text_color));
        set.setValueTextSize(10f);

        return set;
    }

    protected void addEntry(int ndx) {

        LineData dataChart = mChart.getData();

        if (dataChart == null) {
            return;
        }

        LineDataSet set = (LineDataSet) dataChart.getDataSetByIndex(0);

        if (set == null) {
            set = createSet();
            dataChart.addDataSet(set);
        }

        dataChart.addXValue("");

        double val = Double.valueOf(data.get(ndx)[1]);
        dataChart.addEntry(new Entry((float) val, set.getEntryCount()), 0);
    }

    @Override
    public void onPause() {

        Log.d("onPauseAct SigStr", "+++++++++++++++++++++++++++++++++++ onPause");
        try {
            if (signalStrengthListener != null) {
                tm.listen(signalStrengthListener, SignalStrengthListener.LISTEN_NONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d("onDestroyAct SigStr", "+++++++++++++++++++++++++++++++++++ onDestroy");
        try {
            if (signalStrengthListener != null) {
                tm.listen(signalStrengthListener, SignalStrengthListener.LISTEN_NONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------
    protected class SignalStrengthListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {

            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);


            ltestr = signalStrength.toString();
            parts = ltestr.split(" ");

            try {
                cellInfoList = tm.getAllCellInfo();

                for (CellInfo cellInfo : cellInfoList) {

                    if (cellInfo instanceof CellInfoLte) {
                        // cast to CellInfoLte and call all the CellInfoLte methods you need
                        // Gets the LTE PCI: (returns Physical Cell Id 0..503, Integer.MAX_VALUE if unknown)
                        cellPci = ((CellInfoLte) cellInfo).getCellIdentity().getPci();
                        mcc = ((CellInfoLte) cellInfo).getCellIdentity().getMcc();
                        mnc = ((CellInfoLte) cellInfo).getCellIdentity().getMnc();
                    }
                }
            } catch (Exception e) {
                Log.d("SignalStrength", "Exception: " + e.getMessage());
            }

            super.onSignalStrengthsChanged(signalStrength);
        }
    }

}