"""Adapts superhac's score-server plugin (https://github.com/superhac/score-server) to the VPX 10.8.1 plugin API
and adds the two messages VPin Studio needs for per game achievements.

score-server targets an older plugin API. Changes made to the checked out source (upstream stays untouched):

  * VPX 10.8.1 has no controller game events (CTLPI_EVT_ON_GAME_START/END with the ROM name) anymore. The VPX
    player events are used instead, the ROM name is read from the PinMAME controller once the table script has
    created it (polled in onPrepareFrame through the "PinMAME/GetController" message of the patched PinMAME plugin).
  * The PinMAME script class is called "PinMAME_Controller".
  * MsgPluginAPI::UnsubscribeMsg has a userData argument.
  * PSC_CLASS_START/PSC_FUNCTIONn/PSC_CLASS_END and SubmitTypeLibrary(endpointId) have new signatures.
  * New messages (VPin Studio):
      round_start     a pinball game starts (current ball changes from 0 to 1+)
      nvram_snapshot  the live NVRAM (base64), sent on every player/ball change and otherwise at most once per
                      second when it changed

The script stops with an error when an expected piece of the upstream source is missing, so upstream changes are
noticed in the build instead of producing a broken plugin.

score-server is GPLv3+, so is the code added by this script.

Usage: python patch_score_server.py <score-server.cpp>
"""

import re
import sys
from pathlib import Path

MARKER = "VPin Studio: VPX 10.8.1 patch"


class Patcher:
    def __init__(self, text: str):
        self.text = text

    def require(self, needle: str, what: str) -> None:
        if needle not in self.text:
            sys.exit(f"[patch] upstream source changed, not found: {what}")

    def replace(self, old: str, new: str, what: str) -> None:
        self.require(old, what)
        self.text = self.text.replace(old, new, 1)

    def insert_before(self, anchor: str, code: str, what: str) -> None:
        self.replace(anchor, code + anchor, what)

    def insert_after(self, anchor: str, code: str, what: str) -> None:
        self.replace(anchor, anchor + code, what)


CONTROLLER_HELPERS = r'''
// ---- VPin Studio: PinMAME controller access (VPX 10.8.1) --------------------
static ScriptClassDef* vpsControllerClass() {
    if (!scriptApi) return nullptr;
    ScriptClassDef* def = scriptApi->GetClassDef("PinMAME_Controller");
    return def ? def : scriptApi->GetClassDef("Controller");
}

static std::string vpsControllerString(const char* property) {
    ScriptClassDef* def = vpsControllerClass();
    if (!def || !pinmameController) return std::string();
    for (unsigned int i = 0; i < def->nMembers; i++) {
        if (def->members[i].nArgs != 0 || std::string(def->members[i].name.name) != property) continue;
        ScriptVariant result = {};
        def->members[i].Call(pinmameController, (int)i, nullptr, &result);
        std::string value = result.vString.string ? std::string(result.vString.string) : std::string();
        if (result.vString.Release) result.vString.Release(&result.vString);
        return value;
    }
    return std::string();
}

// "ROMName" is the driver PinMAME runs (an alias like afm_113Serum becomes afm_113b, the name the NVRAM maps use),
// "GameName" the name the table asked for.
static std::string vpsRunningRom() {
    std::string rom = vpsControllerString("ROMName");
    return rom.empty() ? vpsControllerString("GameName") : rom;
}
// -----------------------------------------------------------------------------

'''

GAME_START_OLD = (
    "void onGameStart(const unsigned int eventId, void* userData, void* eventData) {\n"
    "    // Game starting - get the ROM name\n"
    "    CtlOnGameStartMsg* msg = static_cast<CtlOnGameStartMsg*>(eventData);\n"
    "    if (msg && msg->gameId) {\n"
    "        currentRomName = msg->gameId;\n")

