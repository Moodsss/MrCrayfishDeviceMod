package com.mrcrayfish.device.core;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.device.MrCrayfishDeviceMod;
import com.mrcrayfish.device.Reference;
import com.mrcrayfish.device.api.ApplicationManager;
import com.mrcrayfish.device.api.app.Application;
import com.mrcrayfish.device.api.app.Dialog;
import com.mrcrayfish.device.api.app.Layout;
import com.mrcrayfish.device.api.app.System;
import com.mrcrayfish.device.api.app.component.Image;
import com.mrcrayfish.device.api.io.Drive;
import com.mrcrayfish.device.api.io.File;
import com.mrcrayfish.device.api.task.Callback;
import com.mrcrayfish.device.api.task.Task;
import com.mrcrayfish.device.api.task.TaskManager;
import com.mrcrayfish.device.api.utils.RenderUtil;
import com.mrcrayfish.device.core.client.LaptopFontRenderer;
import com.mrcrayfish.device.core.task.TaskInstallApp;
import com.mrcrayfish.device.object.AppInfo;
import com.mrcrayfish.device.programs.system.SystemApplication;
import com.mrcrayfish.device.programs.system.component.FileBrowser;
import com.mrcrayfish.device.programs.system.task.TaskUpdateApplicationData;
import com.mrcrayfish.device.programs.system.task.TaskUpdateSystemData;
import com.mrcrayfish.device.tileentity.TileEntityLaptop;
import com.mrcrayfish.device.util.GuiHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.IOException;
import java.util.*;

//TODO Intro message (created by mrcrayfish, donate here)

public class Laptop extends GuiScreen implements System
{
	public static final int ID = 1;
	
	private static final ResourceLocation LAPTOP_GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/laptop.png");

	public static final ResourceLocation ICON_TEXTURES = new ResourceLocation(Reference.MOD_ID, "textures/atlas/app_icons.png");
	public static final int ICON_SIZE = 14;

	public static final FontRenderer fontRenderer = new LaptopFontRenderer(Minecraft.getMinecraft());

	private static final List<Application> APPLICATIONS = new ObjectArrayList<>();
	private static final List<ResourceLocation> WALLPAPERS = new ObjectArrayList<>();

	private static final int BORDER = 10;
	private static final int DEVICE_WIDTH = 384;
	private static final int DEVICE_HEIGHT = 216;
	static final int SCREEN_WIDTH = DEVICE_WIDTH - BORDER * 2;
	static final int SCREEN_HEIGHT = DEVICE_HEIGHT - BORDER * 2;

	private static System system;
	private static BlockPos pos;
	private static Drive mainDrive;

	private final Settings settings;
	private final TaskBar bar;
	private final LaptopWindowManager windowManager;
	private Layout context = null;

	private final NBTTagCompound appData;
	private final NBTTagCompound systemData;

	private int currentWallpaper;
	private int lastMouseX, lastMouseY;
	protected boolean dragging = false;

	protected final List<AppInfo> installedApps = new ObjectArrayList<>();

	public Laptop(TileEntityLaptop laptop)
	{
		this.appData = laptop.getApplicationData();
		this.systemData = laptop.getSystemData();
		this.windowManager = new LaptopWindowManager(this);
		this.settings = Settings.fromTag(systemData.getCompoundTag("Settings"));
		this.bar = new TaskBar(this);
		this.currentWallpaper = systemData.getInteger("CurrentWallpaper");
		if(currentWallpaper < 0 || currentWallpaper >= WALLPAPERS.size()) {
			this.currentWallpaper = 0;
		}
		Laptop.system = this;
		Laptop.pos = laptop.getPos();
	}

	/**
	 * Returns the position of the laptop the player is currently using. This method can ONLY be
	 * called when the laptop GUI is open, otherwise it will return a null position.
	 *
	 * @return the position of the laptop currently in use
	 */
	@Nullable
	public static BlockPos getPos()
	{
		return pos;
	}

