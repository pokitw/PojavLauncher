public class ScreenRecordingService extends Service {
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaRecorder mMediaRecorder;
    private boolean mIsRecording;

    @Override
    public void onCreate() {
        super.onCreate();
        mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }

        int resultCode = intent.getIntExtra("resultCode", -1);
        Intent data = intent.getParcelableExtra("data");
        String outputDir = intent.getStringExtra("outputDir");

        if (resultCode == -1 || data == null) {
            return START_NOT_STICKY;
        }

        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaRecorder = new MediaRecorder();

        initRecorder(outputDir);
        createVirtualDisplay();

        mMediaRecorder.start();
        mIsRecording = true;

        return START_STICKY;
    }

    private void initRecorder(String outputDir) {
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(1280, 720);

            File outputFile = new File(outputDir, "gameplay_" + System.currentTimeMillis() + ".mp4");
            outputFile.getParentFile().mkdirs();
            mMediaRecorder.setOutputFile(outputFile.getAbsolutePath());

            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenRecording",
                1280, 720, getResources().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null, null);
    }

    @Override
    public void onDestroy() {
        if (mIsRecording) {
            mIsRecording = false;
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mVirtualDisplay.release();
            mMediaProjection.stop();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
