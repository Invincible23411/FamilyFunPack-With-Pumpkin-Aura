package family_fun_pack.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import family_fun_pack.SpecialTagCompound;
import family_fun_pack.Tooltip;

@SideOnly(Side.CLIENT)
public class InfoItemGui extends GuiScreen {

  private static int WIDTH = 320;
  private static int HEIGHT = 136;

  public static int lineMaxChar = 54;
  public static int maxLines = 13;

  private int x, y, x_end, y_end;

  public int current_slot;

  private String title;

  private List<String> tag;

  private ScrollBar scroll;
  private OpenButton previewOpen;

  public Container inventory;

  private Tooltip tooltip;

  public InfoItemGui(Container inventory, Tooltip tooltip) {
    this.inventory = inventory;
    this.current_slot = -1;
    this.tag = new ArrayList<String>();
    this.tooltip = tooltip;
  }

  public void initGui() {
    this.x = (this.width / 2) - (InfoItemGui.WIDTH / 2);
    this.y = (this.height / 2) - (InfoItemGui.HEIGHT / 2);
    this.x_end = this.x + InfoItemGui.WIDTH;
    this.y_end = this.y + InfoItemGui.HEIGHT;
    this.scroll = new ScrollBar(0, this.x_end - 8, this.y + 20, 0, this.y_end - 2);
    InfoItemGui.lineMaxChar = ((this.x_end - 10 - (this.x + 89)) / (int)(((float)this.fontRenderer.getStringWidth("A")) * 0.7f)) - 5;
    InfoItemGui.maxLines = ((this.y_end - 2 - (this.y + 21)) / this.fontRenderer.FONT_HEIGHT);
  }

  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if(keyCode == Keyboard.KEY_ESCAPE || keyCode == this.tooltip.openGUIKey.getKeyCode()) {
      this.mc.displayGuiScreen(null);
      if (this.mc.currentScreen == null) {
        this.mc.setIngameFocus();
      }
    }
  }

  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    Gui.drawRect(this.x, this.y, this.x_end, this.y_end, CommandGui.BACKGROUND_COLOR); // GUI background

    // borders
    Gui.drawRect(this.x, this.y, this.x_end, this.y + 1, 0xffbbbbbb);
    Gui.drawRect(this.x, this.y, this.x + 1, this.y_end, 0xffbbbbbb);
    Gui.drawRect(this.x_end - 1, this.y, this.x_end, this.y_end, 0xffbbbbbb);
    Gui.drawRect(this.x, this.y_end - 1, this.x_end, this.y_end, 0xffbbbbbb);

    // items border
    int info_x = this.x + 4;
    int info_y = this.y + 4;
    int info_x_end = this.x + 84;
    int info_y_end = this.y + 132;
    Gui.drawRect(info_x, info_y, info_x_end, info_y_end, 0xff999999);
    for(int i = 0; i < 6; i ++) {
      Gui.drawRect(info_x - 1 + i*16, info_y, info_x + i*16, info_y_end, 0xffcccccc);
    }
    for(int i = 0; i < 9; i ++) {
      Gui.drawRect(info_x, info_y - 1 + i*16, info_x_end, info_y + i*16, 0xffcccccc);
    }

    // draw tag border
    info_x = this.x + 86;
    info_y = this.y + 18;
    info_x_end = this.x_end - 1;
    info_y_end = this.y_end - 1;
    Gui.drawRect(info_x, info_y, info_x_end, info_y + 1, 0xffbbbbbb);
    Gui.drawRect(info_x, info_y, info_x + 1, info_y_end, 0xffbbbbbb);
    //Gui.drawRect(info_x_end - 1, info_y, info_x_end, info_y_end, 0xffbbbbbb);
    //Gui.drawRect(info_x, info_y_end - 1, info_x_end, info_y_end, 0xffbbbbbb);

    // Draw preview open button
    if(this.previewOpen != null) this.previewOpen.drawButton(this.mc, mouseX, mouseY, partialTicks);

    // Draw tag
    if(this.current_slot != -1) {

      // Draw title
      int width = this.fontRenderer.getStringWidth(this.title);
      info_x += ((this.x_end - 2 - info_x) / 2 - (width) / 2);
      this.fontRenderer.drawStringWithShadow(this.title, info_x, this.y + 4, 0xffbbbbbb);

      GlStateManager.pushMatrix();
      float scale = 0.7f;
      GlStateManager.scale(scale, scale, scale);
      info_x = (int)(((float)this.x + 89f) / scale);

      for(int i = this.scroll.current_scroll; i < (this.scroll.current_scroll + InfoItemGui.maxLines) && i < this.tag.size(); i ++) {

        info_y = (int)(((float)this.y + 21f + (float)((i - this.scroll.current_scroll) * this.fontRenderer.FONT_HEIGHT)) / scale);
        this.drawString(this.fontRenderer, this.tag.get(i), info_x, info_y, 0xffbbbbbb);
      }

      GlStateManager.popMatrix();
    }

    // Update scroll
    if(this.scroll.clicked) {
      this.scroll.dragged(mouseX, mouseY);
    }

    // Draw scroll bar
    this.scroll.drawButton(this.mc, mouseX, mouseY, partialTicks);

    // Draw items
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    RenderHelper.enableGUIStandardItemLighting();
    for(int i = 0; i < 8; i ++) {
      for(int j = 0; j < 5; j ++) {
        Slot slot = this.inventory.getSlot((i * 5) + j + 5);
        ItemStack stack = slot.getStack();
        int x = this.x + 4 + j * 16;
        int y = this.y + 4 + i * 16;
        if(stack.isEmpty()) {
          TextureAtlasSprite sprite = slot.getBackgroundSprite();
          if (sprite != null) {
            GlStateManager.disableLighting();
            this.mc.getTextureManager().bindTexture(slot.getBackgroundLocation());
            drawTexturedModalRect(x, y, sprite, 16, 16);
            GlStateManager.enableLighting();
          }
        } else {
          // this.drawRect(x, y, x + 16, y + 16, -2130706433);
          GlStateManager.enableDepth();
          this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, stack, x, y);
          this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, stack, x, y, null);
        }
      }
    }

    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    if(mouseButton == 0) {

      if(this.scroll.mousePressed(this.mc, mouseX, mouseY)) return;
      else if(this.previewOpen != null && this.previewOpen.mousePressed(this.mc, mouseX, mouseY)) {
        this.previewOpen.performAction();
        return;
      }

      int x = (mouseX - 4 - this.x) / 16;
      int y = (mouseY - 4 - this.y) / 16;
      if(x >= 0 && x < 5 && y >= 0 && y < 8) {
        this.current_slot = (y * 5) + x + 5;
        ItemStack stack = this.inventory.getSlot(this.current_slot).getStack();
        if(!stack.isEmpty()) {
          this.title = stack.getDisplayName();

          NBTTagCompound tag = stack.getTagCompound();
          String tag_string;
          if(tag != null) {
            tag_string = tag.toString();
          } else tag_string = "Empty tag compound";

          this.tag.clear();
          this.tag.add("ItemStack damage field: " + Integer.toString(SpecialTagCompound.getStackDamage(stack))
            + ", NBTTagCompound:");
          for(int i = 0; i <= (tag_string.length() - 1) / InfoItemGui.lineMaxChar; i ++) {
            int begin = i * InfoItemGui.lineMaxChar;
            int end = begin + InfoItemGui.lineMaxChar;
            if(end > tag_string.length()) end = tag_string.length();
            this.tag.add(tag_string.substring(begin, end));
            int max_scroll = this.tag.size() - InfoItemGui.maxLines;
            if(max_scroll < 0) max_scroll = 0;
            this.scroll.resetMaxScroll(max_scroll);

            if(tag != null && tag.hasKey("BlockEntityTag") && tag.getTagId("BlockEntityTag") == 10) {
              NBTTagCompound blockTag = tag.getCompoundTag("BlockEntityTag");
              if(blockTag.hasKey("Items") && blockTag.getTagId("Items") == 9) {
                this.previewOpen = new OpenButton(0, this.x_end - 46, this.y + 3, this.fontRenderer, "Preview") {
                  public void performAction() {
                    InfoItemGui.this.mc.displayGuiScreen(new PreviewGui(
                      InfoItemGui.this.inventory.getSlot(InfoItemGui.this.current_slot).getStack().getTagCompound().getCompoundTag("BlockEntityTag").getTagList("Items", 10),
                      InfoItemGui.this,
                      InfoItemGui.this.tooltip
                    ));
                  };
                };
              } else this.previewOpen = null;
            } else this.previewOpen = null;

          }
        } else this.current_slot = -1;
      }
    }
  }

  public void mouseReleased(int mouseX, int mouseY, int state) {
    if(state == 0) {
      this.scroll.mouseReleased(mouseX, mouseY);
    }
  }

  public boolean doesGuiPauseGame() {
	   return false;
	}

}