GAME_START_NEW = (
    "static bool vpsRomPending = false;\n"
    "static unsigned int vpsRomPollFrames = 0;\n"
    "void vpsStartRom(const char* gameId);\n"
    "\n"
    "void onGameStart(const unsigned int eventId, void* userData, void* eventData) {\n"
    "    // VPX 10.8.1: the player starts before the table script creates the PinMAME controller,\n"
    "    // the ROM is resolved in onPrepareFrame.\n"
    "    vpsRomPending = true;\n"
    "    vpsRomPollFrames = 0;\n"
    "    pinmameController = nullptr;\n"
    "}\n"
    "\n"
    "void vpsStartRom(const char* gameId) {\n"
    "    if (gameId && *gameId) {\n"
    "        currentRomName = gameId;\n")

GAME_END = "void onGameEnd(const unsigned int eventId, void* userData, void* eventData) {\n"
GAME_END_ADD = (
    "    vpsRomPending = false;\n"
    "    if (currentRomName.empty()) return; // no PinMAME game was running\n")

PREPARE_FRAME = "void onPrepareFrame(const unsigned int eventId, void* userData, void* eventData) {\n"
PREPARE_FRAME_ADD = (
    "    if (vpsRomPending && (vpsRomPollFrames++ % 30) == 0) {\n"
    "        void* ctrl = nullptr;\n"
    "        msgApi->BroadcastMsg(endpointId, getControllerId, &ctrl);\n"
    "        if (ctrl) {\n"
    "            pinmameController = ctrl;\n"
    "            std::string rom = vpsRunningRom();\n"
    "            if (!rom.empty()) {\n"
    "                vpsRomPending = false;\n"
    "                vpsStartRom(rom.c_str());\n"
    "            }\n"
    "        }\n"
    "    }\n")

STUDIO_MESSAGES = r'''
    // ---- VPin Studio: round_start + nvram_snapshot ----
    {
        static std::string vpsRom;
        static int vpsLastBall = -1;
        static int vpsSnapPlayers = -1, vpsSnapPlayer = -1, vpsSnapBall = -1;
        static std::vector<uint8_t> vpsSnapData;
        static std::chrono::steady_clock::time_point vpsSnapTime;

        const bool vpsRomChanged = vpsRom != currentRomName;
        if (vpsRomChanged) {
            vpsLastBall = -1;
        }
        if (vpsLastBall == 0 && currentBall > 0) {
            std::stringstream vpsMsg;
            vpsMsg << "{\"type\":\"round_start\","
                << "\"timestamp\":\"" << getTimestamp() << "\","
                << "\"rom\":\"" << currentRomName << "\""
                << addMachineIdField() << ","
                << "\"players\":" << playerCount << ","
                << "\"current_player\":" << currentPlayer << ","
                << "\"current_ball\":" << currentBall << "}";
            broadcastWebSocket(vpsMsg.str());
        }
        vpsLastBall = currentBall;

        const auto vpsNow = std::chrono::steady_clock::now();
        const bool vpsStateChanged = vpsRomChanged || playerCount != vpsSnapPlayers || currentPlayer != vpsSnapPlayer
                                  || currentBall != vpsSnapBall;
        const bool vpsDataChanged = liveNvram != vpsSnapData && (vpsNow - vpsSnapTime) >= std::chrono::milliseconds(1000);
        if (vpsStateChanged || vpsDataChanged) {
            vpsRom = currentRomName;
            vpsSnapPlayers = playerCount;
            vpsSnapPlayer = currentPlayer;
            vpsSnapBall = currentBall;
            vpsSnapData = liveNvram;
            vpsSnapTime = vpsNow;
            std::stringstream vpsMsg;
            vpsMsg << "{\"type\":\"nvram_snapshot\","
                << "\"timestamp\":\"" << getTimestamp() << "\","
                << "\"rom\":\"" << currentRomName << "\""
                << addMachineIdField() << ","
                << "\"players\":" << playerCount << ","
                << "\"current_player\":" << currentPlayer << ","
                << "\"current_ball\":" << currentBall << ","
                << "\"base\":" << nvramBaseAddress << ","
                << "\"size\":" << liveNvram.size() << ","
                << "\"data\":\"" << base64_encode(liveNvram.data(), liveNvram.size()) << "\"}";
            broadcastWebSocket(vpsMsg.str());
        }
    }
'''


