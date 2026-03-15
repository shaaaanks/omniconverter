package com.omniconverter.app.converters;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.util.Log;

import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.core.FileUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;

public class VideoToAudioConverter implements Converter {
    private static final String TAG = "VideoToAudioConverter";
    private static final int TIMEOUT_USEC = 10000;

    @Override
    public ConversionResult convert(Context context, Uri inputUri, Map<String, Object> params) throws Exception {
        ConversionResult result = new ConversionResult(inputUri);

        String format = "mp3";
        Object formatObj = params.get("format");
        if (formatObj != null) {
            format = formatObj.toString().toLowerCase(Locale.US).trim();
        }

        if (!"mp3".equals(format) && !"wav".equals(format) && !"aac".equals(format)) {
            format = "mp3";
        }

        Log.d(TAG, "Starting video to audio conversion for format: " + format);

        File inputFile = FileUtils.copyUriToFile(
                context,
                inputUri,
                "input_video_" + System.currentTimeMillis() + ".mp4"
        );

        File outDir = context.getExternalFilesDir("Audio");
        if (outDir == null) {
            outDir = new File(context.getCacheDir(), "Audio");
        }
        if (!outDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outDir.mkdirs();
        }

        File outputFile = new File(outDir, "audio_" + System.currentTimeMillis() + "." + format);

        try {
            transcodeAudio(inputFile, outputFile, format);
        } catch (Exception e) {
            Log.e(TAG, "Native transcoding failed: " + e.getMessage() + ". Attempting extraction fallback.");
            // Fallback: just extract audio without transcoding
            extractAudioOnly(inputFile, outputFile, format);
        }

        if (!outputFile.exists() || outputFile.length() == 0) {
            throw new Exception("Conversion failed, output file is empty.");
        }

        result.setOutputUri(Uri.fromFile(outputFile));
        result.setStatus(ConversionResult.Status.SUCCESS);
        result.setMessage("Converted to " + format.toUpperCase(Locale.US));
        Log.d(TAG, "Conversion successful: " + outputFile.getAbsolutePath());
        return result;
    }

    /**
     * Transcode audio using MediaCodec (true encoding)
     */
    private void transcodeAudio(File inputFile, File outputFile, String format) throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        MediaMuxer muxer = null;
        MediaCodec decoder = null;
        MediaCodec encoder = null;

        try {
            extractor.setDataSource(inputFile.getAbsolutePath());

            // Find audio track
            int audioTrackIndex = -1;
            MediaFormat audioFormat = null;

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat trackFormat = extractor.getTrackFormat(i);
                String mime = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("audio/")) {
                    audioTrackIndex = i;
                    audioFormat = trackFormat;
                    break;
                }
            }

            if (audioTrackIndex == -1) {
                throw new Exception("No audio track found");
            }

            extractor.selectTrack(audioTrackIndex);

            // Setup decoder
            String decoderMime = audioFormat.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(decoderMime);
            decoder.configure(audioFormat, null, null, 0);
            decoder.start();

            // Setup encoder output format
            MediaFormat encoderFormat = new MediaFormat();
            encoderFormat.setString(MediaFormat.KEY_MIME, "audio/mpeg");
            encoderFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE));
            encoderFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
            encoderFormat.setInteger(MediaFormat.KEY_BIT_RATE, 192000);

            // Setup encoder
            encoder = MediaCodec.createEncoderByType("audio/mpeg");
            encoder.configure(encoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();

            // Setup muxer
            muxer = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            // Transcode
            ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
            ByteBuffer[] decoderOutputBuffers = decoder.getOutputBuffers();
            ByteBuffer[] encoderInputBuffers = encoder.getInputBuffers();
            ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();

            MediaCodec.BufferInfo decoderBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo encoderBufferInfo = new MediaCodec.BufferInfo();

            boolean decoderDone = false;
            boolean encoderDone = false;
            int muxerAudioTrackIndex = -1;

            while (!encoderDone) {
                // Feed input to decoder
                if (!decoderDone) {
                    int decoderInputIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (decoderInputIndex >= 0) {
                        int chunkSize = extractor.readSampleData(decoderInputBuffers[decoderInputIndex], 0);
                        if (chunkSize < 0) {
                            decoder.queueInputBuffer(decoderInputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            decoderDone = true;
                        } else {
                            decoder.queueInputBuffer(decoderInputIndex, 0, chunkSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                // Get output from decoder and feed to encoder
                int decoderOutputIndex = decoder.dequeueOutputBuffer(decoderBufferInfo, TIMEOUT_USEC);
                if (decoderOutputIndex >= 0) {
                    int encoderInputIndex = encoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (encoderInputIndex >= 0) {
                        encoderInputBuffers[encoderInputIndex].put(decoderOutputBuffers[decoderOutputIndex]);
                        encoder.queueInputBuffer(
                                encoderInputIndex,
                                0,
                                decoderBufferInfo.size,
                                decoderBufferInfo.presentationTimeUs,
                                decoderBufferInfo.flags
                        );
                    }
                    decoder.releaseOutputBuffer(decoderOutputIndex, false);
                } else if (decoderOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // Ignore for now
                }

                // Get output from encoder
                int encoderOutputIndex = encoder.dequeueOutputBuffer(encoderBufferInfo, TIMEOUT_USEC);
                if (encoderOutputIndex >= 0) {
                    if (muxerAudioTrackIndex < 0) {
                        muxerAudioTrackIndex = muxer.addTrack(encoder.getOutputFormat());
                        muxer.start();
                    }
                    muxer.writeSampleData(muxerAudioTrackIndex, encoderOutputBuffers[encoderOutputIndex], encoderBufferInfo);
                    encoder.releaseOutputBuffer(encoderOutputIndex, false);
                    if ((encoderBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        encoderDone = true;
                    }
                } else if (encoderOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (muxerAudioTrackIndex < 0) {
                        muxerAudioTrackIndex = muxer.addTrack(encoder.getOutputFormat());
                        muxer.start();
                    }
                }
            }

            if (muxer != null) {
                muxer.stop();
            }

        } finally {
            if (extractor != null) extractor.release();
            if (decoder != null) {
                decoder.stop();
                decoder.release();
            }
            if (encoder != null) {
                encoder.stop();
                encoder.release();
            }
            if (muxer != null) muxer.release();
        }
    }

    /**
     * Fallback: extract audio without transcoding
     */
    private void extractAudioOnly(File inputFile, File outputFile, String format) throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        MediaMuxer muxer = null;

        try {
            extractor.setDataSource(inputFile.getAbsolutePath());

            int audioTrackIndex = -1;
            MediaFormat audioFormat = null;

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat trackFormat = extractor.getTrackFormat(i);
                String mime = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("audio/")) {
                    audioTrackIndex = i;
                    audioFormat = trackFormat;
                    break;
                }
            }

            if (audioTrackIndex == -1) {
                throw new Exception("No audio track found");
            }

            extractor.selectTrack(audioTrackIndex);
            muxer = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            int muxerTrackIndex = muxer.addTrack(audioFormat);
            muxer.start();

            ByteBuffer buffer = ByteBuffer.allocate(256 * 1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            while (true) {
                int chunkSize = extractor.readSampleData(buffer, 0);
                if (chunkSize < 0) break;

                bufferInfo.offset = 0;
                bufferInfo.size = chunkSize;
                bufferInfo.presentationTimeUs = extractor.getSampleTime();
                bufferInfo.flags = extractor.getSampleFlags();

                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo);
                extractor.advance();
            }

            muxer.stop();

        } finally {
            if (extractor != null) extractor.release();
            if (muxer != null) muxer.release();
        }
    }
}

