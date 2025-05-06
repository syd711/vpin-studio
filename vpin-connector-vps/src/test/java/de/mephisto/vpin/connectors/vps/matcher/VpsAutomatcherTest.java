package de.mephisto.vpin.connectors.vps.matcher;

import org.apache.commons.lang3.StringUtils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;

public class VpsAutomatcherTest {

  private void doMatch(VpsAutomatcher matcher, VPS vpsDatabase, String gameFileName, 
            String rom, String author, String version, 
            String expectedTableId, String expectedVersionId, String parsedVersion) {

    String[] tableFormats = null;

    // first run a match with findClosest
    VpsMatch vpsMatch = matcher.autoMatch(vpsDatabase, tableFormats, gameFileName, rom, author, version);

    // do control
    assertEquals(expectedTableId, vpsMatch.getExtTableId());
    assertEquals(expectedVersionId, vpsMatch.getExtTableVersionId());

    // optional test on parsed version
    if (StringUtils.isNotEmpty(parsedVersion)) {
        assertEquals(parsedVersion, vpsMatch.getVersion());
    }

    // redo a quick check with vpsMatch filled to confirm the match, match should not change 
    matcher.autoMatch(vpsMatch, vpsDatabase, tableFormats, gameFileName, rom, null, author, version, null, true);
    assertEquals(expectedTableId, vpsMatch.getExtTableId());
    if (StringUtils.isNotEmpty(expectedVersionId)) {
        assertEquals(expectedVersionId, vpsMatch.getExtTableVersionId());
    }

    // redo a full check with vpsMatch filled to confirm the match, match should not change 
    matcher.autoMatch(vpsMatch, vpsDatabase, tableFormats, gameFileName, rom, null, author, version, null, true);
    assertEquals(expectedTableId, vpsMatch.getExtTableId());
    if (StringUtils.isNotEmpty(expectedVersionId)) {
        assertEquals(expectedVersionId, vpsMatch.getExtTableVersionId());
    }

    // Do the check via autoMatchTable method
    VpsTable vpsTable = matcher.autoMatchTable(vpsDatabase, gameFileName, rom);
    assertEquals(expectedTableId, vpsTable.getId());
  }  

