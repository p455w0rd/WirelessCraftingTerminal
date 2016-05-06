package net.p455w0rd.wirelesscraftingterminal.client.gui.widgets;

import cpw.mods.fml.client.config.GuiCheckBox;
import cpw.mods.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class WCTGuiCheckBox extends GuiCheckBox {
	
	private boolean isChecked;
    private int     boxWidth;

	public WCTGuiCheckBox(int id, int xPos, int yPos, String displayString, boolean isChecked, int width) {
		super(id, xPos, yPos, displayString, isChecked);
		this.isChecked = isChecked;
        this.boxWidth = 11;
        this.height = 11;
        //this.width = this.boxWidth + 2 + Minecraft.getMinecraft().fontRenderer.getStringWidth(displayString);
        this.width = width;
	}
	
	@Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.boxWidth && mouseY < this.yPosition + this.height;
            GuiUtils.drawContinuousTexturedBox(buttonTextures, this.xPosition + (this.width - this.boxWidth), this.yPosition, 0, 46, this.boxWidth, this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
            this.mouseDragged(mc, mouseX, mouseY);
            int color = 14737632;
            
            if (!this.enabled)
            {
                color = 10526880;
            }
            else {
            	color = 4210752;
            }
            if (this.isChecked)
                this.drawCenteredString(mc.fontRenderer, "✔", this.xPosition + (this.width - this.boxWidth) + this.boxWidth / 2 + 1, this.yPosition + 1, 0x00FF00);
            
            this.drawStringNoShadow(mc.fontRenderer, displayString, xPosition, yPosition + 2, color);
        }
    }
	
	public void drawStringNoShadow(FontRenderer p_73731_1_, String p_73731_2_, int p_73731_3_, int p_73731_4_, int p_73731_5_)
    {
        p_73731_1_.drawString(p_73731_2_, p_73731_3_, p_73731_4_, p_73731_5_);
    }
	
	@Override
    public boolean mousePressed(Minecraft p_146116_1_, int p_146116_2_, int p_146116_3_)
    {
        if (this.enabled && this.visible && p_146116_2_ >= this.xPosition && p_146116_3_ >= this.yPosition && p_146116_2_ < this.xPosition + this.width && p_146116_3_ < this.yPosition + this.height)
        {
            this.isChecked = !this.isChecked;
            return true;
        }
        
        return false;
    }
    
    public boolean isChecked()
    {
        return this.isChecked;
    }
    
    public void setIsChecked(boolean isChecked)
    {
        this.isChecked = isChecked;
    }

}