	@Override
	public void initGui() 
	{
		Keyboard.enableRepeatEvents(true);
		int posX = (width - DEVICE_WIDTH) / 2;
		int posY = (height - DEVICE_HEIGHT) / 2;
		bar.init(posX + BORDER, posY + DEVICE_HEIGHT - 28);

		installedApps.clear();
		NBTTagList tagList = systemData.getTagList("InstalledApps", Constants.NBT.TAG_STRING);
		for(int i = 0; i < tagList.tagCount(); i++)
		{
			AppInfo info = ApplicationManager.getApplication(tagList.getStringTagAt(i));
			if(info != null)
			{
				installedApps.add(info);
			}
		}
		installedApps.sort(AppInfo.SORT_NAME);
	}

	@Override
	public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);

        this.windowManager.onGuiClose();

		/* Send system data */
		this.updateSystemData();

		Laptop.pos = null;
        Laptop.system = null;
		Laptop.mainDrive = null;
    }

    private void updateSystemData()
	{
		systemData.setInteger("CurrentWallpaper", currentWallpaper);
		systemData.setTag("Settings", settings.toTag());

		NBTTagList tagListApps = new NBTTagList();
		installedApps.forEach(info -> tagListApps.appendTag(new NBTTagString(info.getFormattedId())));
		systemData.setTag("InstalledApps", tagListApps);

		TaskManager.sendTask(new TaskUpdateSystemData(pos, systemData));
	}
	
	@Override
	public void onResize(@NotNull Minecraft mcIn, int width, int height)
	{
		super.onResize(mcIn, width, height);
		this.windowManager.markForUpdate();
	}
	
	@Override
	public void updateScreen()
	{
		this.bar.onTick();

		this.windowManager.onTick();

		FileBrowser.refreshList = false;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) 
	{
		//Fixes the strange partialTicks that Forge decided to give us
		partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

		this.drawDefaultBackground();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(LAPTOP_GUI);
		
		/* Physical Screen */
		int posX = (width - DEVICE_WIDTH) / 2;
		int posY = (height - DEVICE_HEIGHT) / 2;
		
		/* Corners */
		this.drawTexturedModalRect(posX, posY, 0, 0, BORDER, BORDER); // TOP-LEFT
		this.drawTexturedModalRect(posX + DEVICE_WIDTH - BORDER, posY, 11, 0, BORDER, BORDER); // TOP-RIGHT
		this.drawTexturedModalRect(posX + DEVICE_WIDTH - BORDER, posY + DEVICE_HEIGHT - BORDER, 11, 11, BORDER, BORDER); // BOTTOM-RIGHT
		this.drawTexturedModalRect(posX, posY + DEVICE_HEIGHT - BORDER, 0, 11, BORDER, BORDER); // BOTTOM-LEFT
		
		/* Edges */
		RenderUtil.drawRectWithTexture(posX + BORDER, posY, 10, 0, SCREEN_WIDTH, BORDER, 1, BORDER); // TOP
		RenderUtil.drawRectWithTexture(posX + DEVICE_WIDTH - BORDER, posY + BORDER, 11, 10, BORDER, SCREEN_HEIGHT, BORDER, 1); // RIGHT
		RenderUtil.drawRectWithTexture(posX + BORDER, posY + DEVICE_HEIGHT - BORDER, 10, 11, SCREEN_WIDTH, BORDER, 1, BORDER); // BOTTOM
		RenderUtil.drawRectWithTexture(posX, posY + BORDER, 0, 11, BORDER, SCREEN_HEIGHT, BORDER, 1); // LEFT
		
		/* Center */
		RenderUtil.drawRectWithTexture(posX + BORDER, posY + BORDER, 10, 10, SCREEN_WIDTH, SCREEN_HEIGHT, 1, 1);
		
		/* Wallpaper */
		this.mc.getTextureManager().bindTexture(WALLPAPERS.get(currentWallpaper));
		RenderUtil.drawRectWithFullTexture(posX + 10, posY + 10, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

		if(!MrCrayfishDeviceMod.DEVELOPER_MODE)
		{
			drawString(fontRenderer, "Alpha v" + Reference.VERSION, posX + BORDER + 5, posY + BORDER + 5, Color.WHITE.getRGB());
		}
		else
		{
			drawString(fontRenderer, "Developer Version - " + Reference.VERSION, posX + BORDER + 5, posY + BORDER + 5, Color.WHITE.getRGB());
		}

		boolean insideContext = false;
		if(context != null)
		{
			insideContext = GuiHelper.isMouseInside(mouseX, mouseY, context.xPosition, context.yPosition, context.xPosition + context.width, context.yPosition + context.height);
		}

		Image.CACHE.forEach((s, cachedImage) -> cachedImage.delete());

		/* Window */
		this.windowManager.onRender(posX + BORDER, posY + BORDER, mouseX, mouseY, insideContext, partialTicks);

		/* Application Bar */
		bar.render(this, mc, posX + 10, posY + DEVICE_HEIGHT - 28, mouseX, mouseY, partialTicks);

		if(context != null)
		{
			context.render(this, mc, context.xPosition, context.yPosition, mouseX, mouseY, true, partialTicks);
		}

		Image.CACHE.entrySet().removeIf(entry ->
		{
			Image.CachedImage cachedImage = entry.getValue();
			if(cachedImage.isDynamic() && cachedImage.isPendingDeletion())
			{
				int texture = cachedImage.getTextureId();
				if(texture != -1)
				{
					GL11.glDeleteTextures(texture);
				}
				return true;
			}
			return false;
		});

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		this.lastMouseX = mouseX;
		this.lastMouseY = mouseY;
		
		int posX = (width - SCREEN_WIDTH) / 2;
		int posY = (height - SCREEN_HEIGHT) / 2;

		if(this.context != null)
		{
			int dropdownX = context.xPosition;
			int dropdownY = context.yPosition;
			if(GuiHelper.isMouseInside(mouseX, mouseY, dropdownX, dropdownY, dropdownX + context.width, dropdownY + context.height))
			{
				this.context.handleMouseClick(mouseX, mouseY, mouseButton);
				return;
			}
			else
			{
				this.context = null;
			}
		}

		this.bar.handleClick(this, posX, posY + SCREEN_HEIGHT - TaskBar.BAR_HEIGHT, mouseX, mouseY, mouseButton);

		this.windowManager.onMouseClicked(posX, posY, mouseX, mouseY, mouseButton);
		
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) 
	{
		super.mouseReleased(mouseX, mouseY, state);
		this.dragging = false;
		if(this.context != null)
		{
			int dropdownX = context.xPosition;
			int dropdownY = context.yPosition;
			if(GuiHelper.isMouseInside(mouseX, mouseY, dropdownX, dropdownY, dropdownX + context.width, dropdownY + context.height))
			{
				this.context.handleMouseRelease(mouseX, mouseY, state);
			}
		}

		Window<?> window = this.windowManager.currentWindow();
		if(window != null)
		{
			window.handleMouseRelease(mouseX, mouseY, state);
		}
	}
	
	@Override
	public void handleKeyboardInput() throws IOException
    {
        if (Keyboard.getEventKeyState())
        {
        	char pressed = Keyboard.getEventCharacter();
        	int code = Keyboard.getEventKey();

			Window<?> window = this.windowManager.currentWindow();
			if(window != null)
			{
				window.handleKeyTyped(pressed, code);
			}
            
            super.keyTyped(pressed, code);
        }
        else
        {
			Window<?> window = this.windowManager.currentWindow();
			if(window != null)
			{
				window.handleKeyReleased(Keyboard.getEventCharacter(), Keyboard.getEventKey());
			}
        }

        this.mc.dispatchKeypresses();
    }
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) 
	{
		int posX = (width - SCREEN_WIDTH) / 2;
		int posY = (height - SCREEN_HEIGHT) / 2;

		if(this.context != null)
		{
			int dropdownX = context.xPosition;
			int dropdownY = context.yPosition;
			if(GuiHelper.isMouseInside(mouseX, mouseY, dropdownX, dropdownY, dropdownX + context.width, dropdownY + context.height))
			{
				this.context.handleMouseDrag(mouseX, mouseY, clickedMouseButton);
			}
			return;
		}

		Window<?> window = this.windowManager.currentWindow();
		if(window != null)
		{
			if(window.getContent() instanceof Application)
			{
				Window<Dialog> dialogWindow = ((Application) window.getContent()).getActiveDialog();
				if(dragging)
				{
					if(isMouseOnScreen(mouseX, mouseY))
					{
						if(dialogWindow == null)
						{
							window.handleWindowMove(posX, posY, -(lastMouseX - mouseX), -(lastMouseY - mouseY));
						}
						else
						{
							dialogWindow.handleWindowMove(posX, posY, -(lastMouseX - mouseX), -(lastMouseY - mouseY));
						}
					}
					else
					{
						dragging = false;
					}
				}
				else
				{
					if(isMouseWithinWindow(mouseX, mouseY, window) || isMouseWithinWindow(mouseX, mouseY, dialogWindow))
					{
						window.handleMouseDrag(mouseX, mouseY, clickedMouseButton);
					}
				}
			}
		}

		this.lastMouseX = mouseX;
		this.lastMouseY = mouseY;
	}
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		int scroll = Mouse.getEventDWheel();
		if(scroll != 0)
		{
			Window<?> window = this.windowManager.currentWindow();
			if(window != null)
			{
				window.handleMouseScroll(mouseX, mouseY, scroll >= 0);
			}
		}
	}

	@Override
	public void drawHoveringText(@NotNull List<String> textLines, int x, int y)
	{
		super.drawHoveringText(textLines, x, y);
	}

	public void drawHoveringText(String[] textLines, int x, int y)
	{
		this.drawHoveringText(ImmutableList.copyOf(textLines), x, y);
	}

	public boolean sendApplicationToFront(AppInfo info)
	{
		return this.windowManager.sendApplicationToFront(info);
	}

	@Override
	public void openApplication(AppInfo info)
	{
		openApplication(info, (NBTTagCompound) null);
	}

	@Override
	public void openApplication(AppInfo info, NBTTagCompound intentTag)
	{
		Optional<Application> optional = APPLICATIONS.stream().filter(app -> app.getInfo() == info).findFirst();
		optional.ifPresent(application -> openApplication(application, intentTag));
	}

	private void openApplication(Application app, NBTTagCompound intent)
	{
		if(!isApplicationInstalled(app.getInfo()))
			return;

		if(!isValidApplication(app.getInfo()))
			return;

		if(sendApplicationToFront(app.getInfo()))
			return;

		Window<Application> window = new Window<>(app, this);
		window.init((width - SCREEN_WIDTH) / 2, (height - SCREEN_HEIGHT) / 2, intent);

		if(appData.hasKey(app.getInfo().getFormattedId()))
		{
			app.load(appData.getCompoundTag(app.getInfo().getFormattedId()));
		}

		if(app instanceof SystemApplication)
		{
			((SystemApplication) app).setLaptop(this);
		}

		if(app.getCurrentLayout() == null)
		{
			app.restoreDefaultLayout();
		}
		
		addWindow(window);

	    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Override
	public boolean openApplication(AppInfo info, File file)
	{
		if(!isApplicationInstalled(info))
			return false;

		if(!isValidApplication(info))
			return false;

		Optional<Application> optional = APPLICATIONS.stream().filter(app -> app.getInfo() == info).findFirst();
		if(optional.isPresent())
		{
			Application application = optional.get();
			boolean alreadyRunning = isApplicationRunning(info);
			openApplication(application, null);
			if(isApplicationRunning(info))
			{
				if(!application.handleFile(file))
				{
					if(!alreadyRunning)
					{
						closeApplication(application);
					}
					return false;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void closeApplication(AppInfo info)
	{
		Optional<Application> optional = APPLICATIONS.stream().filter(app -> app.getInfo() == info).findFirst();
		optional.ifPresent(this::closeApplication);
	}

	private void closeApplication(Application app)
	{
		this.windowManager.closeApplication(app);
	}
	
	private void addWindow(Window<Application> window)
	{
		this.windowManager.addWindow(window);
	}

	private boolean hasReachedWindowLimit()
	{
		return this.windowManager.hasReachedWindowLimit();
	}

	private boolean isMouseOnScreen(int mouseX, int mouseY)
	{
		int posX = (width - SCREEN_WIDTH) / 2;
		int posY = (height - SCREEN_HEIGHT) / 2;
		return GuiHelper.isMouseInside(mouseX, mouseY, posX, posY, posX + SCREEN_WIDTH, posY + SCREEN_HEIGHT);
	}

	protected boolean isMouseWithinWindowBar(int mouseX, int mouseY, Window<?> window)
	{
		if(window == null) return false;
		int posX = (width - SCREEN_WIDTH) / 2;
		int posY = (height - SCREEN_HEIGHT) / 2;
		return GuiHelper.isMouseInside(mouseX, mouseY, posX + window.offsetX + 1, posY + window.offsetY + 1, posX + window.offsetX + window.width - 13, posY + window.offsetY + 11);
	}

	protected boolean isMouseWithinWindow(int mouseX, int mouseY, Window<?> window)
	{
		if(window == null) return false;
		int posX = (width - SCREEN_WIDTH) / 2;
		int posY = (height - SCREEN_HEIGHT) / 2;
		return GuiHelper.isMouseInside(mouseX, mouseY, posX + window.offsetX, posY + window.offsetY, posX + window.offsetX + window.width, posY + window.offsetY + window.height);
	}
	
	public boolean isMouseWithinApp(int mouseX, int mouseY, Window<?> window)
	{
		int posX = (width - SCREEN_WIDTH) / 2;
		int posY = (height - SCREEN_HEIGHT) / 2;
		return GuiHelper.isMouseInside(mouseX, mouseY, posX + window.offsetX + 1, posY + window.offsetY + 13, posX + window.offsetX + window.width - 1, posY + window.offsetY + window.height - 1);
	}

	public boolean isApplicationRunning(AppInfo info)
	{
		return this.windowManager.isApplicationRunning(info);
	}

	public void nextWallpaper()
	{
		if(currentWallpaper + 1 < WALLPAPERS.size())
		{
			currentWallpaper++;
		}
	}
	
	public void prevWallpaper()
	{
		if(currentWallpaper - 1 >= 0)
		{
			currentWallpaper--;
		}
	}

	public int getCurrentWallpaper()
	{
		return currentWallpaper;
	}

	public static void addWallpaper(ResourceLocation wallpaper)
	{
		if(wallpaper != null)
		{
			WALLPAPERS.add(wallpaper);
		}
	}

	public List<ResourceLocation> getWallapapers()
	{
		return ImmutableList.copyOf(WALLPAPERS);
	}

	@Nullable
	public Application getApplication(String appId)
	{
		return APPLICATIONS.stream().filter(app -> app.getInfo().getFormattedId().equals(appId)).findFirst().orElse(null);
	}

	@Override
	public List<AppInfo> getInstalledApplications()
	{
		return ImmutableList.copyOf(installedApps);
	}

	protected NBTTagCompound getAppData()
	{
		return this.appData;
	}

	public boolean isApplicationInstalled(AppInfo info)
	{
		return info.isSystemApp() || installedApps.contains(info);
	}

	private boolean isValidApplication(AppInfo info)
	{
		if(MrCrayfishDeviceMod.proxy.hasAllowedApplications())
		{
			return MrCrayfishDeviceMod.proxy.getAllowedApplications().contains(info);
		}
		return true;
	}

	public void installApplication(AppInfo info, @Nullable Callback<Object> callback)
	{
		if(!isValidApplication(info))
			return;

		Task task = new TaskInstallApp(info, pos, true);
		task.setCallback((tagCompound, success) ->
		{
            if(success)
			{
				installedApps.add(info);
				installedApps.sort(AppInfo.SORT_NAME);
			}
			if(callback != null)
			{
				callback.execute(null, success);
			}
        });
		TaskManager.sendTask(task);
	}

	public void removeApplication(AppInfo info, @Nullable Callback<Object> callback)
	{
		if(!isValidApplication(info))
			return;

		Task task = new TaskInstallApp(info, pos, false);
		task.setCallback((tagCompound, success) ->
		{
			if(success)
			{
				installedApps.remove(info);
			}
			if(callback != null)
			{
				callback.execute(null, success);
			}
		});
		TaskManager.sendTask(task);
	}

	public static System getSystem()
	{
		return system;
	}

	public static void setMainDrive(Drive mainDrive)
	{
		if(Laptop.mainDrive == null)
		{
			Laptop.mainDrive = mainDrive;
		}
	}

	@Nullable
	public static Drive getMainDrive()
	{
		return mainDrive;
	}

	public List<Application> getApplications()
	{
		return APPLICATIONS;
	}

	public TaskBar getTaskBar()
	{
		return bar;
	}

	public Settings getSettings()
	{
		return settings;
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	public void openContext(Layout layout, int x, int y)
	{
		layout.updateComponents(x, y);
		context = layout;
		layout.init();
	}

	@Override
	public boolean hasContext()
	{
		return context != null;
	}

	@Override
	public void closeContext()
	{
		context = null;
		dragging = false;
	}
}
