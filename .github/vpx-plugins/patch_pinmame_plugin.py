"""Exposes the PinMAME controller to other plugins ("PinMAME/GetController" message, superhac's VPX change
https://github.com/vpinball/vpinball/commit/c1ca14346fc30c1ff883a28cf15ec0197a818a94) and registers the score-server
plugin in the VPX CMake build.

The PinMAME change is applied by anchors instead of a diff, so it survives line shifts between VPX builds.
It is GPLv3+ like VPX.

Usage (from the vpinball checkout): python patch_pinmame_plugin.py
"""

import sys
from pathlib import Path

PLUGIN = Path("plugins/pinmame/PinMAMEPlugin.cpp")

CONTROLLER = "static Controller* controller = nullptr;\n"
SUBSCRIBE = "   msgApi->SubscribeMsg(endpointId, getAudioSrcId, OnGetAudioSrc, nullptr);\n"
UNLOAD = "   scriptApi->SetCOMObjectOverride(\"VPinMAME.Controller\", nullptr);\n"


def patch_pinmame() -> None:
    s = PLUGIN.read_text(encoding="utf-8")
    if '"GetController"' in s:
        print("[patch] PinMAME plugin already exposes GetController")
        return
    for anchor in (CONTROLLER, SUBSCRIBE, UNLOAD):
        if anchor not in s:
            sys.exit(f"[patch] PinMAMEPlugin.cpp changed, not found: {anchor.strip()}")
    s = s.replace(CONTROLLER, CONTROLLER + (
        "static unsigned int getControllerMsgId = 0;\n"
        "\n"
        "// Hands the Controller to other plugins (score-server)\n"
        "static void OnGetController(const unsigned int eventId, void* userData, void* eventData)\n"
        "{\n"
        "   void** out = static_cast<void**>(eventData);\n"
        "   if (out)\n"
        "      *out = static_cast<void*>(controller);\n"
        "}\n"), 1)
    s = s.replace(SUBSCRIBE, SUBSCRIBE + (
        "   msgApi->SubscribeMsg(endpointId, getControllerMsgId = msgApi->GetMsgID(\"PinMAME\", \"GetController\"), OnGetController, nullptr);\n"), 1)
    s = s.replace(UNLOAD, (
        "   msgApi->UnsubscribeMsg(getControllerMsgId, OnGetController, nullptr);\n"
        "   msgApi->ReleaseMsgID(getControllerMsgId);\n") + UNLOAD, 1)
    PLUGIN.write_text(s, encoding="utf-8", newline="\n")
    print("[patch] PinMAME plugin patched")


def register_score_server() -> None:
    # the score-server CMake file uses the variable names of an older VPX build system
    cmake = Path("plugins/score-server/CMakeLists_plugin_ScoreServer.txt").read_text(encoding="utf-8")
    cmake = cmake.replace("PluginPlatform", "PLATFORM").replace("PluginArch", "ARCH")
    Path("make/CMakeLists_plugin_ScoreServer.txt").write_text(cmake, encoding="utf-8", newline="\n")

    plugins = Path("make/CMakeLists_plugins.txt")
    text = plugins.read_text(encoding="utf-8")
    include = 'include("${CMAKE_SOURCE_DIR}/make/CMakeLists_plugin_ScoreServer.txt")'
    if include not in text:
        plugins.write_text(text.rstrip("\n") + "\n" + include + "\n", encoding="utf-8", newline="\n")
    print("[patch] score-server registered in the VPX build")


if __name__ == "__main__":
    patch_pinmame()
    register_score_server()
