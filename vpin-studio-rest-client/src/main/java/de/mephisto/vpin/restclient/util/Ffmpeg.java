package de.mephisto.vpin.restclient.util;

public interface Ffmpeg {
  String DEFAULT_COMMAND = "ffmpeg.exe -y -video_size [width]x[height] -offset_x [x] -offset_y [y] " +
      "-rtbufsize 100M -f gdigrab -framerate 30 -t [duration] -draw_mouse 0 -i desktop -c:v libx264 -r 30 -preset ultrafast " +
      "-tune zerolatency -crf 25 -pix_fmt yuv420p";
}
