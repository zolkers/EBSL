package fr.riege.ebsl.ui.imgui;

import cn.enaium.fabric.imgui.DefaultImGui;
import fr.riege.ebsl.EbslMod;

public final class EbslImGuiService extends DefaultImGui {
    public EbslImGuiService() {
        super(EbslMod.MOD_ID);
    }
}
