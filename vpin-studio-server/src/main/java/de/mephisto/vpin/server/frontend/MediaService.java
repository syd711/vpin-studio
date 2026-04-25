package de.mephisto.vpin.server.frontend;


import de.mephisto.vpin.restclient.assets.AssetMetaData;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.assets.AssetService;
import org.jspecify.annotations.NonNull;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * Base class extended by GameMediaService and PLaylistMediaService to shae common code
 */
public abstract class MediaService  {
  private final static Logger LOG = LoggerFactory.getLogger(MediaService.class);

  public static final byte[] EMPTY_MP4 = Base64.getDecoder().decode("AAAAIGZ0eXBpc29tAAACAGlzb21pc28yYXZjMW1wNDEAAAAIZnJlZQAAJRFtZGF0AAACcQYF//9t3EXpvebZSLeWLNgg2SPu73gyNjQgLSBjb3JlIDE1MCByMjgzMyBkZjc5MDY3IC0gSC4yNjQvTVBFRy00IEFWQyBjb2RlYyAtIENvcHlsZWZ0IDIwMDMtMjAxNyAtIGh0dHA6Ly93d3cudmlkZW9sYW4ub3JnL3gyNjQuaHRtbCAtIG9wdGlvbnM6IGNhYmFjPTAgcmVmPTIgZGVibG9jaz0xOjA6MCBhbmFseXNlPTB4MToweDExMSBtZT1oZXggc3VibWU9NiBwc3k9MSBwc3lfcmQ9MS4wMDowLjAwIG1peGVkX3JlZj0xIG1lX3JhbmdlPTE2IGNocm9tYV9tZT0xIHRyZWxsaXM9MSA4eDhkY3Q9MCBjcW09MCBkZWFkem9uZT0yMSwxMSBmYXN0X3Bza2lwPTEgY2hyb21hX3FwX29mZnNldD0tMiB0aHJlYWRzPTkgbG9va2FoZWFkX3RocmVhZHM9MSBzbGljZWRfdGhyZWFkcz0wIG5yPTAgZGVjaW1hdGU9MSBpbnRlcmxhY2VkPTAgYmx1cmF5X2NvbXBhdD0wIGNvbnN0cmFpbmVkX2ludHJhPTAgYmZyYW1lcz0wIHdlaWdodHA9MCBrZXlpbnQ9MjUwIGtleWludF9taW49MjUgc2NlbmVjdXQ9NDAgaW50cmFfcmVmcmVzaD0wIHJjX2xvb2thaGVhZD0zMCByYz1jcmYgbWJ0cmVlPTEgY3JmPTI2LjAgcWNvbXA9MC42MCBxcG1pbj0wIHFwbWF4PTY5IHFwc3RlcD00IGlwX3JhdGlvPTEuNDAgYXE9MToxLjAwAIAAAAbYZYiEC/JigACe/JycnJycnJycnJycnJycnJycnJycnJycnJycnJycnJycnXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXgAAAACEGaOBfgAkJgAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAACEGbQC/ABITAAAAACEGbYC/ABITAAAAACEGbgC/ABITAAAAACEGboC/ABITAAAAACEGbwC/ABITAAAAACEGb4C/ABITAAAAACEGaAC/ABITAAAAACEGaIC/ABITAAAAACEGaQC/ABITAAAAACEGaYC/ABITAAAAACEGagC/ABITAAAAACEGaoC/ABITAAAAACEGawC/ABITAAAAACEGa4C/ABITAAAAACEGbAC/ABITAAAAACEGbIC/ABITAAAAG2GWIggO8mKAAKvMnJycnJycnJycnJycnJycnJycnJycnJycnJycnJycnJyddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddeAAAAAhBmjgX4AJCYAAAAAhBmkAvwASEwAAAAAhBmmAvwASEwAAAAAhBmoAvwASEwAAAAAhBmqAvwASEwAAAAAhBmsAvwASEwAAAAAhBmuAvwASEwAAAAAhBmwAvwASEwAAAAAhBmyAvwASEwAAAAAhBm0AvwASEwAAAAAhBm2AvwASEwAAAAAhBm4AvwASEwAAAAAhBm6AvwASEwAAAAAhBm8AvwASEwAAAAAhBm+AvwASEwAAAAAhBmgAvwASEwAAAAAhBmiAvwASEwAAAAAhBmkAvwASEwAAAAAhBmmAvwASEwAAAAAhBmoAvwASEwAAAAAhBmqAvwASEwAAAAAhBmsAvwASEwAAAAAhBmuAvwASEwAAAAAhBmwAvwASEwAAAAAhBmyAvwASEwAAAAAhBm0AvwASEwAAAAAhBm2AvwASEwAAAAAhBm4AvwASEwAAAAAhBm6AvwASEwAAAAAhBm8AvwASEwAAAAAhBm+AvwASEwAAAAAhBmgAvwASEwAAAAAhBmiAvwASEwAAAAAhBmkAvwASEwAAAAAhBmmAvwASEwAAAAAhBmoAvwASEwAAAAAhBmqAvwASEwAAAAAhBmsAvwASEwAAAAAhBmuAvwASEwAAAAAhBmwAvwASEwAAAAAhBmyAvwASEwAAAAAhBm0AvwASEwAAAAAhBm2AvwASEwAAAAAhBm4AvwASEwAAAAAhBm6AvwASEwAAAAAhBm8AvwASEwAAAAAhBm+AvwASEwAAAAAhBmgAvwASEwAAAAAhBmiAvwASEwAAAAAhBmkAvwASEwAAAAAhBmmAvwASEwAAAAAhBmoAvwASEwAAAAAhBmqAvwASEwAAAAAhBmsAvwASEwAAAAAhBmuAvwASEwAAAAAhBmwAvwASEwAAAAAhBmyAvwASEwAAAAAhBm0AvwASEwAAAAAhBm2AvwASEwAAAAAhBm4AvwASEwAAAAAhBm6AvwASEwAAAAAhBm8AvwASEwAAAAAhBm+AvwASEwAAAAAhBmgAvwASEwAAAAAhBmiAvwASEwAAAAAhBmkAvwASEwAAAAAhBmmAvwASEwAAAAAhBmoAvwASEwAAAAAhBmqAvwASEwAAAAAhBmsAvwASEwAAAAAhBmuAvwASEwAAAAAhBmwAvwASEwAAAAAhBmyAvwASEwAAAAAhBm0AvwASEwAAAAAhBm2AvwASEwAAAAAhBm4AvwASEwAAAAAhBm6AvwASEwAAAAAhBm8AvwASEwAAAAAhBm+AvwASEwAAAAAhBmgAvwASEwAAAAAhBmiAvwASEwAAAAAhBmkAvwASEwAAAAAhBmmAvwASEwAAAAAhBmoAvwASEwAAAAAhBmqAvwASEwAAAAAhBmsAvwASEwAAAAAhBmuAvwASEwAAAAAhBmwAvwASEwAAAAAhBmyAvwASEwAAAAAhBm0AvwASEwAAAAAhBm2AvwASEwAAAAAhBm4AvwASEwAAAAAhBm6AvwASEwAAAAAhBm8AvwASEwAAAAAhBm+AvwASEwAAAAAhBmgAvwASEwAAAAAhBmiAvwASEwAAAAAhBmkAvwASEwAAAAAhBmmAvwASEwAAAAAhBmoAvwASEwAAAAAhBmqAvwASEwAAAAAhBmsAvwASEwAAAAAhBmuAvwASEwAAAAAhBmwAvwASEwAAAAAhBmyAvwASEwAAAAAhBm0AvwASEwAAAAAhBm2AvwASEwAAAAAhBm4AvwASEwAAAAAhBm6AvwASEwAAAAAhBm8AvwASEwAAAAAhBm+AvwASEwAAAAAhBmgAvwASEwAAAAAhBmiAvwASEwAAAAAhBmkAvwASEwAAAAAhBmmAvwASEwAAAAAhBmoAvwASEwAAAAAhBmqAvwASEwAAAAAhBmsAvwASEwAAAAAhBmuAvwASEwAAAAAhBmwAvwASEwAAAAAhBmyAvwASEwAAAAAhBm0AvwASEwAAAAAhBm2AvwASEwAAAAAhBm4AvwASEwAAAAAhBm6AvwASEwAAAAAhBm8AvwASEwAAAAAhBm+AvwASEwAAAAAhBmgAvwASEwAAAAAhBmiAvwASEwAAAAAhBmkAvwASEwAAAAAhBmmAvwASEwAAAAAhBmoAvwASEwAAAAAhBmqAvwASEwAAAAAhBmsAvwASEwAAAAAhBmuAvwASEwAAAAAhBmwAvwASEwAAAAAhBmyAvwASEwAAAAAhBm0AvwASEwAAAAAhBm2AvwASEwAAAAAhBm4AvwASEwAAAAAhBm6AvwASEwAAAAAhBm8AvwASEwAAAAAhBm+AvwASEwAAAAAhBmgAvwASEwAAAAAhBmiAvwASEwAAAAAhBmkAvwASEwAAAAAhBmmAvwASEwAAAAAhBmoAvwASEwAAAAAhBmqAvwASEwAAAAAhBmsAvwASEwAAAAAhBmuAvwASEwAAAAAhBmwAvwASE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AAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAG3AAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAMAAAADAAAAAwAAAAUc3RjbwAAAAAAAAABAAAAMAAAAGJ1ZHRhAAAAWm1ldGEAAAAAAAAAIWhkbHIAAAAAAAAAAG1kaXJhcHBsAAAAAAAAAAAAAAAALWlsc3QAAAAlqXRvbwAAAB1kYXRhAAAAAQAAAABMYXZmNTcuNzMuMTAw");
  public static final byte[] EMPTY_MP3 = Base64.getDecoder().decode("SUQzAwAAAAADJVRGTFQAAAAPAAAB//5NAFAARwAvADMAAABDT01NAAAAggAAAGRldWlUdW5TTVBCACAwMDAwMDAwMCAwMDAwMDAwMCAwMDAwMDAwMCAwMDAwMDAwMDAwMDAxMmMxIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/+7RAAAAE4ABLgAAACAAACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////8=");

