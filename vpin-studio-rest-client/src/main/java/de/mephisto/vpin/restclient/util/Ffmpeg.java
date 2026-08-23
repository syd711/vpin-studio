package de.mephisto.vpin.restclient.util;

public interface Ffmpeg {
  String DEFAULT_COMMAND = "ffmpeg.exe -y -video_size [width]x[height] -offset_x [x] -offset_y [y] " +
      "-rtbufsize 100M -f gdigrab -framerate 30 -t [duration] -draw_mouse 0 -i desktop -c:v libx264 -r 30 -preset ultrafast " +
      "-tune zerolatency -crf 25 -pix_fmt yuv420p";

  String OPEN_GL_COMMAND = "ffmpeg.exe -y -init_hw_device d3d11va:[adapter_idx] -filter_complex \"ddagrab=output_idx=[output_idx]:framerate=30:video_size=[width]x[height]:draw_mouse=0,hwdownload,format=bgra\" " +
      "-c:v libx264 -r 30 -t [duration] -preset ultrafast -tune zerolatency -crf 25 -pix_fmt yuv420p";

  /**
   * NVIDIA hardware-encoded variant of DEFAULT_COMMAND, mirroring PinUP Popper's recordstart.bat
   * "hasNVIDIA=1" branch: gdigrab capture, -probesize 10M to cut ffmpeg's startup probing delay,
   * and h264_nvenc so the GPU's dedicated encoder chip is used instead of software libx264,
   * which keeps CPU load down while VPX is rendering the table.
   */
  String DEFAULT_COMMAND_NVENC = "ffmpeg.exe -y -video_size [width]x[height] -offset_x [x] -offset_y [y] " +
      "-rtbufsize 100M -probesize 10M -f gdigrab -framerate 30 -t [duration] -draw_mouse 0 -i desktop -c:v h264_nvenc -preset:v fast -pix_fmt nv12 -r 30 -b:v 5M";
}