  @Test
  public void testMatch() {
    // load the database from resources
    VPS vpsDatabase = new VPS();
    vpsDatabase.reload();

    VpsAutomatcher matcher = VpsAutomatcher.getInstance();

    // how to add a table in test: starts with propper version and use debugger or VPS to get ids
    // add derivation, add non-matched real cases

    doMatch(matcher, vpsDatabase, "24 (Stern 2009)", "", null, null, "P15WeTaO", null, "");
    doMatch(matcher, vpsDatabase, "24 (Stern 2009) VPX08 (DT-FS-FSS-VR) v.2.1", "", null, null, "P15WeTaO", "QcOG228URt", "2.1");

    doMatch(matcher, vpsDatabase, "2001 (Gottlieb 1971)", "", null, null, "sMBqx5fp", null, "");
    doMatch(matcher, vpsDatabase, "2001", "", null, null, "sMBqx5fp", null, "");
    doMatch(matcher, vpsDatabase, "2001", "", "Wrd1972, Loserman76", null, "sMBqx5fp", "hOl7UpABdl", "");

    doMatch(matcher, vpsDatabase, "Cactus Canyon (Bally 1998)", "cc_13", null, "", "vZDUDUii", null, "");
    doMatch(matcher, vpsDatabase, "Cactus Canyon (Bally 1998)", "cc_13", null, "VP10", "vZDUDUii", null, "");
    doMatch(matcher, vpsDatabase, "Cactus Canyon (Bally 1998)", "", null, "VP10.1", "vZDUDUii", null, "");
    doMatch(matcher, vpsDatabase, "Cactus Canyon (1998 Bally)", "cc_13", null, "1.0.1", "vZDUDUii", "Z7qrllPW", "1.0.1");
    doMatch(matcher, vpsDatabase, "Cactus Canyon", "", null, null, "vZDUDUii", null, "");
    doMatch(matcher, vpsDatabase, "VP10_Cactus_Canyon_Dozer_RTM_1.2", "", null, null, "vZDUDUii", "2mGdjGXkXV", "1.2");
    doMatch(matcher, vpsDatabase, "Cactus Canyon (Bally 1998)1.2R2", "cc_13", null, null, "vZDUDUii", "Y2qyhzHwra", "1.2r2");
    doMatch(matcher, vpsDatabase, "Cactus Canyon (Bally 1998) VPW 1.1", "cc_13", null, null, "vZDUDUii", "Z7qrllPW", "1.1");
    //---
    doMatch(matcher, vpsDatabase, "Cactus Canyon Continued (Original 2019)", "", null, null, "gBpX0hxi", null, "");
    doMatch(matcher, vpsDatabase, "Cactus Canyon Continued", "", "VPW Team", null, "gBpX0hxi", "ZgnyWyHG", "");
    doMatch(matcher, vpsDatabase, "Cactus Canyon Continued by Ninuzzu 1.1", "", null, null, "gBpX0hxi", "R0tbYz3t", "1.1");

    doMatch(matcher, vpsDatabase, "Capersville (Bally)(1966)(scottacus)[(3.1.3)[VPX04][FS+DOF arngrim]", "", null, null, "27OdyImH", "eRMSr3iN", "3.1.3");
    doMatch(matcher, vpsDatabase, "Capersville (1966)", "", null, null, "27OdyImH", "eRMSr3iN", "");
    doMatch(matcher, vpsDatabase, "Capersville (William1966)", "", null, null, "27OdyImH", "eRMSr3iN", "");

    doMatch(matcher, vpsDatabase, "Death Race 2000 (Original 2022)", "", null, null, "F8hMdDku36", "AX1V-lGKrI", ""); // only one version

    doMatch(matcher, vpsDatabase, "Demolition Man (Williams 1994)", "", null, null, "vT78ph4O", null, "");
    doMatch(matcher, vpsDatabase, "Demolition Man (Williams 1994)_Bigus(MOD)1.2", "", null, null, "vT78ph4O", "uN4crZzX", "1.2");
    doMatch(matcher, vpsDatabase, "Demolition Man (Williams1994)v1.2R2.5-[D&N][FSS][DMD][TOP][CCX+PGI][SHD]", "", null, null, "vT78ph4O", "ifVdLU3h", "1.2r2.5");

    doMatch(matcher, vpsDatabase, "Dirty Harry (Williams1995)", "", null, null, "my2RZ_v_", null, "");
    doMatch(matcher, vpsDatabase, "Dirty Harry 2.0 (Williams1995).shiny mod", "", null, null, "my2RZ_v_", "PSyhidSh", "2.0"); // match on shiny mod

    doMatch(matcher, vpsDatabase, "Dr. Dude and His Excellent Ray (Bally 1990)", "", null, null, "klXtdvkv", null, "");
    doMatch(matcher, vpsDatabase, "Dr Dude (Midway 1990)", "", null, null, "klXtdvkv", null, "");
    doMatch(matcher, vpsDatabase, "Dr Dude (Bally 1990)2.2", "", "Arconovum", null, "klXtdvkv", "Gu0H6FKV", "2.2");
    doMatch(matcher, vpsDatabase, "Dr Dude (1990)(Bally)_2.1b", "", "Arconovum", null, "klXtdvkv", "Gu0H6FKV", "2.1b");

    doMatch(matcher, vpsDatabase, "Electric Mayhem (Original 2016)", "", null, null, "cDa0_8QG", "40-_M06J", "");
    doMatch(matcher, vpsDatabase, "electricmayhem- Lodger 2016.vpx", "", null, null, "cDa0_8QG", "40-_M06J", "");

    doMatch(matcher, vpsDatabase, "Fish Tales (Williams 1992)", "", null, null, "tV1GotAP", null, "");
    doMatch(matcher, vpsDatabase, "Fish Tales (Williams 1992)_JLMD Edition v0.8", "", null, null, "tV1GotAP", "PI8WMVQOwS", "0.8");

    doMatch(matcher, vpsDatabase, "Guns N' Roses (Data East 1994)", "", null, null, "M7FYR1GJ", null, "");
    doMatch(matcher, vpsDatabase, "Guns N' Roses (1994DataEast)", "", null, null, "M7FYR1GJ", null, "");
    doMatch(matcher, vpsDatabase, "Guns&Roses (1994 Data East)", "", null, null, "M7FYR1GJ", null, "");
    doMatch(matcher, vpsDatabase, "Guns n' Roses (DataEast1994) desktop version", "", "Versins77, Bigus1, TeamPP", null, "M7FYR1GJ", "SV-Ul3CNuE", "");
    doMatch(matcher, vpsDatabase, "Guns N Roses (Data East 1994) VPW v1.1", "", null, null, "M7FYR1GJ", "1cD0itEsay", "1.1");
    doMatch(matcher, vpsDatabase, "Guns & Roses (Data East 1994) VPW R1.1", "", null, null, "M7FYR1GJ", "1cD0itEsay", "1.1");
    doMatch(matcher, vpsDatabase, "Guns N Roses (1994) 4K MOD 2.0", "", null, null, "M7FYR1GJ", "6T4X2Moa", "2.0");
    doMatch(matcher, vpsDatabase, "Guns and Roses (1994) Bigus(MOD)1.0", "", null, null, "M7FYR1GJ", "qRZWl6SA", "1.0");

    doMatch(matcher, vpsDatabase, "Harlem Globetrotters on Tour (Bally 1979)", "", null, null, "ZrSKKrc4", null, "");
    doMatch(matcher, vpsDatabase, "Harlem_Globetrotters_1.2.0 Mikcab MOD", "", null, null, "ZrSKKrc4", "9CGwinY1yQ", "1.2.0");
    doMatch(matcher, vpsDatabase, "Harlem Globetrotters (Bally 1979)_VPX_DOZER_1.0a", "", null, null, "ZrSKKrc4", "Q0LATrbd", "1.0a");

    doMatch(matcher, vpsDatabase, "Haunted House (Gottlieb 1982) 1.0", "", "Arconovum", null, "q0RmE-GZ", "KvxmJruh", "1.0");
    doMatch(matcher, vpsDatabase, "Haunted House (Gottlieb 1982) VPW 2.1", "", null, null, "q0RmE-GZ", "TscqlNWDQz", "2.1");

    doMatch(matcher, vpsDatabase, "Indiana Jones (Stern 2008)", "", null, null, "NqaGRv8k", null, "");
    doMatch(matcher, vpsDatabase, "VP10_Indiana Jones (Stern 2008)-Hanibal-2.4", "", null, null, "NqaGRv8k", "i3khx2zJ", "2.4");

    doMatch(matcher, vpsDatabase, "Goldeneye (Sega 1996)", "", null, null, "lU4NSKWp", null, "");
    doMatch(matcher, vpsDatabase, "007 Goldeneye (Sega 1996)", "", null, null, "lU4NSKWp", null, "");
    //----
    doMatch(matcher, vpsDatabase, "James Bond 007 (Gottlieb 1980)", "", null, null, "628qJGb1", null, "");
    doMatch(matcher, vpsDatabase, "James Bond 007 (1980)", "", "Teisen, 23assassin", null, "628qJGb1", "Z5yroNaS20", "");
    doMatch(matcher, vpsDatabase, "James Bond 007 (Gottlieb 1980) 1.0", "", null, null, "628qJGb1", "A1wxTjE3", "1.0");

    doMatch(matcher, vpsDatabase, "Kingpin (Capcom 1996)", "", null, null, "hM7A-E0Z", null, "");
    doMatch(matcher, vpsDatabase, "Kingpin (Capcom)(1996) FS_2_1a_DX9", "", null, null, "hM7A-E0Z", "ASgpE6Yx", "2.1a");
    doMatch(matcher, vpsDatabase, "Kingpin (Capcom 1996) Bigus(MOD) 1.1", "", null, null, "hM7A-E0Z", "ASgpE6Yx", "1.1");

    doMatch(matcher, vpsDatabase, "Laser Ball (Williams 1979)", "", null, null, "mcfh2SWU", null, "");
    doMatch(matcher, vpsDatabase, "Laser Ball (Williams 1979)1.0b", "", null, null, "mcfh2SWU", "J0H5WwfJ", "1.0b"); 
    doMatch(matcher, vpsDatabase, "LaserBall_VP9.2_1.03_FS", "", null, null, "mcfh2SWU", null, "1.03");
    doMatch(matcher, vpsDatabase, "Laser Ball (Williams)(1979)(Allknowing2012)(1.0)[VPX05][DT+FS]", "", null, null, "mcfh2SWU", "J0H5WwfJ", "1.0");

    doMatch(matcher, vpsDatabase, "No Fear - Dangerous Sports (Williams 1995)", "", null, null, "mtKGKnBh", null, "");
    doMatch(matcher, vpsDatabase, "No Fear - Dangerous Sports (Williams 1994)", "", null, null, "mtKGKnBh", null, "");
    doMatch(matcher, vpsDatabase, "No Fear Dangerous Sports (Williams 1994)", "", null, null, "mtKGKnBh", null, "");
    doMatch(matcher, vpsDatabase, "No Fear Dangerous Sports (Bally 1996)", "", null, null, "mtKGKnBh", null, "");
    doMatch(matcher, vpsDatabase, "No Fear (Williams 1995) VPW v1.0", "", null, null, "mtKGKnBh", "nRFoCEXqBB", "1.0");
    doMatch(matcher, vpsDatabase, "No Fear-Dangerous Sports (Williams 1995)_Bigus(MOD)2.0", "", null, null, "mtKGKnBh", "sOp56Ke2", "2.0");

    doMatch(matcher, vpsDatabase, "Pirates of the Caribbean (Stern 2006)", "", null, null, "fhehIu6i", null, "");
    doMatch(matcher, vpsDatabase, "Pirates of the Caribbean (Stern 2006) Siggis Mod 1.0", "", null, null, "fhehIu6i", "rOx6ED7h", "1.0");
    doMatch(matcher, vpsDatabase, "Pirates of the Caribbean (Stern 2006)1.1", "", null, null, "fhehIu6i", "_Xbzc1ag", "1.1");

    doMatch(matcher, vpsDatabase, "Stephen King's Pet Sematary (Original 2019)", "", null, null, "uP4_P2lE", "nnDQCFrX", "");
    doMatch(matcher, vpsDatabase, "Pet Sematary (TBA 2019)", "", null, null, "uP4_P2lE", "nnDQCFrX", "");

    doMatch(matcher, vpsDatabase, "The Getaway - High Speed II (Williams 1992)", "", null, null, "cCV4A6uc", null, "");
    doMatch(matcher, vpsDatabase, "Getaway, The - High Speed II (Williams 1992)", null, null, "", "cCV4A6uc", null, "");
    doMatch(matcher, vpsDatabase, "The Getaway High Speed II (Williams 1992) v2_0", "", null, null, "cCV4A6uc", "YoXfmzDV", "2.0");
    doMatch(matcher, vpsDatabase, "The Getaway High Speed II (Williams 1992) Bigus(MOD) 2.0", "", null, null, "cCV4A6uc", "YoXfmzDV", "2.0");
    doMatch(matcher, vpsDatabase, "Getaway, The - High Speed II v1.03", null, null, "", "cCV4A6uc", "UNbB3Z3a", "1.03");
    doMatch(matcher, vpsDatabase, "Getaway 1.0 - FSS", "", null, null, "cCV4A6uc", "UNbB3Z3a", "1.0");

    doMatch(matcher, vpsDatabase, "The Who's Tommy Pinball Wizard (Data East 1994)", "tomy_500", null, null, "p3HP_P2H", null, "");
    doMatch(matcher, vpsDatabase, "The Who's Tommy Pinball Wizard (Data East 1994)", "", null, null, "p3HP_P2H", null, "");
    //fixme doMatch(matcher, vpsDatabase, "Tommy_VP10_Data East_1.1", "tomy_500", "Ninuzzu", null, "p3HP_P2H", "vvVop8xT", "1.1");
    doMatch(matcher, vpsDatabase, "The Who's Tommy Pinball Wizard (Data East 1994) VPWMod 1.2.1", "", null, null, "p3HP_P2H", "T8COSpvkl-", "1.2.1");
    doMatch(matcher, vpsDatabase, "Tommy Pinball Wizard, The Who's (Data East 1994)", null, null, "", "p3HP_P2H", null, "");
    //-----
    doMatch(matcher, vpsDatabase, "Tommy Boy_VP10_1.1", "", null, null, "-nZmKE8kts", "IFe_O5ZeeC", "1.1");

    doMatch(matcher, vpsDatabase, "Vegas (Gottlieb 1990)", "", null, null, "VJRn9QME", "f90d5qRk", "");
    doMatch(matcher, vpsDatabase, "Vegas (Premier - 1990)1.0.1", "", null, null, "VJRn9QME", "f90d5qRk", "1.0.1");
    doMatch(matcher, vpsDatabase, "Vegas (1990)", "", null, null, "VJRn9QME", "f90d5qRk", "");

    // grrr, some author naming their table v.1 really wanted to make matching of table complicated :)
    doMatch(matcher, vpsDatabase, "v.1 (IDSA 1986)", "", null, null, "32OBVwzC", null, "");  
    doMatch(matcher, vpsDatabase, "v.1 (IDSA 1986)_2.0a", "", null, null, "32OBVwzC", "s8NeWzRw", "2.0a");  
    doMatch(matcher, vpsDatabase, "v.1 (1986) v2.0", "", null, null, "32OBVwzC", "s8NeWzRw", "2.0");  
  }

}