  @Autowired
  protected FrontendService frontendService;

  public boolean setDefaultAsset(int objectId, VPinScreen screen, String defaultName) {
    List<File> mediaFiles = getMediaFiles(objectId, screen);
    File torename = mediaFiles.stream().filter(f -> f.getName().equals(defaultName)).findFirst().orElse(null);
    if (torename == null) {
      LOG.info("Cannot set default asset as {} does not exist in the assets for game/playlist {} and screen {}", defaultName, objectId, screen);
      return false;
    }

    File temp = new File(torename.getParentFile(), "temp_" + defaultName);
    if (torename.renameTo(temp)) {
      String extension = FilenameUtils.getExtension(defaultName);
      for (File file : mediaFiles) {
        // find existing default files, mind there could be several with different extensions
        String fileext = FilenameUtils.getExtension(file.getName());
        if (FileUtils.isDefaultAsset(file.getName()) && StringUtils.equalsIgnoreCase(fileext, extension)) {
          File defaultFile = FileUtils.uniqueAsset(file);
          if (file.renameTo(defaultFile)) {
            LOG.info("Renamed \"{}\" to \"{}\"", file.getAbsolutePath(), defaultFile.getName());
            notifyGameScreenAssetsChanged(objectId, screen, defaultFile);
          }
          else {
            LOG.warn("Cannot rename \"{}\" to \"{}\", state may be inconsistent", file.getAbsolutePath(), defaultFile.getName());
            return false;
          }
        }
      }
      // ends by renaming temp file to default
      File newFile = new File(torename.getParent(), FileUtils.baseUniqueAsset(defaultName) + "." + extension);
      if (temp.renameTo(newFile)) {
        LOG.info("New default asset set \"{} \"for game {} and screen{}", newFile.getAbsolutePath(), objectId, screen);
        return true;
      }
      else {
        LOG.warn("Cannot rename \"{}\" to \"{}\", state may be inconsistent", temp.getAbsolutePath(), newFile.getName());
      }

    }
    else {
      LOG.warn("Cannot rename \"{}\", set as default operation ignored", torename.getAbsolutePath());
    }
    return false;
  }