def fix_psc_function(m: re.Match) -> str:
    args = [a.strip() for a in m.group(2).split(",")]
    if args and args[0] == "ScoreServerClass":
        args = args[1:]
    return f"PSC_FUNCTION{m.group(1)}({', '.join(args)})"


def main(path: str) -> None:
    file = Path(path)
    p = Patcher(file.read_text(encoding="utf-8"))
    if MARKER in p.text:
        print("[patch] already patched")
        return

    # controller access (replace the class lookup first, the helpers contain the same call as fallback)
    p.replace('scriptApi->GetClassDef("Controller");', "vpsControllerClass();", 'GetClassDef("Controller")')
    p.insert_before("// Get NVRAM data directly from PinMAME Controller via Scriptable API\n", CONTROLLER_HELPERS,
                    "getNVRAMFromController comment")

    # game start/end through the VPX player events
    p.replace(GAME_START_OLD, GAME_START_NEW, "onGameStart")
    p.insert_after(GAME_END, GAME_END_ADD, "onGameEnd")
    p.insert_after(PREPARE_FRAME, PREPARE_FRAME_ADD, "onPrepareFrame")
    for evt in ("START", "END"):
        p.replace(f"msgApi->GetMsgID(CTLPI_NAMESPACE, CTLPI_EVT_ON_GAME_{evt})",
                  f"msgApi->GetMsgID(VPXPI_NAMESPACE, VPXPI_EVT_ON_GAME_{evt})", f"CTLPI_EVT_ON_GAME_{evt}")
    if "CTLPI_EVT_ON_GAME" in p.text:
        sys.exit("[patch] CTLPI_EVT_ON_GAME is still used")

    # API signature changes
    p.text, count = re.subn(r"msgApi->UnsubscribeMsg\((\w+),\s*(\w+)\);", r"msgApi->UnsubscribeMsg(\1, \2, nullptr);", p.text)
    if count == 0:
        sys.exit("[patch] upstream source changed, not found: UnsubscribeMsg")
    p.text = re.sub(r"PSC_FUNCTION(\d+)\(([^)]*)\)", fix_psc_function, p.text)
    p.replace("PSC_CLASS_START(ScoreServerClass)\n", "PSC_CLASS_START(ScoreServerClass, ScoreServerClass)\n", "PSC_CLASS_START")
    p.replace("PSC_CLASS_END(ScoreServerClass)", "PSC_CLASS_END()", "PSC_CLASS_END")
    p.replace("RegisterScoreServerClassSCD(", "RegisterScoreServerClass(", "RegisterScoreServerClassSCD")
    p.replace("scriptApi->SubmitTypeLibrary();", "scriptApi->SubmitTypeLibrary(endpointId);", "SubmitTypeLibrary")

    # round_start + nvram_snapshot, in the per frame score check (not in the logger, which decodes the ball too)
    function = "void checkAndBroadcastCurrentScores() {"
    ball = "    int currentBall = decodeValue(liveNvram, currentBallObj);\n"
    p.require(function, "checkAndBroadcastCurrentScores")
    pos = p.text.find(ball, p.text.index(function))
    if pos < 0:
        sys.exit("[patch] upstream source changed, not found: currentBall in checkAndBroadcastCurrentScores")
    pos += len(ball)
    p.text = p.text[:pos] + STUDIO_MESSAGES + p.text[pos:]

    file.write_text(f"// {MARKER} (vpin-studio/.github/vpx-plugins/patch_score_server.py)\n" + p.text, encoding="utf-8", newline="\n")
    print("[patch] score-server patched")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(sys.argv[1])
