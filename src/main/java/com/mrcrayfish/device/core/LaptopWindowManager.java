package com.mrcrayfish.device.core;

import com.google.common.base.Preconditions;
import com.mrcrayfish.device.api.app.Application;
import com.mrcrayfish.device.api.app.Dialog;
import com.mrcrayfish.device.api.task.TaskManager;
import com.mrcrayfish.device.object.AppInfo;
import com.mrcrayfish.device.programs.system.SystemApplication;
import com.mrcrayfish.device.programs.system.task.TaskUpdateApplicationData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

public class LaptopWindowManager
{
    private final Window<?>[] windows;
    private final Laptop laptop;

    public LaptopWindowManager(Laptop laptop)
    {
        this.laptop = laptop;
        this.windows = new Window<?>[5];
    }

    public void onRender(int posX, int posY, int mouseX, int mouseY, boolean insideContext, float partialTicks)
    {
        Window<?>[] windows = this.windows;
        ArrayUtils.reverse(windows);
        for(Window<?> window : windows)
        {
            if(window != null)
            {
                window.render(this.laptop, this.laptop.mc, posX, posY, mouseX, mouseY, windows[0] == window && !insideContext, partialTicks);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void onMouseClicked(int posX, int posY, int mouseX, int mouseY, int mouseButton)
    {
        for(int i = 0; i < windows.length; i++)
        {
            Window<?> window = windows[i];
            if(window != null)
            {
                if(window.getContent() instanceof Application)
                {
                    Window<Dialog> dialogWindow = ((Window<Application>) window).getContent().getActiveDialog();
                    if(this.laptop.isMouseWithinWindow(mouseX, mouseY, window) || this.laptop.isMouseWithinWindow(mouseX, mouseY, dialogWindow))
                    {
                        this.windows[i] = null;
                        this.updateWindowStack();
                        this.windows[0] = window;

                        this.windows[0].handleMouseClick(this.laptop, posX, posY, mouseX, mouseY, mouseButton);

                        if(this.laptop.isMouseWithinWindowBar(mouseX, mouseY, dialogWindow))
                        {
                            this.laptop.dragging = true;
                            return;
                        }

                        if(this.laptop.isMouseWithinWindowBar(mouseX, mouseY, window) && dialogWindow == null)
                        {
                            this.laptop.dragging = true;
                            return;
                        }
                        break;
                    }
                }
            }
        }
    }

    public void onTick()
    {
        for(Window<?> window : this.windows)
        {
            if(window != null)
            {
                window.onTick();
            }
        }
    }

    public void markForUpdate()
    {
        for(Window<?> window : this.windows)
        {
            if(window != null)
            {
                window.content.markForLayoutUpdate();
            }
        }
    }

    public void onGuiClose()
    {
        /* Close all known open windows and sendTask application data */
        for(Window<?> window : this.windows)
        {
            if(window != null)
            {
                window.close();
            }
        }
    }

    protected void updateWindowStack()
    {
        Window<?>[] windows = this.windows;
        for(int i = windows.length - 1; i >= 0; i--)
        {
            if(windows[i] != null)
            {
                if(i + 1 < windows.length)
                {
                    if(i == 0 || windows[i - 1] != null)
                    {
                        if(windows[i + 1] == null)
                        {
                            windows[i + 1] = windows[i];
                            windows[i] = null;
                        }
                    }
                }
            }
        }
    }

    protected void closeApplication(Application app)
    {
        for(int i = 0; i < this.windows.length; i++)
        {
            Window<?> window = this.windows[i];
            if(window != null)
            {
                if(window.content instanceof Application)
                {
                    if(((Application) window.content).getInfo() == app.getInfo())
                    {
                        if(app.isDirty())
                        {
                            NBTTagCompound container = new NBTTagCompound();
                            app.save(container);
                            app.clean();
                            this.laptop.getAppData().setTag(app.getInfo().getFormattedId(), container);

                            BlockPos pos = Laptop.getPos();
                            Preconditions.checkNotNull(pos, "Laptop position is null when player is active?");
                            TaskManager.sendTask(new TaskUpdateApplicationData(pos.getX(), pos.getY(), pos.getZ(), app.getInfo().getFormattedId(), container));
                        }

                        if(app instanceof SystemApplication)
                        {
                            ((SystemApplication) app).setLaptop(null);
                        }

                        window.handleClose();
                        windows[i] = null;
                        return;
                    }
                }
            }
        }
    }

    protected void addWindow(Window<Application> window)
    {
        if(!hasReachedWindowLimit())
        {
            this.updateWindowStack();
            this.windows[0] = window;
        }
    }

    @Nullable
    public Window<?> currentWindow()
    {
        return this.windows[0];
    }

    public boolean sendApplicationToFront(AppInfo info)
    {
        for(int i = 0; i < windows.length; i++)
        {
            Window<?> window = windows[i];
            if(window != null)
            {
                if(window.content instanceof Application)
                {
                    if(((Application) window.content).getInfo() == info)
                    {
                        windows[i] = null;
                        updateWindowStack();
                        windows[0] = window;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isApplicationRunning(AppInfo info)
    {
        for(Window<?> window : windows)
        {
            if(window != null && window.content instanceof Application && ((Application) window.content).getInfo() == info)
            {
                return true;
            }
        }
        return false;
    }

    public boolean hasReachedWindowLimit()
    {
        for(Window<?> window : this.windows)
        {
            if(window == null)
            {
                return false;
            }
        }
        return true;
    }
}