  public boolean renameAsset(int objectId, VPinScreen screen, String oldName, String newName) {
    File mediaFile = getMediaFile(objectId, screen, oldName);
    if (mediaFile != null && mediaFile.exists()) {
      File renamed = new File(mediaFile.getParentFile(), newName);
      if (mediaFile.renameTo(renamed)) {
        LOG.info("Renamed \"{}\" to \"{}\"", mediaFile.getAbsolutePath(), renamed.getAbsolutePath());
        return true;
      }
    }
    return false;
  }

  public boolean copyAsset(int objectId, VPinScreen screen, String name, VPinScreen target) {
    try {
      File mediaFile = getMediaFile(objectId, screen, name);
      if (mediaFile != null && mediaFile.exists()) {
        String extension = FilenameUtils.getExtension(name);
        File targetFile = uniqueMediaAsset(objectId, target, extension, true);
        FileUtil.copyFile(mediaFile, targetFile);
        notifyGameScreenAssetsChanged(objectId, screen, targetFile);
        return true;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to copy asset {} to {}: {}", name, target, e.getMessage(), e);
    }
    return false;
  }

  public boolean toFullscreenMedia(int objectId, VPinScreen screen) throws IOException {
    List<File> mediaFiles = getMediaFiles(objectId, screen);
    if (mediaFiles.size() == 1) {
      File mediaFile = mediaFiles.get(0);
      String name = mediaFile.getName();
      String baseName = FilenameUtils.getBaseName(name);
      String suffix = FilenameUtils.getExtension(name);
      String updatedBaseName = baseName + "01(SCREEN3)." + suffix;

      LOG.info("Renaming {} to '{}'", mediaFile.getAbsolutePath(), updatedBaseName);
      File renameTarget = new File(mediaFile.getParentFile(), updatedBaseName);
      boolean renamed = mediaFile.renameTo(renameTarget);
      if (!renamed) {
        LOG.error("Renaming to {} failed, file already exists: {}", updatedBaseName, renameTarget.exists());
        return false;
      }

      File target = new File(mediaFile.getParentFile(), name);

      LOG.info("Copying blank asset to {}", target.getAbsolutePath());
      FileOutputStream out = new FileOutputStream(target);
      //copy base64 encoded 0s video
      IOUtils.write(EMPTY_MP4, out);
      out.close();

      return true;
    }
    return false;
  }

  public boolean addBlank(int objectId, VPinScreen screen) throws IOException {

    String suffix = "mp4";
    if (screen.equals(VPinScreen.AudioLaunch) || screen.equals(VPinScreen.Audio)) {
      suffix = "mp3";
    }

    File target = uniqueMediaAsset(objectId, screen, suffix, true);
    try (FileOutputStream out = new FileOutputStream(target)) {
      // copy base64 asset
      if (screen.equals(VPinScreen.AudioLaunch) || screen.equals(VPinScreen.Audio)) {
        IOUtils.write(EMPTY_MP3, out);
      }
      else {
        IOUtils.write(EMPTY_MP4, out);
      }
      LOG.info("Written blank asset \"{}\"", target.getAbsolutePath());
    }
    return true;
  }

  public boolean deleteMedia(int objectId, VPinScreen screen, String filename) {
    File media = getMediaFile(objectId, screen, filename);
    if (media != null && media.exists()) {
      LOG.info("Deleted {} of screen {}", media.getAbsolutePath(), screen.name());
      if (screen.equals(VPinScreen.Wheel)) {
        new WheelAugmenter(media).deAugment();
        new WheelIconDelete(media).delete();
      }
      if (media.delete()) {
        notifyGameScreenAssetsChanged(objectId, screen, media);
        return true;
      }
    }
    return false;
  }

  public boolean deleteMedia(int objectId, VPinScreen screen) {
    List<File> files = getMediaFiles(objectId, screen);
    boolean success = true;
    for (File file : files) {
      success &= deleteMedia(objectId, screen, file.getName());
    }
    return success;
  }

  public boolean deleteMedia(int gameId) {
    VPinScreen[] values = VPinScreen.values();
    for (VPinScreen screen : values) {
      List<File> files = getMediaFiles(gameId, screen);
      for (File file : files) {
        if (screen.equals(VPinScreen.Wheel)) {
          new WheelAugmenter(file).deAugment();
          new WheelIconDelete(file).delete();
        }
        if (file.delete()) {
          LOG.info("Deleted game media {} of screen {}", file.getAbsolutePath(), screen.name());
          notifyGameScreenAssetsChanged(gameId, screen, file);
        }
      }
    }
    return true;
  }

  public AssetMetaData getMetadata(int objectId, VPinScreen screen, String filename) {
    File mediaFile = getMediaFile(objectId, screen, filename);
    return AssetService.getMetadata(mediaFile);
  }


  public File getMediaFile(int objectId, VPinScreen screen, String name) {
    List<File> mediaFiles = getMediaFiles(objectId, screen);
    return mediaFiles.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
  }
  

  public abstract @NonNull List<File> getMediaFiles(int objectId, VPinScreen screen);

  protected abstract File uniqueMediaAsset(int objectId, VPinScreen screen, String suffix, boolean append);

  protected abstract void notifyGameScreenAssetsChanged(int objectId, VPinScreen screen, File asset);


}